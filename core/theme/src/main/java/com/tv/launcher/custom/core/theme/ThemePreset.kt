package com.tv.launcher.custom.core.theme

import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.model.theme.ThemeBlockType as T

data class ThemePreset(val name: String, val composition: ThemeComposition, val backgroundAssetName: String? = null)

/**
 * Declarative themes rendered by CompositionCanvas and exportable as ZIP.
 * They mirror the native layouts; native layouts remain the reference until parity is validated.
 */
object ThemePresets {
    private fun c(v: Long) = v.toInt()

    private val androidTv = ThemePreset("Android TV", ThemeComposition(
        padding = 40, spacing = 12,
        topBar = TopBarConfig(
            style = TopBarStyle.TABS_TEXT, tabs = "Inicio,Buscar,Apps", logo = "Android TV",
            showSearch = false, showSettings = true, showClock = true
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 200),
            ThemeBlock(T.TITLE, text = "Tus apps", textSize = 14),
            ThemeBlock(T.APPS_ROW, maxItems = 14, itemHeight = 65, aspect = CardAspect.WIDE,
                labelPos = LabelPos.BELOW, cornerRadius = 6),
            ThemeBlock(T.TITLE, text = "Apps para ti", textSize = 13),
            ThemeBlock(T.APPS_ROW, maxItems = 12, itemHeight = 124, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 6)
        )))

    private val googleTv = ThemePreset("Google TV", ThemeComposition(
        padding = 40, spacing = 12,
        topBar = TopBarConfig(
            style = TopBarStyle.TABS_PILL, tabs = "Para ti,Películas,Series,Apps", logo = "Google TV",
            showSearch = false, showSettings = true, showClock = true
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 200),
            ThemeBlock(T.TITLE, text = "Apps para ti", textSize = 13),
            ThemeBlock(T.APPS_ROW, maxItems = 12, itemHeight = 108, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 8),
            ThemeBlock(T.TITLE, text = "Tus apps", textSize = 14),
            ThemeBlock(T.APPS_ROW, maxItems = 14, itemHeight = 60, aspect = CardAspect.WIDE,
                labelPos = LabelPos.BELOW, cornerRadius = 8)
        )))

    private val fireTv = ThemePreset("Fire TV", ThemeComposition(
        padding = 32, spacing = 12,
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 216),
            ThemeBlock(T.NAV_ROW, text = "Inicio,Buscar,Apps", showSystemSettings = true,
                showPersonalization = true, spacing = 12),
            ThemeBlock(T.TITLE, text = "Tus apps", textSize = 13),
            ThemeBlock(T.APPS_ROW, maxItems = 8, itemHeight = 92, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 10),
            ThemeBlock(T.APPS_ROW, maxItems = 8, itemHeight = 92, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 10, itemOffset = 8)
        )))

    private val roku = ThemePreset("Roku", ThemeComposition(
        padding = 48, spacing = 12,
        rail = RailConfig(
            style = RailStyle.LIST, items = "Inicio,Apps,Buscar,Ajustes,Personalización",
            logo = "Roku", width = 326, cornerRadius = 2, textSize = 18
        ),
        blocks = listOf(
            ThemeBlock(T.APPS_GRID, maxItems = 18, columns = 3, aspect = CardAspect.CLASSIC,
                cornerRadius = 2, labelPos = LabelPos.BELOW, spacing = 12)
        )))

    private val tizen = ThemePreset("Tizen", ThemeComposition(
        padding = 16, spacing = 8,
        rail = RailConfig(
            style = RailStyle.ICONS, items = "Inicio,Buscar,Apps", width = 56,
            circular = true, bottomItems = ""
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 248),
            ThemeBlock(T.NAV_ROW, text = "Inicio,Apps", align = BlockAlign.CENTER, spacing = 24),
            ThemeBlock(T.TITLE, text = "Apps para ti", textSize = 13),
            ThemeBlock(T.APPS_ROW, maxItems = 12, itemHeight = 92, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 8),
            ThemeBlock(T.APPS_ROW, maxItems = 20, itemHeight = 54, aspect = CardAspect.SQUARE,
                labelPos = LabelPos.NONE, cornerRadius = 6, spacing = 8)
        )))

    private val webOs = ThemePreset("webOS", ThemeComposition(
        padding = 8, topPadding = 24, spacing = 12,
        rail = RailConfig(style = RailStyle.ICONS, items = "Inicio,Buscar,Apps,Ajustes,Personalización", width = 56, overlay = true),
        blocks = listOf(
            ThemeBlock(T.HERO, itemHeight = 189, appContent = AppContent.FEATURED_APP, startInset = 48, endInset = 12),
            ThemeBlock(T.CHIPS_ROW, itemHeight = 46, appContent = AppContent.SHORTCUTS),
            ThemeBlock(T.FAVORITES_ROW, maxItems = 20, itemHeight = 51, appContent = AppContent.APP_ICONS),
            ThemeBlock(T.TITLE, text = "Tus apps", appContent = AppContent.SHORTCUT_EDITOR),
            ThemeBlock(T.APPS_ROW, maxItems = 20, itemHeight = 86, appContent = AppContent.APP_CARDS)
        )))

    private val tvOs = ThemePreset("Apple tvOS", ThemeComposition(
        padding = 16, spacing = 12,
        rail = RailConfig(
            style = RailStyle.ICONS_LABELS, items = "Inicio,Buscar,Apps,Ajustes,Personalización",
            logo = "TV", width = 184, floating = true, cornerRadius = 20, textSize = 15,
            bgColor = c(0xFF2A211C), bgAlpha = 90, appShortcutCount = 3
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 378),
            ThemeBlock(T.TITLE, text = "Tus apps", textSize = 13, placement = BlockPlacement.BOTTOM),
            ThemeBlock(T.APPS_ROW, maxItems = 12, itemHeight = 102, aspect = CardAspect.WIDE,
                labelPos = LabelPos.NONE, cornerRadius = 6, placement = BlockPlacement.BOTTOM)
        )))

    private val freeMyTv = ThemePreset("Free My TV", ThemeComposition(
        padding = 28, spacing = 12,
        topBar = TopBarConfig(
            style = TopBarStyle.MINIMAL, tabs = "", logo = "Free My TV",
            showSearch = false, showSettings = true, showClock = true
        ),
        blocks = listOf(
            ThemeBlock(T.NAV_ROW, text = "Inicio,Buscar,Apps", spacing = 12),
            ThemeBlock(T.HERO, text = "DESTACADO", subtitle = "Todo lo que te gusta, en tu pantalla",
                textSize = 34, itemHeight = 180, bgAlpha = 0, padding = 12),
            ThemeBlock(T.TITLE, text = "Favoritos", textSize = 14),
            ThemeBlock(T.FAVORITES_ROW, maxItems = 8, itemHeight = 60, aspect = CardAspect.SQUARE,
                appContent = AppContent.APP_ICON_ROW, labelPos = LabelPos.BELOW, cornerRadius = 12)
        )))

    private val minimal = ThemePreset("Minimalista", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFF000000), padding = 48, spacing = 28,
        blocks = listOf(
            ThemeBlock(T.SPACER, weight = 3),
            ThemeBlock(T.FAVORITES_ROW, maxItems = 7, itemHeight = 96, aspect = CardAspect.SQUARE, cardStyle = CardStyle.NONE,
                labelPos = LabelPos.BELOW, spacing = 28, focusScale = 120, align = BlockAlign.CENTER)
        )))

    private val minimalClock = ThemePreset("Minimal + reloj", ThemeComposition(
        bgStyle = BgStyle.RADIAL, bgColor = c(0xFF000000), bgColor2 = c(0xFF1A1A22), padding = 48, spacing = 24,
        topBar = TopBarConfig(style = TopBarStyle.MINIMAL, showSearch = false, showSettings = false),
        blocks = listOf(
            ThemeBlock(T.SPACER, weight = 4),
            ThemeBlock(T.APPS_ROW, maxItems = 8, itemHeight = 88, aspect = CardAspect.SQUARE, cardStyle = CardStyle.NONE,
                labelPos = LabelPos.BELOW, spacing = 24, align = BlockAlign.CENTER, focusStyle = FocusStyle.GLOW)
        )))

    private val cleanGrid = ThemePreset("Cuadrícula limpia", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFF0E0E12), padding = 40, spacing = 20,
        blocks = listOf(
            ThemeBlock(T.TITLE, textSize = 26),
            ThemeBlock(T.APPS_GRID, maxItems = 18, columns = 6, itemHeight = 96, aspect = CardAspect.SQUARE,
                cardStyle = CardStyle.GLASS, cornerRadius = 20, labelPos = LabelPos.BELOW)
        )))

    private val liquidGlass = ThemePreset("macOS Liquid Glass", ThemeComposition(
        bgStyle = BgStyle.LANDSCAPE, bgColor = c(0xFF183753),
        padding = 0, topPadding = 76, spacing = 16,
        rail = RailConfig(position = RailPosition.LEFT, style = RailStyle.ICONS_LABELS,
            items = "Inicio,Apps,Buscar,Ajustes,Personalización",
            width = 170, floating = true, cornerRadius = 16, textSize = 12, glass = true,
            topInset = 91, height = 326),
        widgets = listOf(
            ThemeWidget(ThemeWidgetType.CLOCK, WidgetAnchor.TOP_START, width = 180, height = 80,
                overlay = true, offsetX = 20, offsetY = 14, glass = false),
            ThemeWidget(ThemeWidgetType.WEATHER, WidgetAnchor.TOP_END, width = 140, height = 100,
                overlay = true, offsetX = 34, offsetY = 76)
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, appContent = AppContent.FEATURED_APP, itemHeight = 182,
                cardStyle = CardStyle.GLASS, cornerRadius = 18, endInset = 186),
            ThemeBlock(T.TITLE, text = "APLICACIONES", textSize = 12),
            ThemeBlock(T.APPS_ROW, maxItems = 12, itemHeight = 62, aspect = CardAspect.SQUARE,
                cardStyle = CardStyle.GLASS, cornerRadius = 14, labelPos = LabelPos.BELOW, spacing = 24,
                align = BlockAlign.START, textSize = 10),
            ThemeBlock(T.TITLE, text = "Todas las aplicaciones", textSize = 16),
            ThemeBlock(T.APPS_GRID, maxItems = 24, columns = 7, itemHeight = 62, aspect = CardAspect.SQUARE,
                cardStyle = CardStyle.GLASS, cornerRadius = 14, labelPos = LabelPos.BELOW)
        )
    ))

    private val orbita = ThemePreset("Órbita", ThemeComposition(
        bgStyle = BgStyle.RADIAL, bgColor = c(0xFF17212B), bgColor2 = c(0xFF344757),
        padding = 44, spacing = 24,
        widgets = listOf(ThemeWidget(ThemeWidgetType.WEATHER, WidgetAnchor.BOTTOM_END, width = 180, height = 80,
            overlay = true, offsetX = 44, offsetY = 24, glass = false)),
        topBar = TopBarConfig(style = TopBarStyle.TABS_TEXT, logo = "Órbita", tabs = "Inicio,Apps,Buscar",
            showSearch = false, showClock = false, textColor = c(0xFFE8EDF1)),
        blocks = listOf(
            ThemeBlock(T.SPACER, weight = 3),
            ThemeBlock(T.CLOCK, textSize = 72, align = BlockAlign.CENTER, textColor = c(0xFFE8EDF1),
                use24Hour = true),
            ThemeBlock(T.SPACER, weight = 2),
            ThemeBlock(T.APPS_ROW, maxItems = 5, itemHeight = 68, aspect = CardAspect.SQUARE,
                cornerRadius = 60, cardStyle = CardStyle.FILLED,
                textColor = c(0xFFE8EDF1), labelPos = LabelPos.BELOW, spacing = 28,
                align = BlockAlign.CENTER, focusStyle = FocusStyle.BORDER)
        )))

    private val bruma = ThemePreset("Bruma", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFFE9EDF0), padding = 40, topPadding = 64, spacing = 16,
        widgets = listOf(ThemeWidget(ThemeWidgetType.WEATHER, WidgetAnchor.BOTTOM_END, width = 180, height = 80,
            overlay = true, offsetX = 40, offsetY = 56, glass = false)),
        rail = RailConfig(items = "Inicio,Apps,Buscar,Ajustes", logo = "Bruma", width = 156,
            textSize = 14, cornerRadius = 12, textColor = c(0xFF253846), bgColor = c(0xFFE9EDF0)),
        blocks = listOf(
            ThemeBlock(T.TITLE, text = "Todo en su lugar.", textSize = 32, textColor = c(0xFF253846)),
            ThemeBlock(T.TITLE, text = "Tus aplicaciones favoritas, sin distracciones.", textSize = 14, textColor = c(0xFF465C6A)),
            ThemeBlock(T.SPACER, weight = 1),
            ThemeBlock(T.APPS_ROW, maxItems = 5, itemHeight = 72, aspect = CardAspect.SQUARE,
                cornerRadius = 16, cardStyle = CardStyle.LIGHT, textColor = c(0xFF253846),
                labelPos = LabelPos.BELOW, spacing = 20, align = BlockAlign.START, focusStyle = FocusStyle.BORDER),
            ThemeBlock(T.SPACER, weight = 1),
            ThemeBlock(T.CLOCK, textSize = 48, align = BlockAlign.START, textColor = c(0xFF253846),
                use24Hour = true)
        )))

    private val horizonte = ThemePreset("Horizonte", ThemeComposition(
        bgStyle = BgStyle.RIDGES, bgColor = c(0xFF173B48), padding = 44, topPadding = 32, spacing = 20,
        topBar = TopBarConfig(style = TopBarStyle.MINIMAL, logo = "Horizonte", tabs = "",
            showClock = true, showDate = true, use24Hour = true, showSettings = false, showSearch = false),
        rail = RailConfig(position = RailPosition.BOTTOM, items = "Inicio,Apps,Buscar,Ajustes",
            style = RailStyle.LIST, width = 144, textSize = 14, floating = true, glass = true, cornerRadius = 16,
            textColor = c(0xFFE8EDF1), bgColor = c(0xFF102D39), bgAlpha = 85),
        blocks = listOf(
            ThemeBlock(T.TITLE, text = "Un espacio para desconectar.", textSize = 32),
            ThemeBlock(T.TITLE, text = "Elige qué quieres abrir.", textSize = 14),
            ThemeBlock(T.SPACER, weight = 2),
            ThemeBlock(T.FAVORITES_ROW, text = "Favoritos", textSize = 14, maxItems = 5,
                itemHeight = 60, aspect = CardAspect.SQUARE, cornerRadius = 12,
                textColor = c(0xFFE8EDF1), cardStyle = CardStyle.GLASS, labelPos = LabelPos.BELOW,
                align = BlockAlign.START, spacing = 24),
            ThemeBlock(T.APPS_ROW, text = "Todas las aplicaciones", textSize = 14, maxItems = 6,
                itemHeight = 56, aspect = CardAspect.SQUARE, cornerRadius = 12,
                textColor = c(0xFFE8EDF1), cardStyle = CardStyle.GLASS, labelPos = LabelPos.BELOW,
                align = BlockAlign.START, spacing = 24)
        )), backgroundAssetName = "ridges_landscape.png")

    private val estudio = ThemePreset("Estudio", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFF202120), padding = 44, topPadding = 32, spacing = 16,
        rail = RailConfig(position = RailPosition.RIGHT, items = "Inicio,Apps,Buscar,Ajustes", logo = "Estudio",
            width = 156, textSize = 14, bgColor = c(0xFF292C27), textColor = c(0xFFEBECE6)),
        topBar = TopBarConfig(style = TopBarStyle.MINIMAL, logo = "Tu biblioteca", tabs = "",
            showSearch = false, showSettings = false, use24Hour = true, textColor = c(0xFFE5E8DD)),
        blocks = listOf(
            ThemeBlock(T.TITLE, text = "Lo esencial, a mano.", textSize = 32, textColor = c(0xFFE5E8DD)),
            ThemeBlock(T.TITLE, text = "Una colección sencilla de tus aplicaciones.", textSize = 14, textColor = c(0xFFB7BEAA)),
            ThemeBlock(T.APPS_GRID, maxItems = 8, columns = 4, itemHeight = 104,
                aspect = CardAspect.CLASSIC, cornerRadius = 12,
                textColor = c(0xFFE5E8DD), labelPos = LabelPos.INSIDE, spacing = 20, focusStyle = FocusStyle.BORDER)
        )))

    val all: List<ThemePreset> get() =
        listOf(androidTv, googleTv, fireTv, roku, tizen, webOs, tvOs, freeMyTv, minimal, minimalClock, cleanGrid, liquidGlass,
            orbita, bruma, horizonte, estudio)

    fun byName(name: String): ThemeComposition? = all.firstOrNull { it.name == name }?.composition
}

/**
 * Composición declarativa que corresponde a cada layout. Se usa cuando un tema instalado
 * no trae `composition` (paquetes v1) y para el tema inicial de una instalación nueva.
 */
fun defaultCompositionFor(themeId: ThemeId): ThemeComposition =
    ThemePresets.byName(
        when (themeId) {
            ThemeId.WEBOS -> "webOS"
            ThemeId.FIRE_TV -> "Fire TV"
            ThemeId.ROKU -> "Roku"
            ThemeId.TIZEN -> "Tizen"
            ThemeId.ANDROID_TV -> "Android TV"
            ThemeId.GOOGLE_TV -> "Google TV"
            ThemeId.APPLE_TV -> "Apple tvOS"
            ThemeId.FREE_MY_TV -> "Free My TV"
        }
    ) ?: ThemePresets.all.first().composition
