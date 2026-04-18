package com.paolomarchionetti.savio.feature.results

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paolomarchionetti.savio.core.designsystem.*
import com.paolomarchionetti.savio.core.designsystem.components.ConfidenceLevel as UiConfidenceLevel
import com.paolomarchionetti.savio.domain.model.BasketEstimate
import com.paolomarchionetti.savio.domain.model.ConfidenceLevel as DomainConfidenceLevel

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
                title = {
                    Column {
                        Text("Dove conviene", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (uiState.estimates.isNotEmpty()) {
                            Text(
                                text  = "Dati al ${uiState.estimates.firstOrNull()?.dataAsOf ?: ""}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                // ── Loading ───────────────────────────────────────────────────
                uiState.isLoading -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = SavioGreen700)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text  = "Sto confrontando i supermercati…",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // ── Vuoto ─────────────────────────────────────────────────────
                uiState.estimates.isEmpty() -> {
                    Column(
                        modifier            = Modifier.fillMaxSize().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("🔍", fontSize = 56.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text       = "Nessun risultato",
                            style      = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign  = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text      = "Prova con prodotti diversi o assicurati di aver selezionato un'area coperta.",
                            style     = MaterialTheme.typography.bodyMedium,
                            color     = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(onClick = onBack) { Text("Modifica la lista") }
                    }
                }

                // ── Risultati ─────────────────────────────────────────────────
                else -> {
                    LazyColumn(
                        modifier        = Modifier.fillMaxSize(),
                        contentPadding  = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Intestazione risultati
                        item {
                            Text(
                                text       = "Supermercati nella tua zona",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier   = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        itemsIndexed(uiState.estimates) { index, estimate ->
                            RankingCard(
                                estimate = estimate,
                                position = index + 1,
                                onClick  = { onStoreClick(estimate.store.id) }
                            )
                        }

                        // Disclaimer
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SavioNeutral100
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text("⚠️", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text  = "Stima basata sui dati coperti. I prezzi reali possono variare. Alcune offerte richiedono carta fedeltà.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SavioNeutral600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showMethodologySheet) {
        MethodologySheet(onDismiss = { showMethodologySheet = false })
    }
}

// ── RankingCard ───────────────────────────────────────────────────────────────

@Composable
fun RankingCard(
    estimate: BasketEstimate,
    position: Int,
    onClick: () -> Unit
) {
    val isWinner = position == 1

    val cardBackground = if (isWinner)
        Brush.linearGradient(listOf(SavioGreen700, SavioGreen600))
    else
        Brush.linearGradient(listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface
        ))

    val textColor     = if (isWinner) White else MaterialTheme.colorScheme.onSurface
    val subTextColor  = if (isWinner) White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isWinner) 6.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) SavioGreen700 else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ── Posizione ─────────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isWinner) White.copy(alpha = 0.2f)
                            else SavioNeutral100
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = when (position) { 1 -> "🏆"; 2 -> "🥈"; else -> "🥉" },
                        fontSize   = if (position == 1) 20.sp else 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // ── Nome negozio ──────────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text       = estimate.store.name,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 17.sp,
                        color      = textColor
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint     = subTextColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text  = estimate.store.branch,
                            style = MaterialTheme.typography.bodySmall,
                            color = subTextColor
                        )
                    }
                }

                // ── Prezzo ────────────────────────────────────────────────────
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text       = "€ %.2f".format(estimate.estimatedTotalEur),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 22.sp,
                        color      = if (isWinner) White else SavioGreen700
                    )
                    Text(
                        text  = "stimato",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ── Barra copertura ───────────────────────────────────────────────
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text  = "Copertura lista",
                        style = MaterialTheme.typography.labelSmall,
                        color = subTextColor
                    )
                    Text(
                        text       = "${estimate.coveragePercent}%",
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (isWinner) White else when {
                            estimate.coveragePercent >= 70 -> SavioGreen700
                            estimate.coveragePercent >= 40 -> SavioAmber600
                            else                           -> SavioNeutral600
                        }
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress          = { estimate.coveragePercent / 100f },
                    modifier          = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color             = if (isWinner) White else SavioGreen700,
                    trackColor        = if (isWinner) White.copy(alpha = 0.25f) else SavioNeutral100
                )
            }

            // Prodotti non trovati
            if (estimate.uncoveredItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = "Non trovati: ${estimate.uncoveredItems.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = subTextColor
                )
            }
        }
    }
}

// ── MethodologySheet ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MethodologySheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 40.dp)) {
            Text(
                text       = "Come funziona la stima",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            listOf(
                "💰" to "Prezzo stimato del carrello coperto",
                "📋" to "Percentuale della lista coperta dai dati",
                "📅" to "Freschezza e affidabilità del dato",
                "🔁" to "Match esatto vs categoria equivalente"
            ).forEach { (emoji, text) ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Surface(color = SavioGreen050, shape = RoundedCornerShape(10.dp)) {
                Text(
                    text     = "Savio non garantisce il risparmio — ti aiuta a scegliere dove iniziare. Verifica sempre il prezzo in negozio.",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = SavioGreen800,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}
