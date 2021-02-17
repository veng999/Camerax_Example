package com.example.camerax_example.utils

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis

private const val STRATEGY_DELIVERY_LATEST = ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
object CameraUtils {
    fun getCameraSelector(lensFacing: Int) =
        CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

    fun getImageAnalysis() = ImageAnalysis.Builder().setBackpressureStrategy(STRATEGY_DELIVERY_LATEST).build()
}