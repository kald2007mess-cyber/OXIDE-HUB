# OXIDE HUB — Native Android rebuild

This repository now contains the first native Android application shell. It is being separated from the legacy website; it is not a WebView wrapper.

## Implemented in this first phase
- Kotlin + Jetpack Compose native Android activity and initial interface.
- Android application module and Gradle Kotlin DSL configuration.
- GitHub Actions workflow for a debug APK.
- The app shell explicitly shows that the backend is not connected yet.

## Not yet implemented
- New independent API and database.
- Server-issued OX-XXXXXXXX identity and verified legacy identity migration.
- Points, missions, ad network callbacks, referrals, withdrawals, and support.
- Telegram admin bot connection and role-protected admin API.
- Five-language maintenance-only page on the legacy site.
- The exact supplied splash artwork and final launcher icon verification.

## Target architecture
- Android app: native UI and bundled assets; no browser and no dependency on the old website for rendering.
- Independent API: authentication, identity, points, missions, referrals, withdrawals, support, releases, and admin actions.
- New database: empty earning/activity data at launch. Preserve old OX identities only through a verified migration map.
- Telegram admin bot: uses the same authenticated admin API as the Android app.
- Legacy website: maintenance-only page in Arabic, English, French, Spanish, and German.

## Safety rules
1. Do not delete or reset the legacy database until identity export and mapping are verified.
2. User IDs must be issued by the server, not generated only on-device.
3. Never put bot tokens, session secrets, or database credentials in the APK or Git.
4. Protect every admin endpoint with Telegram identity validation and explicit permissions.
5. Retire the old website only after API, bot, migration, and APK tests pass.

## Build
GitHub Actions builds a debug APK on pushes to main. A distributable release APK requires a private signing key stored as GitHub Actions secrets; never commit the key.
