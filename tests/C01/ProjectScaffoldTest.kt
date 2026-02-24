package com.traffic.drunkguard

import org.junit.Test
import org.junit.Assert.*
import java.io.File

class ProjectScaffoldTest {

    private val projectRoot = File(".")

    @Test
    fun `gradle wrapper exists`() {
        assertTrue("gradle-wrapper.properties should exist",
            File(projectRoot, "gradle/wrapper/gradle-wrapper.properties").exists())
    }

    @Test
    fun `project build gradle exists`() {
        val buildGradle = File(projectRoot, "build.gradle")
        assertTrue("build.gradle should exist", buildGradle.exists())

        val content = buildGradle.readText()
        assertTrue("Should have Android plugin", content.contains("com.android.application"))
        assertTrue("Should have Hilt plugin", content.contains("hilt.android"))
        assertTrue("Should have KSP plugin", content.contains("devtools.ksp"))
    }

    @Test
    fun `app build gradle exists with all dependencies`() {
        val appBuildGradle = File(projectRoot, "app/build.gradle")
        assertTrue("app/build.gradle should exist", appBuildGradle.exists())

        val content = appBuildGradle.readText()

        // Core dependencies
        assertTrue("Should have Hilt", content.contains("hilt-android"))
        assertTrue("Should have Room", content.contains("room-runtime"))
        assertTrue("Should have CameraX", content.contains("camera-core"))
        assertTrue("Should have MLKit", content.contains("text-recognition"))
        assertTrue("Should have TFLite", content.contains("tensorflow-lite"))
        assertTrue("Should have iText7", content.contains("itext7-core"))
        assertTrue("Should have ZXing", content.contains("zxing"))
        assertTrue("Should have Coil", content.contains("coil-kt"))
        assertTrue("Should have Maps", content.contains("play-services-maps"))
        assertTrue("Should have Biometric", content.contains("biometric"))
    }

    @Test
    fun `android manifest exists with all permissions`() {
        val manifest = File(projectRoot, "app/src/main/AndroidManifest.xml")
        assertTrue("AndroidManifest.xml should exist", manifest.exists())

        val content = manifest.readText()

        // Permissions
        assertTrue("Should have CAMERA permission", content.contains("CAMERA"))
        assertTrue("Should have FINE_LOCATION permission", content.contains("ACCESS_FINE_LOCATION"))
        assertTrue("Should have COARSE_LOCATION permission", content.contains("ACCESS_COARSE_LOCATION"))
        assertTrue("Should have BIOMETRIC permission", content.contains("USE_BIOMETRIC"))
        assertTrue("Should have INTERNET permission", content.contains("INTERNET"))

        // Application components
        assertTrue("Should have DrunkGuardApp", content.contains("DrunkGuardApp"))
        assertTrue("Should have MainActivity", content.contains("MainActivity"))
        assertTrue("Should have FileProvider", content.contains("FileProvider"))
    }

    @Test
    fun `folder structure exists`() {
        val folders = listOf(
            "app/src/main/java/com/traffic/drunkguard/di",
            "app/src/main/java/com/traffic/drunkguard/data/db",
            "app/src/main/java/com/traffic/drunkguard/data/repository",
            "app/src/main/java/com/traffic/drunkguard/data/model",
            "app/src/main/java/com/traffic/drunkguard/domain/usecase",
            "app/src/main/java/com/traffic/drunkguard/ui/login",
            "app/src/main/java/com/traffic/drunkguard/ui/dashboard",
            "app/src/main/java/com/traffic/drunkguard/ui/newcheck",
            "app/src/main/java/com/traffic/drunkguard/ui/challan",
            "app/src/main/java/com/traffic/drunkguard/ui/records",
            "app/src/main/java/com/traffic/drunkguard/ui/settings",
            "app/src/main/java/com/traffic/drunkguard/ml",
            "app/src/main/java/com/traffic/drunkguard/utils",
            "app/src/main/java/com/traffic/drunkguard/beta",
            "app/src/main/res/layout",
            "app/src/main/res/values",
            "app/src/main/res/values-night",
            "app/src/main/res/drawable",
            "app/src/main/res/navigation",
            "app/src/main/res/xml",
            "app/src/main/assets",
            "app/src/test/java/com/traffic/drunkguard",
            "app/src/androidTest/java/com/traffic/drunkguard"
        )

        folders.forEach { folder ->
            assertTrue("Folder $folder should exist", File(projectRoot, folder).exists())
        }
    }

    @Test
    fun `hilt application class exists`() {
        val appClass = File(projectRoot, "app/src/main/java/com/traffic/drunkguard/DrunkGuardApp.kt")
        assertTrue("DrunkGuardApp.kt should exist", appClass.exists())

        val content = appClass.readText()
        assertTrue("Should have @HiltAndroidApp", content.contains("@HiltAndroidApp"))
    }

    @Test
    fun `main activity exists`() {
        val mainActivity = File(projectRoot, "app/src/main/java/com/traffic/drunkguard/ui/MainActivity.kt")
        assertTrue("MainActivity.kt should exist", mainActivity.exists())

        val content = mainActivity.readText()
        assertTrue("Should have @AndroidEntryPoint", content.contains("@AndroidEntryPoint"))
    }

    @Test
    fun `navigation graph exists`() {
        val navGraph = File(projectRoot, "app/src/main/res/navigation/nav_graph.xml")
        assertTrue("nav_graph.xml should exist", navGraph.exists())

        val content = navGraph.readText()
        assertTrue("Should have login destination", content.contains("loginFragment"))
        assertTrue("Should have dashboard destination", content.contains("dashboardFragment"))
    }

    @Test
    fun `strings xml exists with required strings`() {
        val strings = File(projectRoot, "app/src/main/res/values/strings.xml")
        assertTrue("strings.xml should exist", strings.exists())

        val content = strings.readText()
        assertTrue("Should have app_name", content.contains("DrunkGuard"))
        assertTrue("Should have login strings", content.contains("title_login"))
        assertTrue("Should have beta banner text", content.contains("beta_banner_text"))
    }

    @Test
    fun `mock data json exists`() {
        val mockData = File(projectRoot, "app/src/main/assets/mock_data.json")
        assertTrue("mock_data.json should exist", mockData.exists())

        val content = mockData.readText()
        assertTrue("Should have officer data", content.contains("badgeId"))
        assertTrue("Should have vehicle data", content.contains("plate"))
        assertTrue("Should have location data", content.contains("latitude"))
    }
}
