#!/usr/bin/env python3
"""
savio_update_offers.py
======================
Automazione completa per aggiornare offers_current.json ogni settimana.

Pipeline:
  1. Scarica le pagine volantino di ogni insegna configurata
  2. Estrae il testo HTML / PDF
  3. Invia il testo a Claude API che restituisce JSON strutturato
  4. Valida e merge con il JSON esistente
  5. Salva offers_current.json aggiornato
  6. (opzionale) Push su GitHub via API

Uso:
  python3 savio_update_offers.py                      # aggiorna tutto
  python3 savio_update_offers.py --store conad        # solo un negozio
  python3 savio_update_offers.py --dry-run            # stampa senza salvare
  python3 savio_update_offers.py --store lidl --debug # verbose

Richiede:
  pip install requests beautifulsoup4 pdfplumber anthropic python-dateutil
  export ANTHROPIC_API_KEY="sk-ant-..."
"""

import os, sys, json, re, hashlib, logging, argparse, time
from datetime import date, datetime, timedelta
from pathlib import Path
from typing import Optional

import requests
from bs4 import BeautifulSoup
import pdfplumber
import anthropic
from dateutil.relativedelta import relativedelta

# ─── Configurazione ──────────────────────────────────────────────────────────

BASE_DIR      = Path(__file__).parent
ASSETS_DIR    = BASE_DIR / "assets"           # cartella assets Android
OFFERS_FILE   = ASSETS_DIR / "offers_current.json"
CATALOG_FILE  = ASSETS_DIR / "catalog_v1.json"
STORES_FILE   = ASSETS_DIR / "stores_pilot_v1.json"
LOG_FILE      = BASE_DIR   / "logs" / "update.log"

LOG_FILE.parent.mkdir(parents=True, exist_ok=True)

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler(LOG_FILE),
        logging.StreamHandler(sys.stdout),
    ]
)
log = logging.getLogger(__name__)

# ─── Configurazione insegne ───────────────────────────────────────────────────
# Ogni insegna ha: url volantino, metodo di estrazione, selettori CSS/XPath

STORE_CONFIGS = {
    "conad": {
        "name": "Conad",
        "leaflet_url": "https://www.conad.it/volantini",
        "method": "html",
        "selectors": ["div.product-card", ".offerta", ".promo", "[class*='product']", "[class*='offer']"],
        "fallback_pdf": None,
        "store_ids": ["conad_bologna_centro", "conad_pesaro_centro"],
    },
    "lidl": {
        "name": "Lidl",
        "leaflet_url": "https://www.lidl.it/it/le-nostre-offerte.htm",
        "method": "html",
        "selectors": [".offer-item", ".product-grid__item", "[class*='offer']", "[class*='product']"],
        "fallback_pdf": None,
        "store_ids": ["lidl_bologna_centro", "lidl_pesaro"],
    },
    "eurospin": {
        "name": "Eurospin",
        "leaflet_url": "https://www.eurospin.it/volantino/",
        "method": "html",
        "selectors": [".volantino-item", ".product", "[class*='promo']", "article"],
        "fallback_pdf": None,
        "store_ids": ["eurospin_bologna_nord", "eurospin_pesaro"],
    },
    "carrefour": {
        "name": "Carrefour",
        "leaflet_url": "https://www.carrefour.it/it/volantino",
        "method": "html",
        "selectors": [".product-tile", "[class*='offer']", "[class*='promo']", "[class*='product']"],
        "fallback_pdf": None,
        "store_ids": ["carrefour_pesaro"],
    },
    "md": {
        "name": "MD Discount",
        "leaflet_url": "https://www.mdspa.it/volantino",
        "method": "html",
        "selectors": ["article", ".prodotto", "[class*='product']", "[class*='offer']"],
        "fallback_pdf": None,
        "store_ids": ["md_pesaro"],
    },
    "esselunga": {
        "name": "Esselunga",
        "leaflet_url": "https://www.esselunga.it/it-it/offerte.html",
        "method": "html",
        "selectors": ["[class*='product']", "[class*='offer']", "[class*='promo']", "article"],
        "fallback_pdf": None,
        "store_ids": ["esselunga_bologna_centro", "esselunga_casalecchio"],
    },
    "coop": {
        "name": "Coop",
        "leaflet_url": "https://www.e.coop/volantini",
        "method": "html",
        "selectors": ["[class*='product']", "[class*='offerta']", "[class*='promo']", "article"],
        "fallback_pdf": None,
        "store_ids": ["coop_bologna_centro"],
    },
}

# ─── Classe principale ────────────────────────────────────────────────────────

class SavioUpdater:

    def __init__(self, dry_run: bool = False, debug: bool = False):
        self.dry_run = dry_run
        self.debug   = debug
        self.client  = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"])

        # Carica dati esistenti
        self.existing_offers  = self._load_json(OFFERS_FILE) or []
        self.catalog          = self._load_json(CATALOG_FILE) or []
        self.stores           = self._load_json(STORES_FILE) or []

        self.catalog_ids      = {c["id"] for c in self.catalog}
        self.catalog_by_name  = {c["name"].lower(): c["id"] for c in self.catalog}
        self.store_ids        = {s["id"] for s in self.stores}

        # Prossima settimana di validità
        today       = date.today()
        next_monday = today + timedelta(days=(7 - today.weekday()) % 7)
        self.valid_from = next_monday.isoformat()
        self.valid_to   = (next_monday + timedelta(days=6)).isoformat()

        log.info(f"SavioUpdater avviato | offerte esistenti: {len(self.existing_offers)}")
        log.info(f"Validità target: {self.valid_from} → {self.valid_to}")

    # ── Utility ───────────────────────────────────────────────────────────────

    def _load_json(self, path: Path):
        if not path.exists():
            log.warning(f"File non trovato: {path}")
            return None
        with open(path) as f:
            return json.load(f)

    def _save_json(self, path: Path, data):
        if self.dry_run:
            log.info(f"[DRY-RUN] Avrei scritto {len(data)} voci in {path}")
            return
        path.parent.mkdir(parents=True, exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        log.info(f"✅ Salvato: {path} ({len(data)} offerte)")

    def _make_offer_id(self, store_id: str, category_id: str) -> str:
        """Genera un ID deterministico per evitare duplicati."""
        raw = f"{store_id}_{category_id}_{self.valid_from}"
        return "off_" + hashlib.md5(raw.encode()).hexdigest()[:8]

    # ── Scraping ──────────────────────────────────────────────────────────────

    def fetch_page(self, url: str, timeout: int = 15) -> Optional[str]:
        """Scarica una pagina HTML e restituisce il testo rilevante."""
        headers = {
            "User-Agent": "Mozilla/5.0 (compatible; Savio/1.0; +https://savio.app)",
            "Accept-Language": "it-IT,it;q=0.9",
            "Accept": "text/html,application/xhtml+xml",
        }
        try:
            r = requests.get(url, headers=headers, timeout=timeout)
            r.raise_for_status()
            return r.text
        except requests.RequestException as e:
            log.warning(f"Fetch fallito ({url}): {e}")
            return None

    def extract_text_from_html(self, html: str, selectors: list) -> str:
        """Estrae testo dai selettori CSS — fallback su tutto il body."""
        soup = BeautifulSoup(html, "html.parser")

        # Rimuovi script/style
        for tag in soup(["script", "style", "nav", "footer", "header"]):
            tag.decompose()

        collected = []
        for sel in selectors:
            try:
                elements = soup.select(sel)
                for el in elements[:60]:   # max 60 elementi per selettore
                    text = el.get_text(" ", strip=True)
                    if len(text) > 10:
                        collected.append(text)
            except Exception:
                pass

        if collected:
            return "\n".join(collected[:200])   # max 200 blocchi

        # Fallback: testo grezzo del body
        return soup.get_text(" ", strip=True)[:8000]

    def extract_text_from_pdf(self, pdf_path: str) -> str:
        """Estrae testo da PDF con pdfplumber."""
        text_parts = []
        try:
            with pdfplumber.open(pdf_path) as pdf:
                for page in pdf.pages[:20]:   # prime 20 pagine
                    t = page.extract_text()
                    if t:
                        text_parts.append(t)
        except Exception as e:
            log.warning(f"PDF extraction fallita: {e}")
        return "\n".join(text_parts)[:10000]

    def download_pdf(self, url: str) -> Optional[str]:
        """Scarica un PDF temporaneamente."""
        try:
            r = requests.get(url, timeout=30, stream=True)
            r.raise_for_status()
            tmp_path = f"/tmp/savio_leaflet_{int(time.time())}.pdf"
            with open(tmp_path, "wb") as f:
                for chunk in r.iter_content(chunk_size=8192):
                    f.write(chunk)
            return tmp_path
        except Exception as e:
            log.warning(f"Download PDF fallito: {e}")
            return None

    # ── Claude API — Estrazione strutturata ───────────────────────────────────

    EXTRACTION_PROMPT = """Sei un assistente specializzato nell'estrazione di dati da volantini della spesa italiani.

Hai ricevuto il testo estratto dal sito/volantino di {store_name}.

CATALOGO PRODOTTI DISPONIBILE (usa SOLO questi ID categoria):
{catalog_summary}

COMPITO:
Estrai tutti i prodotti in offerta che riesci a trovare nel testo.
Per ogni prodotto, determina:
1. Quale categoria del catalogo corrisponde meglio (usa l'ID esatto)
2. Il nome del prodotto come appare nel testo
3. Il brand (o null se non specificato o MDD)
4. Il prezzo in euro (solo numero float, es: 1.29)
5. Se richiede tessera fedeltà/fidelity (true/false)

REGOLE IMPORTANTI:
- Includi SOLO prodotti con prezzo chiaro e leggibile
- Se un prodotto non corrisponde a nessuna categoria del catalogo, SALTALO
- I prezzi devono essere in euro, usa il punto come decimale
- Se vedi "€1,29" scrivi 1.29
- Non inventare prezzi: se non è nel testo, salta il prodotto
- Massimo 40 prodotti per insegna

RISPOSTA: Rispondi SOLO con JSON valido, nessun altro testo, nessun markdown.
Formato array:
[
  {{
    "product_category_id": "latte_intero_1l",
    "product_name": "Latte Intero Marca 1L",
    "brand": "Marca",
    "price_eur": 1.15,
    "requires_fidelity": false
  }}
]

Se non trovi nessun prodotto valido rispondi con: []

TESTO DEL VOLANTINO:
{leaflet_text}
"""

    def extract_offers_with_claude(self, store_name: str, text: str) -> list:
        """Usa Claude per estrarre offerte strutturate dal testo grezzo."""
        if not text or len(text) < 50:
            log.warning(f"Testo troppo corto per {store_name}")
            return []

        # Prepara summary del catalogo (abbreviato per evitare token inutili)
        catalog_summary = "\n".join(
            f"- {c['id']}: {c['name']} (aliases: {', '.join(c.get('aliases', [])[:3])})"
            for c in self.catalog[:80]   # prime 80 categorie
        )

        prompt = self.EXTRACTION_PROMPT.format(
            store_name=store_name,
            catalog_summary=catalog_summary,
            leaflet_text=text[:6000],   # max 6000 caratteri di testo volantino
        )

        try:
            log.info(f"  → Invio a Claude ({len(text)} chars testo)...")
            response = self.client.messages.create(
                model="claude-sonnet-4-20250514",
                max_tokens=2000,
                messages=[{"role": "user", "content": prompt}]
            )
            raw = response.content[0].text.strip()

            if self.debug:
                log.debug(f"Claude response ({store_name}):\n{raw[:500]}")

            # Pulizia: rimuovi eventuali markdown fences
            raw = re.sub(r"```(?:json)?", "", raw).strip().rstrip("```").strip()

            parsed = json.loads(raw)
            if not isinstance(parsed, list):
                return []
            return parsed

        except json.JSONDecodeError as e:
            log.error(f"Claude ha restituito JSON non valido per {store_name}: {e}")
            return []
        except Exception as e:
            log.error(f"Errore Claude API per {store_name}: {e}")
            return []

    # ── Validazione e costruzione offerta ─────────────────────────────────────

    def build_offer(self, raw: dict, store_id: str) -> Optional[dict]:
        """Valida i dati estratti da Claude e costruisce l'oggetto offerta."""
        cat_id = raw.get("product_category_id", "").strip()
        if cat_id not in self.catalog_ids:
            if self.debug:
                log.debug(f"  ✗ Categoria sconosciuta: {cat_id}")
            return None

        price = raw.get("price_eur")
        try:
            price = float(str(price).replace(",", "."))
        except (TypeError, ValueError):
            return None

        if not (0.05 <= price <= 500.0):   # sanity check prezzo
            return None

        offer_id = self._make_offer_id(store_id, cat_id)

        return {
            "id": offer_id,
            "store_id": store_id,
            "product_category_id": cat_id,
            "product_name": str(raw.get("product_name", ""))[:120],
            "brand": raw.get("brand") or None,
            "price_eur": round(price, 2),
            "price_per_unit": None,
            "requires_fidelity": bool(raw.get("requires_fidelity", False)),
            "valid_from": self.valid_from,
            "valid_to": self.valid_to,
            "source_type": "LEAFLET",
            "confidence_level": "MEDIUM",   # estratto automaticamente = MEDIUM
        }

    # ── Processo per singola insegna ─────────────────────────────────────────

    def process_store(self, chain_key: str) -> list:
        """Scarica, estrae e valida le offerte di una singola insegna."""
        cfg = STORE_CONFIGS[chain_key]
        log.info(f"\n{'='*50}")
        log.info(f"Processing: {cfg['name']} ({cfg['leaflet_url']})")

        # 1. Scarica pagina
        html = self.fetch_page(cfg["leaflet_url"])
        if not html:
            log.warning(f"  ✗ Impossibile scaricare la pagina di {cfg['name']}")
            return []

        # 2. Estrai testo
        text = self.extract_text_from_html(html, cfg["selectors"])
        log.info(f"  → Testo estratto: {len(text)} caratteri")

        # 3. Claude estrae le offerte
        raw_offers = self.extract_offers_with_claude(cfg["name"], text)
        log.info(f"  → Claude ha trovato {len(raw_offers)} candidati")

        # 4. Costruisci offerte per ogni store_id associato
        new_offers = []
        for store_id in cfg["store_ids"]:
            if store_id not in self.store_ids:
                log.warning(f"  ✗ store_id {store_id} non in stores_pilot_v1.json")
                continue
            for raw in raw_offers:
                offer = self.build_offer(raw, store_id)
                if offer:
                    new_offers.append(offer)

        # Deduplication (stesso ID)
        seen = set()
        deduped = []
        for o in new_offers:
            if o["id"] not in seen:
                seen.add(o["id"])
                deduped.append(o)

        log.info(f"  ✅ {len(deduped)} offerte valide per {cfg['name']}")
        return deduped

    # ── Merge con offerte esistenti ───────────────────────────────────────────

    def merge_offers(self, new_offers: list) -> list:
        """
        Strategia di merge:
        - Mantieni le offerte esistenti che sono ancora valide (valid_to >= oggi)
        - Sostituisci le offerte scadute con le nuove
        - Le nuove offerte con stesso store_id + category_id sovrascrivono le vecchie
        """
        today_str = date.today().isoformat()

        # Offerte esistenti ancora valide (non della settimana prossima)
        still_valid = [
            o for o in self.existing_offers
            if o.get("valid_to", "2000-01-01") >= today_str
        ]

        # Costruisci un dizionario delle nuove offerte per lookup rapido
        new_lookup = {}
        for o in new_offers:
            key = (o["store_id"], o["product_category_id"])
            new_lookup[key] = o

        # Aggiorna/sostituisci le offerte esistenti dove abbiamo nuovi dati
        merged = []
        replaced = 0
        for o in still_valid:
            key = (o["store_id"], o["product_category_id"])
            if key in new_lookup:
                merged.append(new_lookup.pop(key))
                replaced += 1
            else:
                merged.append(o)

        # Aggiungi le nuove offerte rimaste (categorie non presenti prima)
        added = len(new_lookup)
        merged.extend(new_lookup.values())

        log.info(f"\nMerge: {replaced} aggiornate, {added} nuove, {len(merged)} totali")
        return merged

    # ── Entry point ───────────────────────────────────────────────────────────

    def run(self, chains: Optional[list] = None):
        """Esegue l'aggiornamento completo."""
        chains = chains or list(STORE_CONFIGS.keys())
        log.info(f"Aggiornamento offerte per: {', '.join(chains)}")

        all_new = []
        for chain_key in chains:
            if chain_key not in STORE_CONFIGS:
                log.warning(f"Insegna sconosciuta: {chain_key}")
                continue
            try:
                offers = self.process_store(chain_key)
                all_new.extend(offers)
                time.sleep(2)   # rate limiting cortesia verso i siti
            except Exception as e:
                log.error(f"Errore su {chain_key}: {e}")
                continue

        log.info(f"\nTotale nuove offerte: {len(all_new)}")

        if not all_new:
            log.warning("Nessuna offerta estratta. Il file non verrà modificato.")
            return

        merged = self.merge_offers(all_new)
        self._save_json(OFFERS_FILE, merged)

        # Stampa riepilogo per negozio
        by_store = {}
        for o in merged:
            by_store[o["store_id"]] = by_store.get(o["store_id"], 0) + 1

        log.info("\n=== RIEPILOGO FINALE ===")
        for store_id, count in sorted(by_store.items()):
            log.info(f"  {store_id}: {count} offerte")
        log.info(f"  TOTALE: {len(merged)} offerte")


# ─── CLI ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Savio — aggiornamento automatico offerte")
    parser.add_argument("--store", nargs="+", help="Aggiorna solo queste insegne (es: lidl conad)")
    parser.add_argument("--dry-run", action="store_true", help="Non salvare, stampa solo")
    parser.add_argument("--debug", action="store_true", help="Log verbose")
    args = parser.parse_args()

    if "ANTHROPIC_API_KEY" not in os.environ:
        print("❌ Imposta ANTHROPIC_API_KEY prima di eseguire lo script")
        print("   export ANTHROPIC_API_KEY='sk-ant-...'")
        sys.exit(1)

    if args.debug:
        logging.getLogger().setLevel(logging.DEBUG)

    updater = SavioUpdater(dry_run=args.dry_run, debug=args.debug)
    updater.run(chains=args.store)

if __name__ == "__main__":
    main()
