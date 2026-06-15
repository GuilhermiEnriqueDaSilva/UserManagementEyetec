@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.example.usermanagementeyetec.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

@Composable
fun CameraScreen(onImageCaptured: (Uri?) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var detectedFace by remember { mutableStateOf<Face?>(null) }
    var feedbackMessage by remember { mutableStateOf("📸 Aponte a câmera para o rosto") }
    var isCapturing by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    val faceDetector = remember {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        FaceDetection.getClient(options)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            faceDetector.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Preview da câmera
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewViewRef = this
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }

                        imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                            val mediaImage = imageProxy.image
                            if (mediaImage != null) {
                                val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                faceDetector.process(inputImage)
                                    .addOnSuccessListener { faces ->
                                        val face = faces.firstOrNull()
                                        detectedFace = face

                                        val previewWidth = previewViewRef?.width ?: 0
                                        val previewHeight = previewViewRef?.height ?: 0

                                        if (face != null && previewWidth > 0 && previewHeight > 0) {
                                            val box = face.boundingBox
                                            val centerX = box.centerX().toFloat() / previewWidth
                                            val isCentered = centerX in 0.35f..0.65f
                                            val boxArea = box.width() * box.height()
                                            val isTooFar = boxArea < 20000
                                            val isTooClose = boxArea > 80000

                                            feedbackMessage = when {
                                                !isCentered -> "👆 Centralize o rosto"
                                                isTooClose -> "🔍 Muito perto, afaste um pouco"
                                                isTooFar -> "📱 Muito longe, aproxime-se"
                                                else -> "✅ Perfeito! Pode tirar a foto"
                                            }
                                        } else {
                                            feedbackMessage = "👤 Nenhum rosto detectado"
                                        }
                                        imageProxy.close()
                                    }
                                    .addOnFailureListener {
                                        feedbackMessage = "⚠️ Erro ao detectar rosto"
                                        imageProxy.close()
                                    }
                            } else {
                                imageProxy.close()
                            }
                        }

                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Texto de feedback centralizado na tela
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = feedbackMessage,
                color = when {
                    feedbackMessage.contains("Perfeito") -> Color.Green
                    feedbackMessage.contains("Centralize") -> Color.Yellow
                    else -> Color.White
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }

        // Botão capturar (mantido no rodapé)
        Button(
            onClick = {
                if (detectedFace != null && feedbackMessage.contains("Perfeito") && !isCapturing) {
                    isCapturing = true
                    val tempDir = File(context.filesDir, "temp")
                    if (!tempDir.exists()) tempDir.mkdirs()
                    val photoFile = File(tempDir, "temp_${System.currentTimeMillis()}.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                isCapturing = false
                                onImageCaptured(Uri.fromFile(photoFile))
                            }
                            override fun onError(exception: ImageCaptureException) {
                                isCapturing = false
                                feedbackMessage = "❌ Erro ao capturar"
                                onImageCaptured(null)
                            }
                        }
                    )
                } else {
                    feedbackMessage = "⚠️ Aguarde o rosto ser detectado e centralizado"
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            enabled = !isCapturing && detectedFace != null
        ) {
            if (isCapturing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text("Tirar Foto")
            }
        }
    }
}