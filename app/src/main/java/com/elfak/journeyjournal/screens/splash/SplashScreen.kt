package com.elfak.journeyjournal.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.elfak.journeyjournal.data.local.PrefUtility
import com.elfak.journeyjournal.data.remote.FirebaseWrapper
import com.elfak.journeyjournal.ui_components.images.Logo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navigateToWelcomeScreen: () -> Unit,
    navigateToLoginScreen: () -> Unit,
    navigateToMainScreen: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        delay(500)
        if (PrefUtility.isFirstRun(context)) {
            navigateToWelcomeScreen.invoke()
        } else {
            if (FirebaseWrapper.isLoggedIn()) {
                navigateToMainScreen.invoke()
            } else {
                navigateToLoginScreen.invoke()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Logo(
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    SplashScreen(
        navigateToWelcomeScreen = {},
        navigateToLoginScreen = {},
        navigateToMainScreen = {}
    )
}