package com.elfak.journeyjournal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.elfak.journeyjournal.ui.navigation.AppRoot
import com.elfak.journeyjournal.ui.theme.JourneyJournalTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var pendingPlaceId by androidx.compose.runtime.remember {
                mutableStateOf(intent?.getStringExtra(EXTRA_PLACE_ID))
            }
            JourneyJournalTheme {
                AppRoot(
                    pendingPlaceId = pendingPlaceId,
                    onPlaceIdHandled = { pendingPlaceId = null },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    companion object {
        const val EXTRA_PLACE_ID = "extra_place_id"
    }
}
