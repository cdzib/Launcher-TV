package com.tv.launcher.custom.core.data.theme

import com.tv.launcher.custom.core.model.theme.*

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ThemeCompositionValidatorTest {
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
}
