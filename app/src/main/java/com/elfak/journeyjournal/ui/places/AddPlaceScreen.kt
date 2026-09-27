package com.elfak.journeyjournal.ui.places

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.data.model.PlaceType
import com.elfak.journeyjournal.ui.common.PhotoPickerField
import com.elfak.journeyjournal.ui.common.rememberPhotoPicker

/** Adds a new object at the user's current position (requirement 3). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPlaceScreen(
    onBack: () -> Unit,
    onSaved: (String) -> Unit,
    viewModel: AddPlaceViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val photoPicker = rememberPhotoPicker()

    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(PlaceType.OSTALO) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    LaunchedEffect(state.savedPlaceId) {
        state.savedPlaceId?.let(onSaved)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Novo mesto") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refreshLocation) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Osveži lokaciju")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Column {
                        Text("Trenutna lokacija", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = location?.let {
                                String.format("%.5f, %.5f", it.latitude, it.longitude)
                            } ?: "Čekam GPS signal...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            PhotoPickerField(state = photoPicker, placeholder = "Snimite fotografiju mesta")

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Naziv") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Tip objekta", style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceType.entries.forEach { entry ->
                    FilterChip(
                        selected = entry == type,
                        onClick = { type = entry },
                        label = { Text("${entry.emoji} ${entry.label}") },
                    )
                }
            }

            Button(
                onClick = { viewModel.save(title, description, type, photoPicker.uri) },
                enabled = !state.saving && location != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.saving) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp))
                    }
                } else {
                    Text("Sačuvaj mesto (+20 poena)")
                }
            }
        }
    }
}
