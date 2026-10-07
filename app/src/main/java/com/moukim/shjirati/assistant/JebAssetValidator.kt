package com.moukim.shjirati.assistant

import android.content.Context

class JebAssetValidator(private val context: Context) {
    fun missingAssets(): List<String> {
        val required = listOf(
            "jeb/model.onnx",
            "jeb/model.onnx.data",
            "jeb/tokenizer/tokenizer.json",
        )
        return required.filterNot(::assetExists)
    }

    private fun assetExists(path: String): Boolean =
        runCatching { context.assets.open(path).use { true } }.getOrDefault(false)
}
