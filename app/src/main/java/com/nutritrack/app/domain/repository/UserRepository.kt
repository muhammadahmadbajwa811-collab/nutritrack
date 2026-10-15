package com.nutritrack.app.domain.repository

import com.nutritrack.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(): Flow<UserProfile?>
    suspend fun getUserProfileSync(): UserProfile?
    suspend fun saveUserProfile(profile: UserProfile)
    suspend fun clearUserProfile()
}
