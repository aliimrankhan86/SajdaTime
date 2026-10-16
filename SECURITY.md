# Security policy

SajdaTime is a free prayer times and Qibla app (Android and Wear OS) made by one person. It has no
accounts and no server of its own.

## Reporting a problem

Please report a security or privacy problem privately, not in a public issue.

- Use GitHub's private reporting: the **Report a vulnerability** button under this repository's
  **Security** tab, or
- email **aikstudies@gmail.com** with "SajdaTime security" in the subject.

Please include what you found, the version of the app (Settings, About), and how to reproduce it.
Do not include anyone's personal data. You will get a reply from a person. This is a one person project, so there is no bounty, but if
your report leads to a fix you can be credited if you wish.

## What is in scope

The app (phone and Wear OS), the privacy page at
<https://aliimrankhan86.github.io/SajdaTime/privacy.html>, and this repository. In particular, tell
us if the app sends something that the privacy page says it does not.

## Supported versions

Only the version currently on Google Play.

## Not secrets

The Firebase API key in `app/src/main/res/values/firebase_config.xml` is committed on purpose. Google
documents these keys as public identifiers, not secrets. The release signing key is never in this
repository.
