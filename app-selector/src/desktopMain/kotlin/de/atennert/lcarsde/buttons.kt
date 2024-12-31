package de.atennert.lcarsde

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextButtonRound(text: String, modifier: Modifier = Modifier, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp).padding(0.dp),
        shape = RoundedCornerShape(100),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 2.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color,
            contentColor = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Text(
                text,
                fontFamily = FontFamily("Ubuntu Condensed"),
                fontSize = TextUnit(16f, TextUnitType.Sp),
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun TextButton(text: String, modifier: Modifier = Modifier, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp).padding(0.dp),
        shape = RectangleShape,
        contentPadding = PaddingValues(2.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = color,
            contentColor = Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
            Text(
                text,
                fontFamily = FontFamily("Ubuntu Condensed"),
                fontSize = TextUnit(16f, TextUnitType.Sp),
                fontWeight = FontWeight.SemiBold,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                modifier = Modifier.width(IntrinsicSize.Max)
            )
        }
    }
}
