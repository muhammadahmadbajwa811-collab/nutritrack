package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.ServingOption
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<FoodItem>>
    fun getFoodById(id: String): Flow<FoodItem?>
    suspend fun getFoodByIdSync(id: String): FoodItem?
    fun searchFoods(query: String): Flow<List<FoodItem>>
    fun getFoodsByCategory(category: String): Flow<List<FoodItem>>
    fun getFavoriteFoods(): Flow<List<FoodItem>>
    fun getRecentFoods(limit: Int = 20): Flow<List<FoodItem>>
    fun getUserCustomFoods(): Flow<List<FoodItem>>
    suspend fun saveFood(food: FoodItem)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun markFoodUsed(id: String)
    suspend fun deleteFood(id: String)
    fun getServingOptions(foodId: String): Flow<List<ServingOption>>
}
