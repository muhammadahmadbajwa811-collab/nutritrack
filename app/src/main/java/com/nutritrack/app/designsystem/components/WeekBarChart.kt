package com.nutritrack.app.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max

@Composable
fun WeekBarChart(
    dailyValues: List<Int>,
    dayLabels: List<String>,
    goalValue: Int,
    todayIndex: Int,
    modifier: Modifier = Modifier,
    unit: String = "kcal",
    onDaySelected: (Int) -> Unit = {},
    height: Dp = 190.dp
) {
    require(dailyValues.size == 7) { "WeekBarChart expects 7 daily values" }
    require(dayLabels.size == 7) { "WeekBarChart expects 7 day labels" }

    var selectedIndex by remember { mutableStateOf(todayIndex) }

    val maxValue = max(goalValue, dailyValues.maxOrNull() ?: 1).coerceAtLeast(100)

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(dailyValues) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val outlineColor = MaterialTheme.colorScheme.outline
    val goalLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val avgValue = dailyValues.filter { it > 0 }.average().let { if (it.isNaN()) 0 else it.toInt() }
    val a11ySummary = "Weekly $unit intake chart: average $avgValue $unit, daily goal $goalValue $unit. Today: ${dailyValues.getOrElse(todayIndex) { 0 }} $unit."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11ySummary }
    ) {
        // Selected Value Tooltip Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val selectedVal = dailyValues.getOrElse(selectedIndex) { 0 }
            val selectedDay = dayLabels.getOrElse(selectedIndex) { "" }

            Text(
                text = "$selectedDay: $selectedVal $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (selectedIndex == todayIndex) primaryColor else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Goal: $goalValue $unit",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 7-Bar Canvas Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(dailyValues) {
                        detectTapGestures { offset ->
                            val barSlotWidth = size.width / 7f
                            val tappedIndex = (offset.x / barSlotWidth).toInt().coerceIn(0, 6)
                            selectedIndex = tappedIndex
                            onDaySelected(tappedIndex)
                        }
                    }
            ) {
                val chartW = size.width
                val chartH = size.height - 24.dp.toPx() // Reserve 24dp for bottom labels
                val barSlotWidth = chartW / 7f
                val barWidth = barSlotWidth * 0.48f

                // 1. Draw Dashed Goal Line
                val goalY = chartH - (goalValue.toFloat() / maxValue.toFloat()) * chartH
                if (goalY in 0f..chartH) {
                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, goalY),
                        end = Offset(chartW, goalY),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                }

                // 2. Draw 7 Bars
                dailyValues.forEachIndexed { index, value ->
                    val barHeight = ((value.toFloat() / maxValue.toFloat()) * chartH * animProgress.value).coerceAtLeast(4.dp.toPx())
                    val barLeft = index * barSlotWidth + (barSlotWidth - barWidth) / 2f
                    val barTop = chartH - barHeight

                    val isToday = index == todayIndex
                    val isSelected = index == selectedIndex

                    val barColor = when {
                        isToday -> primaryColor
                        isSelected -> primaryColor.copy(alpha = 0.8f)
                        else -> containerColor
                    }

                    // Bar with rounded top
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barLeft, barTop),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                    )
                }
            }

            // Bottom Day Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                dayLabels.forEachIndexed { index, label ->
                    val isToday = index == todayIndex
                    val isSelected = index == selectedIndex

                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isToday) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable {
                            selectedIndex = index
                            onDaySelected(index)
                        }
                    )
                }
            }
        }
    }
}
