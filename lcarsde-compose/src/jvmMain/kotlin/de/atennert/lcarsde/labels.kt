package de.atennert.lcarsde

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalTextApi::class)
@Composable
fun LabelRoundSide(text: String) {
    Surface(
        shape = RoundedCornerShape(100),
        color = LcarsColors.C_99F,
    ) {
        Surface(
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text,
                fontFamily = FontFamily("Ubuntu Condensed"),
                fontSize = TextUnit(24f, TextUnitType.Sp),
                fontWeight = FontWeight.Normal,
                overflow = TextOverflow.Ellipsis,
                softWrap = false,
                color = LcarsColors.C_F90,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}