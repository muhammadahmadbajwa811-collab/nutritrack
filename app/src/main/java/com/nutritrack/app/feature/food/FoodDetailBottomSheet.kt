package com.nutritrack.app.feature.food

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nutritrack.app.designsystem.components.MacroDonut
import com.nutritrack.app.designsystem.components.NtBottomSheet
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtChip
import com.nutritrack.app.designsystem.components.QuantityStepper
import com.nutritrack.app.designsystem.components.ServingSelector
import com.nutritrack.app.domain.model.MealType
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodDetailBottomSheet(
    viewModel: FoodDetailViewModel,
    onDismiss: () -> Unit,
    onEntrySaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMoreNutrition by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onEntrySaved()
        }
    }

    val food = uiState.food
    val totalCalories = uiState.computedNutrition.calories.roundToInt()

    NtBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Close, Title, Favorite Heart
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (uiState.isEditingExistingEntry) {
                        stringResource(id = R.string.update_entry)
                    } else {
                        stringResource(id = R.string.food_detail_title)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = { viewModel.toggleFavorite() }) {
                    Icon(
                        imageVector = if (uiState.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (uiState.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Hero Section: 96dp emoji tile on primaryContainer
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = food?.emoji ?: "🍽️",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Food Name & Category Chip
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = food?.name ?: "Food Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                food?.category?.let { category ->
                    NtChip(
                        label = category,
                        isSelected = true,
                        onClick = {}
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Serving Unit Selector
            if (uiState.servingOptions.isNotEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.serving_size),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val optionLabels = uiState.servingOptions.map { it.label }
                    val selectedLabel = uiState.selectedServingOption?.label ?: optionLabels.firstOrNull() ?: ""

                    ServingSelector(
                        servingOptions = optionLabels,
                        selectedOption = selectedLabel,
                        onOptionSelected = { label ->
                            val chosen = uiState.servingOptions.firstOrNull { it.label == label }
                            if (chosen != null) {
                                viewModel.onServingSelected(chosen)
                            }
                        }
                    )
                }
            }

            // Quantity Stepper
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.quantity),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                QuantityStepper(
                    quantity = uiState.quantity,
                    onQuantityChange = { viewModel.onQuantityChanged(it) },
                    step = 0.5f,
                    minQuantity = 0.5f,
                    maxQuantity = 20.0f
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Dynamic Calories & Macro Donut
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$totalCalories",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.kcal),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                MacroDonut(
                    proteinGrams = uiState.computedNutrition.protein.roundToInt(),
                    carbsGrams = uiState.computedNutrition.carbs.roundToInt(),
                    fatGrams = uiState.computedNutrition.fat.roundToInt(),
                    size = 110.dp,
                    strokeWidth = 12.dp,
                    showLegend = true
                )
            }

            // More Nutrition (Collapsible)
            val hasMicros = uiState.computedNutrition.fiber != null ||
                    uiState.computedNutrition.sugar != null ||
                    uiState.computedNutrition.sodiumMg != null

            if (hasMicros) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        onClick = { showMoreNutrition = !showMoreNutrition },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showMoreNutrition) {
                                stringResource(id = R.string.less_nutrition)
                            } else {
                                stringResource(id = R.string.more_nutrition)
                            },
                            style = MaterialTheme.typography.labelLarge
                        )
                        Icon(
                            imageVector = if (showMoreNutrition) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }

                    AnimatedVisibility(visible = showMoreNutrition) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.computedNutrition.fiber?.let {
                                    MicroNutrientRow(stringResource(id = R.string.macro_fiber), "${it.roundToInt()} g")
                                }
                                uiState.computedNutrition.sugar?.let {
                                    MicroNutrientRow(stringResource(id = R.string.macro_sugar), "${it.roundToInt()} g")
                                }
                                uiState.computedNutrition.sodiumMg?.let {
                                    MicroNutrientRow(stringResource(id = R.string.macro_sodium), "${it.roundToInt()} mg")
                                }
                            }
                        }
                    }
                }
            }

            // Disclaimer label
            Text(
                text = stringResource(id = R.string.approximate_values),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            // Meal Chips (Breakfast, Lunch, Dinner, Snacks)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.meals),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sticky Add/Update to Diary button
            val mealName = uiState.selectedMeal.name.lowercase().replaceFirstChar { it.uppercase() }
            val buttonText = if (uiState.isEditingExistingEntry) {
                stringResource(id = R.string.update_entry_with_kcal, mealName, totalCalories)
            } else {
                stringResource(id = R.string.add_to_diary_with_kcal, mealName, totalCalories)
            }

            NtButton(
                text = buttonText,
                onClick = { viewModel.saveToDiary() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MicroNutrientRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
