package com.android.fontfound.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
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
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors


private var interpreter: InterpreterApi? = null
private const val TAG = "ScanScreen"


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraPermission = Manifest.permission.CAMERA
    val camera: Camera? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var imageCapture: ImageCapture? = remember { null }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (!granted) {
                Toast.makeText(context, "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        // Request camera permission
        if (ContextCompat.checkSelfPermission(context, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(cameraPermission)
        }

        // Initialize CameraX
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Log.e("CameraX", "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(context))

        // Download the Firebase model
        downloadModel(
            context = context,
            onDownloadSuccess = {
                Toast.makeText(context, "Model downloaded successfully", Toast.LENGTH_SHORT).show()
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

            CaptureButton(imageCapture, context)

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
    }
}

@Composable
fun CaptureButton(imageCapture: ImageCapture?, context: Context) {
    val cameraExecutor = Executors.newSingleThreadExecutor()

    Button(
        onClick = {
            val photoFile = File(
                context.externalCacheDir,
                "${System.currentTimeMillis()}.jpg"
            )
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture?.takePicture(
                outputOptions,
                cameraExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        Toast.makeText(context, "Image saved at ${photoFile.absolutePath}", Toast.LENGTH_SHORT).show()
                        Log.d("CameraX", "Image saved at ${photoFile.absolutePath}")
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(context, "Failed to capture image: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("CameraX", "Failed to capture image", exception)
                    }
                }
            )
        }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_camera),
            contentDescription = "Capture",
            tint = Color.White
        )
    }
}

fun isConnectedToInternet(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun uploadToCloud(photoFile: File, deviceId: String) {
    val client = OkHttpClient()
    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("photo", photoFile.name, photoFile.asRequestBody())
        .addFormDataPart("ID_DEVICE", deviceId)
        .build()

    val request = Request.Builder()
        .url("http://localhost:8080/api")
        .post(requestBody)
        .build()

    client.newCall(request).execute()
}

fun saveLocally(photoFile: File) {
    Log.d("CameraX", "Saved locally: ${photoFile.path}")
}

@Synchronized
private fun downloadModel(
    context: Context,
    onDownloadSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val conditions = CustomModelDownloadConditions.Builder()
        .requireWifi()
        .build()

    FirebaseModelDownloader.getInstance()
        .getModel("scan-item", DownloadType.LOCAL_MODEL, conditions)
        .addOnSuccessListener { model: CustomModel ->
            try {
                // Periksa apakah model sudah tersedia
                if (model.file != null && model.file!!.exists()) {
                    Log.d(TAG, "Model already exists locally at: ${model.file!!.absolutePath}")
                } else {
                    Log.d(TAG, "Model file not found locally, re-downloading...")
                    initializeInterpreter(model, onError)
                    onDownloadSuccess()
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
