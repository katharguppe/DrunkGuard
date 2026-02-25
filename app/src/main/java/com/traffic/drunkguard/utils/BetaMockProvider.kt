package com.traffic.drunkguard.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.traffic.drunkguard.beta.BetaCategory
import com.traffic.drunkguard.data.model.Officer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Data class for mock vehicle information from mock_data.json
 */
data class MockVehicle(
    val plate: String,
    val type: String,
    val color: String,
    val make: String
)

/**
 * Data class for mock location information from mock_data.json
 */
data class MockLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String
)

/**
 * Provider for mock data during beta testing.
 * Reads mock_data.json from assets and provides test images from beta folders.
 */
class BetaMockProvider(private val context: Context) {

    private var mockData: JSONObject? = null

    init {
        loadMockData()
    }

    /**
     * Load and parse mock_data.json from assets
     */
    private fun loadMockData() {
        try {
            val jsonString = context.assets.open("mock_data.json").bufferedReader().use { it.readText() }
            mockData = JSONObject(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            mockData = null
        }
    }

    /**
     * Get mock officer data from mock_data.json
     * Returns null if parsing fails
     */
    fun getMockOfficer(): Officer? {
        return try {
            val officerJson = mockData?.getJSONObject("officer") ?: return null
            Officer(
                id = "mock_officer_001",
                badgeId = officerJson.getString("badgeId"),
                name = officerJson.getString("name"),
                station = officerJson.getString("station"),
                passwordHash = hashPassword(officerJson.getString("password")),
                createdAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get mock vehicle data from mock_data.json
     * Returns null if parsing fails
     */
    fun getMockVehicle(): MockVehicle? {
        return try {
            val vehicleJson = mockData?.getJSONObject("vehicle") ?: return null
            MockVehicle(
                plate = vehicleJson.getString("plate"),
                type = vehicleJson.getString("type"),
                color = vehicleJson.getString("color"),
                make = vehicleJson.getString("make")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Get mock location data from mock_data.json
     * Returns null if parsing fails
     */
    fun getMockLocation(): MockLocation? {
        return try {
            val locationJson = mockData?.getJSONObject("location") ?: return null
            MockLocation(
                latitude = locationJson.getDouble("latitude"),
                longitude = locationJson.getDouble("longitude"),
                address = locationJson.getString("address")
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * List all image file names in a beta category folder
     * @param category The beta category to list images from
     * @return List of file names (not full paths)
     */
    fun listBetaImageNames(category: BetaCategory): List<String> {
        return try {
            val folderPath = "beta/${category.folderName()}"
            context.assets.list(folderPath)?.toList()
                ?.filter { it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Load bitmaps from a beta category folder
     * @param category The beta category to load images from
     * @return List of decoded Bitmaps
     */
    suspend fun listBetaImages(category: BetaCategory): List<Bitmap> = withContext(Dispatchers.IO) {
        val imageNames = listBetaImageNames(category)
        imageNames.mapNotNull { loadBitmapFromAssets("beta/${category.folderName()}/$it") }
    }

    /**
     * Load a single bitmap from assets path
     * @param assetPath Path within assets (e.g., "beta/sober/test.jpg")
     * @return Decoded Bitmap or null if loading fails
     */
    fun loadBitmapFromAssets(assetPath: String): Bitmap? {
        return try {
            context.assets.open(assetPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Load a random bitmap from a beta category
     * @param category The beta category to pick from
     * @return A random Bitmap from that category, or null if empty
     */
    suspend fun getRandomBetaImage(category: BetaCategory): Bitmap? = withContext(Dispatchers.IO) {
        val images = listBetaImages(category)
        if (images.isNotEmpty()) images.random() else null
    }

    /**
     * Check if beta images exist for a category
     * @param category The beta category to check
     * @return true if images exist in the folder
     */
    fun hasBetaImages(category: BetaCategory): Boolean {
        return listBetaImageNames(category).isNotEmpty()
    }

    /**
     * Simple SHA-256 password hashing for mock data
     */
    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(bytes)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        @Volatile
        private var instance: BetaMockProvider? = null

        /**
         * Get singleton instance of BetaMockProvider
         */
        fun getInstance(context: Context): BetaMockProvider {
            return instance ?: synchronized(this) {
                instance ?: BetaMockProvider(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
