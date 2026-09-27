package com.elfak.journeyjournal.data.model

import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/** Category of a journal entry - one of the attributes users can filter by. */
enum class PlaceType(
    val label: String,
    val emoji: String,
    val markerHue: Float,
    val color: Color,
) {
    VIDIKOVAC("Vidikovac", "🏔", BitmapDescriptorFactory.HUE_AZURE, Color(0xFF2196F3)),
    ZNAMENITOST("Znamenitost", "🏛", BitmapDescriptorFactory.HUE_VIOLET, Color(0xFF7E57C2)),
    PRIRODA("Priroda", "🌲", BitmapDescriptorFactory.HUE_GREEN, Color(0xFF43A047)),
    HRANA_I_PICE("Hrana i piće", "☕", BitmapDescriptorFactory.HUE_ORANGE, Color(0xFFF57C00)),
    SKRIVENI_DRAGULJ("Skriveni dragulj", "💎", BitmapDescriptorFactory.HUE_ROSE, Color(0xFFEC407A)),
    DOGADJAJ("Događaj", "🎪", BitmapDescriptorFactory.HUE_YELLOW, Color(0xFFFBC02D)),
    OSTALO("Ostalo", "📍", BitmapDescriptorFactory.HUE_RED, Color(0xFFE53935));

    companion object {
        fun fromName(value: String?): PlaceType =
            entries.firstOrNull { it.name == value } ?: OSTALO
    }
}
