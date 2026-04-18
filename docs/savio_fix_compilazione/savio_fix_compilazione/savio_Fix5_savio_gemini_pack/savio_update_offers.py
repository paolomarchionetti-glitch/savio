#!/usr/bin/env python3
"""
savio_update_offers.py
======================
Automazione offerte Savio — powered by Google Gemini 2.5 Flash (GRATUITO).

Pipeline:
  1. Scarica le pagine volantino di ogni insegna
  2. Estrae il testo HTML
  3. Invia il testo a Gemini 2.5 Flash che restituisce JSON strutturato
  4. Valida e merge con il JSON esistente
  5. Salva offers_current.json aggiornato

Uso:
  python3 savio_update_offers.py                      # aggiorna tutto
  python3 savio_update_offers.py --store conad        # solo un negozio
  python3 savio_update_offers.py --dry-run            # stampa senza salvare
  python3 savio_update_offers.py --store lidl --debug # verbose

Richiede:
  pip install requests beautifulsoup4 pdfplumber google-genai python-dateutil
  export GEMINI_API_KEY="AIza..."   ← da aistudio.google.com (GRATUITO)
"""

import os, sys, json, re, hashlib, logging, argparse, time
from datetime import date, timedelta
from pathlib import Path
from typing import Optional

import requests
from bs4 import BeautifulSoup
from google import genai
from google.genai import types

# ─── Configurazione paths ─────────────────────────────────────────────────────

BASE_DIR    = Path(__file__).parent
ASSETS_DIR  = BASE_DIR / "assets"
OFFERS_FILE = ASSETS_DIR / "offers_current.json"
CATALOG_FILE= ASSETS_DIR / "catalog_v1.json"
STORES_FILE = ASSETS_DIR / "stores_pilot_v1.json"
LOG_FILE    = BASE_DIR   / "logs" / "update.log"

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

# ─── Modello Gemini ───────────────────────────────────────────────────────────
# gemini-2.5-flash-preview-04-17  = gratuito, veloce, ottimo per JSON strutturato
# gemini-2.5-pro-preview-03-25    = più potente ma ha limiti free tier più bassi
GEMINI_MODEL = "gemini-2.5-flash-preview-04-17"

# ─── Configurazione insegne ───────────────────────────────────────────────────

STORE_CONFIGS = {
    "conad": {
        "name": "Conad",
        "leaflet_url": "https://www.conad.it/volantini",
        "selectors": ["div.product-card", ".offerta", ".promo", "[class*='product']", "[class*='offer']"],
        "store_ids": ["conad_bologna_centro", "conad_pesaro_centro"],
    },
    "lidl": {
        "name": "Lidl",
        "leaflet_url": "https://www.lidl.it/it/le-nostre-offerte.htm",
        "selectors": [".offer-item", ".product-grid__item", "[class*='offer']", "[class*='product']"],
        "store_ids": ["lidl_bologna_centro", "lidl_pesaro"],
    },
    "eurospin": {
        "name": "Eurospin",
        "leaflet_url": "https://www.eurospin.it/volantino/",
        "selectors": [".volantino-item", ".product", "[class*='promo']", "article"],
        "store_ids": ["eurospin_bologna_nord", "eurospin_pesaro"],
    },
    "carrefour": {
        "name": "Carrefour",
        "leaflet_url": "https://www.carrefour.it/it/volantino",
        "selectors": [".product-tile", "[class*='offer']", "[class*='promo']", "[class*='product']"],
        "store_ids": ["carrefour_pesaro"],
    },
    "md": {
        "name": "MD Discount",
        "leaflet_url": "https://www.mdspa.it/volantino",
        "selectors": ["article", ".prodotto", "[class*='product']", "[class*='offer']"],
        "store_ids": ["md_pesaro"],
    },
    "esselunga": {
        "name": "Esselunga",
        "leaflet_url": "https://www.esselunga.it/it-it/offerte.html",
        "selectors": ["[class*='product']", "[class*='offer']", "[class*='promo']", "article"],
        "store_ids": ["esselunga_bologna_centro", "esselunga_casalecchio"],
    },
    "coop": {
        "name": "Coop",
        "leaflet_url": "https://www.e.coop/volantini",
        "selectors": ["[class*='product']", "[class*='offerta']", "[class*='promo']", "article"],
        "store_ids": ["coop_bologna_centro"],
    },
}

# ─── Classe principale ────────────────────────────────────────────────────────

class SavioUpdater:

    def __init__(self, dry_run: bool = False, debug: bool = False):
        self.dry_run = dry_run
        self.debug   = debug

        # Inizializza client Gemini
        api_key = os.environ.get("GEMINI_API_KEY", "")
        if not api_key:
            log.error("GEMINI_API_KEY non impostata. Esegui: export GEMINI_API_KEY='AIza...'")
            sys.exit(1)
        self.gemini = genai.Client(api_key=api_key)

        # Carica dati esistenti
        self.existing_offers = self._load_json(OFFERS_FILE) or []
        self.catalog         = self._load_json(CATALOG_FILE) or []
        self.stores          = self._load_json(STORES_FILE) or []

        self.catalog_ids  = {c["id"] for c in self.catalog}
        self.store_ids    = {s["id"] for s in self.stores}

        # Settimana di validità (lunedì prossimo → domenica)
        today       = date.today()
        next_monday = today + timedelta(days=(7 - today.weekday()) % 7)
        self.valid_from = next_monday.isoformat()
        self.valid_to   = (next_monday + timedelta(days=6)).isoformat()

        log.info(f"SavioUpdater | modello: {GEMINI_MODEL} | offerte esistenti: {len(self.existing_offers)}")
        log.info(f"Validità target: {self.valid_from} → {self.valid_to}")

    # ── Utility ───────────────────────────────────────────────────────────────

    def _load_json(self, path: Path):
        if not path.exists():
            log.warning(f"File non trovato: {path}")
            return None
        with open(path, encoding="utf-8") as f:
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
        raw = f"{store_id}_{category_id}_{self.valid_from}"
        return "off_" + hashlib.md5(raw.encode()).hexdigest()[:8]

    # ── Scraping ──────────────────────────────────────────────────────────────

    def fetch_page(self, url: str, timeout: int = 15) -> Optional[str]:
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
        soup = BeautifulSoup(html, "html.parser")
        for tag in soup(["script", "style", "nav", "footer", "header"]):
            tag.decompose()

        collected = []
        for sel in selectors:
            try:
                for el in soup.select(sel)[:60]:
                    text = el.get_text(" ", strip=True)
                    if len(text) > 10:
                        collected.append(text)
            except Exception:
                pass

        if collected:
            return "\n".join(collected[:200])
        return soup.get_text(" ", strip=True)[:8000]

    # ── Gemini — Estrazione strutturata ───────────────────────────────────────

    EXTRACTION_PROMPT = """Sei un assistente specializzato nell'estrazione di dati da volantini della spesa italiani.

Hai ricevuto il testo estratto dal sito/volantino di {store_name}.

CATALOGO PRODOTTI DISPONIBILE (usa SOLO questi ID categoria):
{catalog_summary}

COMPITO:
Estrai tutti i prodotti in offerta presenti nel testo.
Per ogni prodotto individua:
1. product_category_id — l'ID del catalogo che corrisponde meglio (copia esatto dalla lista sopra)
2. product_name — nome del prodotto come appare nel testo
3. brand — marca (null se prodotto sfuso o MDD senza brand)
4. price_eur — prezzo in euro come numero float con punto decimale (es: 1.29)
5. requires_fidelity — true se il prezzo richiede tessera fedeltà, altrimenti false

REGOLE:
- Includi solo prodotti con prezzo chiaramente indicato
- Se un prodotto non corrisponde ad alcuna categoria, saltalo
- Converti "€1,29" → 1.29 (usa il punto come decimale)
- Non inventare prezzi non presenti nel testo
- Massimo 40 prodotti per insegna

FORMATO RISPOSTA: solo JSON puro, nessun testo, nessun markdown, nessun backtick.

[
  {{
    "product_category_id": "latte_intero_1l",
    "product_name": "Latte Intero Marca 1L",
    "brand": "Marca",
    "price_eur": 1.15,
    "requires_fidelity": false
  }}
]

Se non trovi prodotti validi rispondi con: []

TESTO VOLANTINO:
{leaflet_text}
"""

    def extract_offers_with_gemini(self, store_name: str, text: str) -> list:
        """Usa Gemini 2.5 Flash per estrarre offerte strutturate dal testo."""
        if not text or len(text) < 50:
            log.warning(f"Testo troppo corto per {store_name}, skip")
            return []

        catalog_summary = "\n".join(
            f"- {c['id']}: {c['name']} (es: {', '.join(c.get('aliases', [])[:3])})"
            for c in self.catalog[:80]
        )

        prompt = self.EXTRACTION_PROMPT.format(
            store_name=store_name,
            catalog_summary=catalog_summary,
            leaflet_text=text[:6000],
        )

        try:
            log.info(f"  → Invio a Gemini ({len(text)} chars)...")

            response = self.gemini.models.generate_content(
                model=GEMINI_MODEL,
                contents=prompt,
                config=types.GenerateContentConfig(
                    temperature=0.1,        # bassa temp = output deterministico/strutturato
                    max_output_tokens=2048,
                    response_mime_type="application/json",  # forza output JSON diretto
                ),
            )

            raw = response.text.strip()

            if self.debug:
                log.debug(f"Gemini response ({store_name}):\n{raw[:500]}")

            # Pulizia difensiva da eventuali markdown fences
            raw = re.sub(r"```(?:json)?", "", raw).strip().rstrip("`").strip()

            parsed = json.loads(raw)
            if not isinstance(parsed, list):
                return []
            return parsed

        except json.JSONDecodeError as e:
            log.error(f"JSON non valido da Gemini per {store_name}: {e}")
            if self.debug:
                log.debug(f"Raw: {raw[:300]}")
            return []
        except Exception as e:
            log.error(f"Errore Gemini API per {store_name}: {e}")
            return []

    # ── Validazione e costruzione offerta ─────────────────────────────────────

    def build_offer(self, raw: dict, store_id: str) -> Optional[dict]:
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

        if not (0.05 <= price <= 500.0):
            return None

        return {
            "id": self._make_offer_id(store_id, cat_id),
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
            "confidence_level": "MEDIUM",
        }

    # ── Processo per singola insegna ─────────────────────────────────────────

    def process_store(self, chain_key: str) -> list:
        cfg = STORE_CONFIGS[chain_key]
        log.info(f"\n{'='*50}")
        log.info(f"Processing: {cfg['name']} ({cfg['leaflet_url']})")

        html = self.fetch_page(cfg["leaflet_url"])
        if not html:
            log.warning(f"  ✗ Pagina non raggiungibile per {cfg['name']}")
            return []

        text = self.extract_text_from_html(html, cfg["selectors"])
        log.info(f"  → Testo estratto: {len(text)} caratteri")

        raw_offers = self.extract_offers_with_gemini(cfg["name"], text)
        log.info(f"  → Gemini ha trovato {len(raw_offers)} candidati")

        new_offers = []
        for store_id in cfg["store_ids"]:
            if store_id not in self.store_ids:
                log.warning(f"  ✗ store_id {store_id} non trovato in stores_pilot_v1.json")
                continue
            for raw in raw_offers:
                offer = self.build_offer(raw, store_id)
                if offer:
                    new_offers.append(offer)

        # Deduplicazione
        seen = set()
        deduped = [o for o in new_offers if not (o["id"] in seen or seen.add(o["id"]))]

        log.info(f"  ✅ {len(deduped)} offerte valide per {cfg['name']}")
        return deduped

    # ── Merge ─────────────────────────────────────────────────────────────────

    def merge_offers(self, new_offers: list) -> list:
        today_str = date.today().isoformat()
        still_valid = [
            o for o in self.existing_offers
            if o.get("valid_to", "2000-01-01") >= today_str
        ]

        new_lookup = {}
        for o in new_offers:
            key = (o["store_id"], o["product_category_id"])
            new_lookup[key] = o

        merged = []
        replaced = 0
        for o in still_valid:
            key = (o["store_id"], o["product_category_id"])
            if key in new_lookup:
                merged.append(new_lookup.pop(key))
                replaced += 1
            else:
                merged.append(o)

        added = len(new_lookup)
        merged.extend(new_lookup.values())
        log.info(f"\nMerge: {replaced} aggiornate, {added} nuove, {len(merged)} totali")
        return merged

    # ── Entry point ───────────────────────────────────────────────────────────

    def run(self, chains: Optional[list] = None):
        chains = chains or list(STORE_CONFIGS.keys())
        log.info(f"Aggiornamento per: {', '.join(chains)}")

        all_new = []
        for chain_key in chains:
            if chain_key not in STORE_CONFIGS:
                log.warning(f"Insegna sconosciuta: {chain_key}")
                continue
            try:
                offers = self.process_store(chain_key)
                all_new.extend(offers)
                time.sleep(1)  # pausa cortesia tra le chiamate
            except Exception as e:
                log.error(f"Errore su {chain_key}: {e}")
                continue

        log.info(f"\nTotale nuove offerte: {len(all_new)}")

        if not all_new:
            log.warning("Nessuna offerta estratta. Il file non verrà modificato.")
            return

        merged = self.merge_offers(all_new)
        self._save_json(OFFERS_FILE, merged)

        # Riepilogo per negozio
        by_store = {}
        for o in merged:
            by_store[o["store_id"]] = by_store.get(o["store_id"], 0) + 1

        log.info("\n=== RIEPILOGO FINALE ===")
        for store_id, count in sorted(by_store.items()):
            log.info(f"  {store_id}: {count} offerte")
        log.info(f"  TOTALE: {len(merged)} offerte")


# ─── CLI ──────────────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(description="Savio — aggiornamento offerte con Gemini")
    parser.add_argument("--store", nargs="+", help="Solo queste insegne (es: lidl conad)")
    parser.add_argument("--dry-run", action="store_true", help="Non salvare")
    parser.add_argument("--debug", action="store_true", help="Log verbose")
    args = parser.parse_args()

    if args.debug:
        logging.getLogger().setLevel(logging.DEBUG)

    updater = SavioUpdater(dry_run=args.dry_run, debug=args.debug)
    updater.run(chains=args.store)


if __name__ == "__main__":
    main()
