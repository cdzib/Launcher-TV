package com.tv.launcher.custom.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.weather.CurrentWeather
import com.tv.launcher.custom.core.model.weather.WeatherCity
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.WeatherUiState
import java.time.Instant

@Composable
fun WeatherSummaryCard(
    state: WeatherUiState,
    backgroundColor: Color,
    contentColor: Color,
    mutedColor: Color,
    onIntent: (HomeIntent) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    stackActions: Boolean = false,
    compact: Boolean = false
) {
    val context = LocalContext.current
    val city = state.settings.city
    val currentWeather = state.currentWeather
    var isFocused by remember { mutableStateOf(false) }
    if (!state.settings.isEnabled || city == null) return
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(backgroundColor.copy(alpha = backgroundColor.alpha * 0.94f))
            .then(if (compact) Modifier.onFocusChanged { isFocused = it.isFocused }
                .then(if (isFocused) Modifier.border(2.dp, contentColor, RoundedCornerShape(16.dp)) else Modifier)
                .clickable(onClick = onOpenSettings) else Modifier).padding(if (compact) 10.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(city.displayName, color = contentColor, fontSize = if (compact) 11.sp else 16.sp, maxLines = 1)
        when {
            state.isLoadingWeather && state.currentWeather == null -> Text(
                stringResource(R.string.weather_loading), color = mutedColor
            )
            currentWeather != null -> {
                val weather = currentWeather
                Text(
                    "${weather.temperature.toInt()}${if (weather.unit == WeatherUnit.CELSIUS) "°C" else "°F"}" +
                        if (compact) "" else " · " + stringResource(weatherConditionString(weather.weatherCode)),
                    color = contentColor,
                    fontSize = if (compact) 28.sp else 24.sp
                )
                Text(
                    stringResource(if (compact) weatherConditionString(weather.weatherCode) else if (weather.isDay) R.string.weather_day else R.string.weather_night),
                    color = mutedColor,
                    fontSize = if (compact) 10.sp else 12.sp
                )
                if (state.hasWeatherError) Text(stringResource(R.string.weather_offline), color = mutedColor, fontSize = 12.sp)
            }
            state.hasWeatherError -> {
                Text(stringResource(R.string.weather_offline), color = contentColor)
                Button(onClick = { onIntent(HomeIntent.RefreshWeather) }) { Text(stringResource(R.string.weather_retry)) }
            }
        }
        val actions: @Composable () -> Unit = {
            Button(onClick = { openWeatherAttribution(context) }) {
                Text(stringResource(R.string.weather_attribution), color = mutedColor, fontSize = 11.sp)
            }
            Button(onClick = onOpenSettings) { Text(stringResource(R.string.weather_settings_title)) }
        }
        if (compact) Text(stringResource(R.string.weather_attribution), color = mutedColor, fontSize = 8.sp)
        else if (stackActions) Column { actions() } else Row { actions() }
    }
}

private fun weatherConditionString(weatherCode: Int): Int = when (weatherCode) {
    0 -> R.string.weather_clear_conditions
    1, 2, 3 -> R.string.weather_cloudy_conditions
    45, 48 -> R.string.weather_fog_conditions
    51, 53, 55, 56, 57 -> R.string.weather_drizzle_conditions
    61, 63, 65, 66, 67 -> R.string.weather_rain_conditions
    71, 73, 75, 77, 85, 86 -> R.string.weather_snow_conditions
    80, 81, 82 -> R.string.weather_showers_conditions
    95, 96, 99 -> R.string.weather_thunder_conditions
    else -> R.string.weather_unknown_conditions
}

@Preview(widthDp = 420, heightDp = 240)
@Composable
private fun WeatherSummaryCardPreview(compact: Boolean = false) = LauncherTheme(com.tv.launcher.custom.core.model.theme.ThemeId.WEBOS) {
    val colors = com.tv.launcher.custom.core.ui.theme.LocalLauncherStyle.current.palette
    WeatherSummaryCard(
        state = WeatherUiState(
            settings = com.tv.launcher.custom.core.model.weather.WeatherSettings(
                city = WeatherCity(1, "Mérida", "México", 20.9674, -89.6237), isEnabled = true
            ),
            currentWeather = CurrentWeather(31.0, WeatherUnit.CELSIUS, 1, true, Instant.parse("2026-10-25T12:00:00Z"))
        ),
        backgroundColor = colors.surface,
        contentColor = colors.onBackground,
        mutedColor = colors.onMuted,
        onIntent = {},
        onOpenSettings = {},
        compact = compact
    )
}

@Preview(name = "Compact host weather", widthDp = 140, heightDp = 120)
@Composable
private fun WeatherSummaryCardCompactPreview() = WeatherSummaryCardPreview(compact = true)
