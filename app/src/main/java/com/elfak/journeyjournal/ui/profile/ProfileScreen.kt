package com.elfak.journeyjournal.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.model.Rank
import com.elfak.journeyjournal.location.LocationService
import com.elfak.journeyjournal.ui.common.LoadingBox
import com.elfak.journeyjournal.ui.common.UserAvatar

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    trackingEnabled: Boolean,
    onTrackingChange: (Boolean) -> Unit,
    viewModel: ProfileViewModel = viewModel(),
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsStateWithLifecycle()
    val myPlaces by viewModel.myPlaces.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()

    val current = user
    if (current == null) {
        LoadingBox()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                top = contentPadding.calculateTopPadding() + 16.dp,
                bottom = contentPadding.calculateBottomPadding() + 16.dp,
                start = 20.dp,
                end = 20.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserAvatar(current.photoUrl, size = 88)
            Column {
                Text(
                    text = current.username,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(current.fullName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = current.phone,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${current.points} poena",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Text(current.rank.title, style = MaterialTheme.typography.titleMedium)
                }
                val toNext = Rank.pointsToNext(current.points)
                LinearProgressIndicator(
                    progress = {
                        val next = Rank.entries.firstOrNull { it.minPoints > current.points }
                        if (next == null) 1f else {
                            val start = current.rank.minPoints.toFloat()
                            ((current.points - start) / (next.minPoints - start)).coerceIn(0f, 1f)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = toNext?.let { "Još $it poena do sledećeg ranga" }
                        ?: "Najviši rang - svaka čast!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                HorizontalDivider()
                Text("Dodatih mesta: ${myPlaces.size}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Praćenje lokacije", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = location?.let {
                            String.format("%.5f, %.5f", it.latitude, it.longitude)
                        } ?: "Lokacija nije poznata",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = trackingEnabled, onCheckedChange = onTrackingChange)
            }
        }

        OutlinedButton(
            onClick = {
                LocationService.stop(context)
                viewModel.logout()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text("  Odjavi se")
        }
    }
}
