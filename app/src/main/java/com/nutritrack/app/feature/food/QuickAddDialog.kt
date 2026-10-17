package com.nutritrack.app.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R
import com.nutritrack.app.designsystem.components.NtTextField

@Composable
fun QuickAddDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, calories: Double, protein: Double, carbs: Double, fat: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var caloriesText by remember { mutableStateOf("") }
    var proteinText by remember { mutableStateOf("") }
    var carbsText by remember { mutableStateOf("") }
    var fatText by remember { mutableStateOf("") }
    var caloriesError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                text = stringResource(id = R.string.quick_add_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val calories = caloriesText.toDoubleOrNull()
                    if (calories == null || calories <= 0) {
                        caloriesError = "Enter valid calories (> 0)"
                    } else {
                        val protein = proteinText.toDoubleOrNull() ?: 0.0
                        val carbs = carbsText.toDoubleOrNull() ?: 0.0
                        val fat = fatText.toDoubleOrNull() ?: 0.0
                        onConfirm(
                            if (name.isBlank()) "Quick Add" else name.trim(),
                            calories,
                            protein,
                            carbs,
                            fat
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.save),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NtTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(id = R.string.food_name_optional),
                placeholder = "e.g. Samosa, Chai",
                modifier = Modifier.fillMaxWidth()
            )

            NtTextField(
                value = caloriesText,
                onValueChange = {
                    caloriesText = it.filter { ch -> ch.isDigit() || ch == '.' }
                    if (caloriesError != null) caloriesError = null
                },
                label = stringResource(id = R.string.calories_required),
                placeholder = "e.g. 250",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = caloriesError != null,
                errorMessage = caloriesError,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Macros (optional):",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NtTextField(
                    value = proteinText,
                    onValueChange = { proteinText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Protein (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                NtTextField(
                    value = carbsText,
                    onValueChange = { carbsText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Carbs (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )

                NtTextField(
                    value = fatText,
                    onValueChange = { fatText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = "Fat (g)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    },
    modifier = modifier
)
}
