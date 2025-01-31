package com.elfak.journeyjournal.screens.register

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.ui_components.buttons.PrimaryButton
import com.elfak.journeyjournal.ui_components.text_fields.InputTextField
import com.elfak.journeyjournal.utils.showMsg

@Composable
fun RegisterScreen(
    navigateToLoginScreen: () -> Unit
) {
    val context = LocalContext.current

    val registerViewModel = viewModel<RegisterViewModel>()

    setUpRegisterObservers(context, registerViewModel, navigateToLoginScreen)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            InputTextField(
                label = stringResource(R.string.username_label),
                onInputChange = {
                    registerViewModel.username.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputTextField(
                label = stringResource(R.string.password_label),
                isPassword = true,
                onInputChange = {
                    registerViewModel.password.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputTextField(
                label = stringResource(R.string.confirm_password_label),
                isPassword = true,
                onInputChange = {
                    registerViewModel.confirmPassword.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputTextField(
                label = stringResource(R.string.email_label),
                onInputChange = {
                    registerViewModel.email.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputTextField(
                label = stringResource(R.string.dob_label),
                onInputChange = {
                    registerViewModel.dob.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                modifier = Modifier.padding(horizontal = 50.dp),
                text = stringResource(R.string.register_label),
                onClick = {
                    registerViewModel.register(context)
                }
            )
            Spacer(modifier = Modifier.height(20.dp))

            val text = buildAnnotatedString {
                append(stringResource(R.string.already_have_account_label))
                append("  ")
                pushStringAnnotation(tag = "log_in", annotation = "log_in")
                withStyle(
                    SpanStyle(
                        color = Color.Red
                    )
                ) {
                    append(stringResource(R.string.login_label))
                }
                pop()
            }

            Text(
                modifier = Modifier.clickable(
                    enabled = true,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    text.getStringAnnotations(tag = "log_in", start = 0, end = text.length)
                        .firstOrNull()?.let {
                            navigateToLoginScreen.invoke()
                        }
                },
                text = text,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp,
                    fontWeight = FontWeight(500),
                    fontFamily = FontFamily(Font(R.font.macondo))
                )
            )
        }
    }
}

private fun setUpRegisterObservers(
    context: Context,
    registerViewModel: RegisterViewModel,
    navigateToLoginScreen: () -> Unit
) {
    with(registerViewModel) {
        registerSuccessfulEvent.observe(context as LifecycleOwner) {
            if (it == true) {
                registerViewModel.saveUser()
            }
        }

        userSuccessfullySaveEvent.observe(context as LifecycleOwner) {
            if (it == true) {
                navigateToLoginScreen.invoke()
            }
        }

        registerUnsuccessfulEvent.observe(context as LifecycleOwner) {
            if (it != null) {
                context.showMsg(it)
            }
        }

        errorEvent.observe(context as LifecycleOwner) {
            if (it != null) {
                context.showMsg(it)
            }
        }
    }
}
