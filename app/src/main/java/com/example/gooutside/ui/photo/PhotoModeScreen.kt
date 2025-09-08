package com.example.gooutside.ui.photo

import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gooutside.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoModeScreen(
    modifier: Modifier = Modifier,
    viewModel: PhotoModeViewModel = hiltViewModel<PhotoModeViewModel>()
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        val photoModeUiState: PhotoModeUiState by viewModel.uiState.collectAsState()
        /*TODO: move to separate components */
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.secondaryContainer) {
            Column {
                Text(text = "Log Your Day 📸", style = MaterialTheme.typography.headlineLarge)
                Text(
                    text = "Take a photo outdoors. The app will analyse it to confirm it's and outside shot.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(modifier = Modifier.padding(16.dp)) {
                    CameraPreview(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .border(
                                5.dp,
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(14.dp)
                            )
                            .align(Alignment.TopCenter),
                        cameraFacing = photoModeUiState.cameraFacing,
                        flashMode = photoModeUiState.flashMode
                    )
                    FilledIconButton(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .size(68.dp)
                            .align(Alignment.BottomCenter),
                        shape = CircleShape,
                        onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_photo_camera_24),
                            tint = MaterialTheme.colorScheme.surface,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                /*TODO back button, flash button, flip camera button*/
            }
        }
    } else {
        {/*TODO move to permission denied screen composable*/ }
        Column {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The camera is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission"
            }
            Text(textToShow)
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
        }
    }
}


@Composable
fun CameraPreview(modifier: Modifier = Modifier, cameraFacing: CameraFacing, flashMode: FlashMode) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraController = remember {
        LifecycleCameraController(context).apply {
            bindToLifecycle(lifecycleOwner)
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
            )
            cameraSelector = cameraFacing.value
            imageCaptureFlashMode = flashMode.value
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).apply {
                controller = cameraController
            }
        }
    )
}