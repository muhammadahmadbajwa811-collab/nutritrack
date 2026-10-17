package com.nutritrack.app.feature.food

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.R
import com.nutritrack.app.designsystem.components.EmptyState
import com.nutritrack.app.designsystem.components.FoodRow
import com.nutritrack.app.designsystem.components.NtBottomSheet
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtButtonVariant
import com.nutritrack.app.designsystem.components.NtChip
import com.nutritrack.app.designsystem.components.NtTextField
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.MealType
import java.time.LocalDate
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodBottomSheet(
    viewModel: AddFoodViewModel,
    onDismiss: () -> Unit,
    onSelectFood: (foodId: String, meal: MealType, date: LocalDate) -> Unit,
    targetDate: LocalDate = LocalDate.now(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showCreateFoodSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.snackbarMessage) {
        val msg = uiState.snackbarMessage
        if (msg != null) {
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoQuickAdd()
            }
            viewModel.clearSnackbar()
        }
    }

    NtBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxHeight(0.92f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Header: Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Food",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Search Bar
                NtTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    label = stringResource(id = R.string.search_foods),
                    placeholder = stringResource(id = R.string.search_foods_hint),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(id = R.string.search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Meal Selector Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MealType.entries.forEach { meal ->
                        val isSelected = uiState.selectedMeal == meal
                        NtChip(
                            label = meal.displayName,
                            isSelected = isSelected,
                            onClick = { viewModel.onMealSelected(meal) }
                        )
                    }
                }

                // Tabs: Recent, Favorites, My Foods, All (hidden during active search)
                AnimatedVisibility(visible = !uiState.isSearching) {
                    PrimaryTabRow(
                        selectedTabIndex = uiState.activeTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant) }
                    ) {
                        AddFoodTab.entries.forEach { tab ->
                            Tab(
                                selected = uiState.activeTab == tab,
                                onClick = { viewModel.onTabSelected(tab) },
                                text = {
                                    Text(
                                        text = when (tab) {
                                            AddFoodTab.RECENT -> stringResource(id = R.string.tab_recent)
                                            AddFoodTab.FAVORITES -> stringResource(id = R.string.tab_favorites)
                                            AddFoodTab.MY_FOODS -> stringResource(id = R.string.tab_my_foods)
                                            AddFoodTab.ALL -> stringResource(id = R.string.tab_all)
                                        },
                                        fontWeight = if (uiState.activeTab == tab) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            )
                        }
                    }
                }

                // Food Results List or Empty State
                if (uiState.foodResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyState(
                            icon = Icons.Default.Search,
                            title = stringResource(id = R.string.no_foods_found),
                            explanation = stringResource(id = R.string.no_foods_subtitle),
                            actionText = stringResource(id = R.string.create_custom_food),
                            onActionClick = { showCreateFoodSheet = true },
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(
                            items = uiState.foodResults,
                            key = { it.id }
                        ) { food ->
                            FoodSearchResultRow(
                                food = food,
                                onTap = { onSelectFood(food.id, uiState.selectedMeal, targetDate) },
                                onQuickAdd = { viewModel.quickAddFood(food, targetDate) }
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                // Bottom Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NtButton(
                        text = stringResource(id = R.string.quick_add_calories),
                        onClick = { showQuickAddDialog = true },
                        variant = NtButtonVariant.OUTLINED,
                        modifier = Modifier.weight(1f)
                    )

                    NtButton(
                        text = stringResource(id = R.string.create_custom_food),
                        onClick = { showCreateFoodSheet = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Snackbar Host at Bottom
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp)
            )
        }
    }

    // Quick Add Dialog Overlay
    if (showQuickAddDialog) {
        QuickAddDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { name, calories, protein, carbs, fat ->
                viewModel.addQuickCalories(name, calories, protein, carbs, fat, targetDate)
                showQuickAddDialog = false
            }
        )
    }

    // Create Custom Food Sheet Overlay
    if (showCreateFoodSheet) {
        CreateCustomFoodSheet(
            targetMeal = uiState.selectedMeal,
            onDismiss = { showCreateFoodSheet = false },
            onSaveFood = { food, logMeal ->
                viewModel.saveCustomFood(food, logMeal, targetDate)
                showCreateFoodSheet = false
            }
        )
    }
}

@Composable
private fun FoodSearchResultRow(
    food: FoodItem,
    onTap: () -> Unit,
    onQuickAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            FoodRow(
                emoji = food.emoji,
                name = food.name,
                servingLabel = food.servingLabel,
                quantity = 1.0f,
                calories = food.calories.roundToInt(),
                onTap = onTap
            )
        }

        // 1-Tap Quick Add Button
        IconButton(
            onClick = onQuickAdd,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Quick add ${food.name}",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
