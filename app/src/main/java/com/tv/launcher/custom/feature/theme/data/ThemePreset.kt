package com.tv.launcher.custom.feature.theme.data

import com.tv.launcher.custom.feature.theme.data.ThemeBlockType as T

data class ThemePreset(val name: String, val composition: ThemeComposition)

object ThemePresets {
    private fun c(v: Long) = v.toInt()

    private val androidTv = ThemePreset("Android TV", ThemeComposition(
        bgStyle = BgStyle.DIAGONAL, bgColor = c(0xFF0B1426), bgColor2 = c(0xFF1B2A4A), padding = 32, spacing = 14,
        topBar = TopBarConfig(style = TopBarStyle.TABS_TEXT, tabs = "Inicio,Descubrir,Apps"),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Destacado", subtitle = "Contenido recomendado para ti", itemHeight = 170),
            ThemeBlock(T.FAVORITES_ROW, text = "Apps favoritas", maxItems = 7, itemHeight = 64, cardStyle = CardStyle.LIGHT, textSize = 14),
            ThemeBlock(T.RECENTS_ROW, text = "Continuar", maxItems = 5, itemHeight = 100, textSize = 14)
        )))

    private val googleTv = ThemePreset("Google TV", ThemeComposition(
        bgStyle = BgStyle.VERTICAL, bgColor = c(0xFF101820), bgColor2 = c(0xFF1C2A38), padding = 32, spacing = 14,
        topBar = TopBarConfig(style = TopBarStyle.TABS_PILL, tabs = "Para ti,Películas,Series,Apps,Biblioteca", logo = "Google TV"),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Título destacado", subtitle = "Una misión que se desarrolla en tiempo real", itemHeight = 150, cornerRadius = 0),
            ThemeBlock(T.RECENTS_ROW, text = "Recomendado para ti", maxItems = 5, itemHeight = 100, cornerRadius = 10, textSize = 14),
            ThemeBlock(T.FAVORITES_ROW, text = "Tus apps", maxItems = 7, itemHeight = 56, cornerRadius = 10, textSize = 14)
        )))

    private val fireTv = ThemePreset("Fire TV", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFF0F1114), padding = 28, spacing = 12,
        topBar = TopBarConfig(style = TopBarStyle.ICONS, tabs = "Inicio,Buscar,En vivo,Ajustes", showSearch = false, showSettings = false, showClock = false),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Estreno de la semana", subtitle = "Todos los episodios disponibles", buttonText = "Más información", itemHeight = 200, cornerRadius = 6),
            ThemeBlock(T.FAVORITES_ROW, maxItems = 6, itemHeight = 48, cardStyle = CardStyle.LIGHT, cornerRadius = 4),
            ThemeBlock(T.APPS_GRID, maxItems = 10, columns = 5, itemHeight = 84, aspect = CardAspect.WIDE, cornerRadius = 6, labelPos = LabelPos.NONE, spacing = 10)
        )))

    private val roku = ThemePreset("Roku", ThemeComposition(
        bgStyle = BgStyle.DIAGONAL, bgColor = c(0xFF6A1B9A), bgColor2 = c(0xFF2A0B4A), padding = 24, spacing = 10,
        topBar = TopBarConfig(style = TopBarStyle.MINIMAL, logo = "Roku", showSearch = false, showSettings = false),
        rail = RailConfig(style = RailStyle.LIST, items = "Inicio,Destacados,Mi feed,Buscar,Canales,Ajustes", width = 230, textSize = 16),
        blocks = listOf(
            ThemeBlock(T.APPS_GRID, maxItems = 9, columns = 3, itemHeight = 84, aspect = CardAspect.WIDE, cornerRadius = 4, labelPos = LabelPos.NONE, spacing = 8)
        )))

    private val tizen = ThemePreset("Tizen", ThemeComposition(
        bgStyle = BgStyle.VERTICAL, bgColor = c(0xFF0A1A1F), bgColor2 = c(0xFF000000), padding = 28, spacing = 14,
        topBar = TopBarConfig(style = TopBarStyle.TABS_PILL, tabs = "Para ti,En vivo,Apps", align = BlockAlign.CENTER, showSearch = false, showSettings = false, showClock = false),
        rail = RailConfig(style = RailStyle.ICONS, items = "Perfil,Buscar,Inicio,Juegos,Ajustes", width = 72),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Ahora en streaming", subtitle = "Nueva temporada", itemHeight = 150, cornerRadius = 16),
            ThemeBlock(T.RECENTS_ROW, text = "Reciente", maxItems = 4, itemHeight = 76, textSize = 14),
            ThemeBlock(T.FAVORITES_ROW, text = "Tus apps", maxItems = 9, itemHeight = 48, aspect = CardAspect.SQUARE, textSize = 14)
        )))

    private val webOs = ThemePreset("webOS", ThemeComposition(
        bgStyle = BgStyle.SOLID, bgColor = c(0xFF050505), padding = 24, spacing = 12,
        rail = RailConfig(style = RailStyle.ICONS, items = "Perfil,Notificaciones,Ajustes,Buscar", width = 64),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Estreno", subtitle = "Disponible ahora", itemHeight = 170, cornerRadius = 8),
            ThemeBlock(T.CHIPS_ROW, text = "Oficina,Juegos,Música,Hogar,Deportes", multicolor = true, itemHeight = 40, cornerRadius = 6),
            ThemeBlock(T.FAVORITES_ROW, maxItems = 12, itemHeight = 52, aspect = CardAspect.SQUARE, spacing = 10, cornerRadius = 8),
            ThemeBlock(T.RECENTS_ROW, text = "Recomendados", maxItems = 5, itemHeight = 92, cornerRadius = 4, textSize = 14)
        )))

    private val tvOs = ThemePreset("Apple tvOS", ThemeComposition(
        bgStyle = BgStyle.VERTICAL, bgColor = c(0xFF3A1F0B), bgColor2 = c(0xFF0A0A0A), padding = 24, spacing = 16,
        rail = RailConfig(
            style = RailStyle.ICONS_LABELS, items = "Inicio,Buscar,Apple TV+,Deportes,Tienda,Biblioteca",
            width = 210, floating = true, cornerRadius = 28, showProfile = true, bgColor = c(0xFF000000), bgAlpha = 35
        ),
        blocks = listOf(
            ThemeBlock(T.HERO, text = "Show destacado", subtitle = "Disponible en Apple TV+", itemHeight = 210, cornerRadius = 0),
            ThemeBlock(T.RECENTS_ROW, text = "Siguiente", maxItems = 5, itemHeight = 96, cornerRadius = 10, textSize = 14, focusScale = 108)
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

    val all: List<ThemePreset> get() =
        listOf(androidTv, googleTv, fireTv, roku, tizen, webOs, tvOs, minimal, minimalClock, cleanGrid)
}