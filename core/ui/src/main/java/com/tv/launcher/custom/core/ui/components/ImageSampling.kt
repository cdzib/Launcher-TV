package com.tv.launcher.custom.core.ui.components

/** Pure sizing policy shared by image decoders and JVM tests. */
object ImageSampling {
    fun calculateInSampleSize(width: Int, height: Int, maxSide: Int): Int {
        require(maxSide > 0) { "Maximum image side must be positive" }
        if (width <= 0 || height <= 0) return 1
        var sampleSize = 1
        while (width / sampleSize > maxSide || height / sampleSize > maxSide) {
            sampleSize *= 2
        }
        return sampleSize
    }
}
