# Journey Journal

Mobile crowd-sensing & collaboration application for the course *Mobilni sistemi i servisi*.

Users mark places worth visiting on a shared map - a viewpoint, a hidden gem, a landmark, a café -
with a photo, a description and a type. Everyone else sees those places live, can walk up to them,
check in, rate them and leave comments, and every interaction earns points that feed a public
ranking.

* **Client:** Kotlin + Jetpack Compose (Material 3), Google Maps Compose, fused location provider.
* **Server:** Firebase - Authentication, Cloud Firestore (live sync between all clients),
  Cloud Storage (photos).

Firebase console setup is described in **[SETUP.md](SETUP.md)** - do that first.

## How the specification maps onto the code

| # | Requirement | Where |
|---|---|---|
| 1 | Registration (username, password, name, phone, photo taken in the app) + login | [`ui/auth`](app/src/main/java/com/elfak/journeyjournal/ui/auth), [`AuthRepository`](app/src/main/java/com/elfak/journeyjournal/data/repo/AuthRepository.kt), [`PhotoRepository`](app/src/main/java/com/elfak/journeyjournal/data/repo/PhotoRepository.kt) |
| 2 | Continuous GPS/network location tracking + user shown on the map | [`LocationService`](app/src/main/java/com/elfak/journeyjournal/location/LocationService.kt) (foreground service), [`LocationRepository`](app/src/main/java/com/elfak/journeyjournal/data/repo/LocationRepository.kt), [`MapScreen`](app/src/main/java/com/elfak/journeyjournal/ui/map/MapScreen.kt) |
| 3 | Add an object at the current location; filter by author, type, attribute values and date range; map **and** table view | [`AddPlaceScreen`](app/src/main/java/com/elfak/journeyjournal/ui/places/AddPlaceScreen.kt), [`PlaceFilter`](app/src/main/java/com/elfak/journeyjournal/data/model/PlaceFilter.kt), [`FilterSheet`](app/src/main/java/com/elfak/journeyjournal/ui/common/FilterSheet.kt), [`PlaceListScreen`](app/src/main/java/com/elfak/journeyjournal/ui/places/PlaceListScreen.kt) |
| 4 | Search by attributes and inside a radius around the current position | same filter sheet - free-text search + radius slider (drawn as a circle on the map) |
| 5 | Public ranking based on collected points | [`LeaderboardScreen`](app/src/main/java/com/elfak/journeyjournal/ui/leaderboard/LeaderboardScreen.kt), points awarded in [`PlaceRepository`](app/src/main/java/com/elfak/journeyjournal/data/repo/PlaceRepository.kt) |

## Points and ranks

| Interaction | Points |
|---|---|
| Adding a new place | +20 |
| Checking in at a place (within 150 m) | +10 |
| Rating a place (first rating only) | +5 |
| Writing a comment | +5 |

Interacting with your own place gives no points. Ranks: *Početnik* (0), *Putnik* (50),
*Istraživač* (150), *Avanturista* (300), *Legenda* (600).

## Architecture

```
ui/            Compose screens + ViewModels (one per feature)
data/model/    Firestore documents as Kotlin data classes + the filter model
data/repo/     Auth, users, places, photos, location - the only code that talks to Firebase
location/      Foreground service that tracks the device and detects nearby places
di/            ServiceLocator: a single shared instance of every repository
util/          Distance, formatting, permissions, Firestore snapshot -> Flow helpers
```

* Every screen observes Firestore **snapshot listeners** exposed as `Flow`, so a place added on one
  device appears on every other device without a refresh.
* Filtering happens in memory over the live list of places. That keeps one listener for the whole
  app and makes combined filters (text + type + author + rating + date range + radius) possible
  without composite indexes.
* Ratings, comments and check-ins are written in Firestore **transactions / batches** together with
  the counters and the awarded points, so the aggregate values can never drift.

## Running

1. Complete [SETUP.md](SETUP.md).
2. Open the project in Android Studio and run on a device or emulator **with Google Play services**.
3. Allow the location permission (and notifications on Android 13+) when prompted.

```bash
./gradlew :app:assembleDebug
```

## Testing the collaboration part

Register two users (two emulators, or one emulator + one phone). Add a place with the first user -
it shows up on the second user's map immediately. Walk the second user close to it (in the emulator:
*Extended controls → Location*), then check in, rate and comment: the points and the ranking update
live on both devices.
