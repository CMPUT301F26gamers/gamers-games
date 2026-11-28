package com.example.campsiteapp

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.campsiteapp.ui.theme.CampBackground
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

/**
 * Screen that allows an entrant to scan a campsite's promotional QR code
 * It requests camera permission and displays a live camera preview
 * When a valid QR code is scanned, it navigates to the campsite details screen
 *
 * @param navController The navigation controller used to move to the details screen.
 */

/* This function was made by Andrés Cruz at
https://www.desarrollolibre.net/blog/android/how-to-create-a-qr-code-reader-on-android-with-android-studio 2026 */
@Composable
fun ScannerScreen(navController: NavController,
                  campsiteRepo: CampsiteRepo
) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasScanned by remember { mutableStateOf(false) }
    var scanError by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            hasCameraPermission = true
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CampBackground)
    ) {
        ScreenHeader(title = "Scan QR Code", onBack = { navController.popBackStack() })

        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.weight(1f),
                hasScanned = hasScanned,
                onQrCodeDetected = { scannedId ->
                    // Check if the scanned ID exists in the repository
                    if (campsiteRepo.campsites.any { it.id == scannedId }) {
                        hasScanned = true
                        navController.navigate("campsiteDetails/$scannedId") {
                            popUpTo("scanner") { inclusive = true }
                        }
                    } else {
                        scanError = "Not a campsite QR"
                    }
                }
            )

            if (scanError != null) {
                Text(
                    text = scanError!!,
                    color = Color.Red,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Text(
                text = "Point your camera at a Campsite QR Code",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )
        } else {
            Text(
                text = "Camera permission is needed to scan QR.",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    hasScanned: Boolean,
    onQrCodeDetected: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { result ->
                            if (!hasScanned) {
                                onQrCodeDetected(result)
                            }
                        })
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) { // Fixed unused exception warning
                    // Handle camera initialization errors
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        }
    )
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

class BarcodeAnalyzer(private val onQrCodeDetected: (String) -> Unit) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        barcodes.firstOrNull()?.rawValue?.let { rawValue ->
                            onQrCodeDetected(rawValue)
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close() // Important: close ImageProxy to release buffer
                }
        } else {
            imageProxy.close()
        }
    }
}