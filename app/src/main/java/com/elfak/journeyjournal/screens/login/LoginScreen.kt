package com.elfak.journeyjournal.screens.login

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
import androidx.compose.ui.platform.LocalFocusManager
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
import com.elfak.journeyjournal.ui_components.images.Logo
import com.elfak.journeyjournal.ui_components.text_fields.InputTextField
import com.elfak.journeyjournal.utils.showMsg

@Composable
fun LoginScreen(
    navigateToForgotPasswordScreen: () -> Unit,
    navigateToRegisterScreen: () -> Unit,
    navigateToMainScreen: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val loginViewModel = viewModel<LoginViewModel>()

    setUpLoginObservers(context, loginViewModel, navigateToMainScreen)

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
            Logo()
            Spacer(modifier = Modifier.height(50.dp))
            InputTextField(
                label = stringResource(R.string.email_label),
                onInputChange = {
                    loginViewModel.email.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputTextField(
                label = stringResource(R.string.password_label),
                isPassword = true,
                onInputChange = {
                    loginViewModel.password.value = it
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                modifier = Modifier.padding(horizontal = 50.dp),
                text = stringResource(R.string.login_label),
                onClick = {
                    loginViewModel.login(context)
                }
            )
            Spacer(modifier = Modifier.height(30.dp))
            Text(
                modifier = Modifier.clickable(
                    enabled = true,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    navigateToForgotPasswordScreen.invoke()
                },
                text = stringResource(R.string.forgot_password_label),
                color = Color.Black,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(500),
                    fontFamily = FontFamily(Font(R.font.macondo))
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            val text = buildAnnotatedString {
                append(stringResource(R.string.dont_have_account_label))
                append("  ")
                pushStringAnnotation(tag = "sign_up", annotation = "sign_up")
                withStyle(
                    SpanStyle(
                        color = Color.Red
                    )
                ) {
                    append(stringResource(R.string.sign_up_label))
                }
                pop()
            }

            Text(
                modifier = Modifier.clickable(
                    enabled = true,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    text.getStringAnnotations(tag = "sign_up", start = 0, end = text.length)
                        .firstOrNull()?.let {
                            navigateToRegisterScreen.invoke()
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

private fun setUpLoginObservers(
    context: Context,
    loginViewModel: LoginViewModel,
    navigateToMainScreen: () -> Unit
) {
    with(loginViewModel) {
        loginSuccessfulEvent.observe(context as LifecycleOwner) {
            if (it == true) {
                context.showMsg(context.getString(R.string.login_successfully_label))
                navigateToMainScreen.invoke()
            }
        }

        loginUnsuccessfulEvent.observe(context as LifecycleOwner) {
            if (it != null) {
                context.showMsg(it)
            }
        }
    }
}
