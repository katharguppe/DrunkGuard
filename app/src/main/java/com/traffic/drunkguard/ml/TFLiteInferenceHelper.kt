package com.traffic.drunkguard.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.annotation.VisibleForTesting
import com.traffic.drunkguard.data.model.IntoxicationLevel
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TensorFlow Lite inference helper for drunk detection.
 * Loads a TFLite model and runs inference on face images to detect intoxication levels.
 *
 * Model input: 224x224 RGB normalized image (ImageNet normalization)
 * Model output: 4-class probabilities [sober, slightly, moderately, heavily]
 */
@Singleton
class TFLiteInferenceHelper @Inject constructor(
    private val context: Context
) {
    private var interpreter: Interpreter? = null
    private val inputImageSize = 224
    private val numClasses = 4

    // Pre-allocate buffers for performance
    private lateinit var inputBuffer: ByteBuffer
    private lateinit var outputBuffer: Array<FloatArray>

    // ImageNet normalization constants
    private val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    /**
     * Model loading state
     */
    val isModelLoaded: Boolean
        get() = interpreter != null

    init {
        try {
            loadModel()
        } catch (e: Exception) {
            // Model will be null, isModelLoaded returns false
        }
    }

    /**
     * Load the TFLite model from assets.
     * Model path: assets/model/drunkguard.tflite
     *
     * @return Result.success(Unit) if loaded successfully, Result.failure otherwise
     */
    fun loadModel(): Result<Unit> {
        return try {
            val modelPath = MODEL_PATH
            val modelBuffer = loadModelFile(modelPath)

            val options = Interpreter.Options().apply {
                numThreads = NUM_THREADS
                useXNNPACK = true
            }

            interpreter = Interpreter(modelBuffer, options)

            // Initialize buffers
            val inputSize = BATCH_SIZE * inputImageSize * inputImageSize * CHANNELS * BYTES_PER_CHANNEL
            inputBuffer = ByteBuffer.allocateDirect(inputSize).apply {
                order(ByteOrder.nativeOrder())
            }
            outputBuffer = Array(BATCH_SIZE) { FloatArray(numClasses) }

            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(ModelLoadException("Failed to load model: ${e.message}", e))
        } catch (e: Exception) {
            Result.failure(ModelLoadException("Unexpected error loading model: ${e.message}", e))
        }
    }

    /**
     * Preprocess a Bitmap into a ByteBuffer for model input.
     * Steps: Resize to 224x224, convert to RGB, apply ImageNet normalization
     *
     * @param bitmap Input bitmap (any size)
     * @return ByteBuffer ready for inference
     */
    fun preprocess(bitmap: Bitmap): ByteBuffer {
        // Resize to 224x224
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, inputImageSize, inputImageSize, true)

        // Clear and rewind input buffer
        inputBuffer.clear()
        inputBuffer.rewind()

        // Convert to RGB array
        val pixels = IntArray(inputImageSize * inputImageSize)
        resizedBitmap.getPixels(pixels, 0, inputImageSize, 0, 0, inputImageSize, inputImageSize)

        // Convert to float and normalize (ImageNet normalization)
        for (pixel in pixels) {
            // Extract RGB channels (0-255)
            val r = ((pixel shr 16) and 0xFF).toFloat()
            val g = ((pixel shr 8) and 0xFF).toFloat()
            val b = (pixel and 0xFF).toFloat()

            // Normalize to [0, 1] then apply ImageNet normalization
            inputBuffer.putFloat(((r / 255.0f) - mean[0]) / std[0])
            inputBuffer.putFloat(((g / 255.0f) - mean[1]) / std[1])
            inputBuffer.putFloat(((b / 255.0f) - mean[2]) / std[2])
        }

        // Recycle if we created a new bitmap
        if (resizedBitmap !== bitmap) {
            resizedBitmap.recycle()
        }

        inputBuffer.rewind()
        return inputBuffer
    }

    /**
     * Preprocess a grayscale Bitmap into a ByteBuffer for model input.
     * Converts grayscale to RGB by duplicating the single channel.
     *
     * @param bitmap Grayscale bitmap
     * @return ByteBuffer ready for inference
     */
    fun preprocessGrayscale(bitmap: Bitmap): ByteBuffer {
        // Convert grayscale to RGB first
        val rgbBitmap = convertGrayscaleToRgb(bitmap)
        return preprocess(rgbBitmap).also {
            if (rgbBitmap !== bitmap) {
                rgbBitmap.recycle()
            }
        }
    }

    /**
     * Run inference on a preprocessed input buffer.
     *
     * @param input Preprocessed ByteBuffer
     * @return InferenceResult with detected level and probabilities
     * @throws IllegalStateException if model is not loaded
     */
    fun runInference(input: ByteBuffer): InferenceResult {
        val interpreterInstance = interpreter
            ?: throw IllegalStateException("Model not loaded. Call loadModel() first.")

        // Run inference
        interpreterInstance.run(input, outputBuffer)

        // Get probabilities (apply softmax if model outputs logits)
        val probabilities = outputBuffer[0].copyOf()

        // Apply softmax to ensure probabilities sum to 1
        val expScores = probabilities.map { kotlin.math.exp(it) }
        val sumExp = expScores.sum()
        val normalizedProbs = expScores.map { it / sumExp }.toFloatArray()

        return InferenceResult.fromProbabilities(normalizedProbs)
    }

    /**
     * Run complete inference pipeline: preprocess bitmap and run inference.
     *
     * @param bitmap Input bitmap (any size, will be resized to 224x224)
     * @return Result containing InferenceResult or failure
     */
    fun detect(bitmap: Bitmap): Result<InferenceResult> {
        if (!isModelLoaded) {
            return Result.failure(ModelLoadException("Model not loaded"))
        }

        return try {
            val input = preprocess(bitmap)
            val result = runInference(input)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(InferenceException("Inference failed: ${e.message}", e))
        }
    }

    /**
     * Run inference on a grayscale image.
     *
     * @param bitmap Grayscale bitmap
     * @return Result containing InferenceResult or failure
     */
    fun detectGrayscale(bitmap: Bitmap): Result<InferenceResult> {
        if (!isModelLoaded) {
            return Result.failure(ModelLoadException("Model not loaded"))
        }

        return try {
            val input = preprocessGrayscale(bitmap)
            val result = runInference(input)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(InferenceException("Inference failed: ${e.message}", e))
        }
    }

    /**
     * Get raw probabilities for all intoxication levels.
     * Useful for displaying confidence bars in UI.
     *
     * @param bitmap Input bitmap
     * @return Map of IntoxicationLevel to confidence score, or empty map on failure
     */
    fun getAllProbabilities(bitmap: Bitmap): Map<IntoxicationLevel, Float> {
        return detect(bitmap).map { result ->
            IntoxicationLevel.values().associateWith { level ->
                result.getProbabilityForLevel(level)
            }
        }.getOrDefault(emptyMap())
    }

    /**
     * Close the interpreter and release resources.
     * Should be called when the helper is no longer needed.
     */
    fun close() {
        interpreter?.close()
        interpreter = null
    }

    /**
     * Load model file from assets.
     */
    @VisibleForTesting
    @Throws(IOException::class)
    internal fun loadModelFile(modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            val startOffset = fileDescriptor.startOffset
            val declaredLength = fileDescriptor.declaredLength
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        }
    }

    /**
     * Convert a grayscale bitmap to RGB by duplicating the single channel.
     */
    private fun convertGrayscaleToRgb(grayscaleBitmap: Bitmap): Bitmap {
        val rgbBitmap = Bitmap.createBitmap(
            grayscaleBitmap.width,
            grayscaleBitmap.height,
            Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(rgbBitmap)
        val paint = Paint()

        // Color matrix to convert grayscale to RGB (identity for RGB channels)
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0f) // Grayscale
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(grayscaleBitmap, 0f, 0f, paint)

        return rgbBitmap
    }

    companion object {
        private const val MODEL_PATH = "model/drunkguard.tflite"
        private const val BATCH_SIZE = 1
        private const val CHANNELS = 3
        private const val BYTES_PER_CHANNEL = 4 // float32
        private const val NUM_THREADS = 4
    }
}

/**
 * Exception thrown when model loading fails
 */
class ModelLoadException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Exception thrown when inference fails
 */
class InferenceException(message: String, cause: Throwable? = null) : Exception(message, cause)
