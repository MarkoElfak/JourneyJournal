package com.elfak.journeyjournal.ui.leaderboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.model.AppUser
import com.elfak.journeyjournal.ui.common.EmptyState
import com.elfak.journeyjournal.ui.common.UserAvatar
import com.elfak.journeyjournal.ui.places.PlacesViewModel

/** Public ranking of all users by collected points (requirement 5). */
@Composable
fun LeaderboardScreen(
    contentPadding: PaddingValues,
    viewModel: PlacesViewModel = viewModel(),
) {
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val currentUid = viewModel.currentUid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding()),
    ) {
        Text(
            text = "Rang lista",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 16.dp),
        )
        Text(
            text = "Poeni: +20 novo mesto, +10 poseta, +5 ocena, +5 komentar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
        )

        if (users.isEmpty()) {
            EmptyState("Rang lista je prazna", "Registrujte se i sakupite prve poene.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = contentPadding.calculateBottomPadding() + 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(users, key = { _, user -> user.uid }) { index, user ->
                    LeaderboardRow(
                        position = index + 1,
                        user = user,
                        highlighted = user.uid == currentUid,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(position: Int, user: AppUser, highlighted: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (highlighted) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(medalColor(position)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$position",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            UserAvatar(user.photoUrl, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${user.rank.title} • ${user.placesCount} mesta",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${user.points}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun medalColor(position: Int) = when (position) {
    1 -> androidx.compose.ui.graphics.Color(0xFFFFD54F)
    2 -> androidx.compose.ui.graphics.Color(0xFFB0BEC5)
    3 -> androidx.compose.ui.graphics.Color(0xFFBCAAA4)
    else -> MaterialTheme.colorScheme.surfaceVariant
}
