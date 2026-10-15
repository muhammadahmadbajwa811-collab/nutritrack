package com.nutritrack.app.domain.calc

import org.json.JSONArray
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SeedDatabaseIntegrityTest {

    @Test
    fun seedFoodsJson_has300PlusItemsAndAccurateData() {
        // Path to assets/foods_seed.json
        val seedFile = File("src/main/assets/foods_seed.json")
        val fallbackFile = File("app/src/main/assets/foods_seed.json")
        val file = if (seedFile.exists()) seedFile else fallbackFile

        assertTrue("Seed file foods_seed.json should exist", file.exists())
        val jsonText = file.readText(Charsets.UTF_8)
        val jsonArray = JSONArray(jsonText)

        // 1. Must contain 300+ foods
        assertTrue("Seed database must contain 300+ foods, but has ${jsonArray.length()}", jsonArray.length() >= 300)

        // 2. Verify all 70 baseline items from web app exist
        val baselineNames = listOf(
            "Apple", "Banana", "Orange", "Mango", "Watermelon", "Grapes", "Strawberries", "Dates",
            "Broccoli", "Carrot", "Cucumber", "Tomato", "Spinach (cooked)", "Boiled potato", "Sweet corn",
            "White rice (cooked)", "Brown rice (cooked)", "Roti / Chapati", "Naan", "Oatmeal (cooked)",
            "Whole wheat bread", "Pasta (cooked)", "Cornflakes", "Boiled egg", "Chicken breast (grilled)",
            "Beef steak (grilled)", "Salmon", "Tuna (canned)", "Lentils / Daal (cooked)", "Chickpeas (cooked)",
            "Almonds", "Peanut butter", "Whole milk", "Plain yogurt", "Cheddar cheese", "Paneer", "Butter",
            "Potato chips", "Dark chocolate", "Biscuits", "Popcorn", "Vanilla ice cream", "Granola bar",
            "Green tea", "Milk tea (chai)", "Black coffee", "Orange juice", "Cola", "Sweet lassi",
            "Sugarcane juice", "Protein shake", "Chicken biryani", "Chicken karahi", "Nihari", "Daal chawal",
            "Aloo paratha", "Plain paratha", "Samosa", "Seekh kabab", "Chicken tikka", "Chana chaat",
            "Halwa puri", "Gulab jamun", "Kheer", "Chicken shawarma", "Beef burger", "Pizza slice",
            "French fries", "Fried chicken", "Club sandwich"
        )

        // Collect names and aliases
        val names = mutableSetOf<String>()
        var desiFoodsWithUrduCount = 0

        for (i in 0 until jsonArray.length()) {
            val item = jsonArray.getJSONObject(i)
            val name = item.getString("name")
            names.add(name)

            // Validate non-negative numbers
            assertTrue("Calories must be >= 0 for $name", item.getDouble("calories") >= 0.0)
            assertTrue("Protein must be >= 0 for $name", item.getDouble("protein") >= 0.0)
            assertTrue("Carbs must be >= 0 for $name", item.getDouble("carbs") >= 0.0)
            assertTrue("Fat must be >= 0 for $name", item.getDouble("fat") >= 0.0)

            // Validate aliases
            val aliases = item.optJSONArray("aliases")
            assertNotNull("Aliases must exist for $name", aliases)
            if (aliases != null && aliases.length() > 0) {
                for (j in 0 until aliases.length()) {
                    val alias = aliases.getString(j)
                    // Check if alias contains Urdu unicode block (0600 - 06FF)
                    if (alias.any { c -> c in '\u0600'..'\u06FF' }) {
                        desiFoodsWithUrduCount++
                        break
                    }
                }
            }
        }

        for (baseline in baselineNames) {
            assertTrue("Baseline food '$baseline' must be present in seed database", names.contains(baseline))
        }

        // 3. Must contain at least 70 desi dishes with Urdu aliases
        assertTrue(
            "Expected at least 70 desi foods with authentic Urdu script aliases, found $desiFoodsWithUrduCount",
            desiFoodsWithUrduCount >= 70
        )
    }
}
