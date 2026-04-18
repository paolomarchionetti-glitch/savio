# Savio — Struttura completa e guida operativa
**Versione progetto**: 1.0 con integrazione Pesaro + Automazione Gemini  
**Data**: Aprile 2026 | **Autore**: Paolo Marchionetti

---

## Indice

1. [Struttura completa cartelle e file](#1-struttura-completa-cartelle-e-file)
2. [Legenda — stato di ogni file](#2-legenda--stato-di-ogni-file)
3. [Prerequisiti di sistema](#3-prerequisiti-di-sistema)
4. [Fase A — Primo build Android](#4-fase-a--primo-build-android)
5. [Fase B — Sostituzione dati Pesaro](#5-fase-b--sostituzione-dati-pesaro)
6. [Fase C — Loghi PNG nei drawable](#6-fase-c--loghi-png-nei-drawable)
7. [Fase D — StoreLogoUtils.kt](#7-fase-d--storelogoutilskt)
8. [Fase E — CDN sync nell'app Android](#8-fase-e--cdn-sync-nellapp-android)
9. [Fase F — Automazione Gemini in locale](#9-fase-f--automazione-gemini-in-locale)
10. [Fase G — GitHub Actions cron settimanale](#10-fase-g--github-actions-cron-settimanale)
11. [Test completo end-to-end](#11-test-completo-end-to-end)
12. [Aggiornamento offerte manuale (fallback)](#12-aggiornamento-offerte-manuale-fallback)

---

## 1. Struttura completa cartelle e file

Questa è la struttura **definitiva** del progetto dopo tutte le modifiche.  
I simboli indicano lo stato di ogni file (vedi legenda alla sezione 2).

```
savio/
│
├── .gitignore                                    ✅ già presente
├── README.md                                     ✅ già presente
├── build.gradle.kts                              ✅ già presente
├── gradle.properties                             ✅ già presente
├── settings.gradle.kts                           ✅ già presente
│
├── .github/                                      🆕 CREARE questa cartella
│   └── workflows/
│       └── update_offers.yml                     🆕 da savio_gemini.zip
│
├── gradle/
│   └── libs.versions.toml                        ✅ già presente
│
├── scripts/                                      🆕 CREARE questa cartella
│   ├── savio_update_offers.py                    🆕 da savio_gemini.zip
│   ├── savio_cdn_push.py                         🆕 da savio_automazione_completa.zip
│   └── requirements.txt                          🆕 da savio_gemini.zip
│
└── app/
    ├── build.gradle.kts                          ✅ già presente
    ├── proguard-rules.pro                        ✅ già presente
    │
    └── src/
        └── main/
            │
            ├── AndroidManifest.xml               ✅ già presente
            │
            ├── assets/                           ← JSON letti dall'app + aggiornati dall'automazione
            │   ├── areas_v1.json                 🔄 SOSTITUIRE — da savio_pesaro.zip (+3 aree Pesaro)
            │   ├── catalog_v1.json               ✅ NON toccare
            │   ├── equivalences_v1.json          ✅ NON toccare
            │   ├── offers_current.json           🔄 SOSTITUIRE — da savio_pesaro.zip (185 offerte)
            │   └── stores_pilot_v1.json          🔄 SOSTITUIRE — da savio_pesaro.zip (+5 negozi Pesaro)
            │
            ├── java/com/paolomarchionetti/savio/
            │   │
            │   ├── SavioApp.kt                   ✅ già presente
            │   ├── MainActivity.kt               ✅ già presente
            │   │
            │   ├── core/
            │   │   │
            │   │   ├── common/
            │   │   │   ├── Result.kt             ⚠️  DA CREARE (codice in sezione 4)
            │   │   │   └── Extensions.kt         ⚠️  DA CREARE (codice in sezione 4)
            │   │   │
            │   │   ├── util/
            │   │   │   ├── DateUtils.kt          ⚠️  DA CREARE (codice in sezione 4)
            │   │   │   ├── PriceFormatter.kt     ⚠️  DA CREARE (codice in sezione 4)
            │   │   │   └── StoreLogoUtils.kt     🆕 DA CREARE (codice in sezione 7)
            │   │   │
            │   │   ├── designsystem/
            │   │   │   ├── Color.kt              ✅ già presente
            │   │   │   ├── Typography.kt         ✅ già presente
            │   │   │   ├── Theme.kt              ✅ già presente
            │   │   │   └── components/
            │   │   │       └── SavioComponents.kt ✅ già presente
            │   │   │
            │   │   └── navigation/
            │   │       ├── AppDestination.kt     ✅ già presente
            │   │       └── AppNavGraph.kt        ✅ già presente
            │   │
            │   ├── data/
            │   │   ├── assets/
            │   │   │   ├── AssetDataSource.kt    🔄 MODIFICARE — aggiungere CDN sync (sezione 8)
            │   │   │   └── dto/
            │   │   │       └── Dtos.kt           ✅ già presente
            │   │   │
            │   │   ├── local/
            │   │   │   ├── dao/
            │   │   │   │   └── Daos.kt           ✅ già presente
            │   │   │   ├── datastore/
            │   │   │   │   └── UserPreferencesDataStore.kt  ✅ già presente
            │   │   │   ├── db/
            │   │   │   │   └── AppDatabase.kt    ✅ già presente
            │   │   │   └── entity/
            │   │   │       └── Entities.kt       ✅ già presente
            │   │   │
            │   │   ├── mapper/
            │   │   │   └── Mappers.kt            ✅ già presente
            │   │   │
            │   │   └── repository/
            │   │       └── RepositoryImpls.kt    ✅ già presente
            │   │
            │   ├── domain/
            │   │   ├── model/
            │   │   │   └── Models.kt             ✅ già presente
            │   │   ├── repository/
            │   │   │   └── Repositories.kt       ✅ già presente
            │   │   └── usecase/
            │   │       ├── GetBestStoreForListUseCase.kt  ✅ già presente
            │   │       └── UseCases.kt           ✅ già presente
            │   │
            │   ├── di/
            │   │   └── Modules.kt                ✅ già presente
            │   │
            │   └── feature/
            │       ├── onboarding/
            │       │   ├── OnboardingScreen.kt   ✅ già presente
            │       │   └── OnboardingViewModel.kt ✅ già presente
            │       ├── area/
            │       │   ├── AreaSelectionScreen.kt  ✅ già presente
            │       │   └── AreaSelectionViewModel.kt ✅ già presente
            │       ├── shoppinglist/
            │       │   ├── ShoppingListScreen.kt ✅ già presente
            │       │   ├── ShoppingListViewModel.kt ✅ già presente
            │       │   └── components/           ✅ cartella vuota (per future card animate)
            │       ├── results/
            │       │   ├── ResultsScreen.kt      ✅ già presente
            │       │   ├── ResultsViewModel.kt   ✅ già presente
            │       │   └── components/           ✅ cartella vuota (per MiniStoreCard v2)
            │       ├── storedetail/
            │       │   ├── StoreDetailScreen.kt  ✅ già presente
            │       │   └── StoreDetailViewModel.kt ✅ già presente
            │       └── settings/
            │           └── SettingsScreen.kt     ✅ già presente
            │
            └── res/
                │
                ├── drawable/                     ← loghi insegne (tutti da savio_automazione_completa.zip)
                │   ├── ic_store_esselunga.png    🆕 da logos/ dello ZIP automazione
                │   ├── ic_store_conad.png        🆕
                │   ├── ic_store_coop.png         🆕
                │   ├── ic_store_lidl.png         🆕
                │   ├── ic_store_eurospin.png     🆕
                │   ├── ic_store_carrefour.png    🆕
                │   └── ic_store_md.png           🆕
                │
                ├── drawable-hdpi/                ← versione 1.5× (288px)
                │   └── ic_store_*.png            🆕 da logos/drawable-hdpi/ dello ZIP
                │
                ├── drawable-xhdpi/               ← versione 2× (384px)
                │   └── ic_store_*.png            🆕 da logos/drawable-xhdpi/ dello ZIP
                │
                ├── drawable-xxhdpi/              ← versione 3× (576px)
                │   └── ic_store_*.png            🆕 da logos/drawable-xxhdpi/ dello ZIP
                │
                ├── mipmap-mdpi/                  ⚠️  DA GENERARE — Image Asset Studio
                │   ├── ic_launcher.png
                │   └── ic_launcher_round.png
                ├── mipmap-hdpi/                  ⚠️  DA GENERARE
                │   ├── ic_launcher.png
                │   └── ic_launcher_round.png
                ├── mipmap-xhdpi/                 ⚠️  DA GENERARE
                │   ├── ic_launcher.png
                │   └── ic_launcher_round.png
                ├── mipmap-xxhdpi/                ⚠️  DA GENERARE
                │   ├── ic_launcher.png
                │   └── ic_launcher_round.png
                ├── mipmap-xxxhdpi/               ⚠️  DA GENERARE
                │   ├── ic_launcher.png
                │   └── ic_launcher_round.png
                │
                ├── values/
                │   ├── colors.xml                ✅ già presente
                │   ├── strings.xml               ✅ già presente
                │   └── themes.xml                ✅ già presente
                │
                └── xml/
                    ├── backup_rules.xml          ✅ già presente
                    ├── data_extraction_rules.xml ✅ già presente
                    └── network_security_config.xml ✅ già presente
```

---

## 2. Legenda — stato di ogni file

| Simbolo | Significato |
|---------|-------------|
| ✅ | Già presente nel progetto — non toccare |
| 🔄 | Sostituire con la versione aggiornata fornita |
| 🆕 | File nuovo da aggiungere |
| ⚠️ | Da creare manualmente (codice fornito in questa guida) |

**ZIP di riferimento e cosa contengono:**

| ZIP | Contenuto |
|-----|-----------|
| `savio_pesaro.zip` | `areas_v1.json`, `stores_pilot_v1.json`, `offers_current.json` aggiornati + guida Pesaro |
| `savio_automazione_completa.zip` | `scripts/`, `.github/workflows/` (versione Claude), loghi PNG in tutte le density |
| `savio_gemini.zip` | `savio_update_offers.py` (Gemini), `requirements.txt`, `update_offers.yml` aggiornati |

> **Nota**: dalla ZIP automazione completa prendi **solo i loghi PNG** e lo script `savio_cdn_push.py`. Lo script `savio_update_offers.py` e il workflow `update_offers.yml` vanno presi dalla ZIP **savio_gemini** (versione Gemini, quella corretta da usare).

---

## 3. Prerequisiti di sistema

Prima di iniziare verifica di avere tutto installato:

**Android / Kotlin:**
- Android Studio Iguana o superiore (2024.1+)
- JDK 17 (incluso in Android Studio)
- Android SDK con API 26 minima e API 35 target
- Emulatore o device fisico Android 8.0+

**Python (per l'automazione):**
- Python 3.11 o superiore
- pip aggiornato

**Account necessari:**
- Account Google per ottenere la Gemini API key (gratuito)
- Account GitHub per il repository e le Actions (gratuito)

**NON serve:**
- Account Anthropic / Claude API
- Alcun metodo di pagamento per l'automazione

---

## 4. Fase A — Primo build Android

### A1 — Apri il progetto

1. Decomprimi `savio_android_project.zip` in una cartella a tua scelta
2. Apri **Android Studio → File → Open** → seleziona la cartella `savio/`
3. Attendi il Gradle sync (scarica circa 200MB di dipendenze al primo avvio)
4. Se Gradle chiede di aggiornare, accetta

### A2 — Crea le icone launcher (obbligatorio per compilare)

Senza le icone launcher il build fallisce con errore `resource not found`.

1. Nel pannello **Project** a sinistra espandi `app → src → main → res`
2. Clic destro su `res` → **New → Image Asset**
3. Nella finestra Image Asset Studio:
   - **Icon Type**: Launcher Icons (Adaptive and Legacy)
   - **Name**: `ic_launcher`
   - **Source Asset**: seleziona **Text**
   - **Text**: `S`
   - **Font**: qualsiasi font Bold
   - **Color**: clicca il quadratino colore → inserisci `#1B6B58` (verde Savio)
4. Clicca **Next → Finish**

Android Studio genera automaticamente tutte le density (`mipmap-mdpi` fino a `mipmap-xxxhdpi`) con versione normale e round.

### A3 — Crea i file utility mancanti

Questi 4 file sono riferiti nel codice ma non ancora presenti. Creali in Android Studio con **File → New → Kotlin File/Class**.

**`core/common/Result.kt`**
```kotlin
package com.paolomarchionetti.savio.core.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (String) -> Unit): Result<T> {
    if (this is Result.Error) action(message)
    return this
}
```

**`core/common/Extensions.kt`**
```kotlin
package com.paolomarchionetti.savio.core.common

fun String.normalizeForSearch(): String =
    this.lowercase().trim()
        .replace("à", "a").replace("è", "e").replace("é", "e")
        .replace("ì", "i").replace("ò", "o").replace("ù", "u")

fun Double.toEurString(): String = "€ %.2f".format(this).replace(".", ",")
```

**`core/util/PriceFormatter.kt`**
```kotlin
package com.paolomarchionetti.savio.core.util

object PriceFormatter {
    fun format(price: Double): String = "€ %.2f".format(price).replace(".", ",")
    fun formatDiff(diff: Double): String {
        val sign = if (diff >= 0) "+" else ""
        return "$sign%.2f€".format(diff).replace(".", ",")
    }
}
```

**`core/util/DateUtils.kt`**
```kotlin
package com.paolomarchionetti.savio.core.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val IT_FORMATTER = DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN)

    fun formatShort(date: LocalDate): String = date.format(IT_FORMATTER)

    fun formatDataAsOf(date: LocalDate): String = "Dati al ${formatShort(date)}"

    fun isExpired(date: LocalDate): Boolean = date.isBefore(LocalDate.now())
}
```

### A4 — Verifica `local.properties`

Android Studio lo genera automaticamente all'apertura, ma se non esiste:

1. Crea il file `local.properties` nella root del progetto (stessa cartella di `build.gradle.kts`)
2. Contenuto:

```properties
sdk.dir=/Users/TUONOME/Library/Android/sdk
```

Su Windows: `sdk.dir=C\:\\Users\\TUONOME\\AppData\\Local\\Android\\Sdk`  
Su Linux: `sdk.dir=/home/TUONOME/Android/Sdk`

Il percorso esatto lo trovi in Android Studio → **File → Project Structure → SDK Location**.

### A5 — Prima compilazione

1. **Build → Rebuild Project** — deve completarsi senza errori
2. **Run → Run 'app'** — seleziona emulatore (Pixel 5, API 31 consigliato)
3. L'app si avvia sull'**OnboardingScreen**

Se vedi errori Hilt o Ksp durante il primo build, prova **Build → Clean Project** poi **Rebuild Project**.

---

## 5. Fase B — Sostituzione dati Pesaro

### B1 — Sostituisci i 3 file JSON

Dal file `savio_pesaro.zip` estrai i 3 JSON aggiornati e copiali nella cartella:

```
app/src/main/assets/
```

I file da sostituire sono:

| File | Prima | Dopo |
|------|-------|------|
| `areas_v1.json` | 5 aree Bologna | 8 aree (Bologna + 3 Pesaro) |
| `stores_pilot_v1.json` | 6 negozi Bologna | 11 negozi (+ 5 Pesaro) |
| `offers_current.json` | ~60 offerte Bologna | 185 offerte (Bologna + Pesaro) |

**Da Android Studio**: clic destro sul file da sostituire nel pannello Project → **Replace File** → seleziona il nuovo.

**Da terminale** (dalla root del progetto):
```bash
cp percorso/savio_pesaro/areas_v1.json        app/src/main/assets/
cp percorso/savio_pesaro/stores_pilot_v1.json  app/src/main/assets/
cp percorso/savio_pesaro/offers_current.json   app/src/main/assets/
```

### B2 — Invalida la cache di Android Studio

**File → Invalidate Caches / Restart → Invalidate and Restart**

Questo assicura che Android Studio non usi i vecchi JSON cachati.

### B3 — Verifica

Avvia l'app → AreaSelection → scrivi `Pesaro` nel campo di ricerca → devono apparire 3 aree:
- Pesaro — Centro / Baia Flaminia
- Pesaro — Nord / Muraglia / Tombaccia
- Pesaro — Sud / Villa Fastiggi / Borgo Santa Maria

---

## 6. Fase C — Loghi PNG nei drawable

### C1 — Dove trovare i loghi

Estrai `savio_automazione_completa.zip`. I loghi sono nella sottocartella `savio_pack/logos/`.

```
savio_pack/logos/
├── ic_store_esselunga.png     ← copia in res/drawable/
├── ic_store_conad.png
├── ic_store_coop.png
├── ic_store_lidl.png
├── ic_store_eurospin.png
├── ic_store_carrefour.png
├── ic_store_md.png
├── drawable-hdpi/             ← copia i PNG in res/drawable-hdpi/
│   └── ic_store_*.png (7 file)
├── drawable-xhdpi/            ← copia i PNG in res/drawable-xhdpi/
│   └── ic_store_*.png (7 file)
└── drawable-xxhdpi/           ← copia i PNG in res/drawable-xxhdpi/
    └── ic_store_*.png (7 file)
```

### C2 — Copia i file nelle cartelle corrette

**Da terminale** (dalla root del progetto):
```bash
# Crea le cartelle se non esistono
mkdir -p app/src/main/res/drawable
mkdir -p app/src/main/res/drawable-hdpi
mkdir -p app/src/main/res/drawable-xhdpi
mkdir -p app/src/main/res/drawable-xxhdpi

# Copia tutti i PNG (adatta il percorso alla tua cartella di download)
LOGOS="percorso/savio_pack/logos"

cp $LOGOS/ic_store_*.png              app/src/main/res/drawable/
cp $LOGOS/drawable-hdpi/ic_store_*.png  app/src/main/res/drawable-hdpi/
cp $LOGOS/drawable-xhdpi/ic_store_*.png app/src/main/res/drawable-xhdpi/
cp $LOGOS/drawable-xxhdpi/ic_store_*.png app/src/main/res/drawable-xxhdpi/
```

**Da Android Studio**: clic destro sulla cartella `res/drawable` → **Show in Finder/Explorer** → incolla i file direttamente dal Finder.

### C3 — Verifica in Android Studio

Nel pannello Project espandi `res/drawable` — devono comparire i 7 file `ic_store_*.png` con l'anteprima dell'immagine visibile.

---

## 7. Fase D — StoreLogoUtils.kt

Questo file mappa ogni `chain` del JSON al drawable corrispondente. Crea il file in `core/util/`.

**`core/util/StoreLogoUtils.kt`**
```kotlin
package com.paolomarchionetti.savio.core.util

import androidx.annotation.DrawableRes
import com.paolomarchionetti.savio.R

/**
 * Restituisce il DrawableRes del logo per una catena di supermercati.
 * Usato in StoreRankingCard (ResultsScreen) e StoreDetailScreen.
 * Ritorna null se la catena non ha un logo — la UI mostra le iniziali come fallback.
 */
@DrawableRes
fun chainToLogoRes(chain: String): Int? = when (chain.lowercase().trim()) {
    "esselunga"  -> R.drawable.ic_store_esselunga
    "conad"      -> R.drawable.ic_store_conad
    "coop"       -> R.drawable.ic_store_coop
    "lidl"       -> R.drawable.ic_store_lidl
    "eurospin"   -> R.drawable.ic_store_eurospin
    "carrefour"  -> R.drawable.ic_store_carrefour
    "md"         -> R.drawable.ic_store_md
    else         -> null
}

/** Colore accent del brand — usato per badge e bordi nelle card. */
fun chainToColor(chain: String): Long = when (chain.lowercase().trim()) {
    "esselunga"  -> 0xFF1B6B58
    "conad"      -> 0xFFE3000F
    "coop"       -> 0xFF005CA9
    "lidl"       -> 0xFF0050AA
    "eurospin"   -> 0xFFF47920
    "carrefour"  -> 0xFF1E3A8A
    "md"         -> 0xFFD61A1A
    else         -> 0xFF888888
}
```

### Come usare il logo nelle card dei risultati

In `ResultsScreen.kt`, nella `StoreRankingCard`, aggiungi un composable `StoreLogoImage`:

```kotlin
import com.paolomarchionetti.savio.core.util.chainToLogoRes

@Composable
fun StoreLogoImage(chain: String, modifier: Modifier = Modifier) {
    val logoRes = chainToLogoRes(chain)
    if (logoRes != null) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = chain,
            modifier = modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Fit
        )
    } else {
        Box(
            modifier = modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = chain.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
```

---

## 8. Fase E — CDN sync nell'app Android

Questa modifica permette all'app di scaricare automaticamente le offerte aggiornate da GitHub Releases all'avvio, senza dover pubblicare un nuovo APK.

### E1 — Verifica BuildConfig.DATA_BASE_URL

In `app/build.gradle.kts` la riga è già presente:

```kotlin
buildConfigField("String", "DATA_BASE_URL",
    "\"https://raw.githubusercontent.com/paolomarchionetti/savio-data/main/\"")
```

Sostituisci `paolomarchionetti` con il tuo username GitHub reale. Oppure usa GitHub Releases:

```kotlin
buildConfigField("String", "DATA_BASE_URL",
    "\"https://github.com/TUOUSERNAME/savio/releases/latest/download/\"")
```

### E2 — Modifica AssetDataSource.kt

Aggiungi il metodo di sync CDN nella classe `AssetDataSource`:

```kotlin
// In AssetDataSource.kt — aggiungi questo metodo

private val httpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .build()

/**
 * Tenta di scaricare offers_current.json dal CDN.
 * In caso di errore ritorna silenziosamente — l'app usa il seed locale.
 */
suspend fun syncOffersFromCdn(): Boolean = withContext(Dispatchers.IO) {
    val url = "${BuildConfig.DATA_BASE_URL}offers_current.json"
    try {
        val request = Request.Builder().url(url).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) return@withContext false

        val body = response.body?.string() ?: return@withContext false

        // Valida che sia JSON valido prima di salvare
        Json.parseToJsonElement(body)

        // Salva nella cache locale dell'app
        val cacheFile = File(context.filesDir, "offers_current.json")
        cacheFile.writeText(body)

        true
    } catch (e: Exception) {
        false   // Silenzioso — usa seed come fallback
    }
}

/**
 * Legge le offerte: prima dalla cache CDN (se fresca), poi dal seed APK.
 * "Fresca" = file modificato nelle ultime 7 giorni.
 */
fun getOffersInputStream(): InputStream {
    val cacheFile = File(context.filesDir, "offers_current.json")
    val sevenDaysMs = 7 * 24 * 60 * 60 * 1000L
    if (cacheFile.exists() &&
        System.currentTimeMillis() - cacheFile.lastModified() < sevenDaysMs) {
        return cacheFile.inputStream()
    }
    return context.assets.open("offers_current.json")
}
```

Chiama `syncOffersFromCdn()` all'avvio dell'app in `SavioApp.kt` o nel `ResultsViewModel` prima di eseguire il ranking.

---

## 9. Fase F — Automazione Gemini in locale

### F1 — Ottieni la API key Gemini (gratis, 3 minuti)

1. Vai su **[aistudio.google.com](https://aistudio.google.com)**
2. Accedi con il tuo account Google (qualsiasi, non serve un account pagamento)
3. Clicca **"Get API key"** → **"Create API key in new project"**
4. Copia la chiave — inizia con `AIzaSy...`

**Limiti free tier Gemini 2.5 Flash** — più che sufficienti per Savio:

| Limite | Valore | Savio usa |
|--------|--------|-----------|
| Richieste/minuto | 10 | 7 per run completo |
| Richieste/giorno | 1.500 | 7 a settimana |
| Token/giorno | 1.000.000 | ~27.000 a settimana |
| Costo | **€0,00** | **€0,00** |

### F2 — Installa le dipendenze Python

```bash
# Dalla cartella scripts/ del progetto
cd scripts/
pip install -r requirements.txt
```

Il `requirements.txt` contiene:
```
google-genai>=1.0.0
requests>=2.31.0
beautifulsoup4>=4.12.0
pdfplumber>=0.10.0
python-dateutil>=2.8.0
lxml>=4.9.0
```

### F3 — Copia i file script nel progetto

Dalla ZIP `savio_gemini.zip` copia nella cartella `scripts/` del progetto:

```bash
# Estrai savio_gemini.zip e copia i file
cp savio_gemini_pack/savio_update_offers.py scripts/
cp savio_gemini_pack/requirements.txt       scripts/
# (update_offers.yml va in .github/workflows/ — vedi sezione G)
```

### F4 — Configura il percorso assets nello script

Apri `scripts/savio_update_offers.py` e controlla che `ASSETS_DIR` punti alla cartella corretta:

```python
BASE_DIR    = Path(__file__).parent.parent   # root del progetto (savio/)
ASSETS_DIR  = BASE_DIR / "app" / "src" / "main" / "assets"
```

Se il tuo progetto ha una struttura diversa, adatta questo percorso.

### F5 — Test locale

```bash
# Imposta la chiave API
export GEMINI_API_KEY="AIzaSy..."

# Test su una sola insegna, senza salvare
python3 scripts/savio_update_offers.py --store lidl --dry-run --debug

# Run completo su tutte le insegne
python3 scripts/savio_update_offers.py

# Solo le insegne di Pesaro
python3 scripts/savio_update_offers.py --store conad eurospin lidl carrefour md
```

**Output atteso (dry-run):**
```
2026-04-16 08:00:01 [INFO] SavioUpdater | modello: gemini-2.5-flash-preview-04-17 | offerte esistenti: 185
2026-04-16 08:00:01 [INFO] Validità target: 2026-04-20 → 2026-04-26
==================================================
2026-04-16 08:00:02 [INFO] Processing: Lidl (https://www.lidl.it/it/le-nostre-offerte.htm)
2026-04-16 08:00:03 [INFO]   → Testo estratto: 3847 caratteri
2026-04-16 08:00:05 [INFO]   → Gemini ha trovato 18 candidati
2026-04-16 08:00:05 [INFO]   ✅ 18 offerte valide per Lidl
[DRY-RUN] Avrei scritto 185 voci in .../offers_current.json
```

### F6 — Schedulazione locale (opzionale, senza GitHub)

Se preferisci eseguire lo script dal tuo Mac/PC senza GitHub Actions, usa cron:

**Mac/Linux:**
```bash
crontab -e
# Aggiungi questa riga (ogni lunedì alle 08:00):
0 8 * * 1 GEMINI_API_KEY="AIzaSy..." /usr/bin/python3 /percorso/savio/scripts/savio_update_offers.py >> /tmp/savio_update.log 2>&1
```

---

## 10. Fase G — GitHub Actions cron settimanale

Questa fase automatizza completamente l'aggiornamento: ogni lunedì mattina GitHub scarica i volantini, aggiorna il JSON e committa nel repository.

### G1 — Crea la struttura .github nel repository

```bash
# Dalla root del progetto savio/
mkdir -p .github/workflows
cp savio_gemini_pack/update_offers.yml .github/workflows/
```

### G2 — Aggiungi il secret GEMINI_API_KEY su GitHub

1. Vai su: **github.com → tuo repo → Settings → Secrets and variables → Actions**
2. Clicca **"New repository secret"**
3. **Name**: `GEMINI_API_KEY`
4. **Secret**: incolla la tua chiave `AIzaSy...`
5. Clicca **"Add secret"**

Il `GITHUB_TOKEN` è già disponibile automaticamente in ogni workflow — non serve configurarlo.

### G3 — Verifica il percorso script nel workflow

Apri `.github/workflows/update_offers.yml` e controlla che il `run` punti allo script corretto:

```yaml
- name: Aggiorna offerte con Gemini 2.5 Flash
  env:
    GEMINI_API_KEY: ${{ secrets.GEMINI_API_KEY }}
  run: |
    python3 scripts/savio_update_offers.py $ARGS
```

### G4 — Committa e pusha il workflow

```bash
git add .github/workflows/update_offers.yml
git add scripts/savio_update_offers.py
git add scripts/requirements.txt
git commit -m "ci: automazione offerte settimanale con Gemini 2.5 Flash"
git push
```

### G5 — Verifica che il workflow sia attivo

1. Vai su **github.com → tuo repo → Actions**
2. Dovresti vedere il workflow **"Savio — Aggiornamento automatico offerte (Gemini)"**
3. Per testarlo subito senza aspettare lunedì: **Run workflow → Run workflow** (bottone verde)
4. Guarda il log in tempo reale — ogni step deve completarsi con la spunta verde

### G6 — Cosa fa il workflow ogni lunedì

```
06:00 UTC (08:00 ora italiana)
  ↓
Checkout del repository
  ↓
Installa Python + dipendenze (google-genai, requests, bs4...)
  ↓
Scarica le pagine HTML di 7 insegne
  ↓
Gemini 2.5 Flash estrae prodotti e prezzi → JSON
  ↓
Valida il JSON (no ID duplicati, no categorie sconosciute, prezzi validi)
  ↓
Commit automatico: "chore(data): offerte settimana 2026-W17 — 185 voci [Gemini]"
  ↓
Push nel repository
  ↓
L'app scarica il nuovo JSON all'avvio successivo dell'utente
```

---

## 11. Test completo end-to-end

Dopo aver completato tutte le fasi, esegui questa sequenza di test:

### Test 1 — Build e avvio

- [ ] Build release senza errori
- [ ] App si avvia → schermata Onboarding
- [ ] Onboarding completabile con swipe
- [ ] Tap "Inizia a risparmiare" → AreaSelection

### Test 2 — Bologna

- [ ] Scrivi "Bologna" → 5 aree visibili
- [ ] Seleziona "Bologna — Centro" → Conferma
- [ ] Lista spesa → aggiungi: `latte, pasta, olio, uova, acqua`
- [ ] FAB "Dove conviene?" appare
- [ ] Tap FAB → ResultsScreen con 3 negozi
- [ ] Ranking mostra Esselunga, Conad, Lidl (o Coop)
- [ ] Logo insegna visibile nella card (non solo testo)
- [ ] Tap negozio #1 → StoreDetailScreen
- [ ] "Apri in Maps" funziona

### Test 3 — Pesaro Centro

- [ ] Torna alle impostazioni → cambia area → "Pesaro — Centro"
- [ ] Lista spesa → aggiungi: `latte, pasta, uova, pelati, biscotti, caffè`
- [ ] Risultati: deve apparire **Conad Superstore** e **Carrefour Market**
- [ ] CoverageBadge ≥ 70% (verde)

### Test 4 — Pesaro Nord

- [ ] Seleziona "Pesaro — Nord / Muraglia"
- [ ] Lista spesa → `acqua, pasta, olio, uova, patate`
- [ ] Risultati: deve apparire **Eurospin** e **Lidl**
- [ ] Eurospin dovrebbe essere il meno caro

### Test 5 — Pesaro Sud

- [ ] Seleziona "Pesaro — Sud / Villa Fastiggi"
- [ ] Lista spesa → `acqua, pasta, latte, uova, pelati`
- [ ] Risultati: deve apparire **MD Discount**
- [ ] MD dovrebbe avere i prezzi più bassi

### Test 6 — Prodotti non coperti

- [ ] Aggiungi alla lista: `pasta, latte, polpo, champagne, tartufo`
- [ ] Risultati: "polpo", "champagne", "tartufo" devono apparire in "Non trovati"
- [ ] Il ranking deve basarsi solo sui prodotti coperti

### Test 7 — Automazione Gemini

- [ ] `export GEMINI_API_KEY="AIzaSy..."`
- [ ] `python3 scripts/savio_update_offers.py --store lidl --dry-run` → nessun errore
- [ ] GitHub Actions → Actions → Run workflow → esecuzione completata

---

## 12. Aggiornamento offerte manuale (fallback)

Se lo script automatico non riesce a estrarre dati (es. il sito usa JavaScript e non restituisce contenuto), puoi aggiornare le offerte manualmente.

### Come aggiornare un'offerta in offers_current.json

Apri `app/src/main/assets/offers_current.json` e modifica il `price_eur` o aggiungi una nuova riga:

```json
{
  "id": "off_999",
  "store_id": "lidl_pesaro",
  "product_category_id": "pasta_spaghetti_500g",
  "product_name": "Spaghetti Combino 500g Lidl",
  "brand": "Combino",
  "price_eur": 0.49,
  "price_per_unit": 0.98,
  "requires_fidelity": false,
  "valid_from": "2026-04-21",
  "valid_to": "2026-04-27",
  "source_type": "LEAFLET",
  "confidence_level": "HIGH"
}
```

### Regole per gli ID

Usa ID progressivi senza riutilizzare quelli esistenti:

| Range | Assegnato a |
|-------|-------------|
| off_001–020 | Esselunga Bologna |
| off_101–115 | Conad Bologna |
| off_201–212 | Lidl Bologna |
| off_301–312 | Coop Bologna |
| off_401–412 | Eurospin Bologna |
| off_501–531 | Conad Pesaro |
| off_601–621 | Lidl Pesaro |
| off_701–719 | Carrefour Pesaro |
| off_801–821 | Eurospin Pesaro |
| off_901–922 | MD Pesaro |
| **off_1001+** | **Nuovi negozi / nuove città** |

### Dove consultare i volantini

| Insegna | URL Volantino | Aggiornamento |
|---------|--------------|---------------|
| Conad | conad.it/volantini | Lunedì |
| Lidl | lidl.it/it/le-nostre-offerte.htm | Giovedì |
| Eurospin | eurospin.it/volantino | Giovedì |
| Carrefour | carrefour.it/it/volantino | Martedì |
| MD Discount | mdspa.it/volantino | Lunedì |
| Esselunga | esselunga.it/it-it/offerte.html | Giovedì |
| Coop | e.coop/volantini | Lunedì |

---

*Documento generato — Aprile 2026 — Savio v1.0*  
*Automazione: Gemini 2.5 Flash (Google AI Studio — gratuito)*
