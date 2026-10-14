package com.nutritrack.app.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun QuantityStepper(
    quantity: Float,
    onQuantityChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    step: Float = 0.5f,
    minQuantity: Float = 0.5f,
    maxQuantity: Float = 20.0f
) {
    val displayValue = if (quantity % 1f == 0f) {
        quantity.toInt().toString()
    } else {
        String.format("%.1f", quantity)
    }

    val canDecrement = quantity > minQuantity
    val canIncrement = quantity < maxQuantity

    Row(
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .semantics { contentDescription = "Quantity stepper: current quantity is $displayValue" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Minus Button
        IconButton(
            onClick = {
                if (canDecrement) {
                    val newQty = ((quantity - step) * 100).roundToInt() / 100f
                    onQuantityChange(newQty.coerceAtLeast(minQuantity))
                }
            },
            enabled = canDecrement,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Decrease quantity",
                tint = if (canDecrement) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Value
        Text(
            text = displayValue,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Plus Button
        IconButton(
            onClick = {
                if (canIncrement) {
                    val newQty = ((quantity + step) * 100).roundToInt() / 100f
                    onQuantityChange(newQty.coerceAtMost(maxQuantity))
                }
            },
            enabled = canIncrement,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Increase quantity",
                tint = if (canIncrement) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
