package com.paolomarchionetti.savio.feature.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paolomarchionetti.savio.core.designsystem.SavioGreen700
import com.paolomarchionetti.savio.core.designsystem.SavioNeutral200
import com.paolomarchionetti.savio.core.designsystem.components.SavioPrimaryButton
import com.paolomarchionetti.savio.core.designsystem.components.SavioSecondaryButton
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val emoji: String,
    val title: String,
    val subtitle: String
)

private val pages = listOf(
    OnboardingPage(
        emoji    = "🛒",
        title    = "La tua lista, la tua spesa",
        subtitle = "Aggiungi quello che vuoi comprare. Anche 'mele' o 'pasta al pomodoro' — Savio capisce."
    ),
    OnboardingPage(
        emoji    = "📍",
        title    = "Dove conviene oggi",
        subtitle = "Savio confronta i supermercati nella tua zona e ti dice dove la tua lista costa meno."
    ),
    OnboardingPage(
        emoji    = "✅",
        title    = "Trasparente sempre",
        subtitle = "Vedi quanto è coperta la stima, la data dei dati e il livello di affidabilità. Nessuna sorpresa."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope      = rememberCoroutineScope()

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // ── Logo ──────────────────────────────────────────────────────────────
        Text(
            text       = "Savio",
            style      = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.primary
        )

        // ── Pager ─────────────────────────────────────────────────────────────
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // ── Indicatori pagina ─────────────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            repeat(pages.size) { idx ->
                val isSelected = pagerState.currentPage == idx
                Box(
                    modifier = Modifier
                        .animateContentSize()
                        .height(8.dp)
                        .width(if (isSelected) 24.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) SavioGreen700 else SavioNeutral200)
                )
            }
        }

        // ── CTA ───────────────────────────────────────────────────────────────
        if (pagerState.currentPage < pages.size - 1) {
            SavioPrimaryButton(
                text    = "Avanti",
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            SavioSecondaryButton(
                text    = "Salta",
                onClick = {
                    viewModel.completeOnboarding()
                    onOnboardingComplete()
                }
            )
        } else {
            SavioPrimaryButton(
                text    = "Inizia a risparmiare",
                onClick = {
                    viewModel.completeOnboarding()
                    onOnboardingComplete()
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text     = page.emoji,
            fontSize = 72.sp
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text      = page.title,
            style     = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text      = page.subtitle,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
