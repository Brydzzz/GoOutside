package com.example.gooutside.ui.photo

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PhotoModeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoModeUiState())
    val uiState: StateFlow<PhotoModeUiState> = _uiState.asStateFlow()

    fun toggleFlash() {
        _uiState.update { it.copy(flashMode = it.flashMode.next()) }
    }

    fun toggleCamera() {
        _uiState.update { it.copy(cameraFacing = it.cameraFacing.toggle()) }
    }

    fun onTakePhotoClick() {
    }

    private fun analyzeImage() {}
}

enum class FlashMode(val value: Int) {
    OFF(ImageCapture.FLASH_MODE_OFF),
    ON(ImageCapture.FLASH_MODE_ON),
    AUTO(ImageCapture.FLASH_MODE_AUTO);

    fun next(): FlashMode = when (this) {
        OFF -> ON
        ON -> AUTO
        AUTO -> OFF
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

data class PhotoModeUiState(
    val cameraFacing: CameraFacing = CameraFacing.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
)

