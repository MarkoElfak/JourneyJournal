package com.elfak.journeyjournal.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.ui_components.buttons.PrimaryButton
import com.elfak.journeyjournal.ui_components.texts.PrimaryText

@Composable
fun WelcomeScreen(
    navigateToLoginScreen: () -> Unit,
    navigateToRegisterScreen: () -> Unit,
    navigateToInfoScreen: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterEnd)
                    .clickable(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navigateToInfoScreen.invoke()
                    },
                painter = painterResource(R.drawable.ic_info),
                contentDescription = null,
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        Image(
            painter = painterResource(R.drawable.airplane),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.height(150.dp))
        PrimaryText(
            text = stringResource(R.string.welcome_label),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(50.dp))
        PrimaryButton(
            modifier = Modifier.padding(horizontal = 50.dp),
            text = stringResource(R.string.login_label),
            onClick = { navigateToLoginScreen.invoke() }
        )
        Spacer(modifier = Modifier.height(20.dp))
        PrimaryButton(
            modifier = Modifier.padding(horizontal = 50.dp),
            text = stringResource(R.string.register_label),
            onClick = { navigateToRegisterScreen.invoke() }
        )
    }
}

@Preview
@Composable
private fun WelcomeScreenPreview() {
    WelcomeScreen(
        navigateToLoginScreen = {},
        navigateToRegisterScreen = {},
        navigateToInfoScreen = {},
    )
}