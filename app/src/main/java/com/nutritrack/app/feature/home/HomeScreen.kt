package com.nutritrack.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutritrack.app.R
import com.nutritrack.app.designsystem.components.CalorieRing
import com.nutritrack.app.designsystem.components.DateStrip
import com.nutritrack.app.designsystem.components.MacroProgress
import com.nutritrack.app.designsystem.components.StatTile
import com.nutritrack.app.designsystem.components.StreakChip
import com.nutritrack.app.designsystem.components.WaterGlass
import com.nutritrack.app.designsystem.components.WeekBarChart
import com.nutritrack.app.designsystem.theme.nutriTrackColors
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.feature.food.AddFoodBottomSheet
import com.nutritrack.app.feature.food.AddFoodViewModel
import com.nutritrack.app.feature.food.FoodDetailBottomSheet
import com.nutritrack.app.feature.food.FoodDetailViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    addFoodViewModel: AddFoodViewModel = hiltViewModel(),
    foodDetailViewModel: FoodDetailViewModel = hiltViewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToDiaryDate: (LocalDate) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom sheet states
    var showAddFoodSheet by remember { mutableStateOf(false) }
    var addFoodPreselectedMeal by remember { mutableStateOf(MealType.LUNCH) }
    var showFoodDetailSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoWaterAdd()
            }
            viewModel.clearSnackbar()
        }
    }

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.onRefresh()
        }
    }
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(pullRefreshState.nestedScrollConnection)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    // 1. Top Bar: Greeting, Streak Chip, Avatar
                    HomeTopBar(
                        greeting = uiState.greeting,
                        formattedDate = uiState.formattedDate,
                        streakDays = uiState.streakDays,
                        userName = uiState.userName,
                        onAvatarClick = onNavigateToSettings
                    )
                }

                // 2. Interactive Week Date Strip
                item {
                    DateStrip(
                        selectedDate = uiState.selectedDate,
                        onDateSelected = { viewModel.onDateSelected(it) },
                        today = uiState.today,
                        onTodayClick = { viewModel.onTodayClick() }
                    )
                }

                // 3. Calorie Hero Card with CalorieRing & 3 StatTiles
                item {
                    CalorieHeroCard(
                        eatenCalories = uiState.eatenCalories,
                        calorieGoal = uiState.calorieGoal,
                        burnedCalories = uiState.burnedCalories
                    )
                }

                // 4. Macronutrient Progress Bars
                item {
                    MacroCardsSection(
                        proteinEatenG = uiState.proteinEatenG,
                        proteinGoalG = uiState.proteinGoalG,
                        carbsEatenG = uiState.carbsEatenG,
                        carbsGoalG = uiState.carbsGoalG,
                        fatEatenG = uiState.fatEatenG,
                        fatGoalG = uiState.fatGoalG
                    )
                }

                // 5. Meals 2x2 Grid with "+" buttons
                item {
                    Text(
                        text = stringResource(id = R.string.meals),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                item {
                    Meals2x2Grid(
                        breakfastKcal = uiState.breakfastSummary.calories,
                        lunchKcal = uiState.lunchSummary.calories,
                        dinnerKcal = uiState.dinnerSummary.calories,
                        snacksKcal = uiState.snacksSummary.calories,
                        onAddFoodClick = { meal ->
                            addFoodPreselectedMeal = meal
                            addFoodViewModel.setInitialMeal(meal)
                            showAddFoodSheet = true
                        }
                    )
                }

                // 6. Water & Steps Cards Side by Side
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        WaterQuickCard(
                            waterMl = uiState.waterIntakeMl,
                            waterGoalMl = uiState.waterGoalMl,
                            onAddWater = { amount -> viewModel.addWater(amount) },
                            modifier = Modifier.weight(1f)
                        )

                        StepsQuickCard(
                            stepCount = uiState.stepCount,
                            stepGoal = uiState.stepGoal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 7. Last 7 Days Intake Chart
                if (uiState.weekCalories.size == 7 && uiState.weekDayLabels.size == 7) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(id = R.string.last_7_days),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                WeekBarChart(
                                    dailyValues = uiState.weekCalories,
                                    dayLabels = uiState.weekDayLabels,
                                    goalValue = uiState.calorieGoal,
                                    todayIndex = uiState.todayWeekIndex,
                                    onDaySelected = { index ->
                                        val weekDays = com.nutritrack.app.core.common.DateUtils.getWeekDays(uiState.selectedDate)
                                        if (index in weekDays.indices) {
                                            viewModel.onDateSelected(weekDays[index])
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }

    // Add Food Bottom Sheet
    if (showAddFoodSheet) {
        AddFoodBottomSheet(
            viewModel = addFoodViewModel,
            targetDate = uiState.selectedDate,
            onDismiss = { showAddFoodSheet = false },
            onSelectFood = { foodId, meal, date ->
                foodDetailViewModel.loadFood(foodId, meal, date)
                showFoodDetailSheet = true
            }
        )
    }

    // Food Detail Bottom Sheet
    if (showFoodDetailSheet) {
        FoodDetailBottomSheet(
            viewModel = foodDetailViewModel,
            onDismiss = { showFoodDetailSheet = false },
            onEntrySaved = {
                showFoodDetailSheet = false
                showAddFoodSheet = false
            }
        )
    }
}

@Composable
private fun HomeTopBar(
    greeting: String,
    formattedDate: String,
    streakDays: Int,
    userName: String,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StreakChip(
                streakDays = streakDays
            )

            // Avatar initial button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(onClick = onAvatarClick),
                contentAlignment = Alignment.Center
            ) {
                val initial = userName.firstOrNull()?.uppercaseChar()?.toString() ?: "U"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CalorieHeroCard(
    eatenCalories: Int,
    calorieGoal: Int,
    burnedCalories: Int
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 240dp CalorieRing
            CalorieRing(
                eatenCalories = eatenCalories,
                targetCalories = calorieGoal,
                burnedCalories = burnedCalories,
                diameter = 220.dp,
                strokeWidth = 16.dp
            )

            // 3 StatTiles: Eaten, Burned, Goal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatTile(
                    icon = Icons.Default.Restaurant,
                    value = "$eatenCalories",
                    label = stringResource(id = R.string.eaten),
                    unit = "kcal",
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "$burnedCalories",
                    label = stringResource(id = R.string.burned),
                    unit = "kcal",
                    iconTint = MaterialTheme.nutriTrackColors.warning,
                    modifier = Modifier.weight(1f)
                )

                StatTile(
                    icon = Icons.AutoMirrored.Filled.DirectionsRun,
                    value = "$calorieGoal",
                    label = stringResource(id = R.string.goal),
                    unit = "kcal",
                    iconTint = MaterialTheme.nutriTrackColors.protein,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MacroCardsSection(
    proteinEatenG: Int,
    proteinGoalG: Int,
    carbsEatenG: Int,
    carbsGoalG: Int,
    fatEatenG: Int,
    fatGoalG: Int
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            MacroProgress(
                macroName = stringResource(id = R.string.macro_protein),
                currentGrams = proteinEatenG,
                targetGrams = proteinGoalG,
                barColor = MaterialTheme.nutriTrackColors.protein
            )
            MacroProgress(
                macroName = stringResource(id = R.string.macro_carbs),
                currentGrams = carbsEatenG,
                targetGrams = carbsGoalG,
                barColor = MaterialTheme.nutriTrackColors.carbs
            )
            MacroProgress(
                macroName = stringResource(id = R.string.macro_fat),
                currentGrams = fatEatenG,
                targetGrams = fatGoalG,
                barColor = MaterialTheme.nutriTrackColors.fat
            )
        }
    }
}

@Composable
private fun Meals2x2Grid(
    breakfastKcal: Int,
    lunchKcal: Int,
    dinnerKcal: Int,
    snacksKcal: Int,
    onAddFoodClick: (MealType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MealTile(
                emoji = "🍳",
                name = stringResource(id = R.string.meal_breakfast),
                calories = breakfastKcal,
                onAddClick = { onAddFoodClick(MealType.BREAKFAST) },
                modifier = Modifier.weight(1f)
            )
            MealTile(
                emoji = "🥗",
                name = stringResource(id = R.string.meal_lunch),
                calories = lunchKcal,
                onAddClick = { onAddFoodClick(MealType.LUNCH) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MealTile(
                emoji = "🍽️",
                name = stringResource(id = R.string.meal_dinner),
                calories = dinnerKcal,
                onAddClick = { onAddFoodClick(MealType.DINNER) },
                modifier = Modifier.weight(1f)
            )
            MealTile(
                emoji = "🍿",
                name = stringResource(id = R.string.meal_snacks),
                calories = snacksKcal,
                onAddClick = { onAddFoodClick(MealType.SNACKS) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MealTile(
    emoji: String,
    name: String,
    calories: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .clickable(onClick = onAddClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$calories kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to $name",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WaterQuickCard(
    waterMl: Int,
    waterGoalMl: Int,
    onAddWater: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = MaterialTheme.nutriTrackColors.water,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(id = R.string.water),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            WaterGlass(
                currentMl = waterMl,
                targetMl = waterGoalMl,
                width = 90.dp,
                height = 120.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.nutriTrackColors.water.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onAddWater(250) }
                ) {
                    Text(
                        text = "+250",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.nutriTrackColors.water,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.nutriTrackColors.water.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .clickable { onAddWater(500) }
                ) {
                    Text(
                        text = "+500",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.nutriTrackColors.water,
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StepsQuickCard(
    stepCount: Int,
    stepGoal: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (stepGoal > 0) (stepCount.toFloat() / stepGoal).coerceIn(0f, 1f) else 0f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsRun,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = stringResource(id = R.string.steps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(80.dp),
                    strokeWidth = 8.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$stepCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Goal: $stepGoal",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
