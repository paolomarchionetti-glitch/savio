package com.paolomarchionetti.savio.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paolomarchionetti.savio.core.designsystem.*

// ── CoverageBadge ─────────────────────────────────────────────────────────────
/**
 * Badge copertura lista. Es: "78% coperta"
 * Colore semantico: verde ≥70%, ambra 40-69%, grigio <40%
 */
@Composable
fun CoverageBadge(
    coveragePercent: Int,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        coveragePercent >= 70 -> SavioGreen100 to SavioGreen800
        coveragePercent >= 40 -> SavioAmber100 to SavioAmber600
        else                  -> SavioNeutral100 to SavioNeutral600
    }
    Badge(
        containerColor = bgColor,
        contentColor   = textColor,
        modifier       = modifier
    ) {
        Text(
            text       = "$coveragePercent% coperta",
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── ConfidenceBadge ───────────────────────────────────────────────────────────
/**
 * Badge affidabilità dato. Es: "Affidabilità: Alta"
 */
enum class ConfidenceLevel(val label: String) {
    HIGH("Alta"), MEDIUM("Media"), LOW("Bassa")
}

@Composable
fun ConfidenceBadge(
    level: ConfidenceLevel,
    modifier: Modifier = Modifier
) {
    val color = when (level) {
        ConfidenceLevel.HIGH   -> ConfidenceHigh
        ConfidenceLevel.MEDIUM -> ConfidenceMedium
        ConfidenceLevel.LOW    -> ConfidenceLow
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text  = "Affidabilità: ${level.label}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── DataFreshnessLabel ────────────────────────────────────────────────────────
/**
 * Label data aggiornamento dati. Sempre visibile — principio trasparenza radicale.
 */
@Composable
fun DataFreshnessLabel(
    dateLabel: String,    // Es: "15/04/2026"
    modifier: Modifier = Modifier
) {
    Text(
        text     = "Dati aggiornati al $dateLabel",
        style    = MaterialTheme.typography.bodySmall,
        color    = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// ── SavioButton ───────────────────────────────────────────────────────────────
@Composable
fun SavioPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        shape    = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text       = text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp
        )
    }
}

@Composable
fun SavioSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick  = onClick,
        shape    = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text(
            text       = text,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp
        )
    }
}

// ── EmptyState ────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text  = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
