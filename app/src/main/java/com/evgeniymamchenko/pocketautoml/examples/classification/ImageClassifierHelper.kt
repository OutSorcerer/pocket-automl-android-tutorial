/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evgeniymamchenko.pocketautoml.examples.classification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import org.tensorflow.lite.support.metadata.MetadataExtractor
import org.tensorflow.lite.support.metadata.schema.NormalizationOptions
import org.tensorflow.lite.support.metadata.schema.ProcessUnitOptions
import java.nio.ByteBuffer

/**
 * Runs image classification with the LiteRT [CompiledModel] API.
 *
 * Why LiteRT and not MediaPipe Tasks: Pocket AutoML exports a full EfficientNet
 * (Swish + Squeeze-Excite) classifier. MediaPipe Tasks runs the GPU in FP16 with
 * no way to opt out, and those ops overflow FP16 → NaN scores. LiteRT lets us
 * force [CompiledModel.GpuOptions.Precision.FP32], so the GPU produces correct
 * results.
 *
 * Threading: the GPU backend is bound to the thread that creates the model, so
 * EVERY call on a given instance ([setup], [classify], [close]) must run on the
 * same single dedicated thread. Callers own that thread (a single-thread
 * ExecutorService); this class does no threading of its own.
 */
class ImageClassifierHelper(
    val context: Context,
    var currentModel: Int = MODEL_POCKET_AUTOML,
    var currentDelegate: Int = DELEGATE_CPU,
    var maxResults: Int = MAX_RESULTS_DEFAULT,
    val listener: ClassifierListener? = null,
) {

    private var model: CompiledModel? = null
    private var labels: List<String> = emptyList()

    // Input geometry/normalization, resolved from the model when it is loaded.
    private var inputWidth: Int = DEFAULT_INPUT_SIZE
    private var inputHeight: Int = DEFAULT_INPUT_SIZE
    // mean/std may hold a single broadcast value or one value per RGB channel.
    private var inputMean: FloatArray = floatArrayOf(0f)
    private var inputStd: FloatArray = floatArrayOf(1f)

    fun isClosed(): Boolean = model == null

    /** Releases the underlying model. Must be called on the owner thread. */
    fun close() {
        model?.close()
        model = null
    }

    /**
     * Creates the [CompiledModel] for the current model + delegate. On GPU,
     * forces FP32 to avoid FP16 overflow (NaN). Falls back to CPU if the GPU
     * delegate can't be created (e.g. a device without GPU support). Must be
     * called on the owner thread.
     */
    fun setup() {
        close()

        val fileName = modelFileName(currentModel)
        try {
            // Read labels and input geometry straight from the model's embedded
            // metadata, so there is no separate labels file to manage at runtime.
            context.assets.open(fileName).use { stream ->
                val extractor = MetadataExtractor(ByteBuffer.wrap(stream.readBytes()))
                labels = readLabels(extractor)
                readInputShape(extractor)
                val (mean, std) =
                    readNormalization(extractor) ?: defaultNormalization(currentModel)
                inputMean = mean
                inputStd = std
            }

            val accelerator =
                if (currentDelegate == DELEGATE_GPU) Accelerator.GPU else Accelerator.CPU
            val options = CompiledModel.Options(accelerator)
            if (accelerator == Accelerator.GPU) {
                // The whole reason we use LiteRT instead of MediaPipe Tasks: force
                // full precision so EfficientNet's Swish/SE ops don't overflow FP16.
                options.gpuOptions = CompiledModel.GpuOptions(
                    precision = CompiledModel.GpuOptions.Precision.FP32
                )
            }
            model = CompiledModel.create(context.assets, fileName, options, null)
            Log.i(TAG, "Created CompiledModel: $fileName, delegate=$currentDelegate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create CompiledModel ($fileName): ${e.message}", e)
            if (currentDelegate == DELEGATE_GPU) {
                // GPU couldn't be initialized on this device; fall back to CPU so
                // classification keeps working.
                currentDelegate = DELEGATE_CPU
                listener?.onError(
                    "GPU acceleration isn't available. Falling back to CPU.",
                    GPU_ERROR
                )
                setup()
            } else {
                listener?.onError(
                    "Image classifier failed to initialize. See error logs for details."
                )
            }
        }
    }

    /**
     * Classifies a single [bitmap], rotating it [rotationDegrees] clockwise to
     * upright first. Returns the top results (sorted by score), or null on error.
     * Must be called on the owner thread.
     */
    fun classify(bitmap: Bitmap, rotationDegrees: Int): ResultBundle? {
        val currentModel = model ?: return null
        val startTime = SystemClock.uptimeMillis()

        val input = preprocess(bitmap, rotationDegrees)

        val inputBuffers = currentModel.createInputBuffers()
        val outputBuffers = currentModel.createOutputBuffers()
        try {
            inputBuffers[0].writeFloat(input)
            currentModel.run(inputBuffers, outputBuffers)
            val scores = outputBuffers[0].readFloat()

            val labelList =
                if (labels.size == scores.size) labels
                else scores.indices.map { "Class $it" }

            val categories = labelList.zip(scores.toList())
                .map { Category(label = it.first, score = it.second) }
                .sortedByDescending { it.score }
                .take(maxResults)

            return ResultBundle(categories, SystemClock.uptimeMillis() - startTime)
        } catch (e: Exception) {
            Log.e(TAG, "Classification failed: ${e.message}", e)
            listener?.onError("Image classifier failed to classify.")
            return null
        } finally {
            inputBuffers.forEach { it.close() }
            outputBuffers.forEach { it.close() }
        }
    }

    /** Scales to the model's input size, rotates upright, and normalizes to NHWC float. */
    private fun preprocess(bitmap: Bitmap, rotationDegrees: Int): FloatArray {
        val scaled = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true)
        val upright = rotate(scaled, rotationDegrees)
        return normalize(upright, inputMean, inputStd)
    }

    private fun rotate(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        if (rotationDegrees % 360 == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationDegrees.toFloat()) }
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    private fun normalize(bitmap: Bitmap, mean: FloatArray, std: FloatArray): FloatArray {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val out = FloatArray(pixels.size * 3)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val idx = i * 3
            out[idx] = (Color.red(pixel) - mean.channel(0)) / std.channel(0)
            out[idx + 1] = (Color.green(pixel) - mean.channel(1)) / std.channel(1)
            out[idx + 2] = (Color.blue(pixel) - mean.channel(2)) / std.channel(2)
        }
        return out
    }

    // A length-1 mean/std broadcasts to all channels; otherwise it is per-channel.
    private fun FloatArray.channel(index: Int): Float = if (size == 1) this[0] else this[index]

    /**
     * Reads the input NormalizationOptions (mean/std) embedded in the model
     * metadata — the same values the model was exported with — so preprocessing
     * always matches the model. Returns null if the model declares none.
     */
    private fun readNormalization(extractor: MetadataExtractor): Pair<FloatArray, FloatArray>? {
        return try {
            val tensorMetadata = extractor.getInputTensorMetadata(0) ?: return null
            for (i in 0 until tensorMetadata.processUnitsLength()) {
                val unit = tensorMetadata.processUnits(i) ?: continue
                if (unit.optionsType().toInt() == ProcessUnitOptions.NormalizationOptions.toInt()) {
                    val options = unit.options(NormalizationOptions()) as? NormalizationOptions
                        ?: continue
                    val mean = FloatArray(options.meanLength()) { options.mean(it) }
                    val std = FloatArray(options.stdLength()) { options.std(it) }
                    if (mean.isNotEmpty() && std.isNotEmpty()) return mean to std
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Could not read normalization from metadata: ${e.message}", e)
            null
        }
    }

    /** Reads the [1, H, W, C] input shape from the model so we resize correctly. */
    private fun readInputShape(extractor: MetadataExtractor) {
        try {
            val shape = extractor.getInputTensorShape(0)
            if (shape.size == 4) {
                inputHeight = shape[1]
                inputWidth = shape[2]
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not read input shape; defaulting to $DEFAULT_INPUT_SIZE", e)
            inputHeight = DEFAULT_INPUT_SIZE
            inputWidth = DEFAULT_INPUT_SIZE
        }
    }

    /** Reads class labels from the first associated label file in the metadata. */
    private fun readLabels(extractor: MetadataExtractor): List<String> {
        if (!extractor.hasMetadata()) return emptyList()
        val labelFile = extractor.associatedFileNames?.firstOrNull { it.endsWith(".txt") }
            ?: return emptyList()
        return extractor.getAssociatedFile(labelFile).bufferedReader().use { reader ->
            reader.readLines().filter { it.isNotBlank() }
        }
    }

    data class Category(val label: String, val score: Float)

    data class ResultBundle(
        val categories: List<Category>,
        val inferenceTime: Long,
    )

    interface ClassifierListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
    }

    companion object {
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val MODEL_POCKET_AUTOML = 0
        const val MODEL_EFFICIENTNETV0 = 1
        const val MODEL_EFFICIENTNETV2 = 2
        const val MAX_RESULTS_DEFAULT = 3
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1

        private const val DEFAULT_INPUT_SIZE = 224
        private const val TAG = "ImageClassifierHelper"

        private fun modelFileName(model: Int): String = when (model) {
            MODEL_EFFICIENTNETV0 -> "efficientnet-lite0.tflite"
            MODEL_EFFICIENTNETV2 -> "efficientnet-lite2.tflite"
            else -> "Kittens-or-Puppies.tflite"
        }

        // Fallback input normalization (mean, std), used only if a model declares no
        // NormalizationOptions metadata. Pocket AutoML's EfficientNetB0 bakes its own
        // rescaling/normalization and expects raw [0, 255] pixels (mean 0, std 1);
        // the EfficientNet-Lite models expect [-1, 1] (mean 127.5, std 127.5).
        private fun defaultNormalization(model: Int): Pair<FloatArray, FloatArray> = when (model) {
            MODEL_EFFICIENTNETV0, MODEL_EFFICIENTNETV2 ->
                floatArrayOf(127.5f) to floatArrayOf(127.5f)
            else -> floatArrayOf(0f) to floatArrayOf(1f)
        }
    }
}
