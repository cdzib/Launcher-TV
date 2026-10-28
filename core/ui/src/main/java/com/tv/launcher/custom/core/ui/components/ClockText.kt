package com.tv.launcher.custom.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.platform.LocalContext
import android.text.format.DateFormat
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.tv.launcher.custom.core.model.theme.ThemeId
import com.tv.launcher.custom.core.ui.theme.LauncherTheme

private const val MINUTE_MILLIS = 60_000L

fun formatClockText(
    dateTime: LocalDateTime,
    use24Hour: Boolean,
    showDate: Boolean,
    locale: Locale
): String {
    val timePattern = if (use24Hour) "HH:mm" else "h:mm a"
    val time = dateTime.format(DateTimeFormatter.ofPattern(timePattern, locale))
    if (!showDate) return time
    val date = dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d", locale))
    return "$time · $date"
}

@Composable
fun ClockText(
    fontSize: TextUnit,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    use24Hour: Boolean? = null,
    showDate: Boolean = false,
    stackedDate: Boolean = false
) {
    val context = LocalContext.current
    val time by produceState(initialValue = LocalTime.now()) {
        while (true) {
            value = LocalTime.now()
            delay(MINUTE_MILLIS - System.currentTimeMillis() % MINUTE_MILLIS)
        }
    }
    val dateTime = LocalDateTime.of(java.time.LocalDate.now(), time)
    if (stackedDate) {
        Column(modifier) {
            Text(formatClockText(dateTime, use24Hour ?: DateFormat.is24HourFormat(context), false, Locale.getDefault()),
                fontSize = fontSize, fontWeight = FontWeight.Thin, color = color, maxLines = 1)
            if (showDate) Text(dateTime.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
                fontSize = fontSize * 0.22f, color = color.copy(alpha = 0.85f), maxLines = 1)
        }
        return
    }
    Text(
        text = formatClockText(
            dateTime = LocalDateTime.of(java.time.LocalDate.now(), time),
            use24Hour = use24Hour ?: DateFormat.is24HourFormat(context),
            showDate = showDate,
            locale = Locale.getDefault()
        ),
        modifier = modifier,
        fontSize = fontSize,
        fontWeight = FontWeight.Light,
        color = color
    )
}

@Preview
@Composable
private fun ClockTextPreview() = LauncherTheme(ThemeId.WEBOS) {
    ClockText(fontSize = 24.sp, showDate = true)
}
