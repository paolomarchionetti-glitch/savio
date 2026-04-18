# Savio — Guida all'automazione completa
**Versione**: 2.0 — Aprile 2026  
**Autore**: Paolo Marchionetti

---

## Cosa fa questa automazione

```
Ogni lunedì mattina (automaticamente):
  ┌─────────────────────────────────────────────────────────┐
  │  GitHub Actions si sveglia alle 06:00 UTC               │
  │        ↓                                                 │
  │  Script scarica le pagine volantino di ogni insegna     │
  │        ↓                                                 │
  │  Claude AI legge il testo ed estrae prodotti+prezzi     │
  │        ↓                                                 │
  │  JSON validato e aggiornato automaticamente             │
  │        ↓                                                 │
  │  Commit + push nel repository                           │
  │        ↓                                                 │
  │  File caricato su GitHub Releases (CDN gratuito)        │
  │        ↓                                                 │
  │  L'app Android scarica i nuovi dati all'avvio           │
  └─────────────────────────────────────────────────────────┘
```

---

## Struttura file

```
savio/
├── app/src/main/assets/          ← JSON letti dall'app (aggiornati dallo script)
│   ├── offers_current.json
│   ├── areas_v1.json
│   ├── stores_pilot_v1.json
│   ├── catalog_v1.json
│   └── equivalences_v1.json
│
├── scripts/
│   ├── savio_update_offers.py    ← script principale (scarica + Claude + merge)
│   ├── savio_cdn_push.py         ← carica i JSON su GitHub Releases
│   └── requirements.txt
│
├── .github/workflows/
│   └── update_offers.yml         ← GitHub Actions (cron settimanale)
│
└── logos/                        ← PNG loghi insegne (metti in res/drawable/)
    ├── ic_store_conad.png
    ├── ic_store_lidl.png
    ├── ic_store_carrefour.png
    ├── ic_store_eurospin.png
    ├── ic_store_md.png
    ├── ic_store_esselunga.png
    ├── ic_store_coop.png
    └── drawable-{mdpi,hdpi,xhdpi,xxhdpi}/   ← versioni multi-density
```

---

## Setup iniziale (una volta sola)

### 1. Aggiungi i secret al repository GitHub

Vai su: **GitHub → tuo repo → Settings → Secrets and variables → Actions**

Aggiungi questi due secret:

| Nome | Valore | Dove ottenerlo |
|------|--------|----------------|
| `ANTHROPIC_API_KEY` | `sk-ant-api03-...` | [console.anthropic.com](https://console.anthropic.com) → API Keys |
| `GITHUB_TOKEN` | Automatico | GitHub lo fornisce automaticamente in ogni workflow |

> `GITHUB_TOKEN` è già disponibile in ogni GitHub Actions run senza configurazione.  
> Lo script di CDN push lo legge da `GITHUB_REPO` — imposta quella variabile nel workflow.

### 2. Copia il workflow nel repository

```bash
# Dalla root del progetto savio/
mkdir -p .github/workflows
cp scripts/github/update_offers.yml .github/workflows/update_offers.yml
git add .github/workflows/update_offers.yml
git commit -m "ci: aggiungi workflow aggiornamento offerte settimanale"
git push
```

### 3. Installa le dipendenze localmente (per test manuali)

```bash
cd scripts/
pip install -r requirements.txt
```

### 4. Test locale prima del deploy

```bash
# Imposta la chiave API
export ANTHROPIC_API_KEY="sk-ant-..."

# Test dry-run (non salva nulla)
python3 scripts/savio_update_offers.py --dry-run

# Test su una sola insegna
python3 scripts/savio_update_offers.py --store lidl --debug

# Aggiornamento completo
python3 scripts/savio_update_offers.py
```

---

## Come funziona lo script passo per passo

### Fase 1 — Scraping HTML

Lo script visita la pagina volantino di ogni insegna configurata con un User-Agent corretto, poi estrae il testo dai selettori CSS configurati.

```python
# Es: per Lidl cerca elementi con classe "offer-item"
selectors = [".offer-item", ".product-grid__item", "[class*='offer']"]
```

Se la pagina usa JavaScript per renderizzare i contenuti (SPA/React), il testo potrebbe essere incompleto. In quel caso aggiungere Playwright al requirements e usare `method: "playwright"`.

### Fase 2 — Estrazione con Claude

Il testo estratto viene inviato a Claude con un prompt strutturato che:
- Gli fornisce il catalogo prodotti disponibile (120+ categorie)
- Gli chiede di restituire **solo JSON** con la struttura esatta attesa
- Lo vincola a usare solo `product_category_id` che esistono nel catalogo

Questo significa che Claude non può inventare categorie — può solo mappare i prodotti reali del volantino alle categorie che Savio conosce.

**Esempio di output Claude:**
```json
[
  {
    "product_category_id": "latte_intero_1l",
    "product_name": "Latte Intero Conad 1L",
    "brand": "Conad",
    "price_eur": 1.09,
    "requires_fidelity": false
  }
]
```

### Fase 3 — Validazione

Ogni offerta estratta viene validata:
- Il `product_category_id` deve esistere in `catalog_v1.json`
- Il `store_id` deve esistere in `stores_pilot_v1.json`
- Il prezzo deve essere tra €0,05 e €500
- L'ID viene generato deterministicamente (MD5 di store+category+settimana)

Le offerte non valide vengono silenziosamente scartate.

### Fase 4 — Merge

```
Offerte esistenti ancora valide   →  Mantenute
Offerte esistenti scadute         →  Rimosse
Nuove offerte (stessa categoria)  →  Sovrascrivono le vecchie
Nuove offerte (nuova categoria)   →  Aggiunte
```

### Fase 5 — Salvataggio e push

Il file `offers_current.json` viene salvato nella cartella `assets/`, poi GitHub Actions committa e pusha automaticamente con un messaggio tipo:

```
chore(data): aggiorna offerte settimana 2026-W17 (185 offerte)
```

---

## Aggiungere una nuova insegna

### 1. Aggiungi il negozio in `stores_pilot_v1.json`

```json
{
  "id": "pam_pesaro",
  "name": "Pam",
  "branch": "Via ...",
  "chain": "pam",
  "area_id": "pesaro_centro",
  "address": "...",
  "maps_url": "...",
  "leaflet_url": "..."
}
```

### 2. Aggiungi la config in `savio_update_offers.py`

```python
"pam": {
    "name": "Pam",
    "leaflet_url": "https://www.pampanorama.it/volantino",
    "method": "html",
    "selectors": ["[class*='product']", "[class*='offerta']", "article"],
    "fallback_pdf": None,
    "store_ids": ["pam_pesaro"],
},
```

### 3. Crea il logo PNG

Copia `generate_logos.py`, aggiungi la voce CHAINS e rigenera.

### 4. Test

```bash
python3 scripts/savio_update_offers.py --store pam --dry-run --debug
```

---

## Dove mettere i loghi nel progetto Android

### Percorso destinazione

```
app/src/main/res/
├── drawable/                     ← copia qui le PNG base (192×192)
│   ├── ic_store_conad.png
│   ├── ic_store_lidl.png
│   ├── ic_store_carrefour.png
│   ├── ic_store_eurospin.png
│   ├── ic_store_md.png
│   ├── ic_store_esselunga.png
│   └── ic_store_coop.png
├── drawable-mdpi/                ← 192px (stesso del base)
├── drawable-hdpi/                ← 288px
├── drawable-xhdpi/               ← 384px
└── drawable-xxhdpi/              ← 576px
```

### Oppure via Android Studio

1. Apri Android Studio
2. Naviga su `app → src → main → res`
3. Clic destro → **New → Image Asset**
4. Seleziona **Image** come Asset Type
5. Carica il PNG corrispondente
6. Nome: `ic_store_conad` (senza estensione)
7. Studio genera automaticamente tutte le density

### Codice Kotlin per usare i loghi

In `StoreLogoUtils.kt` (crea questo file in `core/util/`):

```kotlin
package com.paolomarchionetti.savio.core.util

import androidx.annotation.DrawableRes
import com.paolomarchionetti.savio.R

@DrawableRes
fun chainToLogoRes(chain: String): Int? = when (chain.lowercase()) {
    "conad"      -> R.drawable.ic_store_conad
    "lidl"       -> R.drawable.ic_store_lidl
    "carrefour"  -> R.drawable.ic_store_carrefour
    "eurospin"   -> R.drawable.ic_store_eurospin
    "md"         -> R.drawable.ic_store_md
    "esselunga"  -> R.drawable.ic_store_esselunga
    "coop"       -> R.drawable.ic_store_coop
    else         -> null
}
```

Nella tua `StoreRankingCard` in `ResultsScreen.kt`:

```kotlin
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
        // Fallback: box con iniziale
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

## Sincronizzazione JSON nell'app Android (CDN)

### Come aggiungere il sync nell'app

In `AssetDataSource.kt` — aggiungi un metodo di sync:

```kotlin
class AssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CDN_BASE = BuildConfig.DATA_BASE_URL
        // es: "https://github.com/paolomarchionetti/savio/releases/latest/download/"
    }

    suspend fun syncOffersFromCdn(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "$CDN_BASE/offers_current.json"
            val response = URL(url).openStream().bufferedReader().readText()
            // Valida che sia JSON valido
            val parsed = Json.decodeFromString<List<OfferDto>>(response)
            // Salva in cache locale
            context.openFileOutput("offers_current.json", Context.MODE_PRIVATE).use {
                it.write(response.toByteArray())
            }
            true
        } catch (e: Exception) {
            false  // Usa dati seed come fallback
        }
    }

    fun getOffersJson(): InputStream {
        // Prima prova cache locale (aggiornata dal CDN)
        val cachedFile = File(context.filesDir, "offers_current.json")
        if (cachedFile.exists() && cachedFile.lastModified() > System.currentTimeMillis() - 7 * 86400_000L) {
            return cachedFile.inputStream()
        }
        // Fallback agli asset bundle nell'APK
        return context.assets.open("offers_current.json")
    }
}
```

### BuildConfig.DATA_BASE_URL

In `app/build.gradle.kts`:

```kotlin
buildConfigField(
    "String",
    "DATA_BASE_URL",
    "\"https://github.com/TUONOME/savio/releases/latest/download\""
)
```

---

## Risoluzione problemi comuni

### "Lo script non trova prodotti"

I siti GDO spesso usano JavaScript per renderizzare i contenuti (React, Vue). Il `requests` base non esegue JS.

**Soluzione**: aggiungi Playwright al workflow e cambia `method: "playwright"` nella config.

```bash
pip install playwright
playwright install chromium
```

```python
from playwright.sync_api import sync_playwright

def fetch_page_with_js(url: str) -> str:
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        page.goto(url, wait_until="networkidle")
        html = page.content()
        browser.close()
        return html
```

### "Claude restituisce JSON non valido"

Aumenta il `max_tokens` in `extract_offers_with_claude()` e aggiungi un secondo tentativo:

```python
for attempt in range(2):
    response = self.client.messages.create(...)
    # ...
```

### "Le offerte non si aggiornano nell'app"

Verifica che `AssetDataSource` controlli prima la cache locale.  
Forza un refresh: cancella `context.filesDir/offers_current.json` e riavvia l'app.

### "GitHub Actions fallisce"

Controlla i log nella tab **Actions** del repo.  
Errori comuni:
- `ANTHROPIC_API_KEY` non impostata → aggiungi il secret
- Sito insegna irraggiungibile → normale intermittenza, ritenterà la settimana dopo
- JSON validation error → guarda l'output del step "Valida JSON prodotto"

---

## Costi stimati dell'automazione

| Voce | Costo |
|------|-------|
| GitHub Actions | **Gratuito** (2000 min/mese free tier) |
| GitHub Releases CDN | **Gratuito** |
| Claude API (claude-sonnet-4) | ~€0,05–0,15 per run completo (7 insegne) |
| **Totale mensile** | **~€0,20–0,60/mese** |

Il costo Claude API dipende da quante pagine vengono scrappate con successo.  
Con 7 insegne × 4 run/mese = ~28 chiamate API = costo trascurabile.

---

*Generato — Aprile 2026 — Savio v1.0 Automation*
