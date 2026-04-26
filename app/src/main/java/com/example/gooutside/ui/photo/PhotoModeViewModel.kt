package com.example.gooutside.ui.photo

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Stable
import androidx.core.graphics.scale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gooutside.R
import com.example.gooutside.data.DiaryEntriesRepository
import com.example.gooutside.data.DiaryEntry
import com.example.gooutside.data.PhotoRepository
import com.example.gooutside.data.PhotoSaveResult
import com.example.gooutside.di.ApplicationScope
import com.example.gooutside.domain.AnalyzeImageUseCase
import com.example.gooutside.location.LocationDetails
import com.example.gooutside.location.LocationManager
import com.example.gooutside.util.ToastManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


// TODO: Clean up and refactor
@HiltViewModel
@Stable
class PhotoModeViewModel @Inject constructor(
    private val analyzeImageUseCase: AnalyzeImageUseCase,
    private val photoRepository: PhotoRepository,
    private val diaryRepository: DiaryEntriesRepository,
    private val locationManager: LocationManager,
    private val toastManager: ToastManager,
    @param:ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    companion object {
        private const val TAG = "PhotoModeViewModel"
        private const val ANALYSIS_UX_DELAY_MS: Long = 1000L
    }

    private var pendingSaveBitmap: Bitmap? = null

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

        if (_uiState.value.photoState == PhotoState.Processing) {
            Log.d(TAG, "Already processing, ignoring click")
            return
        }

        _uiState.update { it.copy(photoState = PhotoState.Processing) }

        viewModelScope.launch {
            try {
                val imageProxy = capturePhotoToMemory(controller)
                processCapture(imageProxy)
                val analysisPassed = analyzeImageUseCase(imageProxy)
                Log.d(TAG, "analyzeImageUseCase completed: $analysisPassed")
                imageProxy.close()

                delay(ANALYSIS_UX_DELAY_MS) // delay for ux

                updateUiStateAfterAnalysis(analysisPassed)
            } catch (e: Exception) {
                resetState()
                Log.e(TAG, "Photo capture or analysis failed: ${e.message}", e)
            }
        }
    }

    fun onSaveToDiaryConfirmed() {
        Log.d(TAG, "onSaveToDiaryConfirmed called")
        val bitmapToSave = pendingSaveBitmap
        resetState()

        applicationScope.launch(Dispatchers.IO) {
            val savePhotoResult = bitmapToSave?.let { photoRepository.saveToMediaStore(it) }

            when (savePhotoResult) {
                is PhotoSaveResult.Success -> {
                    Log.d(TAG, "Photo saved to MediaStore: ${savePhotoResult.uri}")


                    @SuppressLint("MissingPermission") // Is being checked in ui
                    val location = locationManager.getCurrentLocation()
                    Log.d(TAG, "Location: $location")

                    var locationDetails: LocationDetails? = null
                    if (location != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            locationDetails = locationManager.reverseGeocode(location)
                            Log.d(TAG, "Location details: $locationDetails")
                        } else {
                            locationDetails = locationManager.reverseGeocodeLegacy(location)
                            Log.d(TAG, "Location details: $locationDetails")
                        }
                    }

                    val entry = DiaryEntry(
                        creationDate = LocalDate.now(),
                        imagePath = savePhotoResult.uri,
                        street = locationDetails?.street,
                        streetNumber = locationDetails?.streetNumber,
                        city = locationDetails?.city,
                        country = locationDetails?.country,
                        longitude = location?.longitude,
                        latitude = location?.latitude,
                    )

                    diaryRepository.insertDiaryEntry(entry)
                }

                is PhotoSaveResult.Failure -> {
                    Log.e(
                        TAG,
                        "Photo saving to MediaStore failed: ${savePhotoResult.exception.message}"
                    )
                    toastManager.show("Diary entry saving failed")
                }

                null -> {
                    Log.e(TAG, "Photo saving to MediaStore failed: bitMapToSave was null")
                    toastManager.show("Diary entry saving failed")
                }
            }
        }
    }

    fun resetState() {
        pendingSaveBitmap = null
        _uiState.update {
            it.copy(
                photoState = PhotoState.Idle,
                showAnalysisOverlay = false,
                displayBitmap = null
            )
        }
        Log.d(TAG, "resetState finished")
    }

    private suspend fun capturePhotoToMemory(controller: LifecycleCameraController): ImageProxy {
        return suspendCancellableCoroutine { continuation ->
            controller.takePicture(
                Dispatchers.Default.asExecutor(),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        continuation.resume(image)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }

    private fun updateUiStateAfterAnalysis(analysisPassed: Boolean) {
        _uiState.update {
            it.copy(
                photoState = PhotoState.Done(analysisPassed),
                showAnalysisOverlay = false
            )
        }
    }

    private suspend fun processCapture(imageProxy: ImageProxy) = withContext(Dispatchers.IO) {
        val raw = imageProxy.toBitmap()
        val rotation = imageProxy.imageInfo.rotationDegrees
        val isFront = _uiState.value.cameraFacing.value == CameraSelector.DEFAULT_FRONT_CAMERA
        // adjust for portrait display
        // NOTE: the rotation depends on producer -  might break in some phones
        val extraRotation = when (rotation) {
            // for front camera default portrait is 270 but upside down is 90
            0 -> 90f
            90 -> if (isFront) 180f else 0f
            180 -> -90f
            270 -> if (isFront) 0f else 180f
            // imageInfo.rotationDegrees returns the rotation in degrees which will be a value in {0, 90, 180, 270}
            else -> 0f
        }
        val scale = 1080f / maxOf(raw.width, raw.height)
        val scaledWidth = (raw.width * scale).toInt()
        val scaledHeight = (raw.height * scale).toInt()
        val scaled = raw.scale(scaledWidth, scaledHeight)


        val displayMatrix = Matrix().apply {
            postRotate(rotation.toFloat())
            if (isFront) postScale(-1f, 1f)
            postRotate(extraRotation)
        }
        val displayBitmap =
            Bitmap.createBitmap(scaled, 0, 0, scaled.width, scaled.height, displayMatrix, true)
        scaled.recycle()


        val saveMatrix = Matrix().apply {
            postRotate(rotation.toFloat())
            if (isFront) postScale(-1f, 1f)
        }
        val saveBitmap = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, saveMatrix, true)
        raw.recycle()

        pendingSaveBitmap = saveBitmap
        _uiState.update {
            it.copy(
                displayBitmap = displayBitmap,
                showAnalysisOverlay = true
            )
        }
    }
}

enum class FlashMode(val value: Int, @param:DrawableRes val icon: Int) {
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

sealed class PhotoState {
    data object Idle : PhotoState()
    data object Processing : PhotoState()
    data class Done(val passed: Boolean) : PhotoState()
}

// todo: check if photo was already taken today
data class PhotoModeUiState(
    val cameraFacing: CameraFacing = CameraFacing.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val photoState: PhotoState = PhotoState.Idle,
    val displayBitmap: Bitmap? = null,
    val showAnalysisOverlay: Boolean = false,
)

