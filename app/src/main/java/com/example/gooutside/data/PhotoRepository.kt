package com.example.gooutside.data

import androidx.camera.core.ImageProxy
import javax.inject.Inject

class PhotoRepository @Inject constructor() {
    suspend fun saveToMediaStore(imageProxy: ImageProxy): String {
        return ""
    }

    suspend fun deleteFromMediaStore(imagePath: String) {}
}