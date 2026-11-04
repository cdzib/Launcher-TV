package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ThemeCompositionValidatorTest {
    @Test fun `clock blocks round trip with bounded size and declarative ridges`() {
        val raw = """{"bgStyle":"RIDGES","topBar":{"showDate":true,"use24Hour":true},"blocks":[{"type":"clock","textSize":72,"align":"CENTER","textColor":"#FFE8EDF1","use24Hour":true}]}"""
        val composition = ThemeCompositionValidator.parse(raw)
        assertEquals(true, composition.topBar?.use24Hour)
        assertEquals(true, composition.blocks.single().use24Hour)
        assertEquals(true, composition.requiresFormatV3())
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
        listOf(raw.replace("72", "0"), raw.replace("72", "999"), raw.replace("CENTER", "OUTSIDE"),
            raw.replace("true", "\"true\"")).forEach {
            assertThrows(ThemeArchiveException::class.java) { ThemeCompositionValidator.parse(it) }
        }
    }

    @Test
    fun `round trips internal widgets and rejects unsafe sizing or duplicate anchors`() {
        val raw = """{"blocks":[{"type":"title"}],"widgets":[{"type":"CLOCK","anchor":"TOP_END","width":240,"height":120,"showDate":true,"use24Hour":false}]}"""
        val composition = ThemeCompositionValidator.parse(raw)
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
        listOf(raw.replace("240", "900"), raw.replace("CLOCK", "EXTERNAL"),
            raw.replace("}]}", "},{\"type\":\"WEATHER\",\"anchor\":\"TOP_END\"}]}")).forEach {
            assertThrows(ThemeArchiveException::class.java) { ThemeCompositionValidator.parse(it) }
        }
    }

    @Test
    fun `bounds widget count types fields and primitive values`() {
        val invalidWidgets = listOf(
            "{}", "[null]", "[{\"type\":\"WEATHER\",\"url\":\"https://example.com\"}]",
            "[{\"type\":\"CLOCK\",\"height\":0}]", "[{\"type\":\"CLOCK\",\"showDate\":\"true\"}]",
            "[{\"type\":\"CLOCK\",\"anchor\":\"CENTER\"}]",
            (1..5).joinToString(prefix = "[", postfix = "]") { "{\"type\":\"CLOCK\"}" }
        )
        invalidWidgets.forEach { widgets ->
            assertThrows(ThemeArchiveException::class.java) {
                ThemeCompositionValidator.parse("{\"blocks\":[{\"type\":\"title\"}],\"widgets\":$widgets}")
            }
        }
    }

    @Test
    fun `preserves dock and bottom bar positions`() {
        val composition = ThemeCompositionValidator.parse(
            """{"blocks":[{"type":"title"}],"rail":{"position":"BOTTOM"},"topBar":{"position":"BOTTOM"}}"""
        )
        assertEquals("BOTTOM", composition.rail!!.position.name)
        assertEquals("BOTTOM", composition.topBar!!.position.name)
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
    }

    @Test
    fun `rejects unsupported navigation positions`() {
        listOf("\"rail\":{\"position\":\"TOP\"}", "\"topBar\":{\"position\":\"LEFT\"}").forEach { field ->
            assertThrows(ThemeArchiveException::class.java) {
                ThemeCompositionValidator.parse("{\"blocks\":[{\"type\":\"title\"}],$field}")
            }
        }
    }

    @Test
    fun `acepta composicion declarativa permitida`() {
        val composition = ThemeCompositionValidator.parse(
            """[{"type":"title"},{"type":"apps_row","maxItems":8},{"type":"spacer","weight":2}]"""
        )

        assertEquals(3, composition.blocks.size)
        assertEquals(ThemeBlockType.APPS_ROW, composition.blocks[1].type)
    }

    @Test
    fun `rechaza tipos propiedades y limites desconocidos`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""[{"type":"custom","code":"run()"}]""")
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""[{"type":"apps_row","maxItems":99}]""")
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""[{"type":"title","url":"https://evil"}]""")
        }
    }

    @Test
    fun `preserva opciones visuales en json round trip`() {
        val raw = """{"spacing":16,"padding":32,"bgColor":"#FF010203","blocks":[{"type":"title","text":"Inicio","textSize":36,"align":"CENTER","padding":8,"textColor":"#FFFFFFFF"},{"type":"apps_grid","maxItems":16,"columns":4,"itemWidth":120,"itemHeight":80,"showLabels":false,"bgColor":"#FF112233","bgAlpha":60}]}"""
        val composition = ThemeCompositionValidator.parse(raw)

        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
    }

    @Test
    fun `acepta alineacion de titulo al serializar`() {
        val composition = ThemeCompositionValidator.parse(
            """[{"type":"title","align":"END","text":"Inicio"}]"""
        )

        assertEquals(
            composition,
            ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString())
        )
    }

    @Test
    fun `serializa posicion de bloque para generar filas ancladas abajo`() {
        val composition = ThemeCompositionValidator.parse(
            """{"blocks":[{"type":"hero","text":"Apps"},{"type":"favorites_row","maxItems":8,"placement":"BOTTOM"}]}"""
        )

        assertEquals(BlockPlacement.BOTTOM, composition.blocks[1].placement)
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
    }

    @Test
    fun `acepta tarjetas multimedia declarativas con recursos del tema y accion opcional`() {
        val composition = ThemeCompositionValidator.parse(
            """[{"type":"media_row","text":"Canales","items":[{"label":"Canal local","imageAsset":"assets/channel.png","launchPackage":"com.example.channel"},{"label":"Imagen informativa","imageAsset":"assets/info.webp"}]}]"""
        )

        assertEquals(ThemeBlockType.MEDIA_ROW, composition.blocks.single().type)
        assertEquals(2, composition.blocks.single().contentItems.size)
        assertEquals("com.example.channel", composition.blocks.single().contentItems.first().launchPackage)
        assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
    }

    @Test
    fun `rechaza rutas de imagen inseguras y paquetes de accion mal formados`() {
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse("""[{"type":"hero","imageAsset":"../outside.png"}]""")
        }
        assertThrows(ThemeArchiveException::class.java) {
            ThemeCompositionValidator.parse(
                """[{"type":"media_row","items":[{"label":"Canal","imageAsset":"assets/channel.png","launchPackage":"not a package"}]}]"""
            )
        }
    }
}
