package com.elfak.journeyjournal.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.ui_components.buttons.PrimaryButton
import com.elfak.journeyjournal.utils.showMsg

@Composable
fun MainScreen(

) {
    val context = LocalContext.current
    val mainViewModel = viewModel<MainViewModel>()

    LaunchedEffect(Unit) {
        mainViewModel.getUser()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = mainViewModel.user.value.toString())
        PrimaryButton(
            text = "asdasd"
        ) {
            context.showMsg(mainViewModel.user.value.toString())
        }
    }
}