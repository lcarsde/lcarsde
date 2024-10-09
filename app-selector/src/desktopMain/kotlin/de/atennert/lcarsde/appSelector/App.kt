package de.atennert.lcarsde.appSelector

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.atennert.lcarsde.LabelRoundSide
import de.atennert.lcarsde.TextButtonRound
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalLayoutApi::class)
@Composable
@Preview
fun App() {
    val appManager = AppManager()
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val state = rememberScrollState()

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(state)) {
            appManager.appsByCategory.map { (category, apps) -> Pair(category, apps) }
                .sortedBy { (category) -> category }
                .forEachIndexed { index, (category, apps) ->
                    if (index > 0) {
                        Spacer(Modifier.height(8.dp))
                    }

                    LabelRoundSide(category)

                    Spacer(Modifier.height(8.dp))

                    FlowRow (
                        modifier = Modifier.fillMaxWidth().align(Alignment.Start),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apps.sortedBy { it.name }.forEach { app ->
                            TextButtonRound(
                                text = checkNotNull(app.name),
                                modifier = Modifier.weight(1f),
                                color = app.color
                            ) {
                                app.start()
                            }
                        }
                    }
                }
        }

        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            style = LocalScrollbarStyle.current.copy(
                unhoverColor = Color(0x449999CC),
                hoverColor = Color(0xAA9999CC)
            )
        )
    }
}