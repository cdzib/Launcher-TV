package com.tv.launcher.custom.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

/** Neutral utility surfaces shared by settings and the theme editor. */
object SettingsColors {
    val background = Color(0xFF12171E)
    val surface = Color(0xFF202731)
    val selected = Color(0xFF263242)
    val accent = Color(0xFFB9D5FF)
    val primary = Color(0xFFD9E8FD)
    val onPrimary = Color(0xFF172536)
    val text = Color(0xFFF4F6F8)
    val muted = Color(0xFFB7C0CE)
    val divider = Color.White.copy(alpha = 0.08f)
    val destructive = Color(0xFFFFB4AB)
}

@Composable
fun SettingsButton(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    primary: Boolean = false,
    destructive: Boolean = false,
    icon: ImageVector? = null,
    containerColor: Color = SettingsColors.surface,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val contentColor = when {
        primary -> SettingsColors.onPrimary
        destructive -> SettingsColors.destructive
        else -> SettingsColors.text
    }
    Button(
        onClick = onClick, enabled = enabled, modifier = modifier.heightIn(min = 48.dp).semantics { this.selected = selected },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp)),
        scale = ButtonDefaults.scale(focusedScale = 1.03f),
        colors = ButtonDefaults.colors(
            containerColor = when { primary -> SettingsColors.primary; selected -> SettingsColors.selected; else -> containerColor },
            contentColor = contentColor,
            focusedContainerColor = if (primary) SettingsColors.primary else SettingsColors.selected,
            focusedContentColor = contentColor
        ),
        border = ButtonDefaults.border(
            border = if (selected) Border(BorderStroke(1.dp, SettingsColors.accent)) else Border.None,
            focusedBorder = Border(BorderStroke(3.dp, SettingsColors.text), inset = (-4).dp)
        )
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(20.dp), tint = contentColor)
            if (label.isNotEmpty()) Spacer(Modifier.width(8.dp))
        }
        if (label.isNotEmpty()) Text(label, fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}
