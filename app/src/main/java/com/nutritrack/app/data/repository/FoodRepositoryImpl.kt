package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.FoodDao
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.data.mapper.toEntity
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.ServingOption
import com.nutritrack.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {

    override fun getAllFoods(): Flow<List<FoodItem>> {
        return foodDao.getAllFoods().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFoodById(id: String): Flow<FoodItem?> {
        return foodDao.getFoodById(id).map { it?.toDomainModel() }
    }

    override suspend fun getFoodByIdSync(id: String): FoodItem? {
        val entity = foodDao.getFoodByIdSync(id) ?: return null
        val options = foodDao.getServingOptionsSync(id)
        return entity.toDomainModel(options)
    }

    override fun searchFoods(query: String): Flow<List<FoodItem>> {
        return foodDao.searchFoods(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFoodsByCategory(category: String): Flow<List<FoodItem>> {
        return foodDao.getFoodsByCategory(category).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getFavoriteFoods(): Flow<List<FoodItem>> {
        return foodDao.getFavoriteFoods().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getRecentFoods(limit: Int): Flow<List<FoodItem>> {
        return foodDao.getRecentFoods(limit).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getUserCustomFoods(): Flow<List<FoodItem>> {
        return foodDao.getUserCustomFoods().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun saveFood(food: FoodItem) {
        val entity = food.toEntity()
        val options = food.servingOptions.map { it.toEntity() }
        foodDao.insertFoodWithOptions(entity, options)
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        foodDao.updateFavorite(id, isFavorite)
    }

    override suspend fun markFoodUsed(id: String) {
        foodDao.updateLastUsed(id, System.currentTimeMillis())
    }

    override suspend fun deleteFood(id: String) {
        foodDao.softDeleteFood(id)
    }

    override fun getServingOptions(foodId: String): Flow<List<ServingOption>> {
        return foodDao.getServingOptions(foodId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}
