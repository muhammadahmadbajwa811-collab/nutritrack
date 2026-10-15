package com.nutritrack.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.designsystem.components.NtButton
import com.nutritrack.app.designsystem.components.NtButtonVariant

data class WelcomeSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeEmoji: String
)

@Composable
fun WelcomeCarouselScreen(
    currentSlideIndex: Int,
    onSlideChanged: (Int) -> Unit,
    onNextClicked: () -> Unit,
    onSkipClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = listOf(
        WelcomeSlide(
            title = "Log meals in seconds",
            subtitle = "Track food in under 10 seconds. Enjoy extensive coverage of everyday meals and authentic Pakistani dishes.",
            icon = Icons.Rounded.Restaurant,
            badgeEmoji = "⚡"
        ),
        WelcomeSlide(
            title = "See your whole day",
            subtitle = "Calories, macros, water, and steps all in one calm dashboard. Clear numbers with zero clutter.",
            icon = Icons.Rounded.PieChart,
            badgeEmoji = "🎯"
        ),
        WelcomeSlide(
            title = "Private by design",
            subtitle = "All your data stays safely on your phone. No account required, no ads, and zero external tracking.",
            icon = Icons.Rounded.Lock,
            badgeEmoji = "🔒"
        )
    )

    val currentSlide = slides[currentSlideIndex.coerceIn(0, slides.size - 1)]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row with Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentSlideIndex < 2) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSkipClicked() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        // Center Content Animated Transition
        AnimatedContent(
            targetState = currentSlide,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "WelcomeSlideContent",
            modifier = Modifier.weight(1f)
        ) { slide ->
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Hero Illustration Container (45% vertical visual)
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = slide.icon,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    // Badge emoji indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = slide.badgeEmoji, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = slide.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = slide.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        // Pager Dots Indicator
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            slides.indices.forEach { index ->
                val isActive = index == currentSlideIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .height(8.dp)
                        .width(if (isActive) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        .clickable { onSlideChanged(index) }
                )
            }
        }

        // Bottom-Pinned Next / Get Started Button
        NtButton(
            text = if (currentSlideIndex == 2) "Get started" else "Next",
            onClick = onNextClicked,
            variant = NtButtonVariant.FILLED,
            isFullWidth = true
        )
    }
}
