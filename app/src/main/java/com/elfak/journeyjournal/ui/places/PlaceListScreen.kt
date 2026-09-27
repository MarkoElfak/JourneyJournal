package com.elfak.journeyjournal.ui.places

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.ui.common.EmptyState
import com.elfak.journeyjournal.ui.common.FilterSheetContent
import com.elfak.journeyjournal.ui.common.RemoteImage
import com.elfak.journeyjournal.ui.common.TypeBadge
import com.elfak.journeyjournal.util.distanceMeters
import com.elfak.journeyjournal.util.formatDateTime
import com.elfak.journeyjournal.util.formatDistance
import com.elfak.journeyjournal.util.formatRating
import com.google.android.gms.maps.model.LatLng

/** Tabular view of every registered place, with the same filters as the map (requirement 3). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreen(
    onPlaceClick: (String) -> Unit,
    contentPadding: PaddingValues,
    viewModel: PlacesViewModel = viewModel(),
) {
    val places by viewModel.visiblePlaces.collectAsStateWithLifecycle()
    val allPlaces by viewModel.allPlaces.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val authors by viewModel.authors.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()

    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val horizontalScroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = filter.query,
                onValueChange = viewModel::setQuery,
                placeholder = { Text("Pretraži...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (filter.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Obriši")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            BadgedBox(badge = {
                if (filter.activeCount > 0) Badge { Text("${filter.activeCount}") }
            }) {
                IconButton(onClick = { showFilters = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filteri")
                }
            }
        }

        Text(
            text = "Prikazano ${places.size} od ${allPlaces.size} objekata • sortirano: ${filter.sort.label}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )

        TableHeader(horizontalScroll)
        HorizontalDivider()

        if (places.isEmpty()) {
            EmptyState(
                title = "Nema objekata",
                subtitle = if (allPlaces.isEmpty()) {
                    "Dodajte prvo mesto pomoću dugmeta + na mapi."
                } else {
                    "Nijedan objekat ne odgovara zadatim filterima."
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 16.dp),
            ) {
                items(places, key = { it.id }) { place ->
                    TableRow(
                        place = place,
                        userLocation = location,
                        scrollState = horizontalScroll,
                        onClick = { onPlaceClick(place.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(onDismissRequest = { showFilters = false }, sheetState = sheetState) {
            FilterSheetContent(
                filter = filter,
                authors = authors,
                hasLocation = location != null,
                onApply = {
                    viewModel.applyFilter(it)
                    showFilters = false
                },
                onReset = viewModel::resetFilter,
            )
        }
    }
}

private val COLUMN_NAME = 200.dp
private val COLUMN_TYPE = 150.dp
private val COLUMN_RATING = 110.dp
private val COLUMN_AUTHOR = 130.dp
private val COLUMN_DISTANCE = 110.dp
private val COLUMN_DATE = 150.dp

@Composable
private fun TableHeader(scrollState: androidx.compose.foundation.ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        HeaderCell("Objekat", COLUMN_NAME)
        HeaderCell("Tip", COLUMN_TYPE)
        HeaderCell("Ocena", COLUMN_RATING)
        HeaderCell("Autor", COLUMN_AUTHOR)
        HeaderCell("Udaljenost", COLUMN_DISTANCE)
        HeaderCell("Kreirano", COLUMN_DATE)
        HeaderCell("Interakcije", COLUMN_DATE)
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width),
    )
}

@Composable
private fun TableRow(
    place: Place,
    userLocation: LatLng?,
    scrollState: androidx.compose.foundation.ScrollState,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.width(COLUMN_NAME),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RemoteImage(
                model = place.photoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small),
            )
            Column {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = place.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(modifier = Modifier.width(COLUMN_TYPE)) { TypeBadge(place.placeType) }
        Text(
            text = if (place.ratingCount == 0L) "–" else "★ ${place.averageRating.formatRating()} (${place.ratingCount})",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(COLUMN_RATING),
        )
        Text(
            text = place.authorUsername.ifBlank { "–" },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(COLUMN_AUTHOR),
        )
        Text(
            text = userLocation?.let { formatDistance(distanceMeters(it, place.position)) } ?: "–",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(COLUMN_DISTANCE),
        )
        Text(
            text = place.createdAt.formatDateTime(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(COLUMN_DATE),
        )
        Text(
            text = "💬 ${place.commentCount}  👣 ${place.visitCount}\n${place.lastInteractionAt.formatDateTime()}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(COLUMN_DATE),
        )
    }
}
