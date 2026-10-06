package com.moukim.shjirati.domain

import com.moukim.shjirati.data.local.PlantCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PlantRecognitionEngineTest {

    @Test
    fun recognizes_apple_in_english_and_arabic() {
        val appleEnglish = PlantRecognitionEngine.recognize("apple")
        assertNotNull(appleEnglish)
        assertEquals("🍎", appleEnglish?.icon)
        assertEquals(PlantCategory.TREE, appleEnglish?.defaultCategory)

        val appleCapitalized = PlantRecognitionEngine.recognize("Apple")
        assertNotNull(appleCapitalized)
        assertEquals("🍎", appleCapitalized?.icon)

        val appleTree = PlantRecognitionEngine.recognize("apple tree")
        assertNotNull(appleTree)
        assertEquals("🍎", appleTree?.icon)

        val appleArabic = PlantRecognitionEngine.recognize("تفاح")
        assertNotNull(appleArabic)
        assertEquals("🍎", appleArabic?.icon)

        val appleArabicTaa = PlantRecognitionEngine.recognize("تفاحة")
        assertNotNull(appleArabicTaa)
        assertEquals("🍎", appleArabicTaa?.icon)

        val appleTreeArabic = PlantRecognitionEngine.recognize("شجرة تفاح")
        assertNotNull(appleTreeArabic)
        assertEquals("🍎", appleTreeArabic?.icon)
    }

    @Test
    fun recognizes_various_fruits_and_vegetables() {
        assertEquals("🍌", PlantRecognitionEngine.recognize("banana")?.icon)
        assertEquals("🍌", PlantRecognitionEngine.recognize("موز")?.icon)

        assertEquals("🍊", PlantRecognitionEngine.recognize("orange")?.icon)
        assertEquals("🍊", PlantRecognitionEngine.recognize("برتقال")?.icon)

        assertEquals("🍅", PlantRecognitionEngine.recognize("tomate")?.icon)
        assertEquals("🍅", PlantRecognitionEngine.recognize("طماطم")?.icon)

        assertEquals("🫒", PlantRecognitionEngine.recognize("olive")?.icon)
        assertEquals("🫒", PlantRecognitionEngine.recognize("زيتون")?.icon)

        assertEquals("🥭", PlantRecognitionEngine.recognize("mango")?.icon)
        assertEquals("🥭", PlantRecognitionEngine.recognize("مانجو")?.icon)

        assertEquals("🍍", PlantRecognitionEngine.recognize("pineapple")?.icon)
        assertEquals("🍍", PlantRecognitionEngine.recognize("أناناس")?.icon)

        assertEquals("🌵", PlantRecognitionEngine.recognize("cactus")?.icon)
        assertEquals("🌵", PlantRecognitionEngine.recognize("صبار")?.icon)
    }

    @Test
    fun returns_null_for_unknown_plant() {
        val unknown = PlantRecognitionEngine.recognize("نبتة غريبة جداً 123")
        assertNull(unknown)
    }
}
