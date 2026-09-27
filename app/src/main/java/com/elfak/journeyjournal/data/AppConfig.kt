package com.elfak.journeyjournal.data

object AppConfig {
    /**
     * Photos are uploaded to Firebase Storage. If your Firebase project cannot use Storage
     * (it requires the Blaze plan on newer projects), set this to false: photos are then
     * downscaled and stored inline in Firestore as base64 data URIs, and everything else
     * in the app keeps working unchanged.
     */
    const val USE_FIREBASE_STORAGE = true

    /** How close the user must be to a place to check in ("obiđi lokaciju"). */
    const val VISIT_RADIUS_METERS = 150.0

    /** Distance at which the tracking service notifies the user about a nearby place. */
    const val NEARBY_RADIUS_METERS = 300.0

    /** Location update interval of the foreground tracking service. */
    const val LOCATION_INTERVAL_MS = 5_000L

    /** A user's position is pushed to Firestore at most this often. */
    const val LOCATION_UPLOAD_INTERVAL_MS = 20_000L
}

/** Points awarded per interaction (requirement 5). */
object Points {
    const val NEW_PLACE = 20L
    const val RATING = 5L
    const val COMMENT = 5L
    const val VISIT = 10L
}
