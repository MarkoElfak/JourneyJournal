# Firebase & Google Maps setup

Everything below is done once, in the browser. The project is already wired to the Firebase
project **`journeyjournal-9eeff`** through `app/google-services.json`.

> **Package name:** that `google-services.json` registers the Android app as
> **`com.elfak.journeyjournal`**, so the app module now uses exactly that `namespace` /
> `applicationId`. If you would rather keep a different package name, add a *new* Android app in
> Firebase with that package and replace `app/google-services.json`.

---

## 1. Authentication (required)

Firebase console → **Build → Authentication → Get started** → tab **Sign-in method** →
enable **Email/Password** (leave "Email link" off) → Save.

The specification asks for a *username*, not an e-mail, so the app maps every username to a
synthetic address `"<username>@journeyjournal.app"` before talking to Firebase Auth. Nothing is
ever sent to that domain - it only makes Auth enforce unique usernames for us. Nothing else has
to be configured for this.

## 2. Cloud Firestore (required)

1. **Build → Firestore Database → Create database**.
2. Location: pick `eur3 (europe-west)` (any region works, but keep it close to you).
3. Start in **production mode** - the app ships its own rules.
4. Open the **Rules** tab, replace everything with the contents of
   [`firebase/firestore.rules`](firebase/firestore.rules), and **Publish**.

No composite indexes are needed: every query the app sends is a single-field ordering
(`places` by `createdAt`, `users` by `points`, `comments` by `createdAt`), which Firestore
indexes automatically.

Collections created by the app at runtime:

```
users/{uid}                       username, fullName, phone, photoUrl, points,
                                  placesCount, createdAt, lastLat, lastLng,
                                  lastSeen, sharingLocation
places/{placeId}                  title, description, type, lat, lng, photoUrl,
                                  authorId, authorUsername, createdAt,
                                  lastInteractionAt, ratingSum, ratingCount,
                                  commentCount, visitCount
places/{placeId}/ratings/{uid}    value (1-5), username, createdAt
places/{placeId}/comments/{id}    text, userId, username, userPhotoUrl, createdAt
places/{placeId}/visits/{uid}     username, createdAt
```

## 3. Cloud Storage (required for photos)

1. **Build → Storage → Get started**, accept the default bucket
   (`journeyjournal-9eeff.firebasestorage.app`, already referenced in `google-services.json`).
2. Open the **Rules** tab, paste [`firebase/storage.rules`](firebase/storage.rules), **Publish**.

> Newer Firebase projects require the **Blaze** (pay-as-you-go) plan before Storage can be
> enabled. Free-tier quotas still apply, so a student project costs nothing - but if you cannot
> or do not want to enable billing, set
> `USE_FIREBASE_STORAGE = false` in
> [`AppConfig.kt`](app/src/main/java/com/elfak/journeyjournal/data/AppConfig.kt).
> Photos are then downscaled and stored inline in the Firestore document as base64, and the whole
> app keeps working - no other change needed.

## 4. Google Maps SDK (required for the map)

The map is **not** part of Firebase; it needs a Maps key from the Google Cloud project that backs
your Firebase project.

1. Open <https://console.cloud.google.com/> and select project **`journeyjournal-9eeff`**
   (Firebase projects are Google Cloud projects).
2. **APIs & Services → Library** → search **"Maps SDK for Android"** → **Enable**.
3. **APIs & Services → Credentials → Create credentials → API key**. Copy the key.
4. (Recommended) **Restrict key** → *Application restrictions*: Android apps → add
   package `com.elfak.journeyjournal` + your debug SHA-1; *API restrictions*: Maps SDK for Android.
   Get the SHA-1 with:

   ```bash
   ./gradlew :app:signingReport
   ```

5. Put the key in **`local.properties`** (git-ignored, never commit it):

   ```properties
   MAPS_API_KEY=AIzaSy...your key...
   ```

   `app/build.gradle.kts` reads it and injects it into the manifest as
   `com.google.android.geo.API_KEY`. Without it the app runs, but the map stays grey.

> Enabling the Maps SDK requires billing to be enabled on the Cloud project. Google's free monthly
> Maps credit covers development use.

## 5. Optional: deploy the rules from the command line

Instead of pasting the rules in the console:

```bash
npm install -g firebase-tools
firebase login
firebase use journeyjournal-9eeff
firebase deploy --only firestore:rules,storage
```

`firebase.json` in the repository root already points at both rule files.

---

## Checklist before the first run

- [ ] Email/Password sign-in enabled
- [ ] Firestore database created + rules published
- [ ] Storage bucket created + rules published (or `USE_FIREBASE_STORAGE = false`)
- [ ] Maps SDK for Android enabled and `MAPS_API_KEY` in `local.properties`
- [ ] Run on a device/emulator with **Google Play services** (the map needs it) and allow the
      location permission when asked

---

## Troubleshooting: "Authorization failure" / grey map

Logcat shows:

```
Authorization failure. Please see https://developers.google.com/maps/documentation/android-sdk/start
Ensure that the "Maps SDK for Android" is enabled.
	API Key: AIzaSy...
	Android Application (<cert_fingerprint>;<package_name>):
	  B3:2F:5B:8C:20:00:87:6A:08:DE:45:D7:A7:7D:DD:5E:27:B7:74:1A;com.elfak.journeyjournal
```

This message means the key **did** reach the app correctly - Google refused it. Nothing in the
project needs to change; check these four things in the Cloud console, in this order:

1. **Billing is enabled on the Cloud project.** Google Maps Platform refuses every request from a
   project without a billing account, even when the API is enabled. Check
   <https://console.cloud.google.com/billing/linkedaccount?project=journeyjournal-9eeff>.
   This is the same thing as putting the Firebase project on the **Blaze** plan; the free monthly
   Maps credit covers development.
2. **Maps SDK for Android is enabled** *in the project the key belongs to*:
   <https://console.cloud.google.com/apis/library/maps-android-backend.googleapis.com?project=journeyjournal-9eeff>
3. **The key's Android restrictions list this exact pair.** Open the key at
   <https://console.cloud.google.com/apis/credentials?project=journeyjournal-9eeff> →
   *Application restrictions* → **Android apps** → *Add* :

   | Field | Value |
   |---|---|
   | Package name | `com.elfak.journeyjournal` |
   | SHA-1 certificate fingerprint | `B3:2F:5B:8C:20:00:87:6A:08:DE:45:D7:A7:7D:DD:5E:27:B7:74:1A` |

   (That is this machine's debug keystore. Re-run `./gradlew :app:signingReport` on any other
   machine - every developer's debug key is different and each one has to be added.)
   Under *API restrictions*, either "Don't restrict key" or make sure **Maps SDK for Android** is
   in the allowed list.
4. **Wait and restart the app.** Enabling an API or editing key restrictions takes a few minutes to
   propagate. Fully kill the app afterwards - the Maps SDK caches the rejection for the process
   lifetime.

Harmless messages in the same log, no action needed: `No AppCheckProvider installed`,
`ACCESS_BACKGROUND_LOCATION isn't requested`, `Phenotype.API is not available`, `Davey!`/skipped
frames (debug build, no R8).
