package com.tv.launcher.custom.feature.home.presentation.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.tv.launcher.custom.R
import com.tv.launcher.custom.core.theme.LocalLauncherStyle

@Composable
fun WifiStatusIndicator(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    val connectivityManager = remember(context) {
        if (isPreview) null else context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    var isWifiConnected by remember(connectivityManager) {
        mutableStateOf(connectivityManager?.isWifiConnected() ?: false)
    }

    DisposableEffect(connectivityManager) {
        if (connectivityManager == null) return@DisposableEffect onDispose {}
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isWifiConnected = connectivityManager.isWifiConnected()
            }

            override fun onLost(network: Network) {
                isWifiConnected = connectivityManager.isWifiConnected()
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                isWifiConnected = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    val palette = LocalLauncherStyle.current.palette
    Surface(
        modifier = modifier,
        shape = SurfaceDefaults.shape,
        colors = SurfaceDefaults.colors(
            containerColor = palette.surface,
            contentColor = palette.onBackground
        )
    ) {
        val status = if (isWifiConnected) R.string.wifi_connected else R.string.wifi_disconnected
        Text(
            text = stringResource(status),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
    }
}

private fun ConnectivityManager.isWifiConnected(): Boolean {
    val network = activeNetwork ?: return false
    val capabilities = getNetworkCapabilities(network) ?: return false
    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
}
