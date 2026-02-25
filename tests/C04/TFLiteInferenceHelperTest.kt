package com.traffic.drunkguard.ml

import android.content.Context
import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.traffic.drunkguard.data.model.IntoxicationLevel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.*
import org.junit.rules.TestRule
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer

/**
 * Unit tests for TFLiteInferenceHelper and InferenceResult
 */
@ExperimentalCoroutinesApi
class TFLiteInferenceHelperTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    @MockK
    private lateinit var mockContext: Context

    @MockK
    private lateinit var mockBitmap: Bitmap

    private lateinit var helper: TFLiteInferenceHelper

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(Bitmap::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ============================================================================
    // InferenceResult Tests
    // ============================================================================

    @Test
    fun `InferenceResult fromProbabilities creates correct result`() {
        val probabilities = floatArrayOf(0.1f, 0.2f, 0.5f, 0.2f)

        val result = InferenceResult.fromProbabilities(probabilities)

        Assert.assertEquals(IntoxicationLevel.MODERATELY, result.level)
        Assert.assertEquals(0.5f, result.confidence, 0.001f)
        Assert.assertArrayEquals(probabilities, result.probabilities, 0.001f)
    }

    @Test
    fun `InferenceResult isConfident returns true when above threshold`() {
        val result = InferenceResult(
            level = IntoxicationLevel.SOBER,
            confidence = 0.75f,
            probabilities = floatArrayOf(0.75f, 0.1f, 0.1f, 0.05f)
        )

        Assert.assertTrue(result.isConfident(0.65f))
        Assert.assertTrue(result.isConfident(0.75f))
        Assert.assertFalse(result.isConfident(0.80f))
    }

    @Test
    fun `InferenceResult getProbabilityForLevel returns correct value`() {
        val probabilities = floatArrayOf(0.7f, 0.1f, 0.15f, 0.05f)
        val result = InferenceResult(
            level = IntoxicationLevel.SOBER,
            confidence = 0.7f,
            probabilities = probabilities
        )

        Assert.assertEquals(0.7f, result.getProbabilityForLevel(IntoxicationLevel.SOBER), 0.001f)
        Assert.assertEquals(0.1f, result.getProbabilityForLevel(IntoxicationLevel.SLIGHTLY), 0.001f)
        Assert.assertEquals(0.15f, result.getProbabilityForLevel(IntoxicationLevel.MODERATELY), 0.001f)
        Assert.assertEquals(0.05f, result.getProbabilityForLevel(IntoxicationLevel.HEAVILY), 0.001f)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `InferenceResult fromProbabilities throws on wrong size`() {
        InferenceResult.fromProbabilities(floatArrayOf(0.5f, 0.5f)) // Only 2 classes
    }

    @Test
    fun `InferenceResult equality works correctly`() {
        val probs1 = floatArrayOf(0.7f, 0.1f, 0.1f, 0.1f)
        val probs2 = floatArrayOf(0.7f, 0.1f, 0.1f, 0.1f)
        val probs3 = floatArrayOf(0.6f, 0.2f, 0.1f, 0.1f)

        val result1 = InferenceResult(IntoxicationLevel.SOBER, 0.7f, probs1)
        val result2 = InferenceResult(IntoxicationLevel.SOBER, 0.7f, probs2)
        val result3 = InferenceResult(IntoxicationLevel.SOBER, 0.7f, probs3)

        Assert.assertEquals(result1, result2)
        Assert.assertNotEquals(result1, result3)
        Assert.assertEquals(result1.hashCode(), result2.hashCode())
    }

    // ============================================================================
    // TFLiteInferenceHelper Tests
    // ============================================================================

    @Test
    fun `isModelLoaded returns false when model not loaded`() {
        // Without setting up assets, the model won't load
        helper = TFLiteInferenceHelper(mockContext)

        Assert.assertFalse(helper.isModelLoaded)
    }

    @Test
    fun `loadModel returns failure when model file not found`() {
        every { mockContext.assets.openFd(any()) } throws IOException("File not found")

        helper = spyk(TFLiteInferenceHelper(mockContext), recordPrivateCalls = true)

        val result = helper.loadModel()

        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is ModelLoadException)
        verify { mockContext.assets.openFd("model/drunkguard.tflite") }
    }

    @Test
    fun `detect returns failure when model not loaded`() {
        helper = TFLiteInferenceHelper(mockContext)

        val result = helper.detect(mockBitmap)

        Assert.assertTrue(result.isFailure)
        Assert.assertTrue(result.exceptionOrNull() is ModelLoadException)
    }

    @Test
    fun `preprocess creates ByteBuffer of correct size`() {
        // Setup a mock MappedByteBuffer for model loading
        val mockByteBuffer = mockk<MappedByteBuffer>(relaxed = true)

        helper = spyk(TFLiteInferenceHelper(mockContext))
        every { helper["loadModelFile"](any<String>()) } returns mockByteBuffer

        // After loading model, preprocess should work
        helper.loadModel()

        // Create a 100x100 bitmap
        every { mockBitmap.width } returns 100
        every { mockBitmap.height } returns 100

        val processedBitmap = mockk<Bitmap>(relaxed = true)
        every { processedBitmap.getPixels(any(), any(), any(), any(), any(), any(), any()) } just Runs

        mockkStatic(Bitmap::class)
        every { Bitmap.createScaledBitmap(mockBitmap, 224, 224, true) } returns processedBitmap

        val inputBuffer = helper.preprocess(mockBitmap)

        // 224 * 224 * 3 channels * 4 bytes per float = 602112 bytes
        Assert.assertEquals(602112, inputBuffer.capacity())
    }

    @Test
    fun `close releases interpreter`() {
        helper = TFLiteInferenceHelper(mockContext)

        // Should not throw even if model not loaded
        helper.close()

        Assert.assertFalse(helper.isModelLoaded)
    }

    @Test
    fun `detect handles all intoxication levels`() {
        // Test that all 4 intoxication levels can be detected
        val levels = IntoxicationLevel.values()

        for (i in levels.indices) {
            val probabilities = FloatArray(4) { if (it == i) 0.7f else 0.1f }
            val result = InferenceResult.fromProbabilities(probabilities)

            Assert.assertEquals(levels[i], result.level)
            Assert.assertTrue(result.confidence > 0.5f)
        }
    }

    @Test
    fun `getAllProbabilities returns empty map on failure`() {
        helper = TFLiteInferenceHelper(mockContext)

        val probabilities = helper.getAllProbabilities(mockBitmap)

        Assert.assertTrue(probabilities.isEmpty())
    }

    @Test
    fun `detectGrayscale returns failure when model not loaded`() {
        helper = TFLiteInferenceHelper(mockContext)

        val result = helper.detectGrayscale(mockBitmap)

        Assert.assertTrue(result.isFailure)
    }

    @Test
    fun `runInference throws when interpreter is null`() {
        helper = TFLiteInferenceHelper(mockContext)

        val buffer = ByteBuffer.allocate(602112)

        try {
            helper.runInference(buffer)
            Assert.fail("Expected IllegalStateException")
        } catch (e: IllegalStateException) {
            Assert.assertTrue(e.message?.contains("Model not loaded") == true)
        }
    }

    companion object {
        private const val MODEL_INPUT_SIZE = 224 * 224 * 3 * 4 // 602112 bytes
    }
}
