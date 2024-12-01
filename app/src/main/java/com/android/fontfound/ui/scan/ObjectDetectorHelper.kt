package com.android.fontfound.ui.scan

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageProxy
import com.android.fontfound.R
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.gms.vision.TfLiteVision
import org.tensorflow.lite.task.gms.vision.detector.Detection
import org.tensorflow.lite.task.gms.vision.detector.ObjectDetector

class ObjectDetectorHelper(
    var threshold: Float = 0.5f,
    var maxResults: Int = 5,
    val modelName: String = "analysis.tflite",
    val context: Context,
    val detectorListener: DetectorListener?
) {
    private var objectDetector: ObjectDetector? = null

    init {
        TfLiteGpu.isGpuDelegateAvailable(context).onSuccessTask { gpuAvailable ->
            val optionsBuilder = TfLiteInitializationOptions.builder()
            if (gpuAvailable) {
                optionsBuilder.setEnableGpuDelegateSupport(true)
            }
            TfLiteVision.initialize(context, optionsBuilder.build())
        }.addOnSuccessListener {
            Log.d(TAG, "TfLiteVision initialized successfully")
            setupObjectDetector()
        }.addOnFailureListener { exception ->
            Log.e(TAG, "TfLiteVision initialization failed: ${exception.message}")
            detectorListener?.onError(context.getString(R.string.app_name))
        }

    }

    private fun setupObjectDetector() {
        Log.d(TAG, "Setting up ObjectDetector...")
        if (!TfLiteVision.isInitialized()) {
            Log.e(TAG, "TfLiteVision is not initialized yet!")
            return
        }
        val optionsBuilder = ObjectDetector.ObjectDetectorOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder()
        baseOptionsBuilder.setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            objectDetector = ObjectDetector.createFromFileAndOptions(
                context,
                modelName,
                optionsBuilder.build()
            )
        } catch (e: IllegalStateException) {
            detectorListener?.onError(context.getString(R.string.app_name))
            Log.e(TAG, e.message.toString())
        }
    }

    fun detectObject(image: ImageProxy) {

        if (!TfLiteVision.isInitialized()) {
            val errorMessage = context.getString(R.string.app_name)
            Log.e(TAG, errorMessage)
            detectorListener?.onError(errorMessage)
            return
        }

        if (objectDetector == null) {
            setupObjectDetector()
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(Rot90Op(-image.imageInfo.rotationDegrees / 90))
            .build()

        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(toBitmap(image)))

        var inferenceTime = SystemClock.uptimeMillis()
        val results = objectDetector?.detect(tensorImage)
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        detectorListener?.onResults(
            results,
            inferenceTime,
            tensorImage.height,
            tensorImage.width
        )
    }

    private fun toBitmap(image: ImageProxy): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width
        val bitmap = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: MutableList<Detection>?,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        private const val TAG = "ObjectDetectorHelper"
    }
}
