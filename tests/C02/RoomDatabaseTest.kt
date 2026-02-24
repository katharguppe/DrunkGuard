package com.traffic.drunkguard.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.traffic.drunkguard.data.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var database: DrunkGuardDatabase
    private lateinit var officerDao: OfficerDao
    private lateinit var vehicleCheckDao: VehicleCheckDao
    private lateinit var settingsDao: SettingsDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            DrunkGuardDatabase::class.java
        ).build()

        officerDao = database.officerDao()
        vehicleCheckDao = database.vehicleCheckDao()
        settingsDao = database.settingsDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun officerDao_insertAndRetrieve() = runBlocking {
        val officer = Officer(
            id = "officer-1",
            badgeId = "KA2301",
            name = "Ravi Kumar",
            station = "Koramangala Traffic PS",
            passwordHash = "hashed_password"
        )

        officerDao.insert(officer)
        val retrieved = officerDao.getByBadgeId("KA2301")

        assertNotNull(retrieved)
        assertEquals("Ravi Kumar", retrieved?.name)
        assertEquals("KA2301", retrieved?.badgeId)
    }

    @Test
    fun officerDao_getCurrentOfficerFlow() = runBlocking {
        val officer = Officer(
            id = "officer-1",
            badgeId = "KA2302",
            name = "Test Officer",
            station = "Test Station",
            passwordHash = "hash"
        )

        officerDao.insert(officer)
        val current = officerDao.getCurrentOfficer().first()

        assertNotNull(current)
        assertEquals("Test Officer", current?.name)
    }

    @Test
    fun vehicleCheckDao_insertAndRetrieve() = runBlocking {
        val check = VehicleCheck(
            id = UUID.randomUUID().toString(),
            officerId = "officer-1",
            latitude = 12.9352,
            longitude = 77.6245,
            address = "Test Address",
            plateNumber = "KA03MJ2247",
            vehicleType = "Car",
            vehicleColor = "White",
            vehicleMake = "Maruti Swift",
            subjectPhotoPath = "/path/to/photo.jpg",
            intoxicationLevel = IntoxicationLevel.MODERATELY,
            confidenceScore = 0.85f,
            challanId = null,
            pdfPath = null,
            whatsappSent = false,
            isMockData = true
        )

        vehicleCheckDao.insert(check)
        val allChecks = vehicleCheckDao.getAll().first()

        assertEquals(1, allChecks.size)
        assertEquals(IntoxicationLevel.MODERATELY, allChecks[0].intoxicationLevel)
        assertEquals(0.85f, allChecks[0].confidenceScore)
    }

    @Test
    fun vehicleCheckDao_getByOfficer() = runBlocking {
        val check1 = VehicleCheck(
            id = UUID.randomUUID().toString(),
            officerId = "officer-1",
            latitude = 12.0,
            longitude = 77.0,
            address = "Addr1",
            plateNumber = "KA01",
            vehicleType = "Car",
            vehicleColor = "Red",
            vehicleMake = "Toyota",
            subjectPhotoPath = "/p1.jpg",
            intoxicationLevel = IntoxicationLevel.SOBER,
            confidenceScore = 0.9f
        )

        val check2 = VehicleCheck(
            id = UUID.randomUUID().toString(),
            officerId = "officer-2",
            latitude = 12.0,
            longitude = 77.0,
            address = "Addr2",
            plateNumber = "KA02",
            vehicleType = "Bike",
            vehicleColor = "Blue",
            vehicleMake = "Honda",
            subjectPhotoPath = "/p2.jpg",
            intoxicationLevel = IntoxicationLevel.SLIGHTLY,
            confidenceScore = 0.7f
        )

        vehicleCheckDao.insert(check1)
        vehicleCheckDao.insert(check2)

        val officer1Checks = vehicleCheckDao.getByOfficer("officer-1").first()

        assertEquals(1, officer1Checks.size)
        assertEquals("officer-1", officer1Checks[0].officerId)
    }

    @Test
    fun settingsDao_setAndGet() = runBlocking {
        val setting = AppSettings("beta_mode", "true")
        settingsDao.set(setting)

        val retrieved = settingsDao.get("beta_mode")

        assertNotNull(retrieved)
        assertEquals("true", retrieved?.value)
    }

    @Test
    fun settingsDao_updateExisting() = runBlocking {
        settingsDao.set(AppSettings("threshold", "0.5"))
        settingsDao.set(AppSettings("threshold", "0.75"))

        val retrieved = settingsDao.get("threshold")

        assertEquals("0.75", retrieved?.value)
    }

    @Test
    fun intoxicationLevelConverter() {
        val converter = Converters()

        assertEquals("SOBER", converter.fromIntoxicationLevel(IntoxicationLevel.SOBER))
        assertEquals("HEAVILY", converter.fromIntoxicationLevel(IntoxicationLevel.HEAVILY))

        assertEquals(IntoxicationLevel.SLIGHTLY, converter.toIntoxicationLevel("SLIGHTLY"))
        assertEquals(IntoxicationLevel.MODERATELY, converter.toIntoxicationLevel("MODERATELY"))
    }
}
