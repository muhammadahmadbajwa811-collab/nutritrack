package com.nutritrack.app.core.database.seed

import android.content.Context
import com.nutritrack.app.core.database.NutriTrackDatabase
import com.nutritrack.app.core.database.entity.AppMetaEntity
import com.nutritrack.app.core.database.entity.FoodEntity
import com.nutritrack.app.core.database.entity.ServingOptionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

object SeedDataLoader {

    const val SEED_VERSION_KEY = "seed_version_applied"
    const val CURRENT_SEED_VERSION = "1"

    /**
     * Loads pre-packaged foods and serving options from assets/foods_seed.json into Room.
     * Runs asynchronously on Dispatchers.IO.
     * Guarantees idempotent execution via AppMetaEntity.
     */
    suspend fun loadSeedFoodsIfNeeded(context: Context, database: NutriTrackDatabase) = withContext(Dispatchers.IO) {
        val currentAppliedVersion = database.appMetaDao().getMeta(SEED_VERSION_KEY)
        if (currentAppliedVersion == CURRENT_SEED_VERSION) {
            return@withContext
        }

        try {
            val jsonString = context.assets.open("foods_seed.json").use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
            }

            val jsonArray = JSONArray(jsonString)
            val foodEntities = ArrayList<FoodEntity>(jsonArray.length())
            val servingOptionEntities = ArrayList<ServingOptionEntity>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)
                val foodId = item.getString("id")
                val name = item.getString("name")

                // Extract aliases array into comma-separated text for SQLite / FTS search
                val aliasesArray = item.optJSONArray("aliases")
                val aliasesList = mutableListOf<String>()
                if (aliasesArray != null) {
                    for (j in 0 until aliasesArray.length()) {
                        aliasesList.add(aliasesArray.getString(j))
                    }
                }
                val aliasesText = aliasesList.joinToString(", ")

                val category = item.getString("category")
                val emoji = item.getString("emoji")
                val servingLabel = item.getString("servingLabel")
                val servingGrams = if (item.has("servingGrams") && !item.isNull("servingGrams")) item.getDouble("servingGrams") else null
                val calories = item.getDouble("calories")
                val protein = item.getDouble("protein")
                val carbs = item.getDouble("carbs")
                val fat = item.getDouble("fat")
                val fiber = if (item.has("fiber") && !item.isNull("fiber")) item.getDouble("fiber") else null
                val sugar = if (item.has("sugar") && !item.isNull("sugar")) item.getDouble("sugar") else null
                val sodiumMg = if (item.has("sodiumMg") && !item.isNull("sodiumMg")) item.getDouble("sodiumMg") else null
                val source = item.optString("source", "SEED")

                foodEntities.add(
                    FoodEntity(
                        id = foodId,
                        name = name,
                        aliases = aliasesText,
                        category = category,
                        emoji = emoji,
                        servingLabel = servingLabel,
                        servingGrams = servingGrams,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        fiber = fiber,
                        sugar = sugar,
                        sodiumMg = sodiumMg,
                        barcode = null,
                        source = source,
                        isFavorite = false,
                        lastUsedAt = null,
                        isDeleted = false,
                        createdAt = System.currentTimeMillis()
                    )
                )

                val optionsArray = item.optJSONArray("servingOptions")
                if (optionsArray != null) {
                    for (k in 0 until optionsArray.length()) {
                        val opt = optionsArray.getJSONObject(k)
                        servingOptionEntities.add(
                            ServingOptionEntity(
                                id = opt.getString("id"),
                                foodId = foodId,
                                label = opt.getString("label"),
                                multiplier = opt.getDouble("multiplier")
                            )
                        )
                    }
                }
            }

            database.foodDao().insertFoods(foodEntities)
            if (servingOptionEntities.isNotEmpty()) {
                database.foodDao().insertServingOptions(servingOptionEntities)
            }

            database.appMetaDao().setMeta(AppMetaEntity(SEED_VERSION_KEY, CURRENT_SEED_VERSION))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
