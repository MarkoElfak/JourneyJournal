package com.elfak.journeyjournal.ui.common

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

class PhotoPickerState(
    val uri: Uri?,
    val takePhoto: () -> Unit,
    val pickFromGallery: () -> Unit,
)

/** Camera capture (as required by the specification) with an optional gallery fallback. */
@Composable
fun rememberPhotoPicker(): PhotoPickerState {
    val context = LocalContext.current
    var photoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var pendingUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) photoUri = pendingUri }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) photoUri = uri }

    return remember(photoUri) {
        PhotoPickerState(
            uri = photoUri,
            takePhoto = {
                val target = createImageUri(context)
                pendingUri = target
                cameraLauncher.launch(target)
            },
            pickFromGallery = {
                galleryLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
        )
    }
}

@Composable
fun PhotoPickerField(
    state: PhotoPickerState,
    modifier: Modifier = Modifier,
    height: Int = 180,
    placeholder: String = "Nije izabrana fotografija",
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Card(modifier = Modifier
            .fillMaxWidth()
            .height(height.dp)) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.uri != null) {
                    RemoteImage(
                        model = state.uri.toString(),
                        contentDescription = "Izabrana fotografija",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PhotoCamera,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = state.takePhoto, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text("  Kamera")
            }
            OutlinedButton(onClick = state.pickFromGallery, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                Text("  Galerija")
            }
        }
    }
}

private fun createImageUri(context: Context): Uri {
    val directory = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File.createTempFile("photo_", ".jpg", directory)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
