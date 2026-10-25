package com.tv.launcher.custom.core.model.theme

enum class ThemeBlockType { TITLE, APPS_ROW, APPS_GRID, FAVORITES_ROW, SPACER, HERO, CHIPS_ROW, RECENTS_ROW }
enum class BlockAlign { START, CENTER, END }
enum class CardAspect(val ratio: Float) { WIDE(16f / 9f), SQUARE(1f), PORTRAIT(2f / 3f), CLASSIC(4f / 3f), ULTRAWIDE(21f / 9f) }
enum class CardStyle { FILLED, LIGHT, GLASS, OUTLINE, NONE }
enum class LabelPos { NONE, BELOW, INSIDE }
enum class FocusStyle { SCALE, BORDER, SCALE_BORDER, GLOW }
enum class BgStyle { SOLID, VERTICAL, DIAGONAL, RADIAL }
enum class TopBarStyle { TABS_PILL, TABS_TEXT, ICONS, MINIMAL }
enum class RailStyle { LIST, ICONS, ICONS_LABELS }

data class ThemeBlock(
    val type: ThemeBlockType,
    val maxItems: Int? = null,
    val columns: Int? = null,
    val weight: Int? = null,
    val text: String? = null,
    val subtitle: String? = null,
    val buttonText: String? = null,
    val textSize: Int? = null,
    val align: BlockAlign? = null,
    val padding: Int? = null,
    val spacing: Int? = null,
    val itemHeight: Int? = null,
    val aspect: CardAspect? = null,
    val cardStyle: CardStyle? = null,
    val labelPos: LabelPos? = null,
    val focusStyle: FocusStyle? = null,
    val focusScale: Int? = null,
    val cornerRadius: Int? = null,
    val bgColor: Int? = null,
    val bgAlpha: Int? = null,
    val textColor: Int? = null,
    val multicolor: Boolean? = null
) {
    fun usesCardLayout() = type == ThemeBlockType.APPS_ROW || type == ThemeBlockType.APPS_GRID ||
        type == ThemeBlockType.FAVORITES_ROW || type == ThemeBlockType.RECENTS_ROW
    fun supportsSectionHeader() = usesCardLayout()
    fun rTextSize() = textSize ?: when (type) {
        ThemeBlockType.TITLE -> 28; ThemeBlockType.HERO -> 34; ThemeBlockType.CHIPS_ROW -> 16; else -> 18
    }
    fun rAlign() = align ?: if (type == ThemeBlockType.TITLE) BlockAlign.START else BlockAlign.CENTER
    fun rPadding() = padding ?: 0
    fun rSpacing() = spacing ?: if (type == ThemeBlockType.CHIPS_ROW) 12 else 14
    fun rItemH() = itemHeight ?: when (type) {
        ThemeBlockType.HERO -> 190; ThemeBlockType.CHIPS_ROW -> 44
        ThemeBlockType.FAVORITES_ROW -> 64; ThemeBlockType.RECENTS_ROW -> 108; else -> 84
    }
    fun rAspect() = aspect ?: when (type) {
        ThemeBlockType.APPS_ROW, ThemeBlockType.APPS_GRID -> CardAspect.CLASSIC
        ThemeBlockType.FAVORITES_ROW -> CardAspect.SQUARE
        else -> CardAspect.WIDE
    }
    fun rCardStyle() = cardStyle ?: CardStyle.FILLED
    fun rLabelPos() = labelPos ?: if (type == ThemeBlockType.FAVORITES_ROW) LabelPos.NONE else LabelPos.BELOW
    fun rFocusStyle() = focusStyle ?: FocusStyle.SCALE
    fun rFocusScale() = (focusScale ?: 112) / 100f
    fun rCorner() = cornerRadius ?: 12
}

data class TopBarConfig(
    val style: TopBarStyle = TopBarStyle.TABS_PILL,
    val tabs: String? = "Inicio,Apps",
    val logo: String? = null,
    val showSearch: Boolean = true,
    val showSettings: Boolean = true,
    val showClock: Boolean = true,
    val align: BlockAlign = BlockAlign.START,
    val textColor: Int? = null,
    val bgColor: Int? = null,
    val bgAlpha: Int? = null
)

data class RailConfig(
    val style: RailStyle = RailStyle.LIST,
    val items: String? = "Inicio,Buscar,Apps,Ajustes",
    val width: Int? = null,
    val floating: Boolean = false,
    val cornerRadius: Int? = null,
    val textSize: Int? = null,
    val textColor: Int? = null,
    val bgColor: Int? = null,
    val bgAlpha: Int? = null,
    val showProfile: Boolean = false
)

data class ThemeComposition(
    val blocks: List<ThemeBlock>,
    val spacing: Int? = null,
    val padding: Int? = null,
    val bgColor: Int? = null,
    val bgColor2: Int? = null,
    val bgStyle: BgStyle? = null,
    val topBar: TopBarConfig? = null,
    val rail: RailConfig? = null
)
