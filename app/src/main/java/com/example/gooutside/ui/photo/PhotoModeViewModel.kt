package com.example.gooutside.ui.photo

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntriesRepository
import com.example.gooutside.data.PhotoRepository
import com.example.gooutside.data.PhotoSaveResult
import com.example.gooutside.domain.AnalyzeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


// TODO: Clean up and refactor
@HiltViewModel
class PhotoModeViewModel @Inject constructor(
    private val analyzeImageUseCase: AnalyzeImageUseCase,
    private val photoRepository: PhotoRepository,
    private val diaryRepository: DiaryEntriesRepository
) : ViewModel() {

    companion object {
        private const val TAG = "PhotoModeViewModel"
    }

    private val _uiState = MutableStateFlow(PhotoModeUiState())
    val uiState: StateFlow<PhotoModeUiState> = _uiState.asStateFlow()

    fun toggleFlash() {
        _uiState.update { it.copy(flashMode = it.flashMode.next()) }
    }

    fun toggleCameraFacing() {
        _uiState.update { it.copy(cameraFacing = it.cameraFacing.toggle()) }
    }

    fun onCapture(controller: LifecycleCameraController) {
        Log.d(TAG, "onCapture called")

        if (_uiState.value.analysisState == AnalysisState.DURING) {
            Log.d(TAG, "Already processing, ignoring click")
            return
        }

        _uiState.update { it.copy(analysisState = AnalysisState.DURING) }

        viewModelScope.launch {
            try {
                val imageProxy = capturePhotoToMemory(controller)

                val analysisPassed = analyzeImageUseCase(imageProxy)
                Log.d(TAG, "analyzeImageUseCase completed: $analysisPassed")
                imageProxy.close()

                delay(1500) // delay for ux

                updateUiStateAfterAnalysis(analysisPassed)
            } catch (e: Exception) {
                resetUiState()
                Log.e(TAG, "Photo capture or analysis failed: ${e.message}", e)
            }
        }
    }

    // TODO: implement
    fun onSaveToDiaryConfirmed() {
        Log.d(TAG, "onSaveToDiaryConfirmed called")
        viewModelScope.launch {
            val savePhotoResult =
                _uiState.value.capturedImageBitmap?.let { photoRepository.saveToMediaStore(it) }

            when (savePhotoResult) {
                is PhotoSaveResult.Success ->
                    Log.d(TAG, "Photo saved to MediaStore: ${savePhotoResult.uri}")

                else ->
                    Log.e(TAG, "Photo saving to MediaStore failed")
            }

            // TODO: create diary entry and save it


        }
        resetUiState()
    }

    fun resetUiState() {
        _uiState.update {
            it.copy(
                analysisPassed = false,
                analysisState = AnalysisState.BEFORE,
                capturedImageBitmap = null,
                showAnalysisOverlay = false
            )
        }
        Log.d(TAG, "resetUiState finished")
    }

    private suspend fun capturePhotoToMemory(controller: LifecycleCameraController): ImageProxy {
        return suspendCoroutine { continuation ->
            val cameraExecutor = Dispatchers.Default.asExecutor()
            controller.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    continuation.resume(image)

                    viewModelScope.launch {
                        val previewBitmap = createPreviewBitmap(image)
                        _uiState.update {
                            it.copy(
                                capturedImageBitmap = previewBitmap,
                                showAnalysisOverlay = true
                            )
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                    continuation.resumeWithException(exception)
                }
            })
        }
    }

    private fun updateUiStateAfterAnalysis(analysisPassed: Boolean) {
        _uiState.update {
            it.copy(
                analysisPassed = analysisPassed,
                analysisState = AnalysisState.AFTER,
                showAnalysisOverlay = false
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun createPreviewBitmap(imageProxy: ImageProxy): Bitmap? {
        return try {
            val bitmap = imageProxy.toBitmap()
            val rotation = imageProxy.imageInfo.rotationDegrees

            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }

            // Flip horizontally when front camera was used to show photo as previewed
            if (_uiState.value.cameraFacing.value == CameraSelector.DEFAULT_FRONT_CAMERA) {
                matrix.postScale(-1f, 1f)
            }

            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0, 0,
                bitmap.width,
                bitmap.height,
                matrix,
                false
            )
            bitmap.recycle()

            rotatedBitmap
        } catch (e: Exception) {
            Log.e(TAG, "Updating captured image bitmap failed: ${e.message}", e)
            null
        }

    }
}

enum class FlashMode(val value: Int, @DrawableRes val icon: Int) {
    OFF(ImageCapture.FLASH_MODE_OFF, R.drawable.ic_flash_off_24),
    ON(ImageCapture.FLASH_MODE_ON, R.drawable.ic_flash_on_24),
    AUTO(ImageCapture.FLASH_MODE_AUTO, R.drawable.ic_flash_auto_24);

    fun next(): FlashMode = when (this) {
        OFF -> AUTO
        AUTO -> ON
        ON -> OFF
    }
}

enum class CameraFacing(val value: CameraSelector) {
    BACK(CameraSelector.DEFAULT_BACK_CAMERA),
    FRONT(CameraSelector.DEFAULT_FRONT_CAMERA);

    fun toggle(): CameraFacing = when (this) {
        BACK -> FRONT
        FRONT -> BACK
    }
}

enum class AnalysisState {
    BEFORE,
    DURING,
    AFTER
}

data class PhotoModeUiState(
    val cameraFacing: CameraFacing = CameraFacing.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val analysisState: AnalysisState = AnalysisState.BEFORE,
    val analysisPassed: Boolean = false,
    val capturedImageBitmap: Bitmap? = null,
    val showAnalysisOverlay: Boolean = false
)

