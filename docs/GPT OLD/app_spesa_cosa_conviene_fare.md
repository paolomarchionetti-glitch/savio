# Cosa ti conviene davvero fare per l’app spesa
## Scelta più fattibile, cosa evitare, e percorso consigliato

**Obiettivo**: costruire un’app Android-first (poi iOS) che faccia risparmiare davvero sulla spesa senza dipendere da AI costosa, senza basarsi su promesse poco difendibili, e senza fondare il business su dati fragili o illegali.

---

## Verdetto netto

Se vuoi partire in modo **realistico, difendibile e veloce**, la soluzione migliore non è una sola ma un **percorso a strati**.

### La scelta che ti conviene di più

**1. PARTI con:**
- **lista della spesa**;
- **volantini / offerte disponibili**;
- **geolocalizzazione minima o CAP manuale**;
- motore che suggerisce **il miglior supermercato unico** per quella lista.

**2. AGGIUNGI dopo:**
- **contributi utenti guidati** su **barcode + prezzo + foto etichetta scaffale**.

**3. USA lo scontrino dopo ancora:**
- non come motore principale;
- ma come **validazione**, storico prezzi e programma fedeltà/punti.

### In una frase

**La strada più intelligente è: `volantini + motore lista spesa + crowdsourcing guidato`, con lo scontrino come fase 2/3 e non come base iniziale.**

---

## Classifica reale delle opzioni

| Soluzione | Fattibilità iniziale | Velocità di lancio | Rischio legale/operativo | Qualità dati iniziale | Scalabilità | Mio giudizio |
|---|---:|---:|---:|---:|---:|---|
| Partnership dirette con supermercati | Bassa | Bassa | Bassa | Alta | Alta | Ottima dopo traction, pessima per partire |
| Volantini + parsing + QA | Alta | Alta | Media | Media | Media | **Miglior MVP** |
| Barcode + prezzo inserito utente + foto etichetta | Medio-alta | Media | Media-bassa | Media | Alta | **Miglior secondo layer** |
| Scontrino OCR crowdsourced | Media | Media-bassa | Media | Media-bassa all’inizio | Alta | **Utile ma non come base** |
| Scraping massivo di siti retailer | Apparentemente alta | Media | Alta | Variabile | Bassa | **Da evitare come fondazione** |
| Feed/licenze data provider | Media-bassa | Bassa | Bassa | Alta | Alta | Bene se hai budget o partner |

---

## La mia raccomandazione: cosa fare davvero

## Modello consigliato

### Fase 1 — MVP serio e lanciabile
**Cuore del prodotto:**
- l’utente scrive cosa vuole comprare;
- seleziona area / CAP o usa posizione;
- l’app mostra il **negozio migliore vicino** per la sua lista;
- l’app mostra anche **quanto è affidabile il risultato**.

### Dati usati in Fase 1
- volantini promo;
- catalogo prodotti normalizzato;
- mapping catena → punto vendita → area;
- equivalenze prodotto (es. acqua naturale 1,5L, biscotti secchi, mele gala, petto di pollo, ecc.).

### Perché questa è la scelta giusta
Perché ti permette di dare **valore percepito subito** senza dover risolvere immediatamente i problemi peggiori dello scontrino:
- descrizioni sporche;
- abbreviazioni casse;
- sconti su righe ambigue;
- privacy sugli scontrini;
- bassa motivazione utente a caricare prove continue.

---

## La soluzione “foto scontrino e prodotto condivisa”: ha senso?

### Sì, ma non come primo motore
La tua idea **non è sbagliata**, anzi può diventare una parte forte del vantaggio competitivo. Però io la userei così:

### Ruolo corretto dello scontrino
- **validare** prezzi già osservati;
- costruire uno **storico prezzi reali**;
- misurare il **risparmio effettivo post-acquisto**;
- dare punti, cashback, gamification;
- alimentare modelli di affidabilità per negozio/prodotto.

### Perché non partirei da lì
Perché lo scontrino è tecnicamente possibile ma operativamente sporco.

#### Problemi reali dello scontrino
1. Le righe scontrino sono spesso abbreviate male.
2. Il nome prodotto spesso non basta a capire formato o variante.
3. Ci sono sconti fidelity, bundle, coupon e promo che confondono il prezzo reale.
4. Lo scontrino può contenere dati personali, orari, negozio, ultime cifre carta o altri metadati sensibili.
5. Il matching prodotto↔riga scontrino richiede un motore complesso.
6. L’utente medio non carica scontrini per altruismo: serve un incentivo forte.

### Quando usarlo allora?
**Dopo** che hai già:
- una base utenti;
- un catalogo decente;
- un primo motore prezzi;
- un sistema reward.

---

## Molto meglio del “receipt-first”: barcode + prezzo + foto etichetta

Questa, per me, è la soluzione più sottovalutata.

### Come funziona
L’utente:
1. scansiona il barcode;
2. sceglie il supermercato;
3. inserisce il prezzo;
4. opzionalmente fotografa l’etichetta scaffale.

### Perché è migliore dello scontrino come punto di partenza
- il **barcode identifica il prodotto meglio** della descrizione sporca dello scontrino;
- la **foto etichetta** è più leggibile e contestuale del totale scontrino;
- riduci il problema privacy;
- puoi fare controllo qualità più semplice;
- puoi assegnare punteggi affidabilità per utente, negozio e prova.

### Contro
- richiede più azione dell’utente in negozio;
- all’inizio il database cresce lentamente;
- devi gamificare bene il contributo.

### Ma il vantaggio enorme è questo
Questa soluzione crea un **database proprietario pulito**, che nel tempo vale molto di più di uno scraping instabile.

---

## Quindi: cosa farei io al tuo posto

## Strategia consigliata in 4 mosse

### Mossa 1 — Android MVP localizzato
Lancia in **1 sola città o area**.

#### Funzioni MVP
- onboarding semplice;
- lista spesa;
- selezione prodotti per categoria + ricerca;
- scelta area o geolocalizzazione attuale;
- risultato: **miglior supermercato unico**;
- vista alternativa: 2° e 3° miglior supermercato;
- risparmio stimato rispetto alla media area;
- storico spesa mensile utente.

#### Cosa NON fare subito
- split multi-negozio complesso;
- route optimization;
- stock realtime;
- promesse di risparmio garantito;
- scraping massivo come unico motore.

---

### Mossa 2 — Layer community
Aggiungi una sezione tipo:

**“Aiuta la community e guadagna vantaggi”**

#### Missioni possibili
- conferma il prezzo di 3 prodotti;
- scansiona 5 barcode in un negozio;
- fotografa 2 etichette scaffale;
- carica 1 scontrino per validare il carrello.

#### Ricompense
- punti;
- premium gratis per 7/30 giorni;
- badge affidabilità;
- cashback simbolico;
- ranking locale.

---

### Mossa 3 — Receipt layer
Solo dopo il layer community.

#### Uso corretto dello scontrino
- verifica differenza tra stima e acquisto reale;
- corregge prezzi errati;
- migliora il ranking negozi;
- mostra all’utente il risparmio reale nel mese.

#### Qui lo scontrino diventa molto forte
Non perché “risolve tutto”, ma perché diventa il tuo **motore di verità** per validare il resto.

---

### Mossa 4 — Partnership e licenze
Quando hai numeri veri:
- vai dai retailer locali;
- mostri traffico generato;
- proponi feed prezzi/offerte;
- costruisci accordi media/affiliate/coupon/sponsorizzazioni.

A quel punto la trattativa cambia: non chiedi un favore, porti utenti.

---

## La soluzione più fattibile in assoluto: la mia top 3

## 1) Volantini + miglior supermercato unico
**È la più fattibile per partire.**

### Perché
- si capisce subito;
- l’utente percepisce subito il valore;
- è gestibile anche con dataset incompleto;
- non richiede subito OCR complesso su scontrini;
- riduce il rischio di promettere più di quanto puoi mantenere.

### Slogan corretto
Non: “ti faccio risparmiare sempre 50€”

Ma:
**“Ti aiuto a scegliere dove comprare la tua lista spendendo meno, in base alle offerte disponibili nella tua zona.”**

---

## 2) Barcode + prezzo + foto etichetta
**È la più intelligente per costruire il fosso competitivo (moat).**

### Perché
- dato migliore del solo scontrino;
- meno privacy problematica;
- qualità controllabile;
- forte asset proprietario nel tempo.

### Il suo ruolo
Non sostituisce l’MVP, lo rende migliore mese dopo mese.

---

## 3) Scontrino OCR come validazione e reward system
**È la più utile come fase 2/3.**

### Perché non la metto al n.1
Perché è più difficile da far funzionare bene di quanto sembri.

### Però è preziosa per:
- provare il risparmio reale;
- fidelizzare;
- far sentire all’utente che l’app “capisce” la sua spesa;
- correggere errori del database.

---

## Cosa eviterei

### 1. Scraping come fondazione del business
Può sembrare la via più rapida, ma è fragile.

Rischi:
- blocchi tecnici;
- termini d’uso contrari;
- contestazioni sui diritti sul database;
- dipendenza da fonti che possono chiudersi domani.

### 2. Promessa “50 euro garantiti al mese”
È troppo rischiosa.

Meglio dire:
- risparmio stimato;
- media utenti in area coperta;
- metodologia trasparente;
- disclaimer chiari.

### 3. Background location
Non ti serve quasi mai all’inizio.

Meglio:
- posizione approssimativa o precisa **solo in uso**;
- alternativa manuale con CAP, quartiere o indirizzo.

---

## Il prodotto che io farei davvero

## Nome del motore
**Smart Basket Engine**

### Input utente
- lista prodotti;
- preferenze marca / no marca;
- distanza massima;
- budget;
- auto sì/no;
- area/CAP.

### Output
- supermercato consigliato;
- costo stimato totale;
- top offerte rilevanti;
- alternative meno costose;
- punteggio affidabilità del risultato.

### Regola chiave
All’inizio dai sempre una risposta semplice:

> “Per questa lista, oggi il supermercato più conveniente vicino a te è X.”

Non complicare il prodotto con 8 opzioni.

---

## Free vs Premium: cosa conviene

## Free
- 1 lista attiva;
- confronto 3 supermercati;
- offerte base;
- risparmio stimato;
- contributi community con punti.

## Premium (0,99€ – 2,99€/mese da testare)
- liste illimitate;
- tracking risparmio mensile;
- preferenze brand avanzate;
- alternative intelligenti;
- alert prezzo prodotto;
- pianificazione spesa settimanale;
- storico e report.

### Nota importante
Io non venderei il premium come:
**“paghi 1€ e risparmi 50€”**

Lo venderei come:
**“risparmia meglio, più velocemente e con meno fatica.”**

---

## Android e iOS: cosa fare sui permessi

## Android
### Chiederei solo:
- internet;
- notifiche (solo se utili);
- posizione foreground, meglio se approssimativa come default;
- camera solo per scanner/foto contributi.

### Eviterei:
- background location;
- permessi invasivi non indispensabili.

### Nota utile
Su Android esiste anche una soluzione barcode che delega la scansione a Google Play Services senza chiedere il permesso camera all’app, utile per alcuni flussi ultra-semplici.

## iOS
### Stessa filosofia:
- location **When In Use**;
- posizione approssimativa se basta;
- camera solo se davvero necessaria;
- fallback manuale sempre disponibile.

---

## Rischi legali principali

## 1. Dati prezzi e diritti sul database
Se costruisci il prodotto prendendo sistematicamente dati da database altrui, il rischio cresce. Devi stare molto attento a non fondare il business sull’estrazione massiva o sistematica da fonti non licenziate.

## 2. Pubblicità comparativa e claim
Se dici che un negozio è “il più conveniente”, devi poterlo sostenere con una metodologia chiara, verificabile e contestualizzata.

## 3. Privacy
Scontrini, posizione, cronologia spesa e preferenze alimentari possono creare un profilo utente molto sensibile dal punto di vista privacy/compliance.

## 4. Abbonamenti
Flusso abbonamento, rinnovo automatico, cancellazione e comunicazioni devono essere estremamente trasparenti.

---

## Cosa farei nei prossimi 90 giorni

## Giorni 1–15
- scegliere 1 città pilota;
- definire 200–500 prodotti iniziali;
- creare tassonomia categorie;
- definire 5–10 insegne prioritarie.

## Giorni 16–30
- prototipo Android;
- lista spesa;
- schermata risultati;
- geolocalizzazione minimale / CAP manuale.

## Giorni 31–45
- ingestione offerte/volantini;
- normalizzazione prodotti;
- ranking miglior negozio unico.

## Giorni 46–60
- test con 30–100 utenti;
- capire dove il ranking sbaglia;
- correggere matching e UX.

## Giorni 61–90
- introdurre contributi community;
- barcode + prezzo + foto etichetta;
- sistema punti affidabilità.

---

## Decisione finale: cosa ti converrebbe fare

### Se mi chiedi una scelta secca, ti dico questa:

**Fai un’app Android-first che parte con `lista spesa + offerte/volantini + miglior supermercato unico`, e prepara fin da subito il secondo strato `barcode + prezzo + foto etichetta`.**

### Lo scontrino?
**Sì, ma come fase 2/3.**

### Le partnership retailer?
**Sì, ma dopo le prime metriche reali.**

### Lo scraping massivo come fondazione?
**No.**

### La promessa “50€ garantiti”?
**No. Troppo rischiosa.**

---

## Il mio consiglio imprenditoriale, senza giri di parole

Se vuoi fare un’app davvero grande, non inseguire la fantasia del “comparatore perfetto di tutti i prezzi in tempo reale” dal giorno uno.

Costruisci invece questo:

1. **uno strumento utilissimo subito**;
2. **un database proprietario che migliora mese dopo mese**;
3. **una community che contribuisce ai prezzi**;
4. **un modello legale e difendibile**.

Questa è la strada più credibile per fare un prodotto forte, storico e vendibile.

---

## Fonti verificate utili (ufficiali o primarie)

1. Android Developers — Request location permissions  
   https://developer.android.com/develop/sensors-and-location/location/permissions

2. Android Developers — Access location in the background  
   https://developer.android.com/develop/sensors-and-location/location/background

3. Android Developers — Request background location  
   https://developer.android.com/develop/sensors-and-location/location/permissions/background

4. Apple Developer — Requesting authorization to use location services  
   https://developer.apple.com/documentation/corelocation/requesting-authorization-to-use-location-services

5. Apple Developer — Choosing the Location Services Authorization to Request  
   https://developer.apple.com/documentation/bundleresources/choosing-the-location-services-authorization-to-request

6. Google ML Kit — Scan barcodes with ML Kit on Android  
   https://developers.google.com/ml-kit/vision/barcode-scanning/android

7. Google ML Kit — Text recognition v2  
   https://developers.google.com/ml-kit/vision/text-recognition/v2

8. Google ML Kit — Google code scanner  
   https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner

9. Apple VisionKit — Scanning data with the camera  
   https://developer.apple.com/documentation/visionkit/scanning-data-with-the-camera

10. Apple Vision — Recognizing Text in Images  
    https://developer.apple.com/documentation/vision/recognizing-text-in-images

11. Your Europe — Database protection in the EU  
    https://europa.eu/youreurope/business/running-business/intellectual-property/database-protection/index_en.htm

12. European Commission — Unfair commercial practices and price indication  
    https://commission.europa.eu/law/law-topic/consumer-protection-law/unfair-commercial-practices-and-price-indication_en

13. European Commission — Misleading and comparative advertising directive  
    https://commission.europa.eu/law/law-topic/consumer-protection-law/unfair-commercial-practices-and-price-indication/misleading-and-comparative-advertising-directive_en

14. European Commission — Price indication directive  
    https://commission.europa.eu/law/law-topic/consumer-protection-law/unfair-commercial-practices-and-price-indication/price-indication-directive_en

15. Your Europe — Returns and the right of withdrawal  
    https://europa.eu/youreurope/citizens/consumers/shopping/returns/index_en.htm

16. AGCM — provvedimenti recenti su pratiche commerciali e dark patterns in ambito abbonamenti  
    https://www.agcm.it/

---

## Chiusura

La tua intuizione sullo scontrino è buona, ma la trasformerei così:

- **non “motore principale”**;
- **sì “motore di verifica e fidelizzazione”**.

La vera combinazione vincente, secondo me, è:

> **MVP semplice + community data + crescita legale progressiva**.
