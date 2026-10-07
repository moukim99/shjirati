#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
DEST="$ROOT/app/src/main/assets/jeb"
mkdir -p "$DEST/tokenizer"
BASE="https://huggingface.co/IJyad/jeb-onnx/resolve/main"
curl -L --fail --retry 3 -o "$DEST/model.onnx" "$BASE/model.onnx"
curl -L --fail --retry 3 -o "$DEST/model.onnx.data" "$BASE/model.onnx.data"
curl -L --fail --retry 3 -o "$DEST/tokenizer/tokenizer.json" "$BASE/tokenizer/tokenizer.json"
curl -L --fail --retry 3 -o "$DEST/tokenizer/tokenizer_config.json" "$BASE/tokenizer/tokenizer_config.json"
curl -L --fail --retry 3 -o "$DEST/tokenizer/special_tokens_map.json" "$BASE/tokenizer/special_tokens_map.json"
echo "JEB assets downloaded to $DEST"
