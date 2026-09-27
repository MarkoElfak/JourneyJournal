package com.elfak.journeyjournal.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.model.Place
import com.elfak.journeyjournal.ui.common.FilterSheetContent
import com.elfak.journeyjournal.ui.common.RatingStars
import com.elfak.journeyjournal.ui.common.RemoteImage
import com.elfak.journeyjournal.ui.common.TypeBadge
import com.elfak.journeyjournal.ui.places.PlacesViewModel
import com.elfak.journeyjournal.util.distanceMeters
import com.elfak.journeyjournal.util.formatDistance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val NIS = LatLng(43.3209, 21.8958)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    hasLocationPermission: Boolean,
    onPlaceClick: (String) -> Unit,
    onAddPlace: () -> Unit,
    contentPadding: PaddingValues,
    viewModel: PlacesViewModel = viewModel(),
) {
    val places by viewModel.visiblePlaces.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val authors by viewModel.authors.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(NIS, 13f)
    }
    var centeredOnUser by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<Place?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Center on the user the first time a fix arrives.
    LaunchedEffect(location) {
        val position = location
        if (position != null && !centeredOnUser) {
            centeredOnUser = true
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(position, 15f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = contentPadding,
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
            ),
            onMapClick = { selectedPlace = null },
        ) {
            places.forEach { place ->
                Marker(
                    state = MarkerState(position = place.position),
                    title = place.title,
                    snippet = place.placeType.label,
                    icon = BitmapDescriptorFactory.defaultMarker(place.placeType.markerHue),
                    onClick = {
                        selectedPlace = place
                        true
                    },
                )
            }

            // Other users that currently share their position.
            users.filter {
                it.sharingLocation && it.lastLat != null && it.lastLng != null &&
                        it.uid != viewModel.currentUid
            }.forEach { user ->
                Marker(
                    state = MarkerState(position = LatLng(user.lastLat!!, user.lastLng!!)),
                    title = user.username,
                    snippet = "Korisnik • ${user.points} poena",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                    zIndex = 1f,
                )
            }

            val radius = filter.radiusKm
            val center = location
            if (radius != null && center != null) {
                Circle(
                    center = center,
                    radius = radius * 1000.0,
                    strokeColor = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4f,
                    fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                )
            }
        }

        // Search + filters
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = contentPadding.calculateTopPadding() + 8.dp)
                .padding(horizontal = 12.dp)
                .fillMaxWidth(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = filter.query,
                    onValueChange = viewModel::setQuery,
                    placeholder = { Text("Pretraži mesta...") },
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                )
                BadgedBox(
                    badge = {
                        if (filter.activeCount > 0) Badge { Text("${filter.activeCount}") }
                    },
                    modifier = Modifier.padding(end = 12.dp),
                ) {
                    IconButton(onClick = { showFilters = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filteri")
                    }
                }
            }
        }

        Text(
            text = "${places.size} mesta na mapi",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = contentPadding.calculateTopPadding() + 76.dp, start = 20.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SmallFloatingActionButton(onClick = {
                viewModel.refreshLocation()
                scope.launch {
                    // Uses the current fix if there is one, otherwise waits for the first.
                    val target = viewModel.location.filterNotNull().first()
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(target, 16f))
                }
            }) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Moja lokacija")
            }
            FloatingActionButton(onClick = onAddPlace) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }

        selectedPlace?.let { place ->
            SelectedPlaceCard(
                place = place,
                userLocation = location,
                onOpen = { onPlaceClick(place.id) },
                onClose = { selectedPlace = null },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp)
                    .padding(horizontal = 16.dp),
            )
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = sheetState,
        ) {
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

@Composable
private fun SelectedPlaceCard(
    place: Place,
    userLocation: LatLng?,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RemoteImage(
                model = place.photoUrl,
                contentDescription = place.title,
                modifier = Modifier
                    .size(72.dp)
                    .clip(MaterialTheme.shapes.medium),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TypeBadge(place.placeType)
                RatingStars(place.averageRating, place.ratingCount)
                userLocation?.let {
                    Text(
                        text = formatDistance(distanceMeters(it, place.position)) + " od vas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Zatvori")
            }
        }
    }
}
