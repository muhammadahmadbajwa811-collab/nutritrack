package com.nutritrack.app.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.theme.nutriTrackColors
import kotlin.math.roundToInt

@Composable
fun MacroDonut(
    proteinGrams: Int,
    carbsGrams: Int,
    fatGrams: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 14.dp,
    showLegend: Boolean = true
) {
    val proteinKcal = (proteinGrams * 4).coerceAtLeast(0)
    val carbsKcal = (carbsGrams * 4).coerceAtLeast(0)
    val fatKcal = (fatGrams * 9).coerceAtLeast(0)
    val totalKcal = proteinKcal + carbsKcal + fatKcal

    val proteinPct = if (totalKcal > 0) proteinKcal.toFloat() / totalKcal else 0.33f
    val carbsPct = if (totalKcal > 0) carbsKcal.toFloat() / totalKcal else 0.34f
    val fatPct = if (totalKcal > 0) fatKcal.toFloat() / totalKcal else 0.33f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(totalKcal) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    val proteinColor = MaterialTheme.nutriTrackColors.protein
    val carbsColor = MaterialTheme.nutriTrackColors.carbs
    val fatColor = MaterialTheme.nutriTrackColors.fat

    val a11ySummary = "Macronutrient breakdown: ${(proteinPct * 100).roundToInt()}% Protein, ${(carbsPct * 100).roundToInt()}% Carbs, ${(fatPct * 100).roundToInt()}% Fat."

    Row(
        modifier = modifier.semantics { contentDescription = a11ySummary },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 120dp Donut Canvas
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val stroke = strokeWidth.toPx()
                val radius = (this.size.minDimension - stroke) / 2f
                val centerOffset = center

                if (totalKcal == 0) {
                    // Empty grey track
                    drawCircle(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        radius = radius,
                        center = centerOffset,
                        style = Stroke(width = stroke)
                    )
                } else {
                    val proteinSweep = proteinPct * 360f * animProgress.value
                    val carbsSweep = carbsPct * 360f * animProgress.value
                    val fatSweep = fatPct * 360f * animProgress.value

                    var startAngle = -90f

                    // 1. Protein Segment
                    if (proteinSweep > 0f) {
                        drawArc(
                            color = proteinColor,
                            startAngle = startAngle,
                            sweepAngle = proteinSweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        startAngle += proteinSweep
                    }

                    // 2. Carbs Segment
                    if (carbsSweep > 0f) {
                        drawArc(
                            color = carbsColor,
                            startAngle = startAngle,
                            sweepAngle = carbsSweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        startAngle += carbsSweep
                    }

                    // 3. Fat Segment
                    if (fatSweep > 0f) {
                        drawArc(
                            color = fatColor,
                            startAngle = startAngle,
                            sweepAngle = fatSweep,
                            useCenter = false,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                    }
                }
            }

            // Center Text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalKcal",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "kcal",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Optional Side Legend
        if (showLegend) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MacroLegendRow("Protein", "${(proteinPct * 100).roundToInt()}% (${proteinGrams}g)", proteinColor)
                MacroLegendRow("Carbs", "${(carbsPct * 100).roundToInt()}% (${carbsGrams}g)", carbsColor)
                MacroLegendRow("Fat", "${(fatPct * 100).roundToInt()}% (${fatGrams}g)", fatColor)
            }
        }
    }
}

@Composable
private fun MacroLegendRow(label: String, detail: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
