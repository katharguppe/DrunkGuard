package com.traffic.drunkguard.beta

/**
 * Categories for beta testing images.
 * Used by BetaMockProvider to load test images from assets/beta/ folders.
 */
enum class BetaCategory {
    SOBER,
    SLIGHTLY,
    MODERATELY,
    HEAVILY,
    VEHICLES;

    /**
     * Returns the folder name in assets/beta/ for this category
     */
    fun folderName(): String = name.lowercase()

    companion object {
        /**
         * Get all intoxication-related categories (excludes VEHICLES)
         */
        fun intoxicationCategories(): List<BetaCategory> =
            listOf(SOBER, SLIGHTLY, MODERATELY, HEAVILY)
    }
}
