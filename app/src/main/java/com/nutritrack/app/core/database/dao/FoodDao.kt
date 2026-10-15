package com.nutritrack.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.nutritrack.app.core.database.entity.FoodEntity
import com.nutritrack.app.core.database.entity.ServingOptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE isDeleted = 0 ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE id = :id AND isDeleted = 0 LIMIT 1")
    fun getFoodById(id: String): Flow<FoodEntity?>

    @Query("SELECT * FROM foods WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getFoodByIdSync(id: String): FoodEntity?

    @Query("""
        SELECT * FROM foods 
        WHERE (name LIKE '%' || :query || '%' OR aliases LIKE '%' || :query || '%') 
        AND isDeleted = 0 
        ORDER BY 
            CASE 
                WHEN name LIKE :query || '%' THEN 1 
                WHEN aliases LIKE '%' || :query || '%' THEN 2 
                ELSE 3 
            END, 
            name ASC
    """)
    fun searchFoods(query: String): Flow<List<FoodEntity>>

    @Query("""
        SELECT foods.* FROM foods 
        JOIN foods_fts ON foods.name = foods_fts.name
        WHERE foods_fts MATCH :query AND foods.isDeleted = 0
    """)
    fun searchFoodsFts(query: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category AND isDeleted = 0 ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE isFavorite = 1 AND isDeleted = 0 ORDER BY name ASC")
    fun getFavoriteFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE lastUsedAt IS NOT NULL AND isDeleted = 0 ORDER BY lastUsedAt DESC LIMIT :limit")
    fun getRecentFoods(limit: Int = 20): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE source = 'USER' AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getUserCustomFoods(): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoods(foods: List<FoodEntity>)

    @Query("UPDATE foods SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE foods SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long)

    @Query("UPDATE foods SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteFood(id: String)

    @Query("SELECT COUNT(*) FROM foods WHERE isDeleted = 0")
    suspend fun getFoodCount(): Int

    // Serving Options
    @Query("SELECT * FROM serving_options WHERE foodId = :foodId")
    fun getServingOptions(foodId: String): Flow<List<ServingOptionEntity>>

    @Query("SELECT * FROM serving_options WHERE foodId = :foodId")
    suspend fun getServingOptionsSync(foodId: String): List<ServingOptionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServingOptions(options: List<ServingOptionEntity>)

    @Query("DELETE FROM serving_options WHERE foodId = :foodId")
    suspend fun deleteServingOptionsForFood(foodId: String)

    @Transaction
    suspend fun insertFoodWithOptions(food: FoodEntity, options: List<ServingOptionEntity>) {
        insertFood(food)
        if (options.isNotEmpty()) {
            insertServingOptions(options)
        }
    }
}
