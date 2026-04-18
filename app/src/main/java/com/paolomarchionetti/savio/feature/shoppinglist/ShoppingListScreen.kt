package com.paolomarchionetti.savio.feature.shoppinglist

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.paolomarchionetti.savio.core.designsystem.*
import com.paolomarchionetti.savio.domain.model.ShoppingListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateToResults: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ShoppingListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(SavioGreen700, SavioGreen600)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier            = Modifier.fillMaxWidth(),
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text       = "Savio",
                            color      = White,
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text  = "La tua lista della spesa",
                            color = White.copy(alpha = 0.75f),
                            fontSize = 13.sp
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Impostazioni",
                            tint = White
                        )
                    }
                }
            }

            // ── Campo inserimento ──────────────────────────────────────────────
            Surface(
                modifier  = Modifier.fillMaxWidth(),
                color     = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value         = uiState.inputText,
                        onValueChange = viewModel::onInputChanged,
                        placeholder   = { Text("Aggiungi prodotto…  es. latte, pane, mele") },
                        leadingIcon   = {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon  = {
                            AnimatedVisibility(visible = uiState.inputText.isNotBlank()) {
                                IconButton(onClick = viewModel::addItem) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.ArrowForward,
                                            contentDescription = "Aggiungi",
                                            tint = White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        },
                        shape      = RoundedCornerShape(14.dp),
                        singleLine = true,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // Suggerimenti autocomplete
                    AnimatedVisibility(
                        visible = uiState.suggestions.isNotEmpty(),
                        enter   = fadeIn() + expandVertically(),
                        exit    = fadeOut() + shrinkVertically()
                    ) {
                        LazyRow(
                            modifier            = Modifier.padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.suggestions) { suggestion ->
                                SuggestionChip(
                                    onClick = { viewModel.addItemFromSuggestion(suggestion) },
                                    label   = { Text(suggestion, fontSize = 13.sp) },
                                    icon    = {
                                        Icon(
                                            Icons.Default.Search,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Lista prodotti ────────────────────────────────────────────────
            if (uiState.items.isEmpty()) {
                EmptyListPlaceholder(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    modifier        = Modifier.weight(1f),
                    contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text  = "${uiState.items.size} prodott${if (uiState.items.size == 1) "o" else "i"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(uiState.items, key = { it.id }) { item ->
                        AnimatedVisibility(
                            visible = true,
                            enter   = fadeIn(tween(200)) + slideInVertically(),
                        ) {
                            ShoppingItemCard(
                                item     = item,
                                onRemove = { viewModel.removeItem(item.id) }
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // spazio FAB
                }
            }
        }

        // ── FAB "Dove conviene?" ──────────────────────────────────────────────
        AnimatedVisibility(
            visible = uiState.items.isNotEmpty(),
            enter   = fadeIn() + scaleIn(),
            exit    = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            Button(
                onClick = onNavigateToResults,
                shape   = RoundedCornerShape(50),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = SavioGreen700
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = White)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text       = "Dove conviene?",
                    color      = White,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 16.sp
                )
            }
        }
    }
}

// ── ShoppingItemCard ──────────────────────────────────────────────────────────

@Composable
private fun ShoppingItemCard(
    item: ShoppingListItem,
    onRemove: () -> Unit
) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pallino colorato categoria
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SavioGreen500)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = item.name,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (item.quantity > 1) {
                    Text(
                        text  = "× ${item.quantity}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Close,
                    contentDescription = "Rimuovi",
                    tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier           = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ── EmptyListPlaceholder ──────────────────────────────────────────────────────

@Composable
private fun EmptyListPlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🛒", fontSize = 56.sp)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text       = "Lista vuota",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text  = "Aggiungi i prodotti che vuoi comprare.\nScrivi anche solo 'latte' o 'pane integrale' — Savio capisce.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
