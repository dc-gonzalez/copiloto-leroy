#!/usr/bin/env bash
# Builds the Sprint 2 hand-in: entrega/saida/Grupo105_Sprint2.zip with the cover PDF and the MVP code.
# Usage: VIDEO_URL=... SITE_URL=... API_URL=... REPO_URL=... bash entrega/gerar.sh
set -euo pipefail

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
SAIDA="$RAIZ/entrega/saida"
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
PENDENTE="(link a definir)"

mkdir -p "$SAIDA"
rm -f "$SAIDA"/Grupo105_Sprint2.pdf "$SAIDA"/Grupo105_Sprint2.zip "$SAIDA"/copiloto-leroy-mvp.zip

sed -e "s|{{VIDEO_URL}}|${VIDEO_URL:-$PENDENTE}|g" \
    -e "s|{{SITE_URL}}|${SITE_URL:-$PENDENTE}|g" \
    -e "s|{{API_URL}}|${API_URL:-$PENDENTE}|g" \
    -e "s|{{REPO_URL}}|${REPO_URL:-$PENDENTE}|g" \
    -e "s|{{DATA}}|$(date +%d/%m/%Y)|g" \
    "$RAIZ/entrega/capa.html" > "$SAIDA/capa.html"

"$CHROME" --headless=new --disable-gpu --no-pdf-header-footer \
  --print-to-pdf="$SAIDA/Grupo105_Sprint2.pdf" "file://$SAIDA/capa.html" 2>/dev/null
rm "$SAIDA/capa.html"

# Only tracked files: secrets, course PDFs, node_modules and build output are never included.
git -C "$RAIZ" archive --format=zip --prefix=copiloto-leroy/ -o "$SAIDA/copiloto-leroy-mvp.zip" HEAD

(cd "$SAIDA" && zip -q Grupo105_Sprint2.zip Grupo105_Sprint2.pdf copiloto-leroy-mvp.zip)

if grep -q "$PENDENTE" <(strings "$SAIDA/Grupo105_Sprint2.pdf" 2>/dev/null) 2>/dev/null; then
  echo "WARNING: some links are still missing in the PDF"
fi
ls -lh "$SAIDA"
