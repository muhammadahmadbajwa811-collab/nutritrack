package com.nutritrack.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nutritrack.app.R

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToGallery: () -> Unit = {}
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(16.dp))

        com.nutritrack.app.designsystem.components.NtButton(
            text = "Open Component Gallery",
            onClick = onNavigateToGallery,
            isFullWidth = false
        )
    }
}
