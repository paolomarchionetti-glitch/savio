package com.paolomarchionetti.savio.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Impostazioni", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text  = "Legale",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            SettingsItem(
                icon     = Icons.Default.Lock,
                title    = "Privacy Policy",
                subtitle = "Come trattiamo i tuoi dati",
                onClick  = {
                    // TODO: sostituire con URL reale
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://savio.app/privacy")))
                }
            )

            SettingsItem(
                icon     = Icons.Default.Info,
                title    = "Termini e Condizioni",
                onClick  = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://savio.app/terms")))
                }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text  = "App",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
            )

            SettingsItem(
                icon     = Icons.Default.Star,
                title    = "Valuta Savio",
                subtitle = "Su Google Play Store",
                onClick  = {
                    context.startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.paolomarchionetti.savio")))
                }
            )

            SettingsItem(
                icon     = Icons.Default.Email,
                title    = "Invia feedback",
                onClick  = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data    = Uri.parse("mailto:feedback@savio.app")
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback Savio")
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text  = "Savio v1.0.0 · com.paolomarchionetti.savio\nNessun dato personale raccolto. Nessun account richiesto.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(text = it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(
            imageVector        = Icons.Default.ChevronRight,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
