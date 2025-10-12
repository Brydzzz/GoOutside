package com.example.gooutside.domain

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabel
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Singleton
class OutdoorImageAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OutdoorImageAnalyzer"
    }

    private val model =
        LocalModel.Builder().setAssetFilePath("resnet50_places365.tflite").build()
    private val options = CustomImageLabelerOptions.Builder(model)
        .setConfidenceThreshold(0.1f)
        .setMaxResultCount(10)
        .build()
    private val labeler = ImageLabeling.getClient(options)

    private val labelsIO = loadIOLabels()

    @OptIn(ExperimentalGetImage::class)
    suspend fun analyseImage(imageProxy: ImageProxy): Boolean = suspendCoroutine { continuation ->
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

    private fun loadIOLabels(): List<Int> {
        val labelsIO = mutableListOf<Int>()
        val fnameIO = "IO_places365.txt"
        val lines = context.assets.open(fnameIO).bufferedReader().use {
            it.readLines()
        }
        lines.forEach {
            val items = it.trim().split(" ")
            labelsIO.add(items.last().toInt())
        }
        return labelsIO
    }

    private fun isOutdoorImage(labels: List<ImageLabel>): Boolean {
        for (label in labels) {
            val text = label.text
            val confidence = label.confidence
            val index = label.index
            Log.v(
                TAG,
                "Label: $text, Index: $index, Confidence: $confidence"
            )
        }

        if (labels.isEmpty()) {
            Log.d(TAG, "No labels found")
            return false
        } else {
            val ioSum = labels.sumOf { label -> labelsIO[label.index] }
            val ioScore = ioSum.toDouble() / labels.size
            Log.d(TAG, "IO score: $ioScore")
            return ioScore > 0.5
        }
    }
}