package com.paolomarchionetti.savio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class principale.
 * @HiltAndroidApp avvia la generazione del codice Hilt e inizializza il grafo DI.
 * Nessuna logica di business qui — solo init infrastrutturale.
 */
@HiltAndroidApp
class SavioApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Future: inizializzazione Firebase Crashlytics, analytics, WorkManager
    }
}
