# Logging CONNECT proxy used to prove "nothing is sent before consent" (HANDOVER section 10, 4 Oct 2026).
#
# Why this exists: `emulator -tcpdump` recorded only the boot burst and could not see an opted-in upload, so its
# silence proved nothing. This proxy logs the TLS SNI host name of every secure connection instead.
#
#   python3 tools/log-sni-proxy.py /tmp/proxy.log &
#   emulator -avd <name> -no-snapshot -wipe-data -http-proxy http://127.0.0.1:8899
#
# Install the sideload build with analytics_allowed switched to true (local edit, never committed), run the
# opted-out scenario for 75+ minutes, then `grep SNI /tmp/proxy.log | awk '{print $4}' | sort | uniq -c`.
# ALWAYS run the positive control in the same session: opt in and confirm app-measurement.com and
# firebaseinstallations.googleapis.com appear. Hits that predate the app's install are the emulator's own
# Google services, not the app. Stdlib only.
import asyncio, time, sys
LOG=open(sys.argv[1],'a',buffering=1)
def log(kind,host): LOG.write(f"{time.strftime('%H:%M:%S')} {kind} {host}\n")

def sni(d):
    try:
        if d[0]!=22: return "-"
        p=43; p+=1+d[p]; p+=2+int.from_bytes(d[p:p+2],'big'); p+=1+d[p]
        e=p+2+int.from_bytes(d[p:p+2],'big'); p+=2
        while p<e:
            t=int.from_bytes(d[p:p+2],'big'); l=int.from_bytes(d[p+2:p+4],'big')
            if t==0:
                n=int.from_bytes(d[p+7:p+9],'big'); return d[p+9:p+9+n].decode()
            p+=4+l
    except Exception: pass
    return "?"
async def pipe(r,w):
    try:
        while (d:=await r.read(65536)): w.write(d); await w.drain()
    except Exception: pass
    finally:
        try: w.close()
        except Exception: pass
async def handle(r,w):
    try:
        line=await r.readline()
        parts=line.decode(errors='ignore').split()
        if len(parts)<2: w.close(); return
        method,target=parts[0],parts[1]
        hdr=b""
        while True:
            l=await r.readline(); hdr+=l
            if l in (b"\r\n",b"\n",b""): break
        if method=="CONNECT":
            host,_,port=target.rpartition(":"); log("CONNECT",host)
            try: ur,uw=await asyncio.open_connection(host,int(port))
            except Exception as e: log("FAIL",host); w.close(); return
            w.write(b"HTTP/1.1 200 Connection established\r\n\r\n"); await w.drain()
            first=await r.read(65536); log("SNI",f"{host} {sni(first)}"); uw.write(first); await uw.drain()
            await asyncio.gather(pipe(r,uw),pipe(ur,w))
        else:
            from urllib.parse import urlparse
            u=urlparse(target); host=u.hostname; port=u.port or 80; log("HTTP",host)
            ur,uw=await asyncio.open_connection(host,port)
            path=(u.path or "/")+("?"+u.query if u.query else "")
            uw.write(f"{method} {path} HTTP/1.1\r\n".encode()+hdr); await uw.drain()
            await asyncio.gather(pipe(r,uw),pipe(ur,w))
    except Exception: pass
    finally:
        try: w.close()
        except Exception: pass
async def main():
    s=await asyncio.start_server(handle,'127.0.0.1',8899); log("START","proxy"); 
    async with s: await s.serve_forever()
asyncio.run(main())
