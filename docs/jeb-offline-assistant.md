# JEB Offline Arabic Assistant — Implementation Plan

## Goal
Add an entirely offline Arabic question-routing layer to Shjirati using IJyad/jeb-onnx as the decision model and the existing local plant catalog as the source of truth.

## Architecture
User text -> JEB intent routing -> local PlantCatalog lookup -> deterministic response builder.

JEB must never be treated as the plant knowledge database and must not generate factual plant data.

## Phases
1. Scaffold the feature behind a dedicated package and branch.
2. Add ONNX Runtime Android dependency.
3. Define typed Shjirati intents and JEB option schemas.
4. Implement deterministic prompt/option construction and confidence handling.
5. Implement local plant-name resolution through PlantCatalogRepository.
6. Implement growing-data response building from growing_data.json.
7. Add JEB ONNX asset/model loader and tokenizer adapter.
8. Add a real-device/instrumented smoke test for model loading and inference.
9. Add Arabic/Algerian-Arabic evaluation cases and confidence thresholds.
10. Connect the assistant to UI only after inference and data-grounding tests pass.
11. Measure APK size, model load time, RAM, latency and cold-start impact.
12. Keep the feature isolated until explicitly approved for merge.

## Safety/Correctness Rules
- No network access is required for inference.
- Plant facts come only from local catalog/growing data.
- Low-confidence JEB decisions must not trigger a factual answer.
- Unknown plants/questions fall back to a safe Arabic response.
- Do not silently fabricate missing growing data.
- Model binaries must be distributed through an appropriate large-file/release mechanism, not ordinary text Git blobs.

## Current external model
Model: IJyad/jeb-onnx
Backbone: MARBERTv2
Parameters: 178M
ONNX graph: opset 17
Runtime target: ONNX Runtime CPU
License: Apache 2.0
The upstream model card states that model.onnx.data is about 708 MB and is required together with model.onnx.

## Current status
- Branch created: feature/jeb-offline-assistant
- Architecture scaffold: in progress
- Model binary: intentionally not committed yet
- UI integration: not started
