package com.example.gooutside.domain

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class OutdoorImageAnalyzer @Inject constructor(
) {
    companion object {
        private const val TAG = "OutdoorImageAnalyzer"
    }

    // TODO: swap with custom model
    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    suspend fun analyseImage(imageProxy: ImageProxy): Boolean = suspendCoroutine { continuation ->
        // TODO: implement analysis with mlkit
        Log.d(TAG, "analyseImage called")
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val isOutdoor = isOutdoorImage(labels)
                    continuation.resume(isOutdoor)
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Photo analysis failed: ${exception.message}", exception)
                    continuation.resumeWithException(exception)
                }
        } else {
            continuation.resumeWithException(Exception("No image available"))
        }
    }

    // TODO: change to work with custom model, below is just an example
    private fun isOutdoorImage(labels: List<ImageLabel>): Boolean {
        var hasHand = false
        var hasOther = false
        for (label in labels) {
            val text = label.text
            val confidence = label.confidence
            Log.v(
                TAG,
                "Label: $text, Confidence: $confidence"
            )

            when (text) {
                "Hand" -> hasHand = true
                "Plant", "Soil" -> hasOther = true
            }
        }
        return hasHand && hasOther
    }
}