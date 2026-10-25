package com.tv.launcher.custom

import com.tv.launcher.custom.feature.editor.move
import com.tv.launcher.custom.feature.editor.canAddThemeBlock
import com.tv.launcher.custom.feature.editor.defaultBlock
import com.tv.launcher.custom.core.model.theme.*
import com.tv.launcher.custom.core.data.theme.ThemeCompositionValidator
import com.tv.launcher.custom.core.theme.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeEditorOperationsTest {
    private val first = ThemeBlock(ThemeBlockType.HERO, text = "Portada", itemHeight = 170)
    private val second = ThemeBlock(ThemeBlockType.APPS_ROW, maxItems = 8, cornerRadius = 16)
    private val third = ThemeBlock(ThemeBlockType.TITLE, text = "Biblioteca")

    @Test
    fun `moving a section preserves its settings and the original draft`() {
        val original = ThemeComposition(listOf(first, second, third), padding = 24, spacing = 16)
        val moved = original.move(0, 2)
        assertEquals(listOf(second, third, first), moved.blocks)
        assertEquals(listOf(first, second, third), original.blocks)
        assertEquals(original.copy(blocks = moved.blocks), moved)
    }

    @Test
    fun `moving beyond either edge or after the list changes leaves draft intact`() {
        val original = ThemeComposition(listOf(first, second))
        assertEquals(original, original.move(0, -1))
        assertEquals(original, original.move(1, 2))
        assertEquals(original, original.move(5, 0))
        val empty = ThemeComposition(emptyList())
        assertEquals(empty, empty.move(0, 0))
    }

    @Test
    fun `adding and duplicating respect total limit and limit per type`() {
        assertTrue(canAddThemeBlock(ThemeComposition(listOf(first)), ThemeBlockType.HERO))
        assertFalse(canAddThemeBlock(ThemeComposition(List(3) { first }), ThemeBlockType.HERO))
        val full = ThemeComposition(listOf(first, second, third, ThemeBlock(ThemeBlockType.SPACER)).flatMap { block -> List(3) { block } })
        assertFalse(canAddThemeBlock(full, ThemeBlockType.APPS_GRID))
    }

    @Test
    fun `every default section and existing template remains serializable`() {
        val compositions = ThemeBlockType.entries.map { ThemeComposition(listOf(it.defaultBlock())) } + ThemePresets.all.map { it.composition }
        compositions.forEach { composition ->
            assertEquals(composition, ThemeCompositionValidator.parse(ThemeCompositionValidator.encode(composition).toString()))
        }
    }
}
