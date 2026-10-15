package com.nutritrack.app.data.mapper

import com.nutritrack.app.core.database.entity.AchievementEntity
import com.nutritrack.app.core.database.entity.DiaryEntryEntity
import com.nutritrack.app.core.database.entity.FoodEntity
import com.nutritrack.app.core.database.entity.ServingOptionEntity
import com.nutritrack.app.core.database.entity.StepDayEntity
import com.nutritrack.app.core.database.entity.UserProfileEntity
import com.nutritrack.app.core.database.entity.WaterLogEntity
import com.nutritrack.app.core.database.entity.WeightLogEntity
import com.nutritrack.app.core.database.entity.WorkoutEntity
import com.nutritrack.app.domain.model.Achievement
import com.nutritrack.app.domain.model.ActivityLevel
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.FoodSource
import com.nutritrack.app.domain.model.Gender
import com.nutritrack.app.domain.model.GoalType
import com.nutritrack.app.domain.model.MacroPreset
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.domain.model.ServingOption
import com.nutritrack.app.domain.model.StepDaySummary
import com.nutritrack.app.domain.model.StepSource
import com.nutritrack.app.domain.model.UserProfile
import com.nutritrack.app.domain.model.WaterEntry
import com.nutritrack.app.domain.model.WeightEntry
import com.nutritrack.app.domain.model.WorkoutEntry
import com.nutritrack.app.domain.model.WorkoutIntensity

// UserProfile Mappers
fun UserProfileEntity.toDomainModel(): UserProfile {
    val parsedGender = try { Gender.valueOf(gender) } catch (e: Exception) { Gender.MALE }
    val parsedActivity = try { ActivityLevel.valueOf(activityLevel) } catch (e: Exception) { ActivityLevel.MODERATELY_ACTIVE }
    val parsedGoal = try { GoalType.valueOf(goalType) } catch (e: Exception) { GoalType.LOSE_WEIGHT }
    val parsedMacro = try { MacroPreset.valueOf(macroPreset) } catch (e: Exception) { MacroPreset.BALANCED }

    val currentYear = java.time.Year.now().value
    val computedAge = maxOf(1, currentYear - birthYear)

    return UserProfile(
        id = id,
        name = name,
        gender = parsedGender,
        birthYear = birthYear,
        age = computedAge,
        heightCm = heightCm,
        weightKg = weightKg,
        goalWeightKg = goalWeightKg,
        activityLevel = parsedActivity,
        goal = parsedGoal,
        paceKgPerWeek = paceKgPerWeek,
        calorieGoal = calorieGoal,
        macroPreset = parsedMacro,
        proteinGoalG = proteinGoalG,
        carbsGoalG = carbsGoalG,
        fatGoalG = fatGoalG,
        waterGoalMl = waterGoalMl,
        stepGoal = stepGoal,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        id = id,
        name = name,
        gender = gender.name,
        birthYear = birthYear,
        heightCm = heightCm,
        weightKg = weightKg,
        goalWeightKg = goalWeightKg,
        activityLevel = activityLevel.name,
        goalType = goal.name,
        paceKgPerWeek = paceKgPerWeek,
        calorieGoal = calorieGoal,
        macroPreset = macroPreset.name,
        proteinGoalG = proteinGoalG,
        carbsGoalG = carbsGoalG,
        fatGoalG = fatGoalG,
        waterGoalMl = waterGoalMl,
        stepGoal = stepGoal,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// FoodItem & ServingOption Mappers
fun FoodEntity.toDomainModel(servingOptions: List<ServingOptionEntity> = emptyList()): FoodItem {
    val aliasesList = if (aliases.isBlank()) emptyList() else aliases.split(",").map { it.trim() }
    val foodSource = try { FoodSource.valueOf(source) } catch (e: Exception) { FoodSource.SEED }

    return FoodItem(
        id = id,
        name = name,
        aliases = aliasesList,
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
        barcode = barcode,
        source = foodSource,
        isFavorite = isFavorite,
        lastUsedAt = lastUsedAt,
        isDeleted = isDeleted,
        createdAt = createdAt,
        servingOptions = servingOptions.map { it.toDomainModel() }
    )
}

fun FoodItem.toEntity(): FoodEntity {
    return FoodEntity(
        id = id,
        name = name,
        aliases = aliases.joinToString(", "),
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
        barcode = barcode,
        source = source.name,
        isFavorite = isFavorite,
        lastUsedAt = lastUsedAt,
        isDeleted = isDeleted,
        createdAt = createdAt
    )
}

fun ServingOptionEntity.toDomainModel(): ServingOption {
    return ServingOption(
        id = id,
        foodId = foodId,
        label = label,
        multiplier = multiplier
    )
}

fun ServingOption.toEntity(): ServingOptionEntity {
    return ServingOptionEntity(
        id = id,
        foodId = foodId,
        label = label,
        multiplier = multiplier
    )
}

// DiaryEntry Mappers
fun DiaryEntryEntity.toDomainModel(): DiaryEntry {
    val mealType = try { MealType.valueOf(meal) } catch (e: Exception) { MealType.LUNCH }
    return DiaryEntry(
        id = id,
        date = date,
        meal = mealType,
        foodId = foodId,
        nameSnapshot = nameSnapshot,
        emojiSnapshot = emojiSnapshot,
        servingLabelSnapshot = servingLabelSnapshot,
        quantity = quantity,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        sugar = sugar,
        sodiumMg = sodiumMg,
        isQuickAdd = isQuickAdd,
        createdAt = createdAt
    )
}

fun DiaryEntry.toEntity(): DiaryEntryEntity {
    return DiaryEntryEntity(
        id = id,
        date = date,
        meal = meal.name,
        foodId = foodId,
        nameSnapshot = nameSnapshot,
        emojiSnapshot = emojiSnapshot,
        servingLabelSnapshot = servingLabelSnapshot,
        quantity = quantity,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        fiber = fiber,
        sugar = sugar,
        sodiumMg = sodiumMg,
        isQuickAdd = isQuickAdd,
        createdAt = createdAt
    )
}

// Water Mappers
fun WaterLogEntity.toDomainModel(): WaterEntry {
    return WaterEntry(
        id = id,
        date = date,
        timestamp = timestamp,
        amountMl = amountMl
    )
}

fun WaterEntry.toEntity(): WaterLogEntity {
    return WaterLogEntity(
        id = id,
        date = date,
        timestamp = timestamp,
        amountMl = amountMl
    )
}

// Step Mappers
fun StepDayEntity.toDomainModel(): StepDaySummary {
    val stepSource = try { StepSource.valueOf(source) } catch (e: Exception) { StepSource.AUTO }
    return StepDaySummary(
        date = date,
        sensorSteps = sensorSteps,
        healthConnectSteps = healthConnectSteps,
        manualSteps = manualSteps,
        source = stepSource,
        updatedAt = updatedAt
    )
}

fun StepDaySummary.toEntity(): StepDayEntity {
    return StepDayEntity(
        date = date,
        sensorSteps = sensorSteps,
        healthConnectSteps = healthConnectSteps,
        manualSteps = manualSteps,
        source = source.name,
        updatedAt = updatedAt
    )
}

// Workout Mappers
fun WorkoutEntity.toDomainModel(): WorkoutEntry {
    val workoutIntensity = try { WorkoutIntensity.valueOf(intensity) } catch (e: Exception) { WorkoutIntensity.MODERATE }
    return WorkoutEntry(
        id = id,
        date = date,
        startTime = startTime,
        activityKey = activityKey,
        name = name,
        minutes = minutes,
        intensity = workoutIntensity,
        met = met,
        calories = calories,
        note = note,
        createdAt = createdAt
    )
}

fun WorkoutEntry.toEntity(): WorkoutEntity {
    return WorkoutEntity(
        id = id,
        date = date,
        startTime = startTime,
        activityKey = activityKey,
        name = name,
        minutes = minutes,
        intensity = intensity.name,
        met = met,
        calories = calories,
        note = note,
        createdAt = createdAt
    )
}

// Weight Mappers
fun WeightLogEntity.toDomainModel(): WeightEntry {
    return WeightEntry(
        id = id,
        date = date,
        weightKg = weightKg,
        note = note,
        createdAt = createdAt
    )
}

fun WeightEntry.toEntity(): WeightLogEntity {
    return WeightLogEntity(
        id = id,
        date = date,
        weightKg = weightKg,
        note = note,
        createdAt = createdAt
    )
}

// Achievement Mappers
fun AchievementEntity.toDomainModel(title: String = key, description: String = "", maxProgress: Int = 1): Achievement {
    return Achievement(
        key = key,
        title = title,
        description = description,
        unlockedAt = unlockedAt,
        progress = progress,
        maxProgress = maxProgress
    )
}

fun Achievement.toEntity(): AchievementEntity {
    return AchievementEntity(
        key = key,
        unlockedAt = unlockedAt,
        progress = progress
    )
}
