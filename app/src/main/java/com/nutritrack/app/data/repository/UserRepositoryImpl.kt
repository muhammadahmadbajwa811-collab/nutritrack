package com.nutritrack.app.data.repository

import com.nutritrack.app.core.database.dao.UserProfileDao
import com.nutritrack.app.data.mapper.toDomainModel
import com.nutritrack.app.data.mapper.toEntity
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao
) : UserRepository {

    override fun getUserProfile(): Flow<UserProfile?> {
        return userProfileDao.getProfile().map { it?.toDomainModel() }
    }

    override suspend fun getUserProfileSync(): UserProfile? {
        return userProfileDao.getProfileSync()?.toDomainModel()
    }

    override suspend fun saveUserProfile(profile: UserProfile) {
        userProfileDao.insertOrUpdateProfile(profile.toEntity())
    }

    override suspend fun clearUserProfile() {
        userProfileDao.clearProfile()
    }
}
