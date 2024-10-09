package de.atennert.lcarsde.logout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.atennert.lcarsde.LcarsColors
import de.atennert.lcarsde.TextButtonRound
import de.atennert.lcarsde.logout.definition.DBusDefinition
import de.atennert.lcarsde.logout.definition.LockScreenDefinition
import de.atennert.lcarsde.logout.definition.LogoutDefinition
import org.jetbrains.compose.ui.tooling.preview.Preview

val definitions = arrayOf(
    DBusDefinition("Shutdown", LcarsColors.C_C66, "Stop", "PowerOff"),
    DBusDefinition("Reboot", LcarsColors.C_F96, "Restart", "Reboot"),
    DBusDefinition("Suspend", LcarsColors.C_C9C, null, "Suspend"),
    DBusDefinition("Hibernate", LcarsColors.C_C9C, null, "Hibernate"),
    LockScreenDefinition(),
    LogoutDefinition(),
).filter { it.isAvailable }

@Composable
@Preview
fun App() {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.width(IntrinsicSize.Min),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            definitions.forEach {
                TextButtonRound(
                    text = it.label,
                    color = it.color,
                ) { it.call() }
            }
        }
    }
}
