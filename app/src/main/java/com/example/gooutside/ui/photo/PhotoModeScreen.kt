package com.example.gooutside.ui.photo

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.annotation.DrawableRes
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gooutside.R
import com.example.gooutside.ui.common.CustomAlertDialog
import com.example.gooutside.ui.theme.GoOutsideTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// TODO: move strings to string resources
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
        val coroutineScope = rememberCoroutineScope()

        val lifecycleOwner = LocalLifecycleOwner.current
        val context = LocalContext.current
        val cameraController = remember {
            LifecycleCameraController(context).apply {
                bindToLifecycle(lifecycleOwner)
                setEnabledUseCases(
                    CameraController.IMAGE_CAPTURE or CameraController.IMAGE_ANALYSIS
                )
            }
        }
        cameraController.cameraSelector = photoModeUiState.cameraFacing.value
        cameraController.imageCaptureFlashMode = photoModeUiState.flashMode.value

        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            if (photoModeUiState.analysisState == AnalysisState.AFTER) {
                when (photoModeUiState.analysisPassed) {
                    true -> AddToDiaryDialog(
                        onDismissRequest = { viewModel.resetUiState() },
                        onConfirmation = { coroutineScope.launch { viewModel.onSaveToDiaryConfirmed() } })

                    false -> AnalysisFailedDialog(
                        onDismissRequest = {
                            viewModel.resetUiState()
                            onNavigateUp()
                        },
                        onConfirmation = { viewModel.resetUiState() }
                    )
                }
            }

            Column(
                modifier = Modifier.padding(
                    top = 12.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 32.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PhotoModeHeader()
                CameraPreviewStyled(
                    onCapture = { viewModel.onCapture(cameraController) },
                    cameraController = cameraController,
                    showAnalysisOverlay = photoModeUiState.showAnalysisOverlay,
                    capturedImageBitmap = photoModeUiState.capturedImageBitmap,
                    analysisState = photoModeUiState.analysisState
                )
                CameraButtons(
                    onNavigateUp = onNavigateUp,
                    toggleFlash = { viewModel.toggleFlash() },
                    toggleCameraFacing = { viewModel.toggleCameraFacing() },
                    flashIcon = photoModeUiState.flashMode.icon
                )
            }
        }
    } else {
        CameraPermissionScreen(
            cameraPermissionState = cameraPermissionState,
            onNavigateUp = onNavigateUp
        )
    }
}


@Composable
fun PhotoModeHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Log Your Day 📸",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Take a photo outdoors. The app will analyse it to confirm it's and outside shot.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun CameraPreviewStyled(
    showAnalysisOverlay: Boolean,
    capturedImageBitmap: Bitmap?,
    cameraController: LifecycleCameraController,
    analysisState: AnalysisState,
    onCapture: () -> Unit
) {
    var shutterFlash by remember { mutableStateOf(false) }

    LaunchedEffect(analysisState) {
        if (analysisState == AnalysisState.DURING) {
            shutterFlash = true
            delay(100)
            shutterFlash = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxHeight(0.9f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                5.dp,
                MaterialTheme.colorScheme.primary,
                RoundedCornerShape(14.dp)
            )
    ) {
        CameraPreview(
            modifier = Modifier.align(Alignment.TopCenter),
            cameraController = cameraController
        )
        FilledIconButton(
            modifier = Modifier
                .padding(bottom = 18.dp)
                .size(66.dp)
                .align(Alignment.BottomCenter),
            shape = CircleShape,
            onClick = {
                onCapture()
                shutterFlash = true
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_photo_camera_24),
                tint = MaterialTheme.colorScheme.surface,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        }

        if (shutterFlash) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )
        }

        if (showAnalysisOverlay) {
            capturedImageBitmap?.let { bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(red = 0, green = 0, blue = 0, alpha = 125)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Analysing your photo...",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surface
                    )
                    Spacer(Modifier.size(6.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.width(64.dp),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }
        }
    }
}

@Composable
fun CameraButtons(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    toggleFlash: () -> Unit,
    toggleCameraFacing: () -> Unit,
    @DrawableRes flashIcon: Int
) {
    val iconSize = 48.dp
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilledTonalIconButton(
            onClick = onNavigateUp,
            modifier = Modifier.size(iconSize)
        ) {
            Icon(painter = painterResource(R.drawable.ic_arrow_back_24), contentDescription = null)
        }
        FilledTonalIconButton(
            onClick = toggleFlash,
            modifier = Modifier.size(iconSize)
        ) {
            Icon(painter = painterResource(flashIcon), contentDescription = null)
        }
        FilledTonalIconButton(
            onClick = toggleCameraFacing,
            modifier = Modifier.size(iconSize)
        ) {
            Icon(painter = painterResource(R.drawable.ic_flip_camera_24), contentDescription = null)
        }
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
fun CameraPreview(modifier: Modifier = Modifier, cameraController: LifecycleCameraController) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            PreviewView(context).apply {
                controller = cameraController
            }
        }
    )
}

@Composable
fun AddToDiaryDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    CustomAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dialogTitle = "Congrats!",
        dialogText = "Your photo passed the test. Would you like to save it to your diary?",
        iconId = R.drawable.ic_save_24,
        iconDescription = "Save icon",
        confirmButtonText = "Save to Diary",
        dismissButtonText = "Discard"
    )
}

@Composable
fun AnalysisFailedDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit
) {
    CustomAlertDialog(
        onDismissRequest = onDismissRequest,
        onConfirmation = onConfirmation,
        dialogTitle = "Oops",
        dialogText = "Your photo hasn't passed the test. Would you like to retake it?",
        iconId = R.drawable.ic_test_failed_24,
        iconDescription = "Test failed icon",
        confirmButtonText = "Retake",
        dismissButtonText = "Back to home"
    )

}


@Preview
@Composable
fun AddToDiaryDialogPreview() {
    GoOutsideTheme {
        AddToDiaryDialog({}, {})
    }
}

@Preview
@Composable
fun AnalysisFailedDialogPreview() {
    GoOutsideTheme {
        AnalysisFailedDialog({}, {})
    }
}