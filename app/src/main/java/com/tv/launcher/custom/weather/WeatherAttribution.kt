package com.tv.launcher.custom.weather

import android.content.Context
import android.content.Intent
import android.net.Uri

internal fun openWeatherAttribution(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(OPEN_METEO_ATTRIBUTION_URL)))
    }
}

private const val OPEN_METEO_ATTRIBUTION_URL = "https://open-meteo.com/en/license"
