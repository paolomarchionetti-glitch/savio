# App per risparmiare sulla spesa quotidiana
## Visione, fattibilità, modalità operative, legale, permessi e roadmap

**Contesto**: app mobile Android-first, poi iOS, che aiuta l’utente a spendere meno sulla spesa confrontando prezzi, offerte e convenienza reale dei supermercati vicini.

**Tesi centrale**: sì, si può fare. Però **non** è principalmente un problema di sviluppo app o di AI. È soprattutto un problema di **raccolta dati prezzi**, **normalizzazione dei prodotti**, **compliance privacy/consumer law** e **credibilità del calcolo del risparmio**.

---

## 1. Verdetto in una riga

**Fattibile?** Sì.

**Facile?** No.

**Il rischio maggiore?** Non è il codice: è ottenere dati prezzi affidabili e usarli in modo legale e sostenibile.

**Il vero vantaggio competitivo?** Non “avere AI”, ma:
1. catalogo prodotti normalizzato bene;
2. confronto prezzo/offerta davvero utile;
3. distanza reale dal supermercato;
4. suggerimento smart della spesa totale, non del singolo prodotto;
5. UX semplicissima.

---

## 2. La verità scomoda: dove prendi i prezzi?

Senza una base prezzi seria, l’app non regge. In Italia/UE non esiste, per quanto emerge dalle fonti istituzionali consultate, un feed pubblico unico e nazionale dei prezzi GDO a livello SKU e punto vendita; le fonti pubbliche trovate monitorano prezzi su panieri e, per alcuni segmenti, prezzi con frequenza periodica o all’ingrosso, non un listino scaffale live per catena e negozio. Questa è un’inferenza ragionevole dalle fonti consultate, non una garanzia assoluta.

### 2.1 Modello A — Partnership dirette con supermercati e retailer
È il modello più forte e più pulito.

**Come funziona**
- firmi accordi con catene o gruppi locali;
- ricevi feed prezzi/offerte/promozioni/punti vendita;
- definisci formato dati, frequenza aggiornamento, SKU equivalenti, territori serviti.

**Pro**
- massima affidabilità;
- meno rischio legale;
- promozioni strutturate;
- possibilità di commissioni/affiliate/coupon.

**Contro**
- lento da chiudere;
- difficile all’inizio;
- i grandi retailer potrebbero non avere incentivo immediato.

**Quando usarlo**
- appena hai traction locale.

### 2.2 Modello B — Volantini digitali + parsing + QA umano
Molto più realistico per partire.

**Come funziona**
- acquisisci volantini digitali dei supermercati;
- estrai prodotti, prezzi, validità promo, quantità, formato;
- colleghi il volantino ai punti vendita/copertura geografica;
- aggiungi controllo qualità umano su estrazioni dubbie.

**Pro**
- veloce da lanciare;
- promozioni percepite bene dagli utenti;
- valore immediato.

**Contro**
- copertura incompleta;
- dati non continui;
- alcuni volantini sono ambigui;
- i prezzi “normali” restano scoperti.

**Nota critica**
Questo modello è ottimo per l’MVP, ma non basta per un motore storico e dominante.

### 2.3 Modello C — Crowdsourcing da scontrini utenti
È uno dei pochi modi scalabili per coprire prezzi reali a livello negozio.

**Come funziona**
- l’utente carica scontrino o foto scontrino;
- il sistema estrae negozio, data, righe prodotto, importi;
- abbina le righe a GTIN/categorie note;
- valida con score di affidabilità;
- restituisce vantaggio all’utente: cashback, punti, premium gratis, badge.

**Pro**
- prezzi reali sul territorio;
- barriera competitiva forte nel tempo;
- crea rete dati proprietaria.

**Contro**
- matching difficile;
- qualità variabile;
- presenza di dati personali nello scontrino;
- incentivi da disegnare bene.

**Come renderlo utile davvero**
- non affidarti solo all’OCR grezzo;
- crea un motore di riconciliazione con:
  - dizionario sigle scontrino;
  - GTIN;
  - brand;
  - taglia/formato;
  - prezzo atteso;
  - negozio;
  - storico conferme utenti.

### 2.4 Modello D — Barcode scan + prezzo inserito dall’utente
Versione ultra-lean del crowdsourcing.

**Come funziona**
- utente scansiona codice a barre;
- seleziona negozio;
- inserisce prezzo o fotografa etichetta scaffale;
- sistema controlla outlier.

**Pro**
- molto semplice da avviare;
- costi bassi;
- alimenta il database.

**Contro**
- richiede impegno utente;
- rischio vandalismo/spam;
- densità dati iniziale bassa.

### 2.5 Modello E — Licensing da data provider / GS1 + partner terzi
GS1 può aiutarti soprattutto su identità e qualità del catalogo, non necessariamente sui prezzi di scaffale. Le API GS1 Italy sono molto interessanti per verificare GTIN e identità prodotto.

**Pro**
- catalogo pulito;
- matching più robusto;
- meno errori tra “biscotti X 300g” e “biscotti X 350g”.

**Contro**
- non risolve da solo il problema prezzo;
- costo licenze/integrazioni.

### 2.6 Modello F — Scraping di siti/volantini/e-commerce retailer
È la scorciatoia che quasi tutti pensano di usare.

**La mia posizione**
Da solo, come fondazione del business, **è fragile**.

**Perché**
- termini d’uso contrari;
- rischio contestazioni su database rights;
- rischio blocchi IP/captcha;
- rischio instabilità continua;
- rischio commerciale altissimo se il tuo prodotto dipende da dati non concessi.

**Conclusione pratica**
Usalo solo come **supporto temporaneo, prudente, limitato e difendibile**, mai come base unica del business.

---

## 3. Quindi l’AI serve o no?

Non serve come cuore del prodotto.

Quello che serve davvero è:
- **motore regole**;
- **catalogo prodotti**;
- **matching prodotto-equivalente**;
- **ottimizzazione cestino**;
- **geografia dei punti vendita**;
- **modello di affidabilità del dato**.

### 3.1 Stack “non AI” che basta per vincere
- identificazione prodotti con GTIN / EAN;
- tassonomia categorie e sottocategorie;
- sinonimi e normalizzazione brand;
- regole per formati equivalenti (es. 500 ml vs 1 L);
- motore promo (3x2, sconto %, bundle, sottocosto, valida fino al…);
- ranking convenienza.

### 3.2 Dove puoi usare AI in modo pragmatico, non indispensabile
- OCR assistito per scontrini e volantini;
- matching fuzzy di descrizioni sporche;
- suggerimenti alternativi (“questa marca costa 18% meno”);
- previsione qualità dato.

**Ma** il core deve restare interpretabile. Questo ti difende anche legalmente e operativamente.

---

## 4. Il vero prodotto non è “trova il prezzo più basso”

Il vero prodotto è:

> “Dimmi dove comprare la mia lista spendendo meno, senza perdere tempo e senza farmi girare 5 negozi per risparmiare 2 euro.”

Questa distinzione è fondamentale.

### 4.1 Tre motori possibili

#### Motore 1 — “Negozio migliore unico”
L’app dice: vai al supermercato X, oggi per **questa lista** è il migliore.

**Perfetto per MVP**.

#### Motore 2 — “Split intelligente”
L’app dice:
- supermercato A per frutta, acqua, biscotti;
- supermercato B per carne e detersivi.

**Però** devi mostrare:
- risparmio lordo;
- costo tempo/spostamento;
- soglia minima per cui ha senso fare split.

#### Motore 3 — “Route ottimizzata”
Versione avanzata:
- prende lista;
- valuta stock/promo/orari/distanza;
- produce il piano ottimale.

Questa è molto potente, ma arriva dopo.

---

## 5. Proposta valore forte: come renderla “storica”

Per diventare grande, non devi essere “un comparatore”. Devi diventare il **pilota automatico della spesa familiare**.

### 5.1 Valore percepito immediato
- lista della spesa intelligente;
- miglior supermercato vicino;
- stima risparmio;
- notifiche vere solo quando conviene.

### 5.2 Valore profondo
- apprende abitudini d’acquisto;
- sa le marche preferite;
- propone equivalenti più convenienti;
- evita acquisti inutili;
- misura andamento spesa mensile.

### 5.3 Valore “wow” da prodotto iconico
- **Budget mode**: “devo stare sotto 250€ questo mese”;
- **Family mode**: 2 adulti + 2 bambini;
- **Meal plan linked to offers**: ricette in base a promo vere;
- **Anti-spreco**: usa prima ciò che hai già in casa;
- **Alert intelligenti**: “la tua acqua preferita oggi è in promo a 800 metri”.

---

## 6. Funzioni free e premium

## Free
- creazione lista spesa;
- confronto supermercato migliore tra quelli coperti;
- geolocalizzazione punti vendita vicini;
- alert base offerte;
- storico risparmio stimato limitato;
- scansione barcode limitata.

## Premium
- split su più supermercati;
- storico prezzi avanzato;
- alert personalizzati su prodotti preferiti;
- suggerimenti sostituti equivalenti;
- budget mensile;
- export spesa/storico;
- priorità su nuove aree;
- rimozione pubblicità;
- bonus su upload scontrini / partecipazione community.

### Prezzo premium
**1 euro/mese** psicologicamente è forte ma economicamente rischia di essere troppo basso, a meno che:
- acquisizione utenti sia organica;
- costo dati sia molto basso;
- hai ricavi extra da affiliate/retailer insights/B2B.

Più realistica una struttura tipo:
- 0€ free;
- 1,99€–2,99€/mese premium consumer;
- oppure annuale scontato.

**1 euro** può esistere come:
- lancio early adopters;
- piano founder;
- promo locale.

---

## 7. Attenzione enorme: “ti faccio risparmiare 50€ al mese”

Questa promessa è pericolosa.

### 7.1 Perché è rischiosa
Se dici “garantito 50€ al mese”, devi poterlo dimostrare in modo solido, oggettivo, replicabile e non ingannevole. Le regole UE/AGCM sulle pratiche scorrette, omissioni ingannevoli e pubblicità comparativa rendono questo punto delicatissimo.

### 7.2 Come dirlo in modo legale/commercialmente forte
Meglio formule tipo:
- “Obiettivo medio di risparmio fino a X€”;
- “Gli utenti attivi nelle aree coperte hanno risparmiato in media Y€”;
- “Stima personalizzata basata su lista, zona e punti vendita coperti”;
- “Ti mostriamo il risparmio **stimato** con metodologia trasparente”.

### 7.3 Formula migliore
> “Ti aiutiamo a ridurre la spesa mensile trovando il carrello più conveniente nella tua zona. Il risparmio mostrato è una stima basata su prodotti, quantità, punti vendita coperti e offerte disponibili.”

### 7.4 Se vuoi usare una garanzia vera
Puoi farlo solo con regole precise, ad esempio:
- premium 30 giorni;
- se non trovi almeno X€ di opportunità documentate sulla tua lista, rimborso del mese;
- con condizioni chiare e verificabili.

Questa è molto più difendibile di una promessa assoluta generalista.

---

## 8. Modello di calcolo del risparmio

Serve una formula chiara. Se no, vieni contestato da utenti e retailer.

## 8.1 Possibili baseline

### Baseline A — confronto con supermercato medio locale
Buona per marketing, ma più discutibile.

### Baseline B — confronto con ultimo negozio usato dall’utente
Più personalizzata, ma richiede storico affidabile.

### Baseline C — confronto con prezzo mediano disponibile nella zona
Molto robusta.

### Baseline D — confronto con “costo del carrello abituale”
Fortissima, ma va costruita nel tempo.

## 8.2 La mia raccomandazione
Per partire:
- mostra **prezzo del carrello consigliato**;
- mostra **seconda migliore alternativa**;
- mostra **delta**;
- non parlare subito di “risparmio assoluto mensile garantito”.

---

## 9. Geolocalizzazione: sì, ma minima e intelligente

La posizione è utile, ma è dato personale. Va trattata con estrema prudenza.

## 9.1 Cosa ti serve davvero
Nella maggior parte dei casi basta:
- localizzazione approssimativa / city-level o area-level;
- posizione in foreground;
- nessuna geolocalizzazione continua in background.

## 9.2 Quando serve la precisa
Solo per:
- distanza reale al negozio molto vicina;
- geofence opzionali (“avvisami quando passo vicino a…”);
- route accurata.

## 9.3 Quando evitare il background
Quasi sempre all’inizio.

Per un’app spesa, il background location è spesso più un rischio che un vantaggio.

## 9.4 Strategia privacy-first consigliata
- default: nessuna posizione salvata fino a quando l’utente usa il confronto zona;
- chiedi **solo approximate location** o equivalente se basta;
- alternativa manuale sempre disponibile: CAP, città, indirizzo, supermercati preferiti;
- salva solo area necessaria, non coordinate storiche continue;
- retention breve;
- opt-in separato per alert geofence.

---

## 10. Permessi app: cosa chiedere davvero

## Android — permessi consigliati
### Necessari o quasi
- **Internet**;
- **Location foreground** solo se l’utente vuole negozi vicini;
- **Notifications** per alert offerte;
- **Camera** solo se fai scansione barcode / scontrini.

### Da evitare nel MVP
- background location;
- accesso file completo;
- contatti;
- microfono;
- foto/media larghi se puoi usare picker selettivo.

## iOS — permessi consigliati
### Necessari o quasi
- location **When In Use**;
- notifiche push;
- camera per barcode/scontrino;
- photo picker per upload immagini.

### Da evitare nel MVP
- Always Location;
- tracking cross-app;
- accessi invasivi non essenziali.

---

## 11. Architettura prodotto consigliata

## 11.1 Oggetti principali
- User
- Household
- Store
- StoreLocation
- Product
- GTIN
- Offer
- BasketItem
- Basket
- PriceObservation
- ReceiptLine
- SavingsEstimate
- CoverageZone

## 11.2 Motori core
1. **Catalog Engine** — normalizza prodotti;
2. **Price Engine** — aggrega prezzi e validità;
3. **Promo Engine** — interpreta offerte;
4. **Store Ranking Engine** — valuta lista vs store;
5. **Savings Engine** — spiega delta e metodologia;
6. **Trust Engine** — score qualità dato;
7. **Geo Engine** — negozi vicini, distanza, tempo.

## 11.3 Regole di ranking suggerite
Punteggio finale =
- prezzo carrello
- distanza
- affidabilità dato
- freschezza dato
- preferenze utente
- penalità per split inutile
- penalità per equivalenza debole

---

## 12. MVP serio: cosa lanciare per primo

### Fase 1 — una città o una provincia
Non tutta Italia. Sarebbe un suicidio operativo.

### Copertura iniziale
- 4–8 insegne;
- 1 area urbana densa;
- focus su 300–800 prodotti ad alta rotazione.

### Funzioni MVP
- lista spesa;
- matching categorie/prodotti frequenti;
- confronto miglior negozio unico;
- mappa negozi vicini;
- volantini e promo;
- barcode scan;
- upload scontrino facoltativo;
- storico semplice.

### Funzioni da rimandare
- route multi-store avanzata;
- pantry completo;
- meal plan sofisticato;
- loyalty unificate;
- couponing complesso.

---

## 13. Roadmap concreta

## 0–3 mesi
- validazione problema;
- dataset locale;
- schema catalogo;
- prototipo ranking carrello;
- UX test.

## 3–6 mesi
- app Android MVP;
- 1 area geografica;
- 1–2 fonti prezzi stabili;
- dashboard qualità dato;
- primi utenti reali.

## 6–12 mesi
- scontrini / crowdsourcing;
- premium semplice;
- analytics risparmio;
- primi accordi retailer o partner locali;
- rilascio iOS.

## 12–24 mesi
- espansione città;
- motore split multi-store;
- profiling preferenze;
- partnership brand/retailer;
- B2B insights anonimizzati e aggregati.

---

## 14. Modelli di business possibili

### Modello 1 — Subscription consumer
Chiaro, pulito, indipendente.

### Modello 2 — Affiliate / lead generation retailer
Pagamenti per traffico qualificato, coupon o redemption.

### Modello 3 — Retail media etico
Spazio promo, ma solo se separato bene dai suggerimenti “miglior convenienza”.

### Modello 4 — Insight B2B aggregati
Statistiche aggregate su prezzi, sensibilità promo, domanda locale.

### Modello 5 — Freemium + gamification community
Gli utenti generano dati e sbloccano vantaggi.

**La combinazione migliore**
- subscription + partnership retailer + community data network.

---

## 15. Rischi legali e regolatori principali

## 15.1 Privacy / GDPR
La geolocalizzazione è dato personale. Se costruisci storico spostamenti, profili di spesa, preferenze e notifiche iper-personalizzate, entri in una zona da gestire con molta disciplina.

**Cosa devi fare**
- base giuridica chiara;
- privacy notice leggibile;
- minimizzazione dati;
- retention breve;
- sicurezza adeguata;
- registro trattamenti;
- accordi con fornitori cloud/processor;
- processo diritti utenti;
- valutazione DPIA quando il rischio cresce;
- DPO se arrivi a monitoraggio regolare e sistematico su larga scala.

## 15.2 Geolocalizzazione
La geolocalizzazione deve essere proporzionata allo scopo.

**Tradotto in prodotto**
Se basta sapere il quartiere o il CAP, non chiedere la posizione precisa.
Se basta la posizione mentre l’app è aperta, non chiedere background.

## 15.3 Prezzi e pubblicità comparativa
Se confronti prezzi tra insegne:
- confronto oggettivo;
- beni omogenei;
- niente confusione o discredito;
- metodologia chiara;
- date di validità;
- unità di misura comparabili.

## 15.4 Pratiche scorrette verso consumatori
Rischi se:
- dichiari risparmi non dimostrabili;
- nascondi limiti di copertura;
- fai dark patterns su abbonamento;
- gonfi il “prezzo barrato”;
- ometti che certe offerte valgono solo con fidelity card o app retailer.

## 15.5 Dati da terzi / scraping / database rights
Qui c’è un rischio serio.

Se estrai sistematicamente dati da database altrui per uso commerciale, potresti esporti a:
- contestazioni contrattuali;
- cease & desist;
- blocchi tecnici;
- contestazioni su diritti sui database;
- contenzioso con retailer o provider.

## 15.6 Trasferimenti extra SEE
Se usi cloud, analytics, CRM o supporto fuori SEE, devi gestire i trasferimenti con adeguate garanzie legali.

## 15.7 Sicurezza
Scontrini, email, preferenze, posizione, abitudini di acquisto sono dati che meritano sicurezza seria:
- cifratura;
- segregazione ambienti;
- controllo accessi;
- logging;
- data retention;
- incident response.

---

## 16. Checklist legale minima prima del lancio

### Documenti
- Termini e condizioni;
- Privacy policy;
- Cookie/privacy SDK disclosure;
- informativa abbonamento;
- policy contenuti utenti / community;
- procedura reclami / contatti.

### GDPR
- data mapping;
- registro trattamenti;
- contratti ex art. 28 con processor;
- misure tecniche-organizzative;
- retention policy;
- procedura data breach;
- eventuale DPIA;
- eventuale nomina DPO.

### Consumer law
- prezzo premium chiaro;
- rinnovo chiaro;
- modalità recesso/cancellazione facile;
- niente interfacce ingannevoli;
- claim marketing dimostrabili.

### Data sourcing
- matrice fonti dati;
- licenze/permessi/ToS review;
- piano fallback se una fonte si interrompe.

### Store compliance
- disclosure App Store / Google Play;
- schede privacy corrette;
- motivazioni permessi coerenti.

---

## 17. Cosa NON fare assolutamente

- non partire in tutta Italia;
- non promettere “50€ garantiti” a tutti;
- non basarti solo su scraping aggressivo;
- non chiedere background location nel MVP;
- non confrontare prodotti non equivalenti come se fossero uguali;
- non mostrare prezzi senza data/validità/condizioni;
- non nascondere che la copertura è parziale;
- non riempire l’app di banner e promo che inquinano il ranking.

---

## 18. Cosa fare per renderla davvero memorabile

### 18.1 Brand promise intelligente
Non “ti mostriamo le offerte”.

Ma:
> “Ti guidiamo alla spesa migliore per la tua famiglia, nella tua zona, con trasparenza e senza perdere tempo.”

### 18.2 Design di fiducia
Ogni suggerimento deve spiegare:
- perché quel negozio è consigliato;
- quali prodotti incidono di più;
- quanto è affidabile il dato;
- quando è stato aggiornato;
- se servono carta fedeltà o condizioni particolari.

### 18.3 Community loop
- carica scontrino → guadagna crediti;
- conferma prezzo → guadagna badge;
- segnala errore → migliora il dato;
- utenti top contributor → premium o vantaggi.

### 18.4 Difesa competitiva
La tua vera difesa non è il codice.
È:
- rete dati locale;
- qualità normalizzazione catalogo;
- fiducia utenti;
- storico prezzi proprietario;
- community attiva;
- accordi retailer.

---

## 19. Strategia Android prima, poi iOS

Scelta corretta.

### Perché Android-first ha senso
- sviluppo iniziale spesso più flessibile;
- base utenti ampia;
- maggior varietà device utili a testare casi reali;
- scansione/camera/barcode spesso molto pratica.

### Quando fare iOS
Non appena:
- il motore dati funziona;
- il prodotto è chiaro;
- la retention è promettente;
- hai capito quali feature usano davvero gli utenti.

---

## 20. Un piano realistico di nascita dell’azienda

## Fase 1 — startup data-light
- una città;
- pochi supermercati;
- pochi SKU molto ricorrenti;
- focus convenienza del carrello.

## Fase 2 — data network
- scontrini;
- barcode;
- validazione community;
- qualità dato.

## Fase 3 — partnerships
- retailer locali;
- brand;
- catene;
- coupon;
- offerte riservate.

## Fase 4 — piattaforma di risparmio domestico
- dispensa;
- meal planning;
- budget familiare;
- benchmark prezzi storici;
- engine anti-spreco.

---

## 21. La mia raccomandazione finale, molto netta

Se vuoi costruire un’app **storica e grandiosa**, non partire come:
- “comparatore prezzi generico”.

Parti come:
- **assistente spesa personale**, con confronto reale del carrello, localizzazione minima, community data, grande trasparenza.

### Strategia migliore in assoluto
1. Android MVP in 1 area;
2. dati da volantini + qualche feed partner + barcode + scontrini;
3. nessun background location nel MVP;
4. claim marketing prudenti e verificabili;
5. premium semplice, non aggressivo;
6. iOS quando il motore qualità dato regge;
7. costruzione lenta di un database proprietario prezzi + storico.

---

## 22. Decisione finale: si può fare?

**Sì, si può fare.**

Ma non devi vendere “AI”.
Devi vendere:
- fiducia;
- trasparenza;
- risparmio plausibile;
- esperienza comoda;
- qualità dati.

Se fai questo bene, puoi creare un prodotto enorme.
Se sbagli raccolta dati, privacy, claim marketing o affidabilità prezzi, rischi di bruciare il progetto anche con una bella app.

---

## 23. Fonti principali considerate per la parte normativa e di contesto

- Commissione UE / EUR-Lex — GDPR, pratiche commerciali scorrette, pubblicità comparativa, price reduction rules.
- Garante Privacy — principi di minimizzazione, localizzazione smartphone, RPD/DPO, privacy by design.
- EDPB — DPO, trasferimenti internazionali, linee guida privacy.
- Google Play / Android Developers — location permissions, background location, minimizzazione permessi.
- Apple Developer — Core Location, approximate location, privacy labels.
- AGCM — pratiche commerciali scorrette, comparatori, dark patterns.
- MIMIT — osservatorio prezzi e monitoraggio.
- GS1 Italy — GTIN/API identità prodotto.

---

## 24. Nota finale importante

Questo documento è strategico-operativo e non sostituisce un parere legale professionale. Prima del lancio reale servono almeno:
- revisione privacy/GDPR;
- revisione consumer law/claim marketing;
- revisione contratti e fonti dati;
- review store compliance Apple/Google.
