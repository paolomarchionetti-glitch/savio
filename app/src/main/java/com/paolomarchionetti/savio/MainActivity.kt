package com.paolomarchionetti.savio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.paolomarchionetti.savio.core.designsystem.SavioTheme
import com.paolomarchionetti.savio.core.navigation.AppNavGraph
import dagger.hilt.android.AndroidEntryPoint

/**
 * Unica Activity dell'app (single-activity architecture).
 * Tutta la navigazione avviene tramite Navigation Compose dentro AppNavGraph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SavioTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph()
                }
            }
        }
    }
}
