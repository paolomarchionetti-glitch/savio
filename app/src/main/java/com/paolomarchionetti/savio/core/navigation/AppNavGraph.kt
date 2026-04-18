package com.paolomarchionetti.savio.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paolomarchionetti.savio.feature.area.AreaSelectionScreen
import com.paolomarchionetti.savio.feature.onboarding.OnboardingScreen
import com.paolomarchionetti.savio.feature.results.ResultsScreen
import com.paolomarchionetti.savio.feature.settings.SettingsScreen
import com.paolomarchionetti.savio.feature.shoppinglist.ShoppingListScreen
import com.paolomarchionetti.savio.feature.storedetail.StoreDetailScreen

/**
 * Grafo di navigazione principale dell'app.
 * Single-activity: tutta la navigazione avviene qui tramite NavHost.
 *
 * Flusso MVP:
 * Onboarding → AreaSelection → ShoppingList → Results → StoreDetail
 *
 * TODO: Gestire startDestination in base a SharedPrefs (onboarding già visto?)
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController  = navController,
        startDestination = AppDestination.Onboarding.route
    ) {

        // ── Onboarding ────────────────────────────────────────────────────────
        composable(AppDestination.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(AppDestination.AreaSelection.route) {
                        popUpTo(AppDestination.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Selezione area ────────────────────────────────────────────────────
        composable(AppDestination.AreaSelection.route) {
            AreaSelectionScreen(
                onAreaSelected = {
                    navController.navigate(AppDestination.ShoppingList.route) {
                        popUpTo(AppDestination.AreaSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Lista spesa ───────────────────────────────────────────────────────
        composable(AppDestination.ShoppingList.route) {
            ShoppingListScreen(
                onNavigateToResults = {
                    navController.navigate(AppDestination.Results.route)
                },
                onNavigateToSettings = {
                    navController.navigate(AppDestination.Settings.route)
                }
            )
        }

        // ── Risultati ─────────────────────────────────────────────────────────
        composable(AppDestination.Results.route) {
            ResultsScreen(
                onStoreClick = { storeId ->
                    navController.navigate(AppDestination.StoreDetail.createRoute(storeId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Dettaglio supermercato ────────────────────────────────────────────
        composable(
            route = AppDestination.StoreDetail.route,
            arguments = listOf(
                navArgument(AppDestination.StoreDetail.ARG_STORE_ID) {
                    type = NavType.StringType
                }
            )
        ) {
            StoreDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // ── Impostazioni ──────────────────────────────────────────────────────
        composable(AppDestination.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
