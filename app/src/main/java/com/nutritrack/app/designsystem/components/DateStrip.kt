package com.nutritrack.app.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nutritrack.app.core.common.DateUtils
import java.time.LocalDate

@Composable
fun DateStrip(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
    onTodayClick: () -> Unit = { onDateSelected(today) }
) {
    val weekDays = remember(selectedDate) {
        DateUtils.getWeekDays(selectedDate)
    }

    val isOffToday = selectedDate != today
    val canGoForward = weekDays.first().plusWeeks(1) <= today

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Quick "Today" shortcut row if not on today
        AnimatedVisibility(
            visible = isOffToday,
            modifier = Modifier.align(Alignment.End)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onTodayClick)
            ) {
                Text(
                    text = "Jump to Today",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Week row with < and > arrows
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous Week
            IconButton(
                onClick = { onDateSelected(selectedDate.minusWeeks(1)) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous week",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 7 Days
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { date ->
                    val isSelected = date == selectedDate
                    val isFuture = date > today
                    val isDateToday = date == today

                    val bgColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isDateToday -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surface
                        },
                        label = "date_strip_bg"
                    )

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            isDateToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        label = "date_strip_text"
                    )

                    val a11yText = "${date.dayOfWeek.name.lowercase()}, ${date.dayOfMonth} ${date.month.name.lowercase()}" +
                            (if (isSelected) ", selected" else "") +
                            (if (isDateToday) ", today" else "") +
                            (if (isFuture) ", disabled" else "")

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(bgColor)
                            .then(
                                if (!isSelected && isDateToday) {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                                } else Modifier
                            )
                            .then(
                                if (!isFuture) {
                                    Modifier.clickable { onDateSelected(date) }
                                } else Modifier
                            )
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .semantics {
                                role = Role.Tab
                                selected = isSelected
                                contentDescription = a11yText
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = DateUtils.getDayOfWeekLabel(date),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = if (isSelected || isDateToday) FontWeight.Bold else FontWeight.Medium,
                                color = textColor
                            )
                            Text(
                                text = "${date.dayOfMonth}",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                                fontWeight = if (isSelected || isDateToday) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = textColor
                            )
                        }
                    }
                }
            }

            // Next Week
            IconButton(
                onClick = {
                    val nextDate = selectedDate.plusWeeks(1)
                    if (nextDate <= today) {
                        onDateSelected(nextDate)
                    } else {
                        onDateSelected(today)
                    }
                },
                enabled = canGoForward,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next week",
                    tint = if (canGoForward) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}
