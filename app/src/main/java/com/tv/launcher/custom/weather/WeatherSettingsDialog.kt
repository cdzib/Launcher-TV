package com.tv.launcher.custom.weather

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.tv.launcher.custom.core.model.weather.WeatherUnit
import com.tv.launcher.custom.core.ui.R
import com.tv.launcher.custom.core.ui.theme.LauncherTheme
import com.tv.launcher.custom.feature.home.presentation.state.HomeIntent
import com.tv.launcher.custom.feature.home.presentation.state.WeatherUiState

@Composable
fun WeatherSettingsDialog(
    state: WeatherUiState,
    onIntent: (HomeIntent) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        WeatherSettingsContent(state, onIntent, onDismiss)
    }
}

@Composable
private fun WeatherSettingsContent(
    state: WeatherUiState,
    onIntent: (HomeIntent) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Surface(Modifier.fillMaxWidth().heightIn(max = 680.dp), shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.weather_settings_title), fontSize = 24.sp)
            Text(stringResource(R.string.weather_settings_description))
            Text(stringResource(R.string.weather_city_search), fontSize = 18.sp)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BasicTextField(
                    value = state.cityQuery,
                    onValueChange = { onIntent(HomeIntent.UpdateWeatherCityQuery(it)) },
                    modifier = Modifier.weight(1f).heightIn(min = 56.dp)
                        .background(Color(0xFFE7E9EE), RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 18.dp),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        if (state.cityQuery.isEmpty()) Text(stringResource(R.string.weather_search_hint), color = Color.DarkGray)
                        innerTextField()
                    }
                )
                Button(
                    onClick = { onIntent(HomeIntent.SearchWeatherCities(state.cityQuery)) },
                    enabled = state.cityQuery.trim().length >= 2 && !state.isSearchingCities
                ) {
                    Text(stringResource(if (state.isSearchingCities) R.string.weather_loading else R.string.weather_city_search_action))
                }
            }
            if (state.isSearchingCities) Text(stringResource(R.string.weather_loading))
            if (state.cities.isEmpty() && state.cityQuery.trim().length >= 2 && !state.isSearchingCities && !state.hasWeatherError) {
                Text(stringResource(R.string.weather_city_not_found))
            }
            state.cities.forEach { city ->
                Button(onClick = { onIntent(HomeIntent.SelectWeatherCity(city)) }, modifier = Modifier.fillMaxWidth()) {
                    Text(city.displayName, maxLines = 1)
                }
            }
            if (state.hasWeatherError) Text(stringResource(R.string.weather_offline))
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.weather_unit), fontSize = 18.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onIntent(HomeIntent.SetWeatherUnit(WeatherUnit.CELSIUS)) },
                    enabled = state.settings.unit != WeatherUnit.CELSIUS
                ) { Text(stringResource(R.string.weather_celsius)) }
                Button(
                    onClick = { onIntent(HomeIntent.SetWeatherUnit(WeatherUnit.FAHRENHEIT)) },
                    enabled = state.settings.unit != WeatherUnit.FAHRENHEIT
                ) { Text(stringResource(R.string.weather_fahrenheit)) }
            }
            Button(
                onClick = { onIntent(HomeIntent.SetWeatherEnabled(!state.settings.isEnabled)) },
                enabled = state.settings.city != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.weather_enable) + if (state.settings.isEnabled) " ✓" else "")
            }
            val selectedCity = state.settings.city
            if (selectedCity != null) {
                Text("${stringResource(R.string.weather_settings_title)}: ${selectedCity.displayName}")
                Button(onClick = { onIntent(HomeIntent.ClearWeatherSettings) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.weather_clear_city))
                }
            }
            Button(onClick = { openWeatherAttribution(context) }) {
                Text(stringResource(R.string.weather_attribution), fontSize = 12.sp)
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.close)) }
        }
    }
}

@Preview(widthDp = 720, heightDp = 680)
@Composable
private fun WeatherSettingsContentPreview() = LauncherTheme(com.tv.launcher.custom.core.model.theme.ThemeId.WEBOS) {
    WeatherSettingsContent(WeatherUiState(cityQuery = "Mérida"), {}, {})
}
