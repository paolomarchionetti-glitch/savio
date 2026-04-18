package com.paolomarchionetti.savio.feature.storedetail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paolomarchionetti.savio.domain.model.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreDetailScreen(
    onBack: () -> Unit,
    viewModel: StoreDetailViewModel = hiltViewModel()
) {
    val store by viewModel.store.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(store?.name ?: "Supermercato", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        store?.let { s ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Info base ─────────────────────────────────────────────────
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = s.name,  style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(text = s.branch, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = s.address, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // ── Link esterni ──────────────────────────────────────────────
                s.mapsUrl?.let { url ->
                    OutlinedButton(
                        onClick  = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apri in Maps")
                    }
                }

                s.leafletUrl?.let { url ->
                    OutlinedButton(
                        onClick  = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.List, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Vedi volantino ufficiale")
                    }
                }

                // ── Nota trasparenza ──────────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text     = "I prezzi mostrati sono stime basate su offerte volantino e contributi utenti verificati. Verifica sempre il prezzo in negozio.",
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
