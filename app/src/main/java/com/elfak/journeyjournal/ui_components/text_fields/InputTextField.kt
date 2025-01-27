package com.elfak.journeyjournal.ui_components.text_fields

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elfak.journeyjournal.R

@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    label: String,
    isPassword: Boolean = false,
    onInputChange: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .then(modifier),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = label,
            textAlign = TextAlign.Center,
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(700),
                fontFamily = FontFamily(Font(R.font.macondo))
            )
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(500),
                fontFamily = FontFamily(Font(R.font.macondo))
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.Black,
                unfocusedIndicatorColor = Color.Black
            ),
            visualTransformation = if (!isPassword) VisualTransformation.None else PasswordVisualTransformation(),
            onValueChange = {
                onInputChange.invoke(it)
                text = it
            },
            singleLine = true
        )
    }
}