package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.FoodDao
import com.nutritrack.app.core.database.entity.FoodItemEntity
import com.nutritrack.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {

    override fun getAllFoods(): Flow<List<FoodItemEntity>> {
        return foodDao.getAllFoods()
    }

    override suspend fun getFoodById(id: String): FoodItemEntity? {
        return foodDao.getFoodById(id)
    }

    override suspend fun searchFoods(query: String): List<FoodItemEntity> {
        return foodDao.searchFoods(query)
    }

    override suspend fun saveFood(food: FoodItemEntity) {
        foodDao.insertFood(food)
    }

    override suspend fun saveFoods(foods: List<FoodItemEntity>) {
        foodDao.insertFoods(foods)
    }
}
