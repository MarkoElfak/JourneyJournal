package com.elfak.journeyjournal.ui.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.AppConfig
import com.elfak.journeyjournal.ui.common.LoadingBox
import com.elfak.journeyjournal.ui.common.RatingInput
import com.elfak.journeyjournal.ui.common.RatingStars
import com.elfak.journeyjournal.ui.common.RemoteImage
import com.elfak.journeyjournal.ui.common.TypeBadge
import com.elfak.journeyjournal.ui.common.UserAvatar
import com.elfak.journeyjournal.util.distanceMeters
import com.elfak.journeyjournal.util.formatDateTime
import com.elfak.journeyjournal.util.formatDistance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: String,
    onBack: () -> Unit,
    viewModel: PlaceDetailViewModel = viewModel(),
) {
    LaunchedEffect(placeId) { viewModel.bind(placeId) }

    val place by viewModel.place.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val myRating by viewModel.myRating.collectAsStateWithLifecycle()
    val visited by viewModel.visited.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var commentText by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(place?.title ?: "Detalji") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    if (place?.authorId == viewModel.currentUid && place != null) {
                        IconButton(onClick = { viewModel.deletePlace(onBack) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Obriši")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val current = place
        if (current == null) {
            LoadingBox(modifier = Modifier.padding(padding))
            return@Scaffold
        }

        val distance = location?.let { distanceMeters(it, current.position) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            RemoteImage(
                model = current.photoUrl,
                contentDescription = current.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = current.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TypeBadge(current.placeType)
                    RatingStars(current.averageRating, current.ratingCount)
                }
                if (current.description.isNotBlank()) {
                    Text(current.description, style = MaterialTheme.typography.bodyLarge)
                }

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        DetailRow("Autor", current.authorUsername.ifBlank { "nepoznat" })
                        DetailRow("Kreirano", current.createdAt.formatDateTime())
                        DetailRow("Poslednja interakcija", current.lastInteractionAt.formatDateTime())
                        DetailRow(
                            "Koordinate",
                            String.format("%.5f, %.5f", current.lat, current.lng),
                        )
                        DetailRow("Udaljenost", distance?.let(::formatDistance) ?: "nepoznata")
                        DetailRow("Posete", "${current.visitCount}")
                    }
                }

                Button(
                    onClick = viewModel::visit,
                    enabled = !busy && !visited &&
                            distance != null && distance <= AppConfig.VISIT_RADIUS_METERS,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.Place, contentDescription = null)
                    Text(
                        when {
                            visited -> "  Već ste obišli ovo mesto"
                            distance == null -> "  Čekam lokaciju..."
                            distance > AppConfig.VISIT_RADIUS_METERS ->
                                "  Priđite bliže (${formatDistance(distance)})"
                            else -> "  Obiđi lokaciju (+10 poena)"
                        }
                    )
                }

                HorizontalDivider()
                Text("Vaša ocena", style = MaterialTheme.typography.titleMedium)
                RatingInput(
                    value = myRating ?: 0,
                    onValueChange = viewModel::rate,
                    enabled = !busy,
                )

                HorizontalDivider()
                Text(
                    text = "Komentari (${current.commentCount})",
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Dodajte komentar...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                    )
                    IconButton(
                        onClick = {
                            viewModel.comment(commentText)
                            commentText = ""
                        },
                        enabled = commentText.isNotBlank() && !busy,
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Pošalji")
                    }
                }

                comments.forEach { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        UserAvatar(comment.userPhotoUrl, size = 36)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = comment.username.ifBlank { "korisnik" },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = comment.createdAt.formatDateTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (comment.userId == viewModel.currentUid) {
                            IconButton(onClick = { viewModel.deleteComment(comment.id) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Obriši komentar")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
