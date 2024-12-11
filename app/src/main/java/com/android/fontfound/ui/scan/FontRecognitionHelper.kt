package com.android.fontfound.ui.scan

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileNotFoundException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FontRecognitionHelper(context: Context) {

    private val interpreter: Interpreter
    private val labelList: List<String>

    init {
        val modelPath = "${context.filesDir}/font_found.tflite"
        val modelFile = File(modelPath)

        if (modelFile.exists()) {
            interpreter = Interpreter(modelFile)
            // Load labels
            val labelInputStream = context.assets.open("labels.txt")
            labelList = labelInputStream.bufferedReader().readLines()
        } else {
            throw FileNotFoundException("TFLite model file not found at: $modelPath")
        }
    }


    fun classifyFont(bitmap: Bitmap): Pair<String, Float> {

        val byteBuffer = imageToByteBuffer(bitmap)

        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputBuffer = ByteBuffer.allocateDirect(4 * outputShape[1])
        outputBuffer.order(ByteOrder.nativeOrder())

        interpreter.run(byteBuffer, outputBuffer)

        val outputTensor = TensorBuffer.createFixedSize(outputShape, org.tensorflow.lite.DataType.FLOAT32)
        outputBuffer.rewind()
        outputTensor.loadBuffer(outputBuffer)

        val probabilities = outputTensor.floatArray
        var maxProb = 0f
        var maxIndex = 0
        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        return Pair(labelList[maxIndex], maxProb)
    }

    private fun imageToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * bitmap.width * bitmap.height * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixel in intValues) {
            byteBuffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
            byteBuffer.putFloat(((pixel and 0xFF) / 255.0f))
        }

        return byteBuffer
    }
}