package com.example.gooutside.ui.photo

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gooutside.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoModeScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhotoModeViewModel = hiltViewModel<PhotoModeViewModel>()
) {
    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )

    if (cameraPermissionState.status.isGranted) {
        val photoModeUiState: PhotoModeUiState by viewModel.uiState.collectAsState()
        /*TODO: move to separate components */
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
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
        CameraPermissionScreen(
            cameraPermissionState = cameraPermissionState,
            onNavigateUp = onNavigateUp
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionScreen(
    cameraPermissionState: PermissionState,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onNavigateUp) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back_24),
                contentDescription = null
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "The camera is important for this app. Please grant the permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Camera permission required for this feature to be available. " +
                        "Please grant the permission."
            }
            Text(
                textToShow,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.size(14.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Request permission")
            }
            Text("OR", style = MaterialTheme.typography.labelLarge)
            Button(onClick = {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null)
                )
                context.startActivity(intent)
            }) {
                Text("Enable in settings")
            }
        }

        Text(
            text = stringResource(R.string.request_permission_not_working_tip),
            style = MaterialTheme.typography.labelSmall
        )
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