package com.elfak.journeyjournal

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.elfak.journeyjournal.di.ServiceLocator
import com.google.android.gms.maps.MapsInitializer

class JourneyApp : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        createNotificationChannels()

        // Warm up the maps renderer so markers can be created as soon as the map is composed.
        runCatching { MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { } }
            .onFailure { Log.w(TAG, "Maps could not be initialized", it) }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components { add(OkHttpNetworkFetcherFactory()) }
            .crossfade(true)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService<NotificationManager>() ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_TRACKING,
                "Praćenje lokacije",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Obaveštenje dok je praćenje lokacije aktivno." }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_NEARBY,
                "Lokacije u blizini",
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply { description = "Obaveštenja kada se približite registrovanoj lokaciji." }
        )
    }

    companion object {
        private const val TAG = "JourneyApp"
        const val CHANNEL_TRACKING = "tracking"
        const val CHANNEL_NEARBY = "nearby"
    }
}
