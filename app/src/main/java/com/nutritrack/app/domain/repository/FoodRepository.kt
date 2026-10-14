package com.nutritrack.app.domain.repository

import com.nutritrack.app.core.database.entity.FoodItemEntity
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<FoodItemEntity>>
    suspend fun getFoodById(id: String): FoodItemEntity?
    suspend fun searchFoods(query: String): List<FoodItemEntity>
    suspend fun saveFood(food: FoodItemEntity)
    suspend fun saveFoods(foods: List<FoodItemEntity>)
}
