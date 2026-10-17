package com.nutritrack.app.feature.food

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.designsystem.components.NtBottomSheet
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtButtonVariant
import com.nutritrack.app.designsystem.components.NtChip
import com.nutritrack.app.designsystem.components.NtTextField
import com.nutritrack.app.domain.model.FoodItem
import com.nutritrack.app.domain.model.FoodSource
import com.nutritrack.app.domain.model.MealType
import java.util.UUID

private val CATEGORY_OPTIONS = listOf(
    "Desi", "Protein", "Grains", "Vegetables", "Fruits",
    "Dairy", "Snacks", "Drinks", "Breakfast", "Fast food", "Sweets", "Soups"
)

private val EMOJI_PRESETS = listOf(
    "🍛", "🍲", "🍗", "🥩", "🥚", "🥗", "🍎", "🍌", "🍞", "🍚",
    "🥛", "☕", "🧃", "🥪", "🍕", "🍔", "🍿", "🍪", "🥞", "🥣"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomFoodSheet(
    onDismiss: () -> Unit,
    onSaveFood: (food: FoodItem, logMeal: MealType?) -> Unit,
    targetMeal: MealType? = null,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Desi") }
    var selectedEmoji by remember { mutableStateOf("🍛") }
    var servingLabel by remember { mutableStateOf("1 serving") }
    var servingGramsText by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var fiberText by remember { mutableStateOf("") }
    var sugarText by remember { mutableStateOf("") }
    var sodiumText by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var caloriesError by remember { mutableStateOf<String?>(null) }
    var servingLabelError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var valid = true
        if (name.isBlank()) {
            nameError = "Name is required"
            valid = false
        } else {
            nameError = null
        }

        val cal = caloriesText.toDoubleOrNull()
        if (cal == null || cal < 0) {
            caloriesError = "Enter valid calories (>= 0)"
            valid = false
        } else {
            caloriesError = null
        }

        if (servingLabel.isBlank()) {
            servingLabelError = "Serving label is required"
            valid = false
        } else {
            servingLabelError = null
        }
        return valid
    }

    fun buildFoodItem(): FoodItem {
        return FoodItem(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            category = selectedCategory,
            emoji = selectedEmoji.ifBlank { "🍽️" },
            servingLabel = servingLabel.trim(),
            servingGrams = servingGramsText.toDoubleOrNull(),
            calories = caloriesText.toDoubleOrNull() ?: 0.0,
            protein = proteinText.toDoubleOrNull() ?: 0.0,
            carbs = carbsText.toDoubleOrNull() ?: 0.0,
            fat = fatText.toDoubleOrNull() ?: 0.0,
            fiber = fiberText.toDoubleOrNull(),
            sugar = sugarText.toDoubleOrNull(),
            sodiumMg = sodiumText.toDoubleOrNull(),
            source = FoodSource.USER,
            createdAt = System.currentTimeMillis()
        )
    }

    NtBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(id = R.string.create_food_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Name
            NtTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError != null) nameError = null
                },
                label = stringResource(id = R.string.food_name),
                placeholder = "e.g. Daal Chawal, Chicken Karahi",
                isError = nameError != null,
                errorMessage = nameError,
                modifier = Modifier.fillMaxWidth()
            )

            // Emoji Picker
            Text(
                text = stringResource(id = R.string.emoji),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EMOJI_PRESETS.forEach { emoji ->
                    NtChip(
                        label = emoji,
                        isSelected = selectedEmoji == emoji,
                        onClick = { selectedEmoji = emoji }
                    )
                }
            }

            // Category Picker
            Text(
                text = stringResource(id = R.string.category),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CATEGORY_OPTIONS.forEach { cat ->
                    NtChip(
                        label = cat,
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            // Serving Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NtTextField(
                    value = servingLabel,
                    onValueChange = {
                        servingLabel = it
                        if (servingLabelError != null) servingLabelError = null
                    },
                    label = "Serving Unit *",
                    placeholder = "1 plate, 1 cup",
                    isError = servingLabelError != null,
                    errorMessage = servingLabelError,
                    modifier = Modifier.weight(1.5f)
                )

                NtTextField(
                    value = servingGramsText,
                    onValueChange = { servingGramsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Grams (opt)",
                    placeholder = "250",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Calories & Core Macros
            NtTextField(
                value = caloriesText,
                onValueChange = {
                    caloriesText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    if (caloriesError != null) caloriesError = null
                },
                label = stringResource(id = R.string.calories_required),
                placeholder = "e.g. 450",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = caloriesError != null,
                errorMessage = caloriesError,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Macronutrients per serving:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NtTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Protein (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                NtTextField(
                    value = carbsText,
                    onValueChange = { carbsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Carbs (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                NtTextField(
                    value = fatText,
                    onValueChange = { fatText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Fat (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            // Optional Micronutrients
            Text(
                text = "Additional nutrients (optional):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NtTextField(
                    value = fiberText,
                    onValueChange = { fiberText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Fiber (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                NtTextField(
                    value = sugarText,
                    onValueChange = { sugarText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Sugar (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                NtTextField(
                    value = sodiumText,
                    onValueChange = { sodiumText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Sodium (mg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (targetMeal != null) {
                    NtButton(
                        text = "Save & Log to ${targetMeal.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        onClick = {
                            if (validate()) {
                                onSaveFood(buildFoodItem(), targetMeal)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                NtButton(
                    text = stringResource(id = R.string.save_only),
                    onClick = {
                        if (validate()) {
                            onSaveFood(buildFoodItem(), null)
                        }
                    },
                    variant = NtButtonVariant.OUTLINED,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
