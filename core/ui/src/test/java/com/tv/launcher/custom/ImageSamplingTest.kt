package com.tv.launcher.custom

import com.tv.launcher.custom.core.ui.components.ImageSampling
import org.junit.Assert.assertEquals
import org.junit.Test

class ImageSamplingTest {
    @Test
    fun `chooses power of two sample sufficient for longest image edge`() {
        assertEquals(4, ImageSampling.calculateInSampleSize(4000, 3000, 1920))
        assertEquals(1, ImageSampling.calculateInSampleSize(1920, 1080, 1920))
        assertEquals(1, ImageSampling.calculateInSampleSize(0, 3000, 1920))
    }
}
