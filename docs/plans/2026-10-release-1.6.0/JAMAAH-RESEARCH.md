# Jama'ah times: competitors and reliability (research, 25 Oct 2026)

Asked by Ali: "There are soo many mosques and they have different jamaat timings, so I don't know based on the location how can we tell which nearest mosque has what is the jamaat time. Also check it with the competitors ... check if it is something we can reliablly do? It shouldnt be something we are doing often user complaints that it is incorrect".

**Short answer:** no app shows the nearest mosque's jama'ah time by itself. The apps that show jama'ah times (MAWAQIT, Masjidal, MyMasjid, Masjidbox) work only because each mosque joins and types in its own times. For an offline app with no server, the only reliable way is for the user to type in their own mosque's times, which is what SajdaTime does.

| App | Jama'ah times? | Where the times come from |
|---|---|---|
| MAWAQIT | Yes | Each mosque's admin enters them; 4.9 from 5.2k App Store ratings |
| Masjidal Athan+ | Yes | Mosques that sign up |
| MyMasjid (UK) | Yes | Mosques on the platform |
| Masjidbox | Yes | Each mosque publishes its own app |
| Muslim Pro | No | Has a halal food and prayer space finder only |
| Pillars | Not yet | "Local Mosque Prayer Times" listed as coming soon for years |
| Athan (IslamicFinder), Salaat First, Al-Moazin, Hamdan, Deen | Not confirmed | |

**Complaints found** were about mosques missing from the list or times not kept up to date by the mosque, not about wrong maths. No complaints were found about versions where the user types the times. Users ask for exactly that (the Sadiq and Masjidbox feedback boards: "fixed time or minutes after Adhan"). Google Play review text could not be loaded.

**Why we can't do "nearest mosque":**
- There is no open, offline list of mosque times, and mosques change their times weekly or seasonally.
- MAWAQIT's API needs a login key (HTTP 401) and publishes no terms that allow other apps to use it.
- It would need the network and would send the user's location, which breaks the no-network rule and the privacy promise (CLAUDE.md).

**Risks of times the user types, and what SajdaTime does about them:**
- *Travelling.* Handled: the times are hidden in another time zone (step 7).
- *Jumu'ah.* Handled: it has its own setting. One sitting only; a second sitting is not supported.
- *A time clearly wrong for the season.* Handled: "Check your jama'ah time" on Times, plus an amber notice that says before or after.
- *A fixed time that drifts but stays in the window.* Not handled. "Minutes after it begins" avoids it. Candidate for 1.6.1: the row says when the times were set.
- *Ramadan.* Not handled: Isha and Fajr often move. Candidate for 1.6.1: a quiet reminder to check the mosque's Ramadan times.
- *Blame.* Handled: the line only ever shows a time the user typed, and the screen says "the jama'ah time on your mosque's board".

Sources: the research agent's report of 25 Oct 2026, with links to the MAWAQIT Play and App Store listings, the Muslim Pro App Store listing, the Masjidal and MyMasjid listings, the feedback boards above, and unitq's MAWAQIT scorecard. Not verified: coverage by country, and complaints in Google Play reviews.
