package com.elfak.journeyjournal.ui_components.images

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.elfak.journeyjournal.R

@Composable
fun Logo(
    modifier: Modifier = Modifier
) {
    Image(
        modifier = modifier,
        painter = painterResource(R.drawable.airplane),
        contentDescription = null,
    )
}