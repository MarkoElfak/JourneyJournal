package com.elfak.journeyjournal.ui_components.texts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.elfak.journeyjournal.R

@Composable
fun PrimaryText(
    modifier: Modifier = Modifier,
    text: String,
    textSize: Int = 30,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        color = Color.Black,
        style = TextStyle(
            fontSize = textSize.sp,
            fontWeight = FontWeight(600),
            fontFamily = FontFamily(Font(R.font.macondo))
        )
    )
}