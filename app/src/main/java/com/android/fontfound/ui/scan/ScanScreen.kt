@file:Suppress("DEPRECATION")

package com.android.fontfound.ui.scan

import OverlayView
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.provider.Settings
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.android.fontfound.R
import com.google.firebase.ml.modeldownloader.CustomModel
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.google.firebase.ml.modeldownloader.DownloadType
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.GpuDelegateFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import com.android.fontfound.data.util.Result

private var interpreter: InterpreterApi? = null
private lateinit var fontRecognitionHelper: FontRecognitionHelper
val currentBoxRect = mutableStateOf<Rect?>(Rect(100,200,600,300))
val currentFontName = mutableStateOf("")
val currentConfidence = mutableFloatStateOf(0f)
private const val TAG = "ScanScreen"

@SuppressLint("HardwareIds")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    navController: NavHostController,
    scanViewModel: ScanViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraPermission = Manifest.permission.CAMERA
    var camera: Camera? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? = remember { null }
    var isLoading by remember { mutableStateOf(false) }

    val uploadResult by scanViewModel.uploadResult.observeAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(cameraPermission)
        }

        isLoading = true
        downloadModel(
            context = context,
            onDownloadSuccess = {
                isLoading = false
                fontRecognitionHelper = FontRecognitionHelper(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    imageCapture = ImageCapture.Builder().build()

                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()

                    imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        processImage(imageProxy)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()

                        camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Failed to bind camera use cases", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            onError = { errorMessage ->
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            interpreter?.close()
        }
    }

    val deviceId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    Box(modifier = Modifier.fillMaxSize()) {
        // ScanScreen content
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopAppBar(
                title = { Text(text = "Scan Font") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Red)
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(2f))

                CaptureButton(
                    imageCapture = imageCapture,
                    context = context,
                    scanViewModel = scanViewModel,
                    deviceId = deviceId ?: "UnknownDevice"
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        isFlashEnabled = !isFlashEnabled
                        camera?.cameraControl?.enableTorch(isFlashEnabled)
                    },
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Icon(
                        painter = if (isFlashEnabled) painterResource(R.drawable.ic_flash_on) else painterResource(R.drawable.ic_flash_off),
                        contentDescription = "Toggle Flash",
                        tint = if (isFlashEnabled) Color.Blue else Color.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text("Downloading model...", Modifier.align(Alignment.CenterHorizontally))
            }
        }

        when (uploadResult) {
            is Result.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Upload successful: ${(uploadResult as Result.Success).data}",
                        color = Color.Green,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is Result.Error -> {
                if ((uploadResult as Result.Error).error == "Loading") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Text("Uploading...", textAlign = TextAlign.Center)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Error: ${(uploadResult as Result.Error).error}",
                            color = Color.Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            null -> {
            }
        }

        // OverlayView
        OverlayView(
            boxRect = currentBoxRect.value,
            fontName = currentFontName.value,
            confidence = currentConfidence.floatValue
        )
    }
}

@Composable
fun CaptureButton(
    imageCapture: ImageCapture?,
    context: Context,
    scanViewModel: ScanViewModel,
    deviceId: String
) {
    val cameraExecutor = Executors.newSingleThreadExecutor()

    Button(
        onClick = {
            val photoFile = File(
                context.externalCacheDir,
                "${System.currentTimeMillis()}.jpg"
            )
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture?.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val currentTimestamp = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            Locale.getDefault()
                        ).format(Date())

                        val safeDeviceId = deviceId.ifBlank { "UnknownDevice" }

                        scanViewModel.uploadHistory(
                            imageUri = photoUri,
                            createdAt = currentTimestamp,
                            updatedAt = currentTimestamp,
                            result = "Font: ${currentFontName.value}, Confidence: ${currentConfidence.floatValue}",
                            deviceId = safeDeviceId,
                            context = context
                        )

                        Toast.makeText(context, "Image captured successfully!", Toast.LENGTH_SHORT).show()
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(context, "Failed to capture image: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_capture),
            contentDescription = "Capture",
            tint = Color.White
        )
    }
}

//fun isConnectedToInternet(context: Context): Boolean {
//    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//    val network = connectivityManager.activeNetwork ?: return false
//    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
//    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//}
//
//fun uploadToCloud(photoFile: File, deviceId: String) {
//    val client = OkHttpClient()
//    val requestBody = MultipartBody.Builder()
//        .setType(MultipartBody.FORM)
//        .addFormDataPart("photo", photoFile.name, photoFile.asRequestBody())
//        .addFormDataPart("ID_DEVICE", deviceId)
//        .build()
//
//    val request = Request.Builder()
//        .url("http://localhost:8080/api")
//        .post(requestBody)
//        .build()
//
//    client.newCall(request).execute()
//}
//
//fun saveLocally(photoFile: File) {
//    Log.d("CameraX", "Saved locally: ${photoFile.path}")
//}

@Synchronized
private fun downloadModel(
    context: Context,
    onDownloadSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    val conditions = CustomModelDownloadConditions.Builder()
        .requireWifi()
        .build()

    FirebaseModelDownloader.getInstance()
        .getModel("font-found", DownloadType.LOCAL_MODEL, conditions)
        .addOnSuccessListener { model: CustomModel ->
            try {
                // check if model already exists locally
                if (model.file != null && model.file!!.exists()) {
                    Log.d(TAG, "Model already exists locally at: ${model.file!!.absolutePath}")
                    val newModelPath = copyModelToInternalStorage(context, model.file!!)
                    onDownloadSuccess(newModelPath)
                } else {
                    Log.d(TAG, "Model file not found locally, re-downloading...")
                    initializeInterpreter(model, onError)
                    Toast.makeText(context, "Model downloaded successfully", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                onError("Failed to initialize interpreter: ${e.message}")
            }
        }
        .addOnFailureListener { e: Exception ->
            onError(context.getString(R.string.firebaseml_model_download_failed))
            Log.e(TAG, "Failed to download model: ${e.message}")
        }
}

private fun copyModelToInternalStorage(context: Context, modelFile: File): String {
    val targetFile = File(context.filesDir, "font_found.tflite")
    modelFile.copyTo(targetFile, overwrite = true)
    return targetFile.absolutePath
}



private fun initializeInterpreter(
    model: CustomModel,
    onError: (String) -> Unit
) {
    interpreter?.close()
    try {
        val options = InterpreterApi.Options()
            .setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
            .addDelegateFactory(GpuDelegateFactory())
        model.file?.let {
            interpreter = InterpreterApi.create(it, options)
        }
    } catch (e: Exception) {
        onError(e.message.toString())
        Log.e(TAG, e.message.toString())
    }
}

@SuppressLint("SetTextI18n")
private fun processImage(image: ImageProxy) {

    val croppedBitmap = cropBitmap(imageToBitmap(image), currentBoxRect.value)
    val resizedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 128, 128, true)
    val (fontName, confidence) = fontRecognitionHelper.classifyFont(resizedBitmap)

    currentFontName.value = fontName
    currentConfidence.floatValue = confidence

    image.close()
}


private fun imageToBitmap(image: ImageProxy): Bitmap {
    val yBuffer = image.planes[0].buffer
    val uBuffer = image.planes[1].buffer
    val vBuffer = image.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    uBuffer.get(nv21, ySize, uSize)
    vBuffer.get(nv21, ySize + uSize, vSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val outStream = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, outStream)
    val imageBytes = outStream.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

// Crop a bitmap to the specified Rect
private fun cropBitmap(bitmap: Bitmap, rect: Rect?): Bitmap {
    return if (rect != null) {
        Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height())
    } else {
        bitmap
    }
}

