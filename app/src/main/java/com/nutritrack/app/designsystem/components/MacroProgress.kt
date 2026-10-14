package com.nutritrack.app.designsystem.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MacroProgress(
    macroName: String,
    currentGrams: Int,
    targetGrams: Int,
    barColor: Color,
    modifier: Modifier = Modifier
) {
    val progressRatio = if (targetGrams > 0) {
        (currentGrams.toFloat() / targetGrams.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress = remember { Animatable(0f) }
    LaunchedEffect(progressRatio) {
        animatedProgress.animateTo(
            targetValue = progressRatio,
            animationSpec = tween(durationMillis = 900, easing = EaseOutCubic)
        )
    }

    val remainingGrams = (targetGrams - currentGrams).coerceAtLeast(0)
    val a11yDescription = "$macroName: $currentGrams of $targetGrams grams eaten, $remainingGrams grams remaining."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = a11yDescription }
    ) {
        // Label Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = macroName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$currentGrams / ${targetGrams}g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 10dp High Pill Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(barColor.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.value)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(barColor)
            )
        }
    }
}
