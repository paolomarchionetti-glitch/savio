# Savio — Documento di progetto
## Riassunto tecnico e strategico completo
**Autore**: Paolo Marchionetti | **Data**: Aprile 2026 | **Versione**: 1.0

---

## In una riga

> **Savio è l'app Android che ti dice dove conviene fare la spesa nella tua zona, confrontando il carrello completo tra i supermercati disponibili — senza tracciarti, senza un account, senza chiederti permessi.**

---

## 1. Il problema che risolve

Ogni famiglia italiana fa la spesa ogni settimana. I prezzi variano significativamente tra insegne diverse — anche a parità di zona. Confrontare volantini, offerte e prezzi manualmente richiede tempo e attenzione. Nessuna app italiana lo fa davvero bene, in modo trasparente e senza dark pattern.

**Savio risolve questo confrontando l'intero carrello**, non il singolo prodotto, e comunicando sempre quanto è affidabile la stima.

---

## 2. Nome e identità

| Campo | Valore |
|---|---|
| **Nome** | Savio |
| **Significato** | Saggio, intelligente — la spesa fatta bene |
| **Package** | `com.paolomarchionetti.savio` |
| **Dominio futuro** | `savio.app` |
| **Piattaforma v1** | Android |
| **Piattaforma v2** | iOS (dopo stabilizzazione del motore) |

---

## 3. Architettura tecnica

### Principio guida
**Offline-first, local-first.** La versione 1 funziona completamente senza backend. Zero costi infrastrutturali al lancio.

### Stack Android

| Componente | Tecnologia | Motivazione |
|---|---|---|
| UI | Jetpack Compose + Material 3 | Moderno, testabile, single-activity |
| Architettura | MVVM + Clean Architecture | Separazione layer, testabilità, iOS-ready |
| Dependency Injection | Hilt | Standard Google, compile-time safe |
| Database locale | Room | Robusto, migrations, Flow integration |
| Preferenze | DataStore | Asincrono, type-safe, sostituisce SharedPrefs |
| Serializzazione | Kotlinx Serialization | Nativa Kotlin, performance |
| HTTP (futuro) | Retrofit + OkHttp | Standard Android, già configurato |
| Navigazione | Navigation Compose | Type-safe con sealed class |
| Async | Coroutines + Flow | Nativo Kotlin, reactive streams |
| Immagini | Coil | Kotlin-first, Compose integration |

### Layer architetturali
```
Presentation (Feature Screens + ViewModels)
        ↓ usa
Domain (Use Cases + Models + Repository Interfaces)
        ↓ usa
Data (Repository Impl + Room + DataStore + AssetDataSource)
        ↓ legge
Assets (JSON seed: catalog, stores, offers, areas, equivalences)
```

### Costi infrastruttura MVP
**Zero.** Tutto gira on-device. I JSON seed vengono aggiornati senza nuovo APK tramite CDN gratuito (GitHub Releases o Cloudflare Pages), configurato in `BuildConfig.DATA_BASE_URL`.

---

## 4. Permessi Android — MVP: zero runtime

| Permesso | Stato | Note |
|---|---|---|
| `INTERNET` | ✅ Incluso | Non è runtime, invisibile all'utente |
| `ACCESS_FINE_LOCATION` | ❌ Assente | Aggiunto in v2.0, solo opt-in foreground |
| `ACCESS_COARSE_LOCATION` | ❌ Assente | Idem |
| `ACCESS_BACKGROUND_LOCATION` | ❌ Mai | Non previsto |
| `CAMERA` | ❌ Assente | v2.0: Google Code Scanner (no permesso) |
| `POST_NOTIFICATIONS` | ❌ Assente | v2.0: solo opt-in esplicito |
| `READ_MEDIA_IMAGES` | ❌ Assente | v2.0: Android Photo Picker (no permesso) |

L'utente seleziona la propria zona tramite CAP/città manuale. Nessun popup al primo avvio.

---

## 5. Flusso utente MVP

```
OnboardingScreen (3 pagine con HorizontalPager)
        ↓
AreaSelectionScreen (ricerca CAP / città — Bologna disponibile)
        ↓
ShoppingListScreen (inserimento prodotti + autocomplete debounced)
        ↓  [FAB "Dove conviene?"]
ResultsScreen (ranking 1+2 supermercati con StoreRankingCard)
        ↓  [tap su card]
StoreDetailScreen (indirizzo, link Maps, link volantino ufficiale)
```

---

## 6. Schermate implementate

### OnboardingScreen
- HorizontalPager con 3 pagine (emoji + titolo + sottotitolo)
- Indicatori pagina animati con `animateContentSize()`
- Bottoni Avanti / Salta / Inizia a risparmiare
- Salva stato in DataStore (`onboardingDone`)

### AreaSelectionScreen
- Campo ricerca per CAP, città o nome zona
- Filtro in tempo reale su `List<Area>`
- `AreaCard` con selezione evidenziata
- Bottone Conferma zona abilitato solo dopo selezione
- 5 aree di Bologna disponibili nel seed

### ShoppingListScreen
- Campo testo con autocomplete e debounce 300ms
- `SuggestionChip` da catalogo prodotti normalizzato
- `LazyColumn` con `key` stabile per animazioni
- Rimozione item con tap su icona ×
- FAB "Dove conviene?" visibile solo con lista non vuota
- Persistenza automatica ad ogni modifica

### ResultsScreen
- `StoreRankingCard` per ogni supermercato nel ranking
- Prezzo stimato carrello in evidenza
- `CoverageBadge` (verde ≥70%, ambra 40-69%, grigio <40%)
- `ConfidenceBadge` con pallino semantico colorato
- `DataFreshnessLabel` — data aggiornamento dati sempre visibile
- Lista prodotti non coperti esplicita
- Disclaimer trasparenza sempre visibile in fondo
- `MethodologyBottomSheet` con spiegazione completa del ranking
- Icona info in TopAppBar

### StoreDetailScreen
- Informazioni negozio (nome, indirizzo, filiale)
- Bottone "Apri in Maps" (deep link Google Maps)
- Bottone "Vedi volantino ufficiale" (link esterno)
- Nota di trasparenza prezzi

### SettingsScreen
- Privacy Policy (link esterno)
- Termini e Condizioni (link esterno)
- Valuta l'app (link Play Store)
- Invia feedback (intent email)
- Footer con versione e nota zero dati personali

---

## 7. Motore di ranking

Il cuore di Savio. Implementato in `GetBestStoreForListUseCase`.

### Formula score (più basso = migliore)
```
score = prezzo_stimato_carrello
      + (100 - copertura%) × 0.50      ← penalità lista incompleta
      + num_equivalenze × 0.20          ← penalità match non esatti
      - min(giorni_freschezza, 14) × 0.05  ← bonus dati recenti
```

### Regole
- Copertura lista < 40%: negozio escluso dal ranking (o mostrato come ultimo se tutti sotto soglia)
- Mai mostrare "risparmio garantito" — sempre "stima basata su dati coperti"
- Massimo 3 risultati per query
- Prodotti non coperti elencati esplicitamente nella card

### Normalizzazione prodotti
Il motore mappa "coca cola zero 1.5" → categoria `bibita_cola_15l` tramite alias nel `catalog_v1.json`. Senza normalizzazione il confronto è inutile — questo catalogo è il vero asset competitivo dell'app.

---

## 8. Dati seed (Bologna pilota)

### Aree disponibili
- Bologna Centro (CAP 40121)
- Bologna Nord / Corticella (40128)
- Bologna Sud / Murri (40137)
- Bologna Est / San Lazzaro (40057)
- Bologna Ovest / Casalecchio (40033)

### Insegne coperte
| Insegna | Area | Offerte seed |
|---|---|---|
| Esselunga | Centro | 20 |
| Conad City | Centro | 15 |
| Lidl | Centro | 15 |
| Coop | Centro | 12 |
| Eurospin | Nord | 12 |

### Catalogo prodotti
**120+ categorie** con alias per la normalizzazione, suddivise in: acqua/bevande, latticini, pasta/riso, olio/condimenti, carne/pesce, frutta/verdura, surgelati, biscotti/colazione, caffè/tè, conserve, salumi/formaggi, pulizia casa, igiene personale, animali domestici, neonati.

---

## 9. Gestione GDPR e privacy

### Rischio attuale: BASSO
- Nessun account obbligatorio
- Nessuna posizione salvata
- Liste salvate localmente sul device (Room)
- Preferenze in DataStore locale
- Nessun tracking
- Nessuna profilazione

### Documenti obbligatori pre-lancio
- Privacy Policy (URL: `savio.app/privacy`)
- Termini e Condizioni (URL: `savio.app/terms`)
- Registro trattamenti art. 30 GDPR
- Procedura data breach

### Il rischio SALE se in futuro si aggiunge
- Geolocalizzazione precisa salvata
- Account con storico spesa nominativo
- Scontrini (dati potenzialmente sensibili)
- Preferenze alimentari (categoria speciale GDPR)

---

## 10. Claim marketing: regole ferme

### MAI usare
- "Ti garantiamo X€ al mese"
- "Il supermercato più conveniente in assoluto"
- "Prezzi sempre aggiornati in tempo reale"
- "Confrontiamo tutti i supermercati"

### SEMPRE usare
- "Stima del carrello tra i punti vendita coperti"
- "Basato su offerte e dati disponibili nell'area selezionata"
- "Copertura dipende da area, prodotti e fonti disponibili"

### Formula raccomandata
> *"Ti aiutiamo a scegliere dove conviene fare la spesa nella tua zona. Una stima trasparente, basata sulle offerte e sui dati coperti dall'app."*

---

## 11. Modello freemium (versione 2.0+)

| Piano | Prezzo | Contenuto |
|---|---|---|
| Free | Gratis | Lista + confronto base + community base |
| Premium | 1,99€/mese o 14,99€/anno | Storico, alert, split multi-negozio, export CSV |
| Founder | 0,99€/mese | Solo per i primi 500 utenti — offerta lancio |
| Community unlock | Contributi dati | Premium sbloccabile contribuendo prezzi verificati |

**La versione 1.0 non ha alcun pagamento.** La monetizzazione entra dalla v2.0.

---

## 12. Roadmap

### v1.0 — MVP Bologna (ora)
Onboarding + area manuale + lista spesa + ranking 3 negozi + dettaglio + settings. Zero permessi, zero account, zero pagamenti. Dati seed Bologna su 5 insegne.

### v1.1
Liste salvate e duplicate, filtri supermercati preferiti, altre città italiane, ordinamento intelligente prodotti.

### v2.0
Barcode scanner (Google Code Scanner, senza permesso CAMERA), contributi community (foto etichetta, conferma prezzo), sistema punti e reward, location foreground opzionale, notifiche alert prezzo opt-in, prime funzioni premium.

### v3.0
Backend reale (Kotlin + Ktor + Supabase), storico prezzi, suggerimenti sostituti equivalenti, split intelligente multi-negozio, iOS (SwiftUI o KMP domain layer).

### v4.0 — Pilota automatico della spesa
Dispensa domestica, meal planning legato alle offerte, budget familiare, anti-spreco, previsioni prezzi, B2B insights aggregati, accordi retailer diretti.

---

## 13. Cosa NON fare mai (lista non negoziabile)

- ❌ Scraping massivo come unica fonte dati
- ❌ Claim di risparmio garantito
- ❌ Background location
- ❌ Account obbligatorio all'avvio
- ❌ Paywall su funzioni promesse come gratuite
- ❌ Dark patterns su abbonamento o cancellazione
- ❌ Ranking mescolato con sponsorizzazioni non etichettate
- ❌ Confronto prodotti non equivalenti come se fossero uguali
- ❌ Prezzi senza data di validità
- ❌ Copertura presentata come totale quando è parziale
- ❌ Lancio in tutta Italia senza dati reali solidi

---

## 14. Checklist prima del primo build

- [ ] Creare icone launcher (Android Studio → Image Asset Studio)
- [ ] Verificare `local.properties` con `sdk.dir` corretto
- [ ] Sync Gradle completato senza errori
- [ ] Build debug su emulatore API 26+ senza crash
- [ ] Flusso completo: Onboarding → Area → Lista → Risultati testato

## 15. Checklist pre-lancio Play Store

- [ ] Privacy Policy pubblicata online
- [ ] Termini e Condizioni pubblicati online
- [ ] Nessun crash su device test (API 26, 31, 35)
- [ ] Ranking < 2 secondi su lista di 10 prodotti
- [ ] App funzionante offline (solo seed)
- [ ] Descrizione Play Store senza claim non verificabili
- [ ] Privacy labels Play Console compilate correttamente
- [ ] Nessun permesso runtime non giustificato

---

## 16. File di progetto generati

| File | Descrizione |
|---|---|
| `savio_android_project.zip` | Progetto Android completo pronto per Android Studio |
| `savio_struttura_progetto.txt` | Albero file/cartelle commentato con descrizione di ogni file |
| `savio_progetto_riassunto.md` | Questo documento |

---

*Documento generato in Aprile 2026. Non sostituisce revisione legale professionale su privacy, consumer law e store compliance prima del lancio pubblico.*
