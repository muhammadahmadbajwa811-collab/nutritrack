package com.nutritrack.app.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.theme.nutriTrackColors
import kotlin.math.sin

@Composable
fun WaterGlass(
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier,
    width: Dp = 130.dp,
    height: Dp = 170.dp
) {
    val fillRatio = if (targetMl > 0) {
        (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    // Animated water height level
    val animatedFill = remember { Animatable(0f) }
    LaunchedEffect(fillRatio) {
        animatedFill.animateTo(
            targetValue = fillRatio,
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic)
        )
    }

    // Infinite wave phase animation
    val infiniteTransition = rememberInfiniteTransition(label = "water_wave")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "water_wave_phase"
    )

    val waterColor = MaterialTheme.nutriTrackColors.water
    val glassBorderColor = MaterialTheme.colorScheme.outline
    val a11ySummary = "Water intake: $currentMl of $targetMl milliliters logged, ${(fillRatio * 100).toInt()}% completed."

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .semantics { contentDescription = a11ySummary },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(width = width, height = height)) {
            val canvasW = size.width
            val canvasH = size.height
            val strokeW = 3.dp.toPx()
            val cornerR = 24.dp.toPx()

            // 1. Create Glass Tumbler Path (tapered modern tumbler)
            val glassPath = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = androidx.compose.ui.geometry.Rect(
                            left = strokeW,
                            top = strokeW,
                            right = canvasW - strokeW,
                            bottom = canvasH - strokeW
                        ),
                        topLeft = CornerRadius(8.dp.toPx()),
                        topRight = CornerRadius(8.dp.toPx()),
                        bottomLeft = CornerRadius(cornerR),
                        bottomRight = CornerRadius(cornerR)
                    )
                )
            }

            // 2. Draw Glass Background (soft tinted)
            drawPath(
                path = glassPath,
                color = waterColor.copy(alpha = 0.08f)
            )

            // 3. Draw Water inside Glass with Animated Sine Waves
            val waterH = canvasH * animatedFill.value
            val waterTopY = canvasH - waterH

            if (animatedFill.value > 0.01f) {
                clipPath(glassPath, clipOp = ClipOp.Intersect) {
                    val wavePath = Path()
                    wavePath.moveTo(0f, canvasH)
                    wavePath.lineTo(0f, waterTopY)

                    // Draw Sine Wave
                    val waveAmp = if (animatedFill.value >= 0.98f) 2.dp.toPx() else 5.dp.toPx()
                    val waveLen = canvasW / 1.5f

                    var x = 0f
                    while (x <= canvasW) {
                        val y = waterTopY + waveAmp * sin((x / waveLen) * 2 * Math.PI.toFloat() + wavePhase)
                        wavePath.lineTo(x, y)
                        x += 4f
                    }
                    wavePath.lineTo(canvasW, canvasH)
                    wavePath.close()

                    drawPath(
                        path = wavePath,
                        color = waterColor.copy(alpha = 0.82f)
                    )
                }
            }

            // 4. Draw Glass Tumbler Outline
            drawPath(
                path = glassPath,
                color = glassBorderColor,
                style = Stroke(width = strokeW, cap = StrokeCap.Round)
            )
        }

        // Center Content Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = "$currentMl",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
                color = if (fillRatio > 0.5f) Color.White else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "of $targetMl ml",
                style = MaterialTheme.typography.labelSmall,
                color = if (fillRatio > 0.5f) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
