package com.elfak.journeyjournal.data.model

import com.elfak.journeyjournal.util.distanceMeters
import com.google.android.gms.maps.model.LatLng

enum class DateField(val label: String) {
    CREATED("Datum kreiranja"),
    LAST_INTERACTION("Poslednja interakcija"),
}

enum class PlaceSort(val label: String) {
    NEWEST("Najnoviji"),
    OLDEST("Najstariji"),
    RATING("Najbolje ocenjeni"),
    DISTANCE("Najbliži"),
    TITLE("Naziv (A-Š)"),
}

/**
 * All filters from requirements 3 and 4: free-text search over the attributes, author, type,
 * attribute values (rating), creation / last-interaction date range and radius around the user.
 */
data class PlaceFilter(
    val query: String = "",
    val types: Set<PlaceType> = emptySet(),
    val author: String = "",
    val minRating: Float = 0f,
    val dateField: DateField = DateField.CREATED,
    val fromDate: Long? = null,
    val toDate: Long? = null,
    val radiusKm: Float? = null,
    val sort: PlaceSort = PlaceSort.NEWEST,
) {
    val isActive: Boolean
        get() = query.isNotBlank() || types.isNotEmpty() || author.isNotBlank() ||
                minRating > 0f || fromDate != null || toDate != null || radiusKm != null

    /** Number of active filter groups, shown as a badge on the filter button. */
    val activeCount: Int
        get() = listOf(
            query.isNotBlank(),
            types.isNotEmpty(),
            author.isNotBlank(),
            minRating > 0f,
            fromDate != null || toDate != null,
            radiusKm != null,
        ).count { it }
}

fun List<Place>.applyFilter(criteria: PlaceFilter, userLocation: LatLng?): List<Place> {
    val query = criteria.query.trim().lowercase()
    val filtered = filter { place ->
        val matchesQuery = query.isEmpty() ||
                place.title.lowercase().contains(query) ||
                place.description.lowercase().contains(query) ||
                place.placeType.label.lowercase().contains(query) ||
                place.authorUsername.lowercase().contains(query)

        val matchesType = criteria.types.isEmpty() || place.placeType in criteria.types

        val matchesAuthor = criteria.author.isBlank() ||
                place.authorUsername.equals(criteria.author, ignoreCase = true)

        val matchesRating = criteria.minRating <= 0f || place.averageRating >= criteria.minRating

        val timestamp = when (criteria.dateField) {
            DateField.CREATED -> place.createdAt
            DateField.LAST_INTERACTION -> place.lastInteractionAt ?: place.createdAt
        }?.toDate()?.time
        val matchesFrom = criteria.fromDate == null || (timestamp != null && timestamp >= criteria.fromDate)
        val matchesTo = criteria.toDate == null || (timestamp != null && timestamp <= criteria.toDate)

        val matchesRadius = criteria.radiusKm == null || userLocation == null ||
                distanceMeters(userLocation, place.position) <= criteria.radiusKm * 1000.0

        matchesQuery && matchesType && matchesAuthor && matchesRating &&
                matchesFrom && matchesTo && matchesRadius
    }

    return when (criteria.sort) {
        PlaceSort.NEWEST -> filtered.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        PlaceSort.OLDEST -> filtered.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
        PlaceSort.RATING -> filtered.sortedByDescending { it.averageRating }
        PlaceSort.TITLE -> filtered.sortedBy { it.title.lowercase() }
        PlaceSort.DISTANCE -> if (userLocation == null) filtered else {
            filtered.sortedBy { distanceMeters(userLocation, it.position) }
        }
    }
}
