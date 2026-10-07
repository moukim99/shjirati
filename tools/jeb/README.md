# JEB model preparation

Use the official IJyad/jeb-onnx export.

Required assets:
- app/src/main/assets/jeb/model.onnx
- app/src/main/assets/jeb/model.onnx.data
- app/src/main/assets/jeb/tokenizer/

The upstream model card states that model.onnx uses opset 17 and model.onnx.data is about 708 MB; both are required. The model is Apache-2.0 licensed.

Do not commit the 708 MB weight file to normal Git history. Download and validate the exact upstream files before emulator testing.

Upstream: https://huggingface.co/IJyad/jeb-onnx
