package com.elfak.journeyjournal.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.elfak.journeyjournal.data.model.DateField
import com.elfak.journeyjournal.data.model.PlaceFilter
import com.elfak.journeyjournal.data.model.PlaceSort
import com.elfak.journeyjournal.data.model.PlaceType
import com.elfak.journeyjournal.util.formatDate
import kotlin.math.roundToInt

private const val END_OF_DAY_MS = 24 * 60 * 60 * 1000L - 1

/**
 * All filters required by the specification: attributes (search text, type, author, rating),
 * a creation / last-interaction date range and a radius around the current position.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheetContent(
    filter: PlaceFilter,
    authors: List<String>,
    hasLocation: Boolean,
    onApply: (PlaceFilter) -> Unit,
    onReset: () -> Unit,
) {
    var draft by remember(filter) { mutableStateOf(filter) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var authorMenu by remember { mutableStateOf(false) }
    var sortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 640.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Filteri", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        OutlinedTextField(
            value = draft.query,
            onValueChange = { draft = draft.copy(query = it) },
            label = { Text("Pretraga (naziv, opis, tip, autor)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Tip objekta")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PlaceType.entries.forEach { type ->
                FilterChip(
                    selected = type in draft.types,
                    onClick = {
                        draft = draft.copy(
                            types = if (type in draft.types) draft.types - type else draft.types + type
                        )
                    },
                    label = { Text("${type.emoji} ${type.label}") },
                )
            }
        }

        SectionLabel("Autor")
        Box {
            OutlinedButton(onClick = { authorMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text(draft.author.ifBlank { "Svi autori" }, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = authorMenu, onDismissRequest = { authorMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Svi autori") },
                    onClick = {
                        draft = draft.copy(author = "")
                        authorMenu = false
                    },
                )
                authors.forEach { author ->
                    DropdownMenuItem(
                        text = { Text(author) },
                        onClick = {
                            draft = draft.copy(author = author)
                            authorMenu = false
                        },
                    )
                }
            }
        }

        SectionLabel(
            if (draft.minRating <= 0f) "Minimalna ocena: bilo koja"
            else "Minimalna ocena: ${draft.minRating.roundToInt()}"
        )
        Slider(
            value = draft.minRating,
            onValueChange = { draft = draft.copy(minRating = it) },
            valueRange = 0f..5f,
            steps = 4,
        )

        HorizontalDivider()
        SectionLabel("Vremenski opseg")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DateField.entries.forEach { field ->
                FilterChip(
                    selected = draft.dateField == field,
                    onClick = { draft = draft.copy(dateField = field) },
                    label = { Text(field.label) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { showFromPicker = true }, modifier = Modifier.weight(1f)) {
                Text("Od: ${draft.fromDate.formatDate()}")
            }
            OutlinedButton(onClick = { showToPicker = true }, modifier = Modifier.weight(1f)) {
                Text("Do: ${draft.toDate.formatDate()}")
            }
        }
        if (draft.fromDate != null || draft.toDate != null) {
            TextButton(onClick = { draft = draft.copy(fromDate = null, toDate = null) }) {
                Text("Ukloni vremenski opseg")
            }
        }

        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pretraga u radijusu", style = MaterialTheme.typography.titleSmall)
                Text(
                    text = if (hasLocation) "U odnosu na trenutnu lokaciju"
                    else "Potrebna je trenutna lokacija",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = draft.radiusKm != null,
                enabled = hasLocation,
                onCheckedChange = { checked ->
                    draft = draft.copy(radiusKm = if (checked) 2f else null)
                },
            )
        }
        draft.radiusKm?.let { radius ->
            Text(
                text = "Radijus: ${String.format("%.1f", radius)} km",
                style = MaterialTheme.typography.bodyMedium,
            )
            Slider(
                value = radius,
                onValueChange = { draft = draft.copy(radiusKm = it) },
                valueRange = 0.2f..50f,
            )
        }

        HorizontalDivider()
        SectionLabel("Sortiranje")
        Box {
            OutlinedButton(onClick = { sortMenu = true }, modifier = Modifier.fillMaxWidth()) {
                Text(draft.sort.label, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                PlaceSort.entries.forEach { sort ->
                    DropdownMenuItem(
                        text = { Text(sort.label) },
                        onClick = {
                            draft = draft.copy(sort = sort)
                            sortMenu = false
                        },
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    draft = PlaceFilter()
                    onReset()
                },
                modifier = Modifier.weight(1f),
            ) { Text("Poništi") }
            Button(onClick = { onApply(draft) }, modifier = Modifier.weight(1f)) {
                Text("Primeni")
            }
        }
    }

    if (showFromPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = draft.fromDate)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    draft = draft.copy(fromDate = pickerState.selectedDateMillis)
                    showFromPicker = false
                }) { Text("U redu") }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) { Text("Otkaži") }
            },
        ) { DatePicker(state = pickerState) }
    }

    if (showToPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = draft.toDate)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    draft = draft.copy(
                        toDate = pickerState.selectedDateMillis?.plus(END_OF_DAY_MS)
                    )
                    showToPicker = false
                }) { Text("U redu") }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) { Text("Otkaži") }
            },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 4.dp),
    )
}
