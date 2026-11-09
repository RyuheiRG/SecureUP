package com.secureup.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as CameraPreview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.secureup.app.R
import com.secureup.app.ui.components.BottomBarDestination
import com.secureup.app.ui.components.InfoBanner
import com.secureup.app.ui.components.InfoBannerVariant
import com.secureup.app.ui.components.SecureUpBottomBar
import com.secureup.app.ui.components.SecureUpButton
import com.secureup.app.ui.components.SecureUpButtonVariant
import com.secureup.app.ui.components.SecureUpTopBar
import com.secureup.app.ui.theme.SecureUPTheme
import com.secureup.app.ui.theme.SecureUpColors
import com.secureup.app.ui.theme.Spacing
import com.secureup.app.viewmodel.ScannerViewModel
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Escáner QR (Frame A08). La cámara se procesa en el dispositivo con ML Kit;
 * ninguna imagen sale de la app. También se puede elegir una imagen de la galería.
 */
@Composable
fun ScannerScreen(
    onResult: (String) -> Unit,
    onHistoryClick: () -> Unit,
    onDestinationSelected: (BottomBarDestination) -> Unit = {},
    viewModel: ScannerViewModel = viewModel(factory = ScannerViewModel.Factory()),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionDenied = !granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let(viewModel::onImageSelected)
    }

    LaunchedEffect(uiState.detectedUrl) {
        uiState.detectedUrl?.let { url ->
            onResult(url)
            viewModel.consumeDetection()
        }
    }

    Scaffold(
        topBar = {
            SecureUpTopBar(title = stringResource(id = R.string.scanner_title))
        },
        bottomBar = {
            SecureUpBottomBar(
                currentDestination = BottomBarDestination.Scanner,
                onDestinationSelected = onDestinationSelected,
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(Spacing.L),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                if (hasCameraPermission) {
                    CameraScannerPreview(
                        onQrDetected = viewModel::onQrDetected,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SecureUpColors.Surface),
                    )
                } else {
                    CameraPermissionPrompt(
                        permissionDenied = permissionDenied,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                    )
                }

                if (uiState.isAnalyzing) {
                    CircularProgressIndicator(color = SecureUpColors.Primary)
                }
            }

            uiState.errorRes?.let { errorRes ->
                InfoBanner(
                    text = stringResource(id = errorRes),
                    variant = InfoBannerVariant.Danger,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            ) {
                SecureUpButton(
                    text = stringResource(id = R.string.scanner_gallery_button),
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.weight(1f),
                    variant = SecureUpButtonVariant.Secondary,
                )
                SecureUpButton(
                    text = stringResource(id = R.string.scanner_history_button),
                    onClick = onHistoryClick,
                    modifier = Modifier.weight(1f),
                    variant = SecureUpButtonVariant.Secondary,
                )
            }
        }
    }
}

@Composable
private fun CameraPermissionPrompt(
    permissionDenied: Boolean,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.L),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.M),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_qr_code),
            contentDescription = null,
            tint = SecureUpColors.Primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = stringResource(id = R.string.scanner_permission_title),
            style = MaterialTheme.typography.titleLarge,
            color = SecureUpColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(
                id = if (permissionDenied) {
                    R.string.scanner_permission_denied
                } else {
                    R.string.scanner_permission_rationale
                }
            ),
            style = MaterialTheme.typography.bodySmall,
            color = SecureUpColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        SecureUpButton(
            text = stringResource(id = R.string.scanner_permission_button),
            onClick = onRequestPermission,
        )
    }
}

/**
 * Vista de cámara con análisis de QR. Solo se analiza un fotograma a la vez y
 * cada `ImageProxy` se cierra siempre, incluso si ML Kit falla.
 */
@OptIn(ExperimentalGetImage::class)
@SuppressLint("UnsafeOptInUsageError") // el @OptIn ya está; lint no lo propaga al lambda del analyzer
@Composable
private fun CameraScannerPreview(
    onQrDetected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnQrDetected by rememberUpdatedState(onQrDetected)
    val scanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val isProcessing = remember { AtomicBoolean(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var torchEnabled by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            scanner.close()
        }
    }

    LaunchedEffect(torchEnabled, camera) {
        camera?.cameraControl?.enableTorch(torchEnabled)
    }

    // Visor + marco guía + linterna comparten el mismo Box.
    Box(modifier = modifier) {
        AndroidView(
            factory = { viewContext ->
                val previewView = PreviewView(viewContext).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
                val providerFuture = ProcessCameraProvider.getInstance(context)
                providerFuture.addListener({
                    val provider = providerFuture.get()
                    val preview = CameraPreview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val analysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    analysis.setAnalyzer(executor) { imageProxy ->
                        if (!isProcessing.compareAndSet(false, true)) {
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val mediaImage = imageProxy.image
                        if (mediaImage == null) {
                            isProcessing.set(false)
                            imageProxy.close()
                            return@setAnalyzer
                        }
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees,
                        )
                        scanner.process(inputImage)
                            .addOnSuccessListener { barcodes ->
                                barcodes
                                    .firstOrNull { !it.rawValue.isNullOrBlank() }
                                    ?.rawValue
                                    ?.let { currentOnQrDetected(it) }
                            }
                            .addOnCompleteListener {
                                isProcessing.set(false)
                                imageProxy.close()
                            }
                    }

                    try {
                        provider.unbindAll()
                        camera = provider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            analysis,
                        )
                    } catch (e: Exception) {
                        // Sin cámara trasera disponible: queda la opción de galería.
                    }
                }, ContextCompat.getMainExecutor(context))
                previewView
            },
            modifier = Modifier.fillMaxSize(),
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_frame_qr),
            contentDescription = null,
            tint = SecureUpColors.TextPrimary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(220.dp)
                .alpha(0.85f),
        )

        CameraOverlay(
            torchEnabled = torchEnabled,
            onToggleTorch = { torchEnabled = !torchEnabled },
            modifier = Modifier.align(Alignment.TopEnd),
        )
    }
}

@Composable
private fun CameraOverlay(
    torchEnabled: Boolean,
    onToggleTorch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onToggleTorch,
        modifier = modifier
            .padding(Spacing.M)
            .background(
                color = SecureUpColors.Background.copy(alpha = 0.6f),
                shape = CircleShape,
            ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_flashlight),
            contentDescription = stringResource(id = R.string.scanner_torch),
            tint = if (torchEnabled) SecureUpColors.Primary else SecureUpColors.TextPrimary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(name = "ScannerScreen", showBackground = true)
@Composable
private fun ScannerScreenPreview() {
    SecureUPTheme {
        ScannerScreen(onResult = {}, onHistoryClick = {})
    }
}
