package com.nutritrack.app.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.theme.nutriTrackColors
import kotlin.math.abs

@Composable
fun CalorieRing(
    eatenCalories: Int,
    targetCalories: Int,
    burnedCalories: Int = 0,
    modifier: Modifier = Modifier,
    diameter: Dp = 240.dp,
    strokeWidth: Dp = 18.dp
) {
    val netCalories = eatenCalories - burnedCalories
    val remainingCalories = targetCalories - netCalories
    val isOverGoal = remainingCalories < 0
    val progressRatio = if (targetCalories > 0) {
        (eatenCalories.toFloat() / targetCalories.toFloat()).coerceAtLeast(0f)
    } else 0f

    // Animated progress sweep
    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progressRatio) {
        animatedProgress.animateTo(
            targetValue = progressRatio.coerceAtMost(1f),
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    // Dynamic progress color (transitions to warning amber if over goal)
    val targetProgressColor = if (isOverGoal) {
        MaterialTheme.nutriTrackColors.warning
    } else {
        MaterialTheme.colorScheme.primary
    }

    val progressColor by animateColorAsState(
        targetValue = targetProgressColor,
        animationSpec = tween(durationMillis = 400),
        label = "calorie_ring_color"
    )

    val trackColor = MaterialTheme.colorScheme.primaryContainer

    val a11ySummary = if (isOverGoal) {
        "Calorie progress: ${abs(remainingCalories)} calories over daily goal of $targetCalories. Total eaten: $eatenCalories calories."
    } else {
        "Calorie progress: $remainingCalories calories remaining of daily goal $targetCalories. Total eaten: $eatenCalories calories."
    }

    Box(
        modifier = modifier
            .size(diameter)
            .semantics { contentDescription = a11ySummary },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = strokeWidth.toPx()
            val canvasRadius = (size.minDimension - stroke) / 2f
            val centerOffset = center

            // 1. Draw Background Track (full 360 circle)
            drawCircle(
                color = trackColor,
                radius = canvasRadius,
                center = centerOffset,
                style = Stroke(width = stroke)
            )

            // 2. Draw Progress Arc
            val sweepAngle = animatedProgress.value * 360f
            if (sweepAngle > 0f) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            val displayValue = if (isOverGoal) abs(remainingCalories) else remainingCalories
            val displayTitle = if (isOverGoal) "kcal over" else "kcal remaining"

            Text(
                text = String.format("%,d", displayValue),
                style = if (diameter >= 200.dp) {
                    MaterialTheme.typography.displayLarge.copy(fontSize = 44.sp, fontWeight = FontWeight.ExtraBold)
                } else {
                    MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                },
                color = if (isOverGoal) MaterialTheme.nutriTrackColors.warning else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
