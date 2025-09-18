package com.example.gooutside.domain

import android.util.Log
import androidx.camera.core.ImageProxy
import javax.inject.Inject

class AnalyzeImageUseCase @Inject constructor(
    private val imageAnalyzer: OutdoorImageAnalyzer
) {
    suspend operator fun invoke(imageProxy: ImageProxy): Boolean {
        Log.d("AnalyzeImageUseCase", "invoke called")
        return imageAnalyzer.analyseImage(imageProxy)
    }
}