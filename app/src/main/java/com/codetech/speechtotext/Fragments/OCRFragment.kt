package com.codetech.speechtotext.Fragments

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraAccessException


import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.PermissionUtils
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentOCRBinding
import java.io.File
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import com.codetech.speechtotext.Helper.TranslationHelper
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OCRFragment : Fragment() {
    private lateinit var binding: FragmentOCRBinding
    private var imageCapture: ImageCapture? = null
    private var selectedTargetLanguage = "zh-CN"
    private var selectedSourceLanguage = "en-US"
    private var cameraControl: CameraControl? = null
    private var cameraInfo: CameraInfo? = null
    private var isFlashOn: Boolean = false


//    private val activityResultLauncher =
//        registerForActivityResult(
//            ActivityResultContracts.RequestMultiplePermissions()
//        )
//        { permissions ->
//            var permissionGranted = true
//            permissions.entries.forEach {
//                if (it.key in REQUIRED_PERMISSIONS && !it.value)
//                    permissionGranted = false
//            }
//            if (!permissionGranted) {
//                Toast.makeText(
//                    requireContext(),
//                    "Permission request denied",
//                    Toast.LENGTH_SHORT
//                ).show()
//            } else {
//                startCamera()
//            }
//        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        checkCameraPermission()

//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            requestPermissions()
//        }

        binding = FragmentOCRBinding.inflate(inflater, container, false)

        binding.takeImage.setOnClickListener {
            takePhoto()
        }

        binding.picGallery.setOnClickListener {
            checkStoragePermissionAndOpenGallery()
        }

        binding.languageSelection1.setOnClickListener {
            showLanguageSelectionPopup()
        }
        binding.languageSelection2.setOnClickListener {
            showLanguageSelectionPopup2()
        }

        binding.iconNext.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            binding.iconNext.startAnimation(rotateAnimation)
            swapLanguageSelections()
        }
        binding.torch.setOnClickListener {
            toggleFlashlight()
        }

        return binding.root
    }


    private fun checkCameraPermission() {

        val cameraPermission = arrayOf(Manifest.permission.CAMERA)
        PermissionUtils.checkPermission(
            requireContext(), permissionArray = cameraPermission,
            object : PermissionUtils.OnPermissionListener {
                override fun onPermissionSuccess() {
                    startCamera()
                }
            }

        )
    }


    private fun checkStoragePermissionAndOpenGallery() {
        val storagePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        PermissionUtils.checkPermission(
            context = requireContext(),
            permissionArray = storagePermissions,
            permissionListener = object : PermissionUtils.OnPermissionListener {
                override fun onPermissionSuccess() {

                    val bundle = Bundle().apply {
                        putString("sourceLanguage", selectedSourceLanguage)
                        putString("targetLanguage", selectedTargetLanguage)
                    }
                    findNavController().navigate(R.id.photoFragment, bundle)
                }
            }
        )
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(getOutputDirectory(), "translateocrapp_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    // Create bitmap from saved image
//                    val bitmap = MediaStore.Images.Media.getBitmap(
//                        requireContext().contentResolver,
//                        savedUri
//                    )
                    val bundle = Bundle().apply {
                        putString("imageUri", savedUri.toString())
                        putString("sourceLanguage", selectedSourceLanguage)
                        putString("targetLanguage", selectedTargetLanguage)
                    }
                    findNavController().navigate(R.id.scanOCRFragment, bundle)
                    //processImageWithTextRecognition(bitmap, savedUri)
                }
            }
        )
    }

    private fun processImageWithTextRecognition(bitmap: Bitmap, imageUri: Uri) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val textBlocks = mutableMapOf<Rect, String>()

                // Collect all text blocks with their positions
                for (block in visionText.textBlocks) {
                    textBlocks[block.boundingBox!!] = block.text
                }

                val bundle = Bundle().apply {
                    putString("imageUri", imageUri.toString())
                    putString("sourceLanguage", selectedSourceLanguage)
                    putString("targetLanguage", selectedTargetLanguage)
                    putSerializable("textBlocks", HashMap(textBlocks))
                }
                findNavController().navigate(R.id.scanOCRFragment, bundle)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Text recognition failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun getOutputDirectory(): File {
        val mediaDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "YourAppName").apply { mkdirs() }
        return if (mediaDir.exists()) mediaDir else requireContext().filesDir
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                // Store camera control and info
                cameraControl = camera.cameraControl
                cameraInfo = camera.cameraInfo

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    private fun showLanguageSelectionPopup() {
        val dialogBinding = DialogLanguageSelectionBinding.inflate(layoutInflater)
        val recyclerView = dialogBinding.languageRecyclerView

        val adapter = LanguageSelectionAdapter(requireContext(), SelectionOfCountry.countries).apply {
            setSelectedLanguage(binding.textView1.text.toString())
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val popupWindow = PopupWindow(dialogBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.animationStyle = R.style.PopupWindowAnimationFromBottom

        dialogBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = dialogBinding.root.measuredHeight

        popupWindow.showAsDropDown(binding.languageSelection1, 0, -(binding.languageSelection1.height + popupHeight))

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView1.text = selectedCountryName

            selectedSourceLanguage = when (selectedCountryName) {
                "USA" -> "en-US"
                "Hindi" -> "in-IND"
                "Canada" -> "fr-CA"
                "Mexico" -> "es-MX"
                "Urdu" -> "ur-PK"
                "France" -> "fr-FR"
                "Germany" -> "de-DE"
                "Spain" -> "es-ES"
                "Italy" -> "it-IT"
                "India" -> "hi-IN"
                "China" -> "zh-CN"
                "Japan" -> "ja-JP"
                "Russia" -> "ru-RU"
                "Brazil" -> "pt-BR"
                "Argentina" -> "es-AR"
                "Australia" -> "en-AU"
                "South Africa" -> "en-ZA"
                "Saudi Arabia" -> "ar-SA"
                "United Kingdom" -> "en-GB"
                "South Korea" -> "ko-KR"
                "Egypt" -> "ar-EG"
                "Turkey" -> "tr-TR"
                else -> "en-US"
            }

            popupWindow.dismiss()
        }
    }

    private fun showLanguageSelectionPopup2() {
        val dialogBinding = DialogLanguageSelectionBinding.inflate(layoutInflater)
        val recyclerView = dialogBinding.languageRecyclerView

        val adapter = LanguageSelectionAdapter(requireContext(), SelectionOfCountry.countries).apply {
            setSelectedLanguage(binding.textView2.text.toString())
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val popupWindow = PopupWindow(dialogBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        popupWindow.animationStyle = R.style.PopupWindowAnimationFromBottom

        dialogBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = dialogBinding.root.measuredHeight

        popupWindow.showAsDropDown(binding.languageSelection2, 0, -(binding.languageSelection2.height + popupHeight))

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView2.text = selectedCountryName

            selectedTargetLanguage = when (selectedCountryName) {
                "USA" -> "en-US"
                "Hindi" -> "in-IND"
                "Canada" -> "fr-CA"
                "Mexico" -> "es-MX"
                "Urdu" -> "ur-PK"
                "France" -> "fr-FR"
                "Germany" -> "de-DE"
                "Spain" -> "es-ES"
                "Italy" -> "it-IT"
                "India" -> "hi-IN"
                "China" -> "zh-CN"
                "Japan" -> "ja-JP"
                "Russia" -> "ru-RU"
                "Brazil" -> "pt-BR"
                "Argentina" -> "es-AR"
                "Australia" -> "en-AU"
                "South Africa" -> "en-ZA"
                "Saudi Arabia" -> "ar-SA"
                "United Kingdom" -> "en-GB"
                "South Korea" -> "ko-KR"
                "Egypt" -> "ar-EG"
                "Turkey" -> "tr-TR"
                else -> "en-US"
            }

            popupWindow.dismiss()
        }
    }

    private fun swapLanguageSelections() {
        val language1 = binding.textView1.text.toString()
        val selectedLanguage1 = selectedSourceLanguage

        val language2 = binding.textView2.text.toString()
        val selectedLanguage2 = selectedSourceLanguage

        binding.textView1.text = language2
        selectedSourceLanguage = selectedLanguage2

        binding.textView2.text = language1
        selectedSourceLanguage = selectedLanguage1
    }

    private fun toggleTorch() {
        if (cameraInfo?.torchState?.value == TorchState.ON) {
            cameraControl?.enableTorch(false)
            binding.torch.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_light
                )
            )
        } else {
            cameraControl?.enableTorch(true)
            binding.torch.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_light
                )
            )
        }
    }


    private fun toggleFlashlight() {
        if (cameraInfo?.torchState?.value == TorchState.ON) {
            cameraControl?.enableTorch(false)
            binding.torch.setImageResource(R.drawable.ic_flash_off)
            binding.flashText.setText(R.string.flash_off)
        } else {
            cameraControl?.enableTorch(true)
            binding.torch.setImageResource(R.drawable.ic_flash_on)
            binding.flashText.setText(R.string.flash_on)
        }
    }

    private fun processTextRecognition(visionText: Text) {
        val blocks = visionText.textBlocks
        if (blocks.isEmpty()) {
            Toast.makeText(requireContext(), "No text detected", Toast.LENGTH_SHORT).show()
            return
        }

        val translationHelper = TranslationHelper(requireActivity())
        translationHelper.setTranslationComplete(object : TranslationHelper.TranslationComplete {
            override fun translationCompleted(translation: String, language: String) {
                // Create a map of original text blocks to translated text
                val translatedBlocks = mutableMapOf<Rect, String>()

                // Split the translation into parts matching the original blocks
                val translations = translation.split("\n")
                blocks.forEachIndexed { index, block ->
                    if (index < translations.size) {
                        translatedBlocks[block.boundingBox!!] = translations[index]
                    }
                }

                // Update the overlay with translated text using the binding
                // binding.translationOverlay.setTranslatedBlocks(translatedBlocks)
            }
        })

        // Combine all text blocks for translation
        val fullText = blocks.joinToString("\n") { it.text }
        translationHelper.initTranslation(fullText, selectedTargetLanguage, selectedSourceLanguage)
    }
}


