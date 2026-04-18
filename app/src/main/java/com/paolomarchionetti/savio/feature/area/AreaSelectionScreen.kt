package com.paolomarchionetti.savio.feature.area

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paolomarchionetti.savio.core.designsystem.components.SavioPrimaryButton
import com.paolomarchionetti.savio.domain.model.Area

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun AreaSelectionScreen(
    onAreaSelected: () -> Unit,
    viewModel: AreaSelectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text       = "Dove fai la spesa?",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = "Seleziona la tua zona. Nessun permesso di posizione necessario.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Ricerca per CAP ───────────────────────────────────────────────────
        OutlinedTextField(
            value         = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChanged,
            label         = { Text("CAP o città") },
            leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine    = true,
            modifier      = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.filteredAreas.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                Text(
                    text  = "Nessuna zona trovata per \"${uiState.searchQuery}\".\nL'app è disponibile a Bologna per ora.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                // ── Lista aree disponibili ─────────────────────────────────
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.filteredAreas) { area ->
                        AreaCard(
                            area       = area,
                            isSelected = area.id == uiState.selectedAreaId,
                            onClick    = { viewModel.selectArea(area) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        SavioPrimaryButton(
            text    = "Conferma zona",
            enabled = uiState.selectedAreaId != null,
            onClick = {
                viewModel.confirmArea()
                onAreaSelected()
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AreaCard(
    area: Area,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) CardDefaults.outlinedCardBorder() else null
    ) {
        Row(
            modifier            = Modifier.padding(16.dp),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text       = area.displayName,
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
                Text(
                    text  = "CAP ${area.cap} · ${area.city}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
