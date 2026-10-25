package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.add
import kotlin.math.abs

private const val MAX_MEDIA_ITEMS = 20
private val ASSET_PATH_PATTERN = Regex("assets/[A-Za-z0-9_-]+\\.(png|jpg|jpeg|webp)")
private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")

object ThemeCompositionValidator {
    private val json = Json { isLenient = false; ignoreUnknownKeys = false }

    private const val MAX_BLOCKS = 12
    private const val MAX_PER_TYPE = 3

    private val TYPE_NAMES = mapOf(
        ThemeBlockType.TITLE to "title",
        ThemeBlockType.APPS_ROW to "apps_row",
        ThemeBlockType.APPS_GRID to "apps_grid",
        ThemeBlockType.FAVORITES_ROW to "favorites_row",
        ThemeBlockType.SPACER to "spacer",
        ThemeBlockType.HERO to "hero",
        ThemeBlockType.CHIPS_ROW to "chips_row",
        ThemeBlockType.RECENTS_ROW to "recents_row",
        ThemeBlockType.MEDIA_ROW to "media_row"
    )
    private val COMPOSITION_KEYS = setOf("blocks", "spacing", "padding", "bgColor", "bgColor2", "bgStyle", "topBar", "rail")
    private val COMMON_KEYS = setOf("align", "padding", "bgColor", "bgAlpha", "textColor", "placement")

    fun parse(raw: String): ThemeComposition {
        val document = try {
            json.parseToJsonElement(raw)
        } catch (_: Exception) {
            throw ThemeArchiveException("Theme composition must be valid JSON")
        }
        val obj = document as? JsonObject
        val values = when (document) {
            is JsonArray -> document
            is JsonObject -> {
                if ("blocks" !in document || (document.keys - COMPOSITION_KEYS).isNotEmpty()) {
                    throw ThemeArchiveException("Invalid composition fields")
                }
                document["blocks"] as? JsonArray ?: throw ThemeArchiveException("Composition blocks must be an array")
            }
            else -> throw ThemeArchiveException("Theme composition must be an array or object")
        }
        if (values.isEmpty() || values.size > MAX_BLOCKS) {
            throw ThemeArchiveException("Composition must contain 1 to 12 blocks")
        }
        val blocks = values.map(::parseBlock)
        if (blocks.groupingBy { it.type }.eachCount().any { it.value > MAX_PER_TYPE }) {
            throw ThemeArchiveException("Composition contains too many blocks of the same type")
        }
        return ThemeComposition(
            blocks = blocks,
            spacing = obj?.optionalInt("spacing", 0..64),
            padding = obj?.optionalInt("padding", 0..96),
            bgColor = obj?.optionalColor("bgColor"),
            bgColor2 = obj?.optionalColor("bgColor2"),
            bgStyle = obj?.optionalEnum<BgStyle>("bgStyle"),
            topBar = obj?.get("topBar")?.let(::parseTopBar),
            rail = obj?.get("rail")?.let(::parseRail)
        )
    }

    // ───────────── Bloques ─────────────

    private fun parseBlock(element: JsonElement): ThemeBlock {
        val item = element as? JsonObject ?: throw ThemeArchiveException("Composition block must be an object")
        val typeName = (item["type"] as? JsonPrimitive)?.contentOrNull
        val type = TYPE_NAMES.entries.firstOrNull { it.value == typeName }?.key
            ?: throw ThemeArchiveException("Unknown composition block")

        val required = when (type) {
            ThemeBlockType.TITLE, ThemeBlockType.HERO, ThemeBlockType.CHIPS_ROW -> setOf("type")
            ThemeBlockType.APPS_ROW, ThemeBlockType.FAVORITES_ROW, ThemeBlockType.RECENTS_ROW -> setOf("type", "maxItems")
            ThemeBlockType.APPS_GRID -> setOf("type", "columns", "maxItems")
            ThemeBlockType.SPACER -> setOf("type", "weight")
            ThemeBlockType.MEDIA_ROW -> setOf("type", "items")
        }
        val optional = when (type) {
            ThemeBlockType.SPACER -> setOf("placement")
            ThemeBlockType.TITLE -> COMMON_KEYS + setOf("text", "textSize", "align")
            ThemeBlockType.HERO -> COMMON_KEYS + setOf(
                "text", "subtitle", "buttonText", "textSize", "itemHeight", "cornerRadius", "imageAsset"
            )
            ThemeBlockType.CHIPS_ROW -> COMMON_KEYS + setOf(
                "text", "textSize", "spacing", "itemHeight", "cornerRadius", "multicolor", "focusStyle", "focusScale"
            )
            ThemeBlockType.MEDIA_ROW -> COMMON_KEYS + setOf(
                "text", "textSize", "spacing", "itemHeight", "aspect", "cardStyle", "labelPos",
                "focusStyle", "focusScale", "cornerRadius", "maxItems"
            )
            // Filas, favoritos, recientes y cuadrícula. itemWidth/showLabels = formato legado (se migran).
            else -> COMMON_KEYS + setOf(
                "text", "textSize", "spacing", "itemHeight", "aspect", "cardStyle", "labelPos",
                "focusStyle", "focusScale", "cornerRadius", "itemWidth", "showLabels"
            )
        }
        item.checkKeys(required, optional, "Invalid composition block fields")

        val itemHeight = item.optionalInt("itemHeight", 28..400)
        val legacyAspect = item.optionalInt("itemWidth", 40..240)?.let { w ->
            itemHeight?.let { h -> nearestAspect(w.toFloat() / h) }
        }
        val legacyLabelPos = item.optionalBoolean("showLabels")?.let { if (it) LabelPos.BELOW else LabelPos.NONE }

        return ThemeBlock(
            type = type,
            placement = item.optionalEnum<BlockPlacement>("placement") ?: BlockPlacement.CONTENT,
            maxItems = item.optionalInt("maxItems", if (type == ThemeBlockType.APPS_GRID) 1..48 else 1..20),
            columns = item.optionalInt("columns", 2..8),
            weight = item.optionalInt("weight", 1..12),
            text = item.optionalString("text", if (type == ThemeBlockType.CHIPS_ROW) 100 else 60),
            subtitle = item.optionalString("subtitle", 90),
            buttonText = item.optionalString("buttonText", 30),
            textSize = item.optionalInt("textSize", 10..72),
            align = item.optionalEnum<BlockAlign>("align"),
            padding = item.optionalInt("padding", 0..64),
            spacing = item.optionalInt("spacing", 0..48),
            itemHeight = itemHeight,
            aspect = item.optionalEnum<CardAspect>("aspect") ?: legacyAspect,
            cardStyle = item.optionalEnum<CardStyle>("cardStyle"),
            labelPos = item.optionalEnum<LabelPos>("labelPos") ?: legacyLabelPos,
            focusStyle = item.optionalEnum<FocusStyle>("focusStyle"),
            focusScale = item.optionalInt("focusScale", 100..150),
            cornerRadius = item.optionalInt("cornerRadius", 0..60),
            bgColor = item.optionalColor("bgColor"),
            bgAlpha = item.optionalInt("bgAlpha", 0..100),
            textColor = item.optionalColor("textColor"),
            multicolor = item.optionalBoolean("multicolor"),
            imageAsset = item.optionalAssetPath("imageAsset"),
            contentItems = if (type == ThemeBlockType.MEDIA_ROW) item.parseContentItems() else emptyList()
        )
    }

    private fun nearestAspect(ratio: Float): CardAspect = CardAspect.entries.minBy { abs(it.ratio - ratio) }

    // ───────────── Barra superior y menú lateral ─────────────

    private fun parseTopBar(element: JsonElement): TopBarConfig {
        val o = element as? JsonObject ?: throw ThemeArchiveException("Top bar must be an object")
        o.checkKeys(
            emptySet(),
            setOf("style", "tabs", "logo", "showSearch", "showSettings", "showClock", "align", "textColor", "bgColor", "bgAlpha"),
            "Invalid top bar fields"
        )
        val d = TopBarConfig()
        return TopBarConfig(
            style = o.optionalEnum<TopBarStyle>("style") ?: d.style,
            tabs = if ("tabs" in o) o.optionalString("tabs", 80) else d.tabs,
            logo = o.optionalString("logo", 20),
            showSearch = o.optionalBoolean("showSearch") ?: d.showSearch,
            showSettings = o.optionalBoolean("showSettings") ?: d.showSettings,
            showClock = o.optionalBoolean("showClock") ?: d.showClock,
            align = o.optionalEnum<BlockAlign>("align") ?: d.align,
            textColor = o.optionalColor("textColor"),
            bgColor = o.optionalColor("bgColor"),
            bgAlpha = o.optionalInt("bgAlpha", 0..100)
        )
    }

    private fun parseRail(element: JsonElement): RailConfig {
        val o = element as? JsonObject ?: throw ThemeArchiveException("Rail must be an object")
        o.checkKeys(
            emptySet(),
            setOf("style", "items", "width", "floating", "cornerRadius", "textSize", "textColor", "bgColor", "bgAlpha", "showProfile"),
            "Invalid rail fields"
        )
        val d = RailConfig()
        return RailConfig(
            style = o.optionalEnum<RailStyle>("style") ?: d.style,
            items = if ("items" in o) o.optionalString("items", 120) else d.items,
            width = o.optionalInt("width", 56..360),
            floating = o.optionalBoolean("floating") ?: d.floating,
            cornerRadius = o.optionalInt("cornerRadius", 0..60),
            textSize = o.optionalInt("textSize", 12..28),
            textColor = o.optionalColor("textColor"),
            bgColor = o.optionalColor("bgColor"),
            bgAlpha = o.optionalInt("bgAlpha", 0..100),
            showProfile = o.optionalBoolean("showProfile") ?: d.showProfile
        )
    }

    // ───────────── Exportación ─────────────

    fun encode(composition: ThemeComposition): JsonObject = buildJsonObject {
        put("blocks", JsonArray(composition.blocks.map(::encodeBlock)))
        composition.spacing?.let { put("spacing", it) }
        composition.padding?.let { put("padding", it) }
        composition.bgColor?.let { put("bgColor", hex(it)) }
        composition.bgColor2?.let { put("bgColor2", hex(it)) }
        composition.bgStyle?.let { put("bgStyle", it.name) }
        composition.topBar?.let { put("topBar", encodeTopBar(it)) }
        composition.rail?.let { put("rail", encodeRail(it)) }
    }

    private fun encodeBlock(block: ThemeBlock): JsonObject = buildJsonObject {
        put("type", TYPE_NAMES.getValue(block.type))
        if (block.placement != BlockPlacement.CONTENT) put("placement", block.placement.name)
        block.maxItems?.let { put("maxItems", it) }
        block.columns?.let { put("columns", it) }
        block.weight?.let { put("weight", it) }
        block.text?.let { put("text", it) }
        block.subtitle?.let { put("subtitle", it) }
        block.buttonText?.let { put("buttonText", it) }
        block.textSize?.let { put("textSize", it) }
        block.align?.let { put("align", it.name) }
        block.padding?.let { put("padding", it) }
        block.spacing?.let { put("spacing", it) }
        block.itemHeight?.let { put("itemHeight", it) }
        block.aspect?.let { put("aspect", it.name) }
        block.cardStyle?.let { put("cardStyle", it.name) }
        block.labelPos?.let { put("labelPos", it.name) }
        block.focusStyle?.let { put("focusStyle", it.name) }
        block.focusScale?.let { put("focusScale", it) }
        block.cornerRadius?.let { put("cornerRadius", it) }
        block.bgColor?.let { put("bgColor", hex(it)) }
        block.bgAlpha?.let { put("bgAlpha", it) }
        block.textColor?.let { put("textColor", hex(it)) }
        block.multicolor?.let { put("multicolor", it) }
        block.imageAsset?.let { put("imageAsset", it) }
        if (block.type == ThemeBlockType.MEDIA_ROW) {
            put("items", buildJsonArray {
                block.contentItems.forEach { item ->
                    add(buildJsonObject {
                        put("label", item.label)
                        put("imageAsset", item.imageAsset)
                        item.launchPackage?.let { put("launchPackage", it) }
                    })
                }
            })
        }
    }

    private fun encodeTopBar(c: TopBarConfig): JsonObject = buildJsonObject {
        put("style", c.style.name)
        put("tabs", c.tabs.orEmpty())
        c.logo?.let { put("logo", it) }
        put("showSearch", c.showSearch)
        put("showSettings", c.showSettings)
        put("showClock", c.showClock)
        put("align", c.align.name)
        c.textColor?.let { put("textColor", hex(it)) }
        c.bgColor?.let { put("bgColor", hex(it)) }
        c.bgAlpha?.let { put("bgAlpha", it) }
    }

    private fun encodeRail(c: RailConfig): JsonObject = buildJsonObject {
        put("style", c.style.name)
        put("items", c.items.orEmpty())
        c.width?.let { put("width", it) }
        put("floating", c.floating)
        c.cornerRadius?.let { put("cornerRadius", it) }
        c.textSize?.let { put("textSize", it) }
        c.textColor?.let { put("textColor", hex(it)) }
        c.bgColor?.let { put("bgColor", hex(it)) }
        c.bgAlpha?.let { put("bgAlpha", it) }
        put("showProfile", c.showProfile)
    }

    private fun hex(color: Int) = "#%08X".format(color)

    // ───────────── Helpers ─────────────

    private fun JsonObject.checkKeys(required: Set<String>, optional: Set<String>, message: String) {
        if (!keys.containsAll(required) || (keys - required - optional).isNotEmpty()) throw ThemeArchiveException(message)
    }

    private fun JsonObject.primitive(key: String): JsonPrimitive? {
        if (key !in this) return null
        return this[key] as? JsonPrimitive ?: throw ThemeArchiveException("Composition field '$key' is invalid")
    }

    private fun JsonObject.optionalInt(key: String, range: IntRange): Int? {
        val p = primitive(key) ?: return null
        val value = (if (p.isString) null else p.intOrNull)
            ?: throw ThemeArchiveException("Composition field '$key' must be an integer")
        if (value !in range) throw ThemeArchiveException("Composition field '$key' is out of range")
        return value
    }

    private fun JsonObject.optionalBoolean(key: String): Boolean? {
        val p = primitive(key) ?: return null
        return (if (p.isString) null else p.booleanOrNull)
            ?: throw ThemeArchiveException("Composition field '$key' must be a boolean")
    }

    private fun JsonObject.optionalString(key: String, maxLength: Int): String? {
        val p = primitive(key) ?: return null
        val value = (if (p.isString) p.contentOrNull else null)
            ?: throw ThemeArchiveException("Composition field '$key' must be text")
        if (value.length > maxLength || value.any(Char::isISOControl)) {
            throw ThemeArchiveException("Composition field '$key' is out of range")
        }
        return value
    }

    private inline fun <reified E : Enum<E>> JsonObject.optionalEnum(key: String): E? {
        val raw = optionalString(key, 20) ?: return null
        return enumValues<E>().firstOrNull { it.name == raw } ?: throw ThemeArchiveException("Invalid $key")
    }

    private fun JsonObject.optionalColor(key: String): Int? {
        val raw = optionalString(key, 9) ?: return null
        if (!raw.matches(Regex("#[A-Fa-f0-9]{8}"))) throw ThemeArchiveException("Invalid $key color")
        return raw.substring(1).toLong(16).toInt()
    }

    private fun JsonObject.optionalAssetPath(key: String): String? = optionalString(key, 80)?.also { path ->
        if (!path.matches(ASSET_PATH_PATTERN)) throw ThemeArchiveException("Invalid theme asset path")
    }

    private fun JsonObject.parseContentItems(): List<ThemeContentItem> {
        val values = this["items"] as? JsonArray
            ?: throw ThemeArchiveException("Media row items must be an array")
        if (values.size > MAX_MEDIA_ITEMS) {
            throw ThemeArchiveException("Media row cannot contain more than $MAX_MEDIA_ITEMS items")
        }
        return values.map { element ->
            val item = element as? JsonObject ?: throw ThemeArchiveException("Media item must be an object")
            item.checkKeys(setOf("label", "imageAsset"), setOf("launchPackage"), "Invalid media item fields")
            val launchPackage = item.optionalString("launchPackage", 200)
            if (launchPackage != null && !launchPackage.matches(PACKAGE_NAME_PATTERN)) {
                throw ThemeArchiveException("Invalid media item launch package")
            }
            ThemeContentItem(
                label = item.requiredString("label", 60),
                imageAsset = item.requiredAssetPath("imageAsset"),
                launchPackage = launchPackage
            )
        }
    }

    private fun JsonObject.requiredString(key: String, maxLength: Int): String =
        optionalString(key, maxLength)?.takeIf { it.isNotBlank() }
            ?: throw ThemeArchiveException("Media item field '$key' is required")

    private fun JsonObject.requiredAssetPath(key: String): String =
        optionalAssetPath(key) ?: throw ThemeArchiveException("Media item asset is required")

}
