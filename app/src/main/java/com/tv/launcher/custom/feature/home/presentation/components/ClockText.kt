package com.tv.launcher.custom.feature.home.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private const val MINUTE_MILLIS = 60_000L
private val ClockFormat = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun ClockText(fontSize: TextUnit, modifier: Modifier = Modifier) {
    val time by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(MINUTE_MILLIS - System.currentTimeMillis() % MINUTE_MILLIS)
        }
    }
    Text(
        text = time.format(ClockFormat),
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.Light
    )
}
