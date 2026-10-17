package com.nutritrack.app.feature.diary

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nutritrack.app.R
import com.nutritrack.app.designsystem.components.FoodRow
import com.nutritrack.app.designsystem.components.MealCard
import com.nutritrack.app.designsystem.components.NtDialog
import com.nutritrack.app.domain.model.DiaryEntry
import com.nutritrack.app.domain.model.MealType
import com.nutritrack.app.feature.food.AddFoodBottomSheet
import com.nutritrack.app.feature.food.AddFoodViewModel
import com.nutritrack.app.feature.food.FoodDetailBottomSheet
import com.nutritrack.app.feature.food.FoodDetailViewModel
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
fun DiaryScreen(
    modifier: Modifier = Modifier,
    viewModel: DiaryViewModel = hiltViewModel(),
    addFoodViewModel: AddFoodViewModel = hiltViewModel(),
    foodDetailViewModel: FoodDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }

    // Bottom sheet states
    var showAddFoodSheet by remember { mutableStateOf(false) }
    var addFoodPreselectedMeal by remember { mutableStateOf(MealType.LUNCH) }
    var showFoodDetailSheet by remember { mutableStateOf(false) }

    // Handle user message / snackbars
    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                if (uiState.recentlyDeletedEntry != null) {
                    viewModel.undoDelete()
                } else {
                    viewModel.undoClearDay()
                }
            }
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Top Date Navigation Bar
                DiaryDateBar(
                    selectedDate = uiState.selectedDate,
                    formattedDate = uiState.formattedDate,
                    isToday = uiState.isToday,
                    onPreviousClick = { viewModel.onPreviousDay() },
                    onNextClick = { viewModel.onNextDay() },
                    onCalendarClick = {
                        val curr = uiState.selectedDate
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                viewModel.onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                            },
                            curr.year,
                            curr.monthValue - 1,
                            curr.dayOfMonth
                        )
                        datePicker.datePicker.maxDate = System.currentTimeMillis()
                        datePicker.show()
                    },
                    menuExpanded = menuExpanded,
                    onMenuToggle = { menuExpanded = it },
                    onCopyYesterday = {
                        menuExpanded = false
                        viewModel.copyFromYesterday()
                    },
                    onClearDay = {
                        menuExpanded = false
                        viewModel.showClearDayDialog()
                    }
                )
            }

            // Summary Card (kcal progress bar + macro summary)
            item {
                DiarySummaryCard(
                    totalCalories = uiState.totalCalories,
                    calorieGoal = uiState.calorieGoal,
                    totalProtein = uiState.totalProtein,
                    proteinGoal = uiState.proteinGoal,
                    totalCarbs = uiState.totalCarbs,
                    carbsGoal = uiState.carbsGoal,
                    totalFat = uiState.totalFat,
                    fatGoal = uiState.fatGoal
                )
            }

            // Four Meal Sections
            items(MealType.entries.size) { index ->
                val meal = MealType.entries[index]
                val entries = uiState.entriesByMeal[meal].orEmpty()
                val totalMealKcal = entries.sumOf { it.calories }.roundToInt()
                val isCollapsed = uiState.isMealCollapsed(meal)

                MealCard(
                    mealEmoji = when (meal) {
                        MealType.BREAKFAST -> "🍳"
                        MealType.LUNCH -> "🥗"
                        MealType.DINNER -> "🍽️"
                        MealType.SNACKS -> "🍿"
                    },
                    mealName = meal.displayName,
                    totalCalories = totalMealKcal,
                    onAddClick = {
                        addFoodPreselectedMeal = meal
                        addFoodViewModel.setInitialMeal(meal)
                        showAddFoodSheet = true
                    },
                    isCollapsible = true
                ) {
                    if (entries.isEmpty()) {
                        EmptyMealRow(
                            onAddClick = {
                                addFoodPreselectedMeal = meal
                                addFoodViewModel.setInitialMeal(meal)
                                showAddFoodSheet = true
                            }
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            entries.forEach { entry ->
                                FoodRow(
                                    emoji = entry.emojiSnapshot,
                                    name = entry.nameSnapshot,
                                    servingLabel = entry.servingLabelSnapshot,
                                    quantity = entry.quantity.toFloat(),
                                    calories = entry.calories.roundToInt(),
                                    onTap = {
                                        foodDetailViewModel.loadForEditing(entry)
                                        showFoodDetailSheet = true
                                    },
                                    onDelete = {
                                        viewModel.deleteEntry(entry)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Confirmation Dialog for Clear Day
    if (uiState.isClearDayDialogOpen) {
        NtDialog(
            title = stringResource(id = R.string.clear_day_confirm_title),
            message = stringResource(id = R.string.clear_day_confirm_message),
            onConfirm = { viewModel.clearDay() },
            onDismiss = { viewModel.dismissClearDayDialog() },
            confirmText = stringResource(id = R.string.clear),
            dismissText = stringResource(id = R.string.cancel),
            isDestructive = true
        )
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

    // Food Detail / Edit Bottom Sheet
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
private fun DiaryDateBar(
    selectedDate: LocalDate,
    formattedDate: String,
    isToday: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onCalendarClick: () -> Unit,
    menuExpanded: Boolean,
    onMenuToggle: (Boolean) -> Unit,
    onCopyYesterday: () -> Unit,
    onClearDay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onPreviousClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous day",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isToday) "Today" else formattedDate,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isToday) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onNextClick,
                enabled = !isToday
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next day",
                    tint = if (!isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCalendarClick) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar date picker",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { onMenuToggle(true) }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { onMenuToggle(false) }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.copy_yesterday)) },
                        onClick = onCopyYesterday
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.clear_day),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = onClearDay
                    )
                }
            }
        }
    }
}

@Composable
private fun DiarySummaryCard(
    totalCalories: Int,
    calorieGoal: Int,
    totalProtein: Int,
    proteinGoal: Int,
    totalCarbs: Int,
    carbsGoal: Int,
    totalFat: Int,
    fatGoal: Int
) {
    val progress = if (calorieGoal > 0) (totalCalories.toFloat() / calorieGoal).coerceIn(0f, 1f) else 0f
    val isOver = totalCalories > calorieGoal

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$totalCalories / $calorieGoal kcal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isOver) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroSummaryChip("Protein", "$totalProtein / ${proteinGoal}g")
                MacroSummaryChip("Carbs", "$totalCarbs / ${carbsGoal}g")
                MacroSummaryChip("Fat", "$totalFat / ${fatGoal}g")
            }
        }
    }
}

@Composable
private fun MacroSummaryChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyMealRow(onAddClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onAddClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(id = R.string.add_food),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
