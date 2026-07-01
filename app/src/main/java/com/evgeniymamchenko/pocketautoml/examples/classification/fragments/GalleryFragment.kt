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
package com.evgeniymamchenko.pocketautoml.examples.classification.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.evgeniymamchenko.pocketautoml.examples.classification.ImageClassifierHelper
import com.evgeniymamchenko.pocketautoml.examples.classification.MainViewModel
import com.evgeniymamchenko.pocketautoml.examples.classification.databinding.FragmentGalleryBinding
import java.util.Locale
import java.util.concurrent.Executors

class GalleryFragment : Fragment(), ImageClassifierHelper.ClassifierListener {
    enum class MediaType {
        IMAGE, UNKNOWN
    }

    private var _fragmentGalleryBinding: FragmentGalleryBinding? = null
    private val fragmentGalleryBinding
        get() = _fragmentGalleryBinding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val classificationResultsAdapter by lazy {
        ClassificationResultsAdapter().apply {
            updateAdapterSize(viewModel.currentMaxResults)
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            // Handle the returned Uri
            uri?.let { mediaUri ->
                when (loadMediaType(mediaUri)) {
                    MediaType.IMAGE -> runClassificationOnImage(mediaUri)
                    MediaType.UNKNOWN -> {
                        updateDisplayView(MediaType.UNKNOWN)
                        Toast.makeText(
                            requireContext(),
                            "Unsupported data type.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentGalleryBinding =
            FragmentGalleryBinding.inflate(inflater, container, false)

        return fragmentGalleryBinding.root
    }

    override fun onDestroyView() {
        _fragmentGalleryBinding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentGalleryBinding.fabGetContent.setOnClickListener {
            getContent.launch(arrayOf("image/*"))
            updateDisplayView(MediaType.UNKNOWN)
        }
        with(fragmentGalleryBinding.recyclerviewResults) {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = classificationResultsAdapter
        }

        initBottomSheetControls()
    }

    private fun initBottomSheetControls() {
        updateControlsUi()

        // When clicked, reduce the maximum number of classification results shown
        fragmentGalleryBinding.bottomSheetLayout.maxResultsMinus.setOnClickListener {
            if (viewModel.currentMaxResults > 1) {
                viewModel.setMaxResults(viewModel.currentMaxResults - 1)
                updateControlsUi()
            }
        }

        // When clicked, increase the maximum number of classification results shown
        fragmentGalleryBinding.bottomSheetLayout.maxResultsPlus.setOnClickListener {
            if (viewModel.currentMaxResults < 3) {
                viewModel.setMaxResults(viewModel.currentMaxResults + 1)
                updateControlsUi()
            }
        }

        // When clicked, change the underlying hardware used for inference. Current options are CPU
        // and GPU
        fragmentGalleryBinding.bottomSheetLayout.spinnerDelegate.setSelection(
            viewModel.currentDelegate, false
        )
        fragmentGalleryBinding.bottomSheetLayout.spinnerDelegate.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {

                    viewModel.setDelegate(p2)
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }

        // When clicked, change the underlying model used for image classification
        fragmentGalleryBinding.bottomSheetLayout.spinnerModel.setSelection(
            viewModel.currentModel, false
        )
        fragmentGalleryBinding.bottomSheetLayout.spinnerModel.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long
                ) {
                    viewModel.setModel(p2)
                    updateControlsUi()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    /* no op */
                }
            }
    }

    // Update the values displayed in the bottom sheet, and clear any shown result.
    @SuppressLint("NotifyDataSetChanged")
    private fun updateControlsUi() {
        fragmentGalleryBinding.imageResult.visibility = View.GONE
        fragmentGalleryBinding.bottomSheetLayout.maxResultsValue.text =
            viewModel.currentMaxResults.toString()
        fragmentGalleryBinding.tvPlaceholder.visibility = View.VISIBLE
        classificationResultsAdapter.updateAdapterSize(viewModel.currentMaxResults)
        classificationResultsAdapter.updateResults(null)
        classificationResultsAdapter.notifyDataSetChanged()
    }

    // Load, display, and classify the selected image.
    @SuppressLint("NotifyDataSetChanged")
    private fun runClassificationOnImage(uri: Uri) {
        setUiEnabled(false)
        updateDisplayView(MediaType.IMAGE)

        val bitmap = decodeBitmap(uri)
        if (bitmap == null) {
            Log.e(TAG, "Unable to decode the selected image.")
            setUiEnabled(true)
            updateDisplayView(MediaType.UNKNOWN)
            return
        }
        fragmentGalleryBinding.imageResult.setImageBitmap(bitmap)

        // Run inference off the UI thread. A fresh single-thread executor is used
        // per selection so the model is created and run on the same thread; it is
        // shut down once classification finishes.
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            val helper = ImageClassifierHelper(
                context = requireContext(),
                currentModel = viewModel.currentModel,
                currentDelegate = viewModel.currentDelegate,
                maxResults = viewModel.currentMaxResults,
                listener = this
            )
            helper.setup()
            val result = helper.classify(bitmap, rotationDegrees = 0)
            helper.close()
            executor.shutdown()

            activity?.runOnUiThread {
                if (_fragmentGalleryBinding == null) return@runOnUiThread
                setUiEnabled(true)
                if (result != null) {
                    classificationResultsAdapter.updateResults(result.categories)
                    classificationResultsAdapter.notifyDataSetChanged()
                    fragmentGalleryBinding.bottomSheetLayout.inferenceTimeVal.text =
                        String.format(Locale.US, "%d ms", result.inferenceTime)
                } else {
                    Log.e(TAG, "Error running image classification.")
                }
            }
        }
    }

    private fun decodeBitmap(uri: Uri): Bitmap? {
        return try {
            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(requireActivity().contentResolver, uri)
                )
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            }
            // Convert to a mutable software ARGB_8888 bitmap so getPixels() works
            // (ImageDecoder may return a hardware bitmap).
            decoded.copy(Bitmap.Config.ARGB_8888, true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode image: ${e.message}", e)
            null
        }
    }

    private fun updateDisplayView(mediaType: MediaType) {
        fragmentGalleryBinding.imageResult.visibility =
            if (mediaType == MediaType.IMAGE) View.VISIBLE else View.GONE
        fragmentGalleryBinding.tvPlaceholder.visibility =
            if (mediaType == MediaType.UNKNOWN) View.VISIBLE else View.GONE
    }

    // Check the type of media that user selected.
    private fun loadMediaType(uri: Uri): MediaType {
        val mimeType = context?.contentResolver?.getType(uri)
        if (mimeType?.startsWith("image") == true) return MediaType.IMAGE
        return MediaType.UNKNOWN
    }

    private fun setUiEnabled(enabled: Boolean) {
        fragmentGalleryBinding.fabGetContent.isEnabled = enabled
        fragmentGalleryBinding.bottomSheetLayout.spinnerModel.isEnabled = enabled
        fragmentGalleryBinding.bottomSheetLayout.maxResultsMinus.isEnabled = enabled
        fragmentGalleryBinding.bottomSheetLayout.maxResultsPlus.isEnabled = enabled
        fragmentGalleryBinding.bottomSheetLayout.spinnerDelegate.isEnabled = enabled
    }

    override fun onError(error: String, errorCode: Int) {
        activity?.runOnUiThread {
            if (_fragmentGalleryBinding == null) return@runOnUiThread
            setUiEnabled(true)
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            if (errorCode == ImageClassifierHelper.GPU_ERROR) {
                fragmentGalleryBinding.bottomSheetLayout.spinnerDelegate.setSelection(
                    ImageClassifierHelper.DELEGATE_CPU,
                    false
                )
            }
        }
    }

    companion object {
        private const val TAG = "GalleryFragment"
    }
}
