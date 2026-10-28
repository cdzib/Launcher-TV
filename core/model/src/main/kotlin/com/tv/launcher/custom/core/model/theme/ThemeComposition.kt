package com.tv.launcher.custom.core.model.theme

enum class ThemeBlockType { TITLE, APPS_ROW, APPS_GRID, FAVORITES_ROW, SPACER, HERO, CHIPS_ROW, RECENTS_ROW, MEDIA_ROW, NAV_ROW }
enum class BlockPlacement { CONTENT, BOTTOM }
enum class BlockAlign { START, CENTER, END }
enum class CardAspect(val ratio: Float) { WIDE(16f / 9f), SQUARE(1f), PORTRAIT(2f / 3f), CLASSIC(4f / 3f), ULTRAWIDE(21f / 9f) }
enum class CardStyle { FILLED, LIGHT, GLASS, OUTLINE, NONE }
enum class LabelPos { NONE, BELOW, INSIDE }
enum class FocusStyle { SCALE, BORDER, SCALE_BORDER, GLOW }
enum class BgStyle { SOLID, VERTICAL, DIAGONAL, RADIAL, LANDSCAPE }
enum class TopBarStyle { TABS_PILL, TABS_TEXT, ICONS, MINIMAL }
enum class RailStyle { LIST, ICONS, ICONS_LABELS }
enum class RailPosition { LEFT, RIGHT, BOTTOM }
enum class TopBarPosition { TOP, BOTTOM }
enum class ThemeWidgetType { CLOCK, WEATHER }
enum class WidgetAnchor { TOP_START, TOP_END, BOTTOM_START, BOTTOM_END }

data class ThemeWidget(
    val type: ThemeWidgetType,
    val anchor: WidgetAnchor = WidgetAnchor.TOP_END,
    val width: Int = 320,
    val height: Int = 200,
    val showDate: Boolean = true,
    val use24Hour: Boolean = true,
    val overlay: Boolean = false,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val glass: Boolean = true
)
enum class AppContent { FEATURED_APP, SHORTCUTS, APP_ICONS, SHORTCUT_EDITOR, APP_CARDS, APP_ICON_ROW }

data class ThemeBlock(
    val type: ThemeBlockType,
    val placement: BlockPlacement = BlockPlacement.CONTENT,
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
    val multicolor: Boolean? = null,
    val imageAsset: String? = null,
    val contentItems: List<ThemeContentItem> = emptyList(),
    val appContent: AppContent? = null,
    val startInset: Int? = null,
    val endInset: Int? = null,
    val itemOffset: Int? = null,
    val showSystemSettings: Boolean? = null,
    val showPersonalization: Boolean? = null
) {
    fun usesCardLayout() = type == ThemeBlockType.APPS_ROW || type == ThemeBlockType.APPS_GRID ||
        type == ThemeBlockType.FAVORITES_ROW || type == ThemeBlockType.RECENTS_ROW || type == ThemeBlockType.MEDIA_ROW
    fun supportsSectionHeader() = usesCardLayout()
    fun rTextSize() = textSize ?: when (type) {
        ThemeBlockType.TITLE -> 28; ThemeBlockType.HERO -> 34; ThemeBlockType.CHIPS_ROW -> 16; else -> 18
    }
    fun rAlign() = align ?: if (type == ThemeBlockType.TITLE) BlockAlign.START else BlockAlign.CENTER
    fun rPadding() = padding ?: 0
    fun rSpacing() = spacing ?: if (type == ThemeBlockType.CHIPS_ROW) 12 else 14
    fun rItemH() = itemHeight ?: when (type) {
        ThemeBlockType.HERO -> 190; ThemeBlockType.CHIPS_ROW -> 44
        ThemeBlockType.FAVORITES_ROW -> 64; ThemeBlockType.RECENTS_ROW -> 108; ThemeBlockType.MEDIA_ROW -> 140; else -> 84
    }
    fun rAspect() = aspect ?: when (type) {
        ThemeBlockType.APPS_ROW, ThemeBlockType.APPS_GRID -> CardAspect.CLASSIC
        ThemeBlockType.FAVORITES_ROW -> CardAspect.SQUARE
        ThemeBlockType.MEDIA_ROW, ThemeBlockType.RECENTS_ROW -> CardAspect.WIDE
        else -> CardAspect.WIDE
    }
    fun rCardStyle() = cardStyle ?: CardStyle.FILLED
    fun rLabelPos() = labelPos ?: if (type == ThemeBlockType.FAVORITES_ROW) LabelPos.NONE else LabelPos.BELOW
    fun rFocusStyle() = focusStyle ?: FocusStyle.SCALE
    fun rFocusScale() = (focusScale ?: 112) / 100f
    fun rCorner() = cornerRadius ?: 12
}

data class ThemeContentItem(
    val label: String,
    val imageAsset: String,
    val launchPackage: String? = null
)

data class TopBarConfig(
    val position: TopBarPosition = TopBarPosition.TOP,
    val glass: Boolean = false,
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
    val topInset: Int = 0,
    val height: Int? = null,
    val position: RailPosition = RailPosition.LEFT,
    val glass: Boolean = false,
    val style: RailStyle = RailStyle.LIST,
    val items: String? = "Inicio,Buscar,Apps,Ajustes",
    val width: Int? = null,
    val floating: Boolean = false,
    val cornerRadius: Int? = null,
    val textSize: Int? = null,
    val textColor: Int? = null,
    val bgColor: Int? = null,
    val bgAlpha: Int? = null,
    val showProfile: Boolean = false,
    val overlay: Boolean = false,
    val logo: String? = null,
    val bottomItems: String? = null,
    val circular: Boolean? = null,
    val appShortcutCount: Int? = null
)

data class ThemeComposition(
    val blocks: List<ThemeBlock>,
    val spacing: Int? = null,
    val padding: Int? = null,
    val bgColor: Int? = null,
    val bgColor2: Int? = null,
    val bgStyle: BgStyle? = null,
    val topBar: TopBarConfig? = null,
    val rail: RailConfig? = null,
    val topPadding: Int? = null,
    val widgets: List<ThemeWidget> = emptyList()
)

fun ThemeComposition.requiresFormatV3(): Boolean =
    widgets.isNotEmpty() || bgStyle == BgStyle.LANDSCAPE || rail?.glass == true || topBar?.glass == true ||
        rail?.position?.let { it != RailPosition.LEFT } == true ||
        topBar?.position?.let { it != TopBarPosition.TOP } == true ||
        rail?.topInset?.let { it != 0 } == true || rail?.height != null
