package com.moukim.shjirati.assistant

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File

class JebOnnxModelLoader(
    private val context: Context,
) {
    fun prepareModelFiles(): File {
        val destination = File(context.filesDir, "jeb")
        destination.mkdirs()

        copyAssetIfMissing("jeb/model.onnx", File(destination, "model.onnx"))
        copyAssetIfMissing("jeb/model.onnx.data", File(destination, "model.onnx.data"))

        check(File(destination, "model.onnx").exists()) { "Missing JEB model.onnx" }
        check(File(destination, "model.onnx.data").exists()) { "Missing JEB model.onnx.data" }

        return File(destination, "model.onnx")
    }

    fun createSession(): OrtSession {
        val modelFile = prepareModelFiles()
        val environment = OrtEnvironment.getEnvironment()
        return environment.createSession(modelFile.absolutePath, OrtSession.SessionOptions())
    }

    private fun copyAssetIfMissing(assetPath: String, destination: File) {
        if (destination.exists() && destination.length() > 0L) return
        destination.parentFile?.mkdirs()
        context.assets.open(assetPath).use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
