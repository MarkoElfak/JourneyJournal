package com.elfak.journeyjournal.screens.forgot_password

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.ui_components.buttons.ButtonWithImage
import com.elfak.journeyjournal.ui_components.text_fields.InputTextField
import com.elfak.journeyjournal.utils.showMsg

@Composable
fun ForgotPasswordScreen(
    navigateToLoginScreen: () -> Unit
) {

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val forgotPasswordViewModel = viewModel<ForgotPasswordViewModel>()

    setUpForgotPasswordObservers(context, forgotPasswordViewModel, navigateToLoginScreen)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp)
            .verticalScroll(rememberScrollState())
            .clickable(
                enabled = true,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputTextField(
                label = stringResource(R.string.enter_email_label),
                onInputChange = {
                    forgotPasswordViewModel.email.value = it
                }
            )
            Spacer(modifier = Modifier.height(50.dp))
            ButtonWithImage(
                modifier = Modifier.padding(horizontal = 50.dp),
                text = stringResource(R.string.send_label),
                iconId = R.drawable.ic_envelope,
                onClick = {
                    forgotPasswordViewModel.forgotPassword(context)
                }
            )
        }
    }
}

private fun setUpForgotPasswordObservers(
    context: Context,
    forgotPasswordViewModel: ForgotPasswordViewModel,
    navigateToLoginScreen: () -> Unit
) {
    with(forgotPasswordViewModel) {
        resetPasswordSuccessfulEvent.observe(context as LifecycleOwner) {
            if (it == true) {
                navigateToLoginScreen.invoke()
            }
        }

        resetPasswordUnsuccessfulEvent.observe(context as LifecycleOwner) {
            if (it != null) {
                context.showMsg(it)
            }
        }
    }
}
