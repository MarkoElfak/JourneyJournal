package com.elfak.journeyjournal.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.elfak.journeyjournal.R
import com.elfak.journeyjournal.ui_components.texts.PrimaryText

@Composable
fun InfoScreen(

) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PrimaryText(
            text = stringResource(R.string.about_label),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(50.dp))
        PrimaryText(
            text = stringResource(R.string.about_description),
            textSize = 14,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(100.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(40.dp),
                painter = painterResource(R.drawable.ic_star),
                contentDescription = null,
            )
            PrimaryText(
                text = stringResource(R.string.star_info_label),
                textSize = 20,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Image(
                modifier = Modifier
                    .size(40.dp),
                painter = painterResource(R.drawable.ic_pin),
                contentDescription = null,
            )
            PrimaryText(
                text = stringResource(R.string.pin_info_label),
                textSize = 20,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun InfoScreenPreview() {
    InfoScreen()
}