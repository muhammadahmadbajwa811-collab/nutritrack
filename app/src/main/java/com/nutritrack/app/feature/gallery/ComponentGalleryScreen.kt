package com.nutritrack.app.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.components.BadgeCard
import com.nutritrack.app.designsystem.components.CalorieRing
import com.nutritrack.app.designsystem.components.EmptyState
import com.nutritrack.app.designsystem.components.FoodRow
import com.nutritrack.app.designsystem.components.LineChart
import com.nutritrack.app.designsystem.components.MacroDonut
import com.nutritrack.app.designsystem.components.MacroProgress
import com.nutritrack.app.designsystem.components.MealCard
import com.nutritrack.app.designsystem.components.NtBottomSheet
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtButtonVariant
import com.nutritrack.app.designsystem.components.NtChip
import com.nutritrack.app.designsystem.components.NtDialog
import com.nutritrack.app.designsystem.components.NtTextField
import com.nutritrack.app.designsystem.components.NtTopAppBar
import com.nutritrack.app.designsystem.components.QuantityStepper
import com.nutritrack.app.designsystem.components.ServingSelector
import com.nutritrack.app.designsystem.components.SkeletonItem
import com.nutritrack.app.designsystem.components.StatTile
import com.nutritrack.app.designsystem.components.StreakChip
import com.nutritrack.app.designsystem.components.WaterGlass
import com.nutritrack.app.designsystem.components.WeekBarChart
import com.nutritrack.app.designsystem.theme.nutriTrackColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentGalleryScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Interactive states for testing
    var calorieEaten by remember { mutableIntStateOf(1650) }
    val calorieTarget by remember { mutableIntStateOf(2000) }
    var waterMl by remember { mutableIntStateOf(1750) }
    val waterTarget by remember { mutableIntStateOf(2500) }
    var stepperQty by remember { mutableFloatStateOf(1.5f) }
    var selectedServing by remember { mutableStateOf("1 cup (240g)") }
    var selectedChip by remember { mutableStateOf("All") }
    var textInput by remember { mutableStateOf("72.5") }
    var showDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            NtTopAppBar(
                title = "Component Gallery",
                isDetailScreen = true,
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Calorie Ring & Hero Metrics
            item {
                SectionHeader("1. Calorie Ring (Hero Visual)")
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CalorieRing(
                        eatenCalories = calorieEaten,
                        targetCalories = calorieTarget,
                        burnedCalories = 250,
                        diameter = 220.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Adjust Eaten: $calorieEaten kcal (Target: $calorieTarget kcal)",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Slider(
                        value = calorieEaten.toFloat(),
                        onValueChange = { calorieEaten = it.toInt() },
                        valueRange = 0f..2600f,
                        modifier = Modifier.fillMaxWidth(0.9f)
                    )
                }
            }

            // Section 2: Macronutrients (Donut & Bars)
            item {
                SectionHeader("2. Macronutrients Visuals")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacroDonut(
                        proteinGrams = 90,
                        carbsGrams = 180,
                        fatGrams = 45,
                        size = 110.dp,
                        showLegend = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroProgress(
                        macroName = "Protein",
                        currentGrams = 90,
                        targetGrams = 120,
                        barColor = MaterialTheme.nutriTrackColors.protein
                    )
                    MacroProgress(
                        macroName = "Carbohydrates",
                        currentGrams = 180,
                        targetGrams = 200,
                        barColor = MaterialTheme.nutriTrackColors.carbs
                    )
                    MacroProgress(
                        macroName = "Fat",
                        currentGrams = 45,
                        targetGrams = 60,
                        barColor = MaterialTheme.nutriTrackColors.fat
                    )
                }
            }

            // Section 3: Stat Tiles
            item {
                SectionHeader("3. Metric Stat Tiles")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatTile(
                        icon = Icons.Filled.LocalDining,
                        value = "1,650",
                        label = "Eaten",
                        unit = "kcal",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Filled.FitnessCenter,
                        value = "420",
                        label = "Burned",
                        unit = "kcal",
                        iconTint = MaterialTheme.nutriTrackColors.warning,
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        icon = Icons.Filled.DirectionsRun,
                        value = "8,432",
                        label = "Steps",
                        iconTint = MaterialTheme.nutriTrackColors.success,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 4: Water Tracker
            item {
                SectionHeader("4. Water Intake Glass")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WaterGlass(
                        currentMl = waterMl,
                        targetMl = waterTarget
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NtButton(
                            text = "+150 ml",
                            onClick = { waterMl = (waterMl + 150).coerceAtMost(waterTarget + 500) },
                            variant = NtButtonVariant.TONAL,
                            isFullWidth = false
                        )
                        NtButton(
                            text = "+250 ml",
                            onClick = { waterMl = (waterMl + 250).coerceAtMost(waterTarget + 500) },
                            variant = NtButtonVariant.FILLED,
                            isFullWidth = false
                        )
                        NtButton(
                            text = "Reset",
                            onClick = { waterMl = 0 },
                            variant = NtButtonVariant.TEXT,
                            isFullWidth = false
                        )
                    }
                }
            }

            // Section 5: Week Bar Chart
            item {
                SectionHeader("5. 7-Day Intake Bar Chart")
                WeekBarChart(
                    dailyValues = listOf(1850, 2100, 1950, 2200, 1600, 1900, 1750),
                    dayLabels = listOf("M", "T", "W", "T", "F", "S", "S"),
                    goalValue = 2000,
                    todayIndex = 6
                )
            }

            // Section 6: Weight Line Chart
            item {
                SectionHeader("6. Weight Moving Average Line Chart")
                LineChart(
                    points = listOf(73.5f, 73.2f, 72.9f, 72.8f, 72.5f, 72.2f, 72.0f),
                    labels = listOf("W1", "W2", "W3", "W4", "W5", "W6", "W7"),
                    targetValue = 70.0f,
                    unit = "kg"
                )
            }

            // Section 7: Meal Cards & Food Rows
            item {
                SectionHeader("7. Meal Card & Food Rows")
                MealCard(
                    mealEmoji = "🍳",
                    mealName = "Breakfast",
                    totalCalories = 480,
                    onAddClick = { showBottomSheet = true }
                ) {
                    FoodRow(
                        emoji = "🥚",
                        name = "2 Boiled Eggs",
                        servingLabel = "large",
                        quantity = 2f,
                        calories = 144,
                        onTap = {}
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    FoodRow(
                        emoji = "🍞",
                        name = "Whole Wheat Toast",
                        servingLabel = "slice",
                        quantity = 2f,
                        calories = 160,
                        onTap = {}
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    FoodRow(
                        emoji = "☕",
                        name = "Chai (Low Sugar)",
                        servingLabel = "cup",
                        quantity = 1f,
                        calories = 85,
                        onTap = {}
                    )
                }
            }

            // Section 8: Stepper & Serving Selector
            item {
                SectionHeader("8. Quantity Stepper & Serving Selector")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    QuantityStepper(
                        quantity = stepperQty,
                        onQuantityChange = { stepperQty = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                ServingSelector(
                    servingOptions = listOf("1 piece (50g)", "100 g", "1 cup (240g)", "1 tbsp (15g)"),
                    selectedOption = selectedServing,
                    onOptionSelected = { selectedServing = it }
                )
            }

            // Section 9: Chips & Streaks
            item {
                SectionHeader("9. Filter Chips & Streak Chip")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StreakChip(streakDays = 7)
                    StreakChip(streakDays = 30)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Recent", "Favourites", "My Foods").forEach { chip ->
                        NtChip(
                            label = chip,
                            isSelected = selectedChip == chip,
                            onClick = { selectedChip = chip }
                        )
                    }
                }
            }

            // Section 10: Badges
            item {
                SectionHeader("10. Achievement Badges")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    BadgeCard(
                        emoji = "🔥",
                        title = "7-Day Streak",
                        description = "Logged 7 days in a row",
                        isUnlocked = true
                    )
                    BadgeCard(
                        emoji = "💧",
                        title = "Hydration Hero",
                        description = "Hit water goal 7 days",
                        isUnlocked = false,
                        progress = 0.6f
                    )
                    BadgeCard(
                        emoji = "🏃",
                        title = "Step Master",
                        description = "10,000 steps 5 days",
                        isUnlocked = false,
                        progress = 0.2f
                    )
                }
            }

            // Section 11: Text Input & Buttons
            item {
                SectionHeader("11. Text Inputs & Buttons")
                NtTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    label = "Current Weight",
                    unitSuffix = "kg",
                    placeholder = "Enter weight"
                )

                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NtButton(
                        text = "Primary Filled Button",
                        onClick = { showDialog = true },
                        variant = NtButtonVariant.FILLED
                    )
                    NtButton(
                        text = "Tonal Secondary Button",
                        onClick = {},
                        variant = NtButtonVariant.TONAL
                    )
                    NtButton(
                        text = "Outlined Button",
                        onClick = {},
                        variant = NtButtonVariant.OUTLINED
                    )
                    NtButton(
                        text = "Text Button",
                        onClick = {},
                        variant = NtButtonVariant.TEXT
                    )
                }
            }

            // Section 12: Empty State & Skeletons
            item {
                SectionHeader("12. Empty States & Skeletons")
                EmptyState(
                    icon = Icons.Filled.SearchOff,
                    title = "No Foods Found",
                    explanation = "Try searching for a different item or create a custom dish.",
                    actionText = "Create Custom Food",
                    onActionClick = {}
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Loading Skeletons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SkeletonItem(height = 48.dp)
                    SkeletonItem(height = 48.dp)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Test Dialog
        if (showDialog) {
            NtDialog(
                title = "Clear Today's Diary?",
                message = "This will remove all logged meals for today. You can undo this immediately from the snackbar.",
                confirmText = "Clear Day",
                isDestructive = true,
                icon = Icons.Filled.Delete,
                onConfirm = { showDialog = false },
                onDismiss = { showDialog = false }
            )
        }

        // Test Bottom Sheet
        if (showBottomSheet) {
            NtBottomSheet(onDismissRequest = { showBottomSheet = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Add Food to Breakfast",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Search across 300+ foods including authentic Pakistani & South Asian dishes with Urdu and Roman Urdu aliases.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    NtButton(
                        text = "Close Bottom Sheet",
                        onClick = { showBottomSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 12.dp, top = 8.dp)
    )
}
