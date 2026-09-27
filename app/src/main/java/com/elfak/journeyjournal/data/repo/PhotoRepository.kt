package com.elfak.journeyjournal.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import com.elfak.journeyjournal.data.AppConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Uploads user photos. Images are always downscaled and re-compressed on the device so that
 * uploads stay small (registration photo, place photo).
 */
class PhotoRepository(private val context: Context) {

    private val storage by lazy { FirebaseStorage.getInstance() }

    suspend fun upload(uri: Uri, path: String): String = withContext(Dispatchers.IO) {
        if (AppConfig.USE_FIREBASE_STORAGE) {
            val bytes = encode(uri, maxSize = 1280, quality = 80)
            val ref = storage.reference.child(path)
            val metadata = StorageMetadata.Builder().setContentType("image/jpeg").build()
            ref.putBytes(bytes, metadata).await()
            ref.downloadUrl.await().toString()
        } else {
            // Fallback: keep the picture inside the Firestore document (must stay well under 1 MB).
            val bytes = encode(uri, maxSize = 640, quality = 55)
            "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    private fun encode(uri: Uri, maxSize: Int, quality: Int): ByteArray {
        val bitmap = decodeScaled(uri, maxSize)
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }

    private fun decodeScaled(uri: Uri, maxSize: Int): Bitmap {
        val resolver = context.contentResolver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder also applies the EXIF orientation of camera photos.
            val source = ImageDecoder.createSource(resolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = false
                val scale = max(info.size.width, info.size.height).toFloat() / maxSize
                if (scale > 1f) {
                    decoder.setTargetSize(
                        (info.size.width / scale).toInt().coerceAtLeast(1),
                        (info.size.height / scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / sample > maxSize) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Nije moguće učitati sliku.")
    }
}
