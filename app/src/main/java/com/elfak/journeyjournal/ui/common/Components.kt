package com.elfak.journeyjournal.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elfak.journeyjournal.data.model.PlaceType
import com.elfak.journeyjournal.util.formatRating

private val StarColor = Color(0xFFF9A825)

/** Read-only average rating. */
@Composable
fun RatingStars(
    rating: Double,
    count: Long,
    modifier: Modifier = Modifier,
    starSize: Int = 16,
    showText: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { index ->
            val icon = when {
                rating >= index -> Icons.Filled.Star
                rating >= index - 0.5 -> Icons.AutoMirrored.Filled.StarHalf
                else -> Icons.Filled.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (count == 0L) MaterialTheme.colorScheme.outline else StarColor,
                modifier = Modifier.size(starSize.dp),
            )
        }
        if (showText) {
            Text(
                text = if (count == 0L) "  bez ocena" else "  ${rating.formatRating()} ($count)",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Tappable stars used to submit a rating. */
@Composable
fun RatingInput(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= value) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Oceni sa $index",
                tint = if (enabled) StarColor else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(36.dp)
                    .padding(2.dp)
                    .then(
                        if (enabled) Modifier.clickable { onValueChange(index) } else Modifier
                    ),
            )
        }
    }
}

@Composable
fun TypeBadge(type: PlaceType, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(type.color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(text = type.emoji, style = MaterialTheme.typography.labelMedium)
        Text(
            text = type.label,
            style = MaterialTheme.typography.labelMedium,
            color = type.color,
        )
    }
}

@Composable
fun UserAvatar(photoUrl: String?, size: Int = 40, modifier: Modifier = Modifier) {
    RemoteImage(
        model = photoUrl,
        contentDescription = "Profilna fotografija",
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

@Composable
fun LoadingBox(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
