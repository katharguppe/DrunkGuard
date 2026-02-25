package com.traffic.drunkguard.ml

import com.traffic.drunkguard.data.model.IntoxicationLevel

/**
 * Result data class for TFLite inference
 *
 * @property level The detected intoxication level (highest probability)
 * @property confidence The confidence score for the detected level (0.0 - 1.0)
 * @property probabilities Raw probabilities for all 4 classes [sober, slightly, moderately, heavily]
 */
data class InferenceResult(
    val level: IntoxicationLevel,
    val confidence: Float,
    val probabilities: FloatArray
) {
    /**
     * Returns true if the confidence meets or exceeds the threshold
     */
    fun isConfident(threshold: Float = 0.65f): Boolean {
        return confidence >= threshold
    }

    /**
     * Get probability for a specific intoxication level
     */
    fun getProbabilityForLevel(level: IntoxicationLevel): Float {
        return probabilities.getOrElse(level.ordinal) { 0f }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InferenceResult

        if (level != other.level) return false
        if (confidence != other.confidence) return false
        if (!probabilities.contentEquals(other.probabilities)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = level.hashCode()
        result = 31 * result + confidence.hashCode()
        result = 31 * result + probabilities.contentHashCode()
        return result
    }

    companion object {
        /**
         * Creates an InferenceResult from raw probabilities
         */
        fun fromProbabilities(probabilities: FloatArray): InferenceResult {
            require(probabilities.size == 4) { "Expected 4 class probabilities, got ${probabilities.size}" }

            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val level = IntoxicationLevel.values()[maxIndex]
            val confidence = probabilities[maxIndex]

            return InferenceResult(level, confidence, probabilities)
        }
    }
}
