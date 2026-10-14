package com.nutritrack.app.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

@Composable
fun LineChart(
    points: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    targetValue: Float? = null,
    unit: String = "kg",
    height: Dp = 190.dp
) {
    if (points.isEmpty()) return

    var selectedIndex by remember { mutableStateOf(points.lastIndex) }

    val rawMin = points.minOrNull() ?: 0f
    val rawMax = points.maxOrNull() ?: 100f
    val targetVal = targetValue ?: rawMin

    val minVal = (min(rawMin, targetVal) - 1f).coerceAtLeast(0f)
    val maxVal = (max(rawMax, targetVal) + 1f)
    val valRange = if (maxVal - minVal > 0f) maxVal - minVal else 1f

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    val targetLineColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    val a11ySummary = "Trend line chart: current ${points.lastOrNull() ?: 0f} $unit, target ${targetValue ?: "none"} $unit."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11ySummary }
    ) {
        // Scrubber / Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val curPoint = points.getOrElse(selectedIndex) { 0f }
            val curLabel = labels.getOrElse(selectedIndex) { "" }

            Text(
                text = "$curLabel: $curPoint $unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            if (targetValue != null) {
                Text(
                    text = "Goal: $targetValue $unit",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val stepX = size.width / (points.size - 1).coerceAtLeast(1)
                            val idx = (offset.x / stepX + 0.5f).toInt().coerceIn(0, points.lastIndex)
                            selectedIndex = idx
                        }
                    }
            ) {
                val chartW = size.width
                val chartH = size.height - 24.dp.toPx()
                val n = points.size
                val stepX = if (n > 1) chartW / (n - 1) else chartW

                // 1. Draw Target Line
                if (targetValue != null) {
                    val targetY = chartH - ((targetValue - minVal) / valRange) * chartH
                    if (targetY in 0f..chartH) {
                        drawLine(
                            color = targetLineColor,
                            start = Offset(0f, targetY),
                            end = Offset(chartW, targetY),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }
                }

                // 2. Compute Point Coordinates
                val coords = points.mapIndexed { index, value ->
                    val x = index * stepX
                    val y = chartH - (((value - minVal) / valRange) * chartH * animProgress.value)
                    Offset(x, y)
                }

                if (coords.isNotEmpty()) {
                    // 3. Build Smooth Cubic Bezier Path
                    val strokePath = Path()
                    val fillPath = Path()

                    strokePath.moveTo(coords.first().x, coords.first().y)
                    fillPath.moveTo(coords.first().x, chartH)
                    fillPath.lineTo(coords.first().x, coords.first().y)

                    for (i in 0 until coords.size - 1) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]
                        val cp1X = (p0.x + p1.x) / 2f
                        val cp1Y = p0.y
                        val cp2X = (p0.x + p1.x) / 2f
                        val cp2Y = p1.y

                        strokePath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1.x, p1.y)
                        fillPath.cubicTo(cp1X, cp1Y, cp2X, cp2Y, p1.x, p1.y)
                    }

                    fillPath.lineTo(coords.last().x, chartH)
                    fillPath.close()

                    // Gradient-Free Fill in primaryContainer
                    drawPath(
                        path = fillPath,
                        color = fillColor
                    )

                    // Line Stroke
                    drawPath(
                        path = strokePath,
                        color = primaryColor,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // 4. Draw Circular Point Dots
                    coords.forEachIndexed { index, offset ->
                        val isSelected = index == selectedIndex
                        val dotRadius = if (isSelected) 6.dp.toPx() else 4.dp.toPx()

                        // Outer ring for selected
                        if (isSelected) {
                            drawCircle(
                                color = primaryColor.copy(alpha = 0.3f),
                                radius = dotRadius + 4.dp.toPx(),
                                center = offset
                            )
                        }

                        drawCircle(
                            color = primaryColor,
                            radius = dotRadius,
                            center = offset
                        )

                        drawCircle(
                            color = Color.White,
                            radius = dotRadius - 1.5.dp.toPx(),
                            center = offset
                        )
                    }
                }
            }

            // Bottom Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                labels.forEachIndexed { index, label ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
