package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.formatClockText
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.util.Locale

class ClockFormatterTest {
    private val dateTime = LocalDateTime.of(2026, 10, 25, 17, 4)

    @Test
    fun `formats clock with explicit 24 hour preference`() {
        assertEquals("17:04", formatClockText(dateTime, use24Hour = true, showDate = false, locale = Locale.US))
    }

    @Test
    fun `formats clock with explicit 12 hour preference`() {
        assertEquals("5:04 PM", formatClockText(dateTime, use24Hour = false, showDate = false, locale = Locale.US))
    }

    @Test
    fun `appends localized date only when requested`() {
        assertEquals("17:04 · Sunday, 25 October", formatClockText(dateTime, true, true, Locale.US))
    }
}
