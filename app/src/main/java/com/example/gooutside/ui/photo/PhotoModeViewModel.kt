package com.example.gooutside.ui.photo

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gooutside.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoModeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoModeUiState())
    val uiState: StateFlow<PhotoModeUiState> = _uiState.asStateFlow()

    fun toggleFlash() {
        _uiState.update { it.copy(flashMode = it.flashMode.next()) }
    }

    fun toggleCameraFacing() {
        _uiState.update { it.copy(cameraFacing = it.cameraFacing.toggle()) }
    }

    // TODO: implement onCapture, savePhoto and analyzeImage
    fun onCapture() {
        Log.d("PhotoModeViewModel", "onCapture")
        analyzeImage()
    }

    fun savingToDiaryDismissed() {
        _uiState.update { it.copy(analysisPassed = false) }
    }

    fun savePhoto() {
        // save Photo to storage and add entry to diary
        _uiState.update { it.copy(analysisPassed = false) }
        Log.d("PhotoModeViewModel", "savePhoto")
    }

    private fun analyzeImage() {
        viewModelScope.launch {
            // simulation - temporary
            _uiState.update { it.copy(isAnalysing = true) }
            delay(1000) // simulate analysis
            _uiState.update { it.copy(isAnalysing = false) }
            _uiState.update { it.copy(analysisPassed = true) }
        }
        Log.d("PhotoModeViewModel", "analyzeImage")
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

data class PhotoModeUiState(
    val cameraFacing: CameraFacing = CameraFacing.BACK,
    val flashMode: FlashMode = FlashMode.OFF,
    val isAnalysing: Boolean = false,
    val analysisPassed: Boolean = false
)

