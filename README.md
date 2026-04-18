# Savio 🛒

> **La spesa intelligente.** Ti dice dove conviene fare la spesa nella tua zona — senza tracciarti, senza un account, senza chiederti permessi inutili.

---

## Package
```
com.paolomarchionetti.savio
```

## Stack tecnologico
| Layer | Tecnologia |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Architettura | MVVM + Clean Architecture |
| DI | Hilt |
| DB locale | Room |
| Preferenze | DataStore |
| Serializzazione | Kotlinx Serialization |
| HTTP (futuro) | Retrofit + OkHttp |
| Async | Coroutines + Flow |

## Struttura
```
app/src/main/
├── assets/            ← JSON seed dati (catalogo, negozi, offerte)
├── java/.../savio/
│   ├── core/          ← design system, navigazione, utility
│   ├── data/          ← Room, DataStore, AssetDataSource, Repository impl
│   ├── domain/        ← modelli, interfacce repository, use case
│   ├── feature/       ← schermate (onboarding, area, lista, risultati...)
│   └── di/            ← moduli Hilt
└── res/               ← strings, themes, xml config
```

## Flusso MVP
```
Onboarding (3 schermate)
    ↓
Selezione area (CAP manuale)
    ↓
Lista spesa (inserimento + autocomplete)
    ↓
Risultati (ranking 3 supermercati)
    ↓
Dettaglio supermercato (link Maps + volantino)
```

## Dati seed — Bologna pilota
- **4 insegne**: Esselunga, Conad, Lidl, Coop (centro) + Eurospin (nord)
- **120+ categorie prodotto** con alias per normalizzazione
- **~60 offerte** settimana corrente
- **5 aree geografiche** del comune di Bologna

## Permessi MVP: ZERO runtime
L'app non richiede nessun permesso all'utente nella versione 1.
Solo `INTERNET` (non runtime) per future sincronizzazioni JSON.

## Aggiornamento dati senza nuovo APK
I file JSON in `assets/` vengono sovrascritti da un CDN pubblico
(GitHub Releases / Cloudflare Pages) senza richiedere un nuovo rilascio.
URL base configurabile in `BuildConfig.DATA_BASE_URL`.

## Costi infrastruttura MVP
**Zero.** Tutto gira on-device. Nessun server, nessun database remoto,
nessuna autenticazione.

## Roadmap
- **v1.0** — MVP Bologna: lista + confronto + ranking
- **v1.1** — Più città, liste salvate, filtri
- **v2.0** — Barcode scan, contributi utenti, location opzionale
- **v3.0** — Backend, storico prezzi, iOS
- **v4.0** — Dispensa, meal planning, B2B insights

---

*Autore: Paolo Marchionetti — Aprile 2026*
*Versione documento: 3.0*
