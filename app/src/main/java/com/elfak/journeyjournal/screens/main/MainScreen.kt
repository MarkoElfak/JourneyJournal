package com.elfak.journeyjournal.screens.main

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.scaleMatrix
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.ui_components.buttons.PrimaryButton
import com.elfak.journeyjournal.ui_components.buttons.PrimaryTextButton
import com.elfak.journeyjournal.ui_components.text_fields.InputTextField

@Composable
fun MainScreen(

) {
    val context = LocalContext.current

    val isLoading = remember { mutableStateOf(true) }

    val isEditing = remember { mutableStateOf(false) }

    val mainViewModel = viewModel<MainViewModel>()

    setUpMainObservers(context, isLoading, mainViewModel)

    LaunchedEffect(Unit) {
        mainViewModel.getUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp)
    ) {
        if (isLoading.value) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(top = 25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            modifier = Modifier.size(120.dp).clip(CircleShape).border(1.dp, Color.Black, CircleShape),
                            painter = painterResource(R.drawable.ic_person),
                            contentScale = ContentScale.FillBounds,
                            contentDescription = null,
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        if (isEditing.value) {
                            PrimaryTextButton(
                                text = stringResource(R.string.set_picture_label),
                                onClick = {

                                }
                            )
                            Spacer(modifier = Modifier.height(15.dp))
                            PrimaryTextButton(
                                text = stringResource(R.string.save_changes_label),
                                onClick = {
                                    isEditing.value = false
                                }
                            )
                        } else {
                            PrimaryTextButton(
                                text = stringResource(R.string.edit_profile_label),
                                onClick = {
                                    isEditing.value = true
                                }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth().weight(1.3f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                modifier = Modifier.size(35.dp).align(Alignment.CenterEnd).clickable(
                                    enabled = true,
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {

                                },
                                painter = painterResource(R.drawable.ic_add_friend),
                                contentDescription = null,
                            )
                        }
                        Spacer(modifier = Modifier.height(15.dp))
                        InputTextField(
                            initValue = mainViewModel.username.value,
                            enabled = isEditing.value,
                            onInputChange = {

                            }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        InputTextField(
                            initValue = mainViewModel.email.value,
                            enabled = isEditing.value,
                            onInputChange = {

                            }
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        Image(
                            modifier = Modifier.size(100.dp),
                            painter = painterResource(R.drawable.ic_map),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

private fun setUpMainObservers(
    context: Context,
    loading: MutableState<Boolean>,
    mainViewModel: MainViewModel
) {
    with(mainViewModel) {
        user.observe(context as LifecycleOwner) {
            if (it != null) {
                extractData()
                loading.value = false
            }
        }
    }
}
