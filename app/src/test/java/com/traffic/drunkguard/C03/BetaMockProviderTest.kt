package com.traffic.drunkguard.C03

import android.content.Context
import android.content.res.AssetManager
import com.traffic.drunkguard.beta.BetaCategory
import com.traffic.drunkguard.utils.BetaMockProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
class BetaMockProviderTest {

    private lateinit var context: Context
    private lateinit var betaMockProvider: BetaMockProvider

    private val mockJson = """
    {
      "officer": {
        "badgeId": "KA2301",
        "name": "Ravi Kumar",
        "station": "Koramangala Traffic PS",
        "password": "test1234"
      },
      "vehicle": {
        "plate": "KA03MJ2247",
        "type": "Car",
        "color": "White",
        "make": "Maruti Swift"
      },
      "location": {
        "latitude": 12.9352,
        "longitude": 77.6245,
        "address": "100 Feet Road, Koramangala, Bengaluru"
      }
    }
    """.trimIndent()

    @Before
    fun setup() {
        context = RuntimeEnvironment.getApplication()
        betaMockProvider = BetaMockProvider(context)
    }

    @Test
    fun `BetaCategory enum has all required values`() {
        val categories = BetaCategory.values()
        assertEquals(5, categories.size)
        assertTrue(categories.contains(BetaCategory.SOBER))
        assertTrue(categories.contains(BetaCategory.SLIGHTLY))
        assertTrue(categories.contains(BetaCategory.MODERATELY))
        assertTrue(categories.contains(BetaCategory.HEAVILY))
        assertTrue(categories.contains(BetaCategory.VEHICLES))
    }

    @Test
    fun `BetaCategory folderName returns lowercase`() {
        assertEquals("sober", BetaCategory.SOBER.folderName())
        assertEquals("slightly", BetaCategory.SLIGHTLY.folderName())
        assertEquals("moderately", BetaCategory.MODERATELY.folderName())
        assertEquals("heavily", BetaCategory.HEAVILY.folderName())
        assertEquals("vehicles", BetaCategory.VEHICLES.folderName())
    }

    @Test
    fun `BetaCategory intoxicationCategories excludes vehicles`() {
        val categories = BetaCategory.intoxicationCategories()
        assertEquals(4, categories.size)
        assertTrue(categories.contains(BetaCategory.SOBER))
        assertTrue(categories.contains(BetaCategory.SLIGHTLY))
        assertTrue(categories.contains(BetaCategory.MODERATELY))
        assertTrue(categories.contains(BetaCategory.HEAVILY))
        assertFalse(categories.contains(BetaCategory.VEHICLES))
    }

    @Test
    fun `getMockOfficer parses JSON correctly`() {
        // This test requires the actual mock_data.json file in assets
        // For unit test, we verify the structure works
        val mockContext = mockk<Context>(relaxed = true)
        val mockAssets = mockk<AssetManager>(relaxed = true)

        every { mockContext.assets } returns mockAssets
        every { mockContext.applicationContext } returns mockContext
        every { mockAssets.open("mock_data.json") } returns ByteArrayInputStream(mockJson.toByteArray())

        val provider = BetaMockProvider(mockContext)
        val officer = provider.getMockOfficer()

        assertNotNull(officer)
        assertEquals("KA2301", officer?.badgeId)
        assertEquals("Ravi Kumar", officer?.name)
        assertEquals("Koramangala Traffic PS", officer?.station)
        assertEquals("mock_officer_001", officer?.id)
    }

    @Test
    fun `getMockVehicle parses JSON correctly`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAssets = mockk<AssetManager>(relaxed = true)

        every { mockContext.assets } returns mockAssets
        every { mockContext.applicationContext } returns mockContext
        every { mockAssets.open("mock_data.json") } returns ByteArrayInputStream(mockJson.toByteArray())

        val provider = BetaMockProvider(mockContext)
        val vehicle = provider.getMockVehicle()

        assertNotNull(vehicle)
        assertEquals("KA03MJ2247", vehicle?.plate)
        assertEquals("Car", vehicle?.type)
        assertEquals("White", vehicle?.color)
        assertEquals("Maruti Swift", vehicle?.make)
    }

    @Test
    fun `getMockLocation parses JSON correctly`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAssets = mockk<AssetManager>(relaxed = true)

        every { mockContext.assets } returns mockAssets
        every { mockContext.applicationContext } returns mockContext
        every { mockAssets.open("mock_data.json") } returns ByteArrayInputStream(mockJson.toByteArray())

        val provider = BetaMockProvider(mockContext)
        val location = provider.getMockLocation()

        assertNotNull(location)
        assertEquals(12.9352, location?.latitude ?: 0.0, 0.0001)
        assertEquals(77.6245, location?.longitude ?: 0.0, 0.0001)
        assertEquals("100 Feet Road, Koramangala, Bengaluru", location?.address)
    }

    @Test
    fun `BetaMockProvider singleton returns same instance`() {
        mockkStatic(BetaMockProvider::class)

        val instance1 = BetaMockProvider.getInstance(context)
        val instance2 = BetaMockProvider.getInstance(context)

        assertSame(instance1, instance2)
    }
}
