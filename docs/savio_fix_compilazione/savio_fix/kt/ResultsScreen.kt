package com.paolomarchionetti.savio.feature.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
// Import espliciti con alias per evitare clash tra i due enum ConfidenceLevel
import com.paolomarchionetti.savio.core.designsystem.components.CoverageBadge
import com.paolomarchionetti.savio.core.designsystem.components.ConfidenceBadge
import com.paolomarchionetti.savio.core.designsystem.components.ConfidenceLevel as UiConfidenceLevel
import com.paolomarchionetti.savio.core.designsystem.components.DataFreshnessLabel
import com.paolomarchionetti.savio.core.designsystem.components.EmptyState
import com.paolomarchionetti.savio.domain.model.BasketEstimate
import com.paolomarchionetti.savio.domain.model.ConfidenceLevel as DomainConfidenceLevel

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultsScreen(
    onStoreClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showMethodologySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Dove conviene", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { showMethodologySheet = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Come funziona")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text  = "Sto confrontando i supermercati…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                uiState.estimates.isEmpty() -> {
                    EmptyState(
                        title    = "Nessun risultato",
                        subtitle = "Non ho trovato supermercati con dati sufficienti per la tua zona. Riprova con una lista più semplice.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            uiState.estimates.firstOrNull()?.let { best ->
                                DataFreshnessLabel(dateLabel = best.dataAsOf.toString())
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        items(uiState.estimates) { estimate ->
                            StoreRankingCard(
                                estimate  = estimate,
                                isTopPick = estimate.rankingPosition == 1,
                                onClick   = { onStoreClick(estimate.store.id) }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text  = "⚠️ Stima basata sui dati coperti nell'area selezionata. I prezzi reali possono variare. Le offerte potrebbero richiedere carta fedeltà.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMethodologySheet) {
        MethodologyBottomSheet(onDismiss = { showMethodologySheet = false })
    }
}

// ── StoreRankingCard ──────────────────────────────────────────────────────────

@Composable
fun StoreRankingCard(
    estimate: BasketEstimate,
    isTopPick: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick    = onClick,
        modifier   = Modifier.fillMaxWidth(),
        colors     = CardDefaults.cardColors(
            containerColor = if (isTopPick)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation  = CardDefaults.cardElevation(
            defaultElevation = if (isTopPick) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTopPick) {
                    Text(text = "🏆", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = estimate.store.name,
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text  = estimate.store.branch,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "€ %.2f".format(estimate.estimatedTotalEur),
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text  = "stima carrello",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Badge — usa gli alias per evitare ambiguità ───────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CoverageBadge(coveragePercent = estimate.coveragePercent)
                ConfidenceBadge(
                    level = when (estimate.confidence) {
                        DomainConfidenceLevel.HIGH   -> UiConfidenceLevel.HIGH
                        DomainConfidenceLevel.MEDIUM -> UiConfidenceLevel.MEDIUM
                        DomainConfidenceLevel.LOW    -> UiConfidenceLevel.LOW
                    }
                )
            }

            if (estimate.uncoveredItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = "Non trovati: ${estimate.uncoveredItems.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── MethodologyBottomSheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MethodologyBottomSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text       = "Come funziona la stima",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text  = """
Savio confronta i supermercati nella tua zona in base alle offerte disponibili per i prodotti della tua lista.

Il punteggio considera:
• Prezzo stimato del carrello coperto
• Percentuale della lista coperta dai dati disponibili
• Freschezza e affidabilità dei dati
• Corrispondenza esatta vs categoria equivalente

La stima è indicativa: i prezzi reali possono variare. I dati provengono da volantini digitali ufficiali e contributi della community verificati.

Savio non garantisce il risparmio — ti aiuta a scegliere dove iniziare.
                """.trimIndent(),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
