package com.codetech.speechtotext.Fragments


import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.canhub.cropper.CropImage.CancelledResult.bitmap
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.Helper.TranslationHelper
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.Constants.isFromOCRCamera
import com.codetech.speechtotext.Utils.OnBlockSelectedListener
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.Utils.SelectionOfCountry.languageCodeToCountryMap
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentScanOCRBinding
import com.codetech.speechtotext.models.TranslationData
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.codetech.speechtotext.Utils.TranslationOverlay

class ScanOCRFragment : Fragment(), OnBlockSelectedListener {

    private lateinit var binding: FragmentScanOCRBinding
    private var selectedTargetLanguage = "zh-CN"
    private var selectedSourceLanguage = "en-US"
    private lateinit var originalTextView: TextView
    private lateinit var translatedTextView: TextView
    private var selectedBlockTranslatedText: String? = null
    private var selectedBlockOriginalText: String? = null


    companion object {
        const val IMAGE_PATH = "image_path"
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        runTextRecognition()
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanOCRBinding.inflate(inflater, container, false)


        val imagePathFromUri = arguments?.getString("imageUri")
        selectedSourceLanguage = arguments?.getString("sourceLanguage").toString()
        selectedTargetLanguage = arguments?.getString("targetLanguage").toString()
        // Set the country names in the TextViews
        binding.textView1.text = languageCodeToCountryMap[selectedSourceLanguage] ?: "Unknown"
        binding.textView2.text = languageCodeToCountryMap[selectedTargetLanguage] ?: "Unknown"
        val imagePathFromExtra = arguments?.getString(IMAGE_PATH)
        originalTextView = binding.originalText
        translatedTextView = binding.translatedText

        val imagePathToUse = imagePathFromUri ?: imagePathFromExtra
        if (imagePathToUse != null) {
            binding.previewImageView.setImageURI(Uri.parse(imagePathToUse))
        } else {
            Log.e("ScanOCRFragment", "No image path provided")
            findNavController().popBackStack()
        }

        binding.translationOverlay.setOnBlockSelectedListener(this)

        binding.detect.setOnClickListener {
            if (selectedBlockOriginalText == null || selectedBlockOriginalText == "") {
                Toast.makeText(requireContext(), "Tap on a text block to move forward.", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle().apply {
                    putString("recognizedText", selectedBlockOriginalText)
                    putString("translatedText", selectedBlockTranslatedText)
                    putString("sourceLanguage", selectedSourceLanguage)
                    putString("targetLanguage", selectedTargetLanguage)
                }
                isFromOCRCamera = true
                findNavController().navigate(
                    R.id.action_scanOCRFragment_to_OCRToTextTranslateFragment,
                    bundle
                )
            }

        }


        binding.languageSelection1.setOnClickListener {
            showLanguageSelectionPopup()
        }

        binding.iconNext.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            binding.iconNext.startAnimation(rotateAnimation)
            swapLanguageSelections()
        }

        binding.languageSelection2.setOnClickListener {
            showLanguageSelectionPopup2()
        }

        binding.originalText.setOnClickListener {
            setSelectedOption(binding.originalText)
        }

        binding.translatedText.setOnClickListener {
            setSelectedOption(binding.translatedText)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runTextRecognition()
    }

    private fun setSelectedOption(selectedView: TextView) {
        if (selectedView == originalTextView) {
            originalTextView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected)
            translatedTextView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected)
            originalTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            translatedTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.translationOverlay.visibility = View.INVISIBLE
        } else {
            translatedTextView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_selected)
            originalTextView.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_unselected)
            translatedTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            originalTextView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            binding.translationOverlay.visibility = View.VISIBLE
        }
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
                "English" -> "en-US"
                "Hindi" -> "hi-IN"
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
                "Hindi" -> "hi-IN"
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
            runTextRecognition()

            popupWindow.dismiss()
        }
    }

    private fun swapLanguageSelections() {
        val language1 = binding.textView1.text.toString()
        val language2 = binding.textView2.text.toString()
        val tempLanguageCode = selectedSourceLanguage

        binding.textView1.text = language2
        binding.textView2.text = language1
        selectedSourceLanguage = selectedTargetLanguage
        selectedTargetLanguage = tempLanguageCode
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.loader.visibility = View.VISIBLE
            binding.loader.playAnimation()
            binding.previewImageView.alpha = 0.5f
        } else {
            binding.loader.visibility = View.GONE
            binding.loader.cancelAnimation()
            binding.previewImageView.alpha = 1.0f
        }
    }

    private fun runTextRecognition() {
        setLoading(true)
        val bitmap = (binding.previewImageView.drawable as BitmapDrawable).bitmap
        val rotationDegree = 0

        val image = InputImage.fromBitmap(bitmap, rotationDegree)
        val options = TextRecognizerOptions.Builder()
            .build()

        val recognizer: TextRecognizer = TextRecognition.getClient(options)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                processTextRecognition(visionText)
            }
            .addOnFailureListener {
                setLoading(false)
                Toast.makeText(requireContext(), "Unsuccessful Operation", Toast.LENGTH_SHORT).show()
            }
    }


    private fun processTextRecognition(visionText: Text) {
        val blocks = visionText.textBlocks
        if (blocks.isEmpty()) {
            setLoading(false)
            Toast.makeText(requireContext(), "Text Could Not Be Detected in the Image", Toast.LENGTH_LONG).show()
            return
        }

        // Get the ImageView and bitmap dimensions
        val imageView = binding.previewImageView
        val bitmap = (imageView.drawable as? BitmapDrawable)?.bitmap

        if (bitmap == null) {
            Toast.makeText(requireContext(), "Error: Image not properly loaded", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate scaling factors considering aspect ratio
        val viewWidth = imageView.width.toFloat()
        val viewHeight = imageView.height.toFloat()
        val bitmapWidth = bitmap.width.toFloat()
        val bitmapHeight = bitmap.height.toFloat()

        // Calculate the actual dimensions of the image within the ImageView
        val imageViewRatio = viewWidth / viewHeight
        val bitmapRatio = bitmapWidth / bitmapHeight

        // Calculate scaling and offset values
        val (scaledWidth, scaledHeight) = if (imageViewRatio > bitmapRatio) {
            // Image is vertically constrained
            val newWidth = viewHeight * bitmapRatio
            Pair(newWidth, viewHeight)
        } else {
            // Image is horizontally constrained
            val newHeight = viewWidth / bitmapRatio
            Pair(viewWidth, newHeight)
        }

        val offsetX = (viewWidth - scaledWidth) / 2
        val offsetY = (viewHeight - scaledHeight) / 2

        // Create a map to store scaled text blocks
        val textBlocksWithCoords = mutableMapOf<Rect, String>()

        blocks.forEach { block ->
            block.boundingBox?.let { originalRect ->
                // Scale coordinates considering the actual image placement
                val scaledRect = Rect(
                    (originalRect.left * scaledWidth / bitmapWidth + offsetX).toInt(),
                    (originalRect.top * scaledHeight / bitmapHeight + offsetY).toInt(),
                    (originalRect.right * scaledWidth / bitmapWidth + offsetX).toInt(),
                    (originalRect.bottom * scaledHeight / bitmapHeight + offsetY).toInt()
                )
                textBlocksWithCoords[scaledRect] = block.text
            }
        }

        // Translate text blocks
        val translationHelper = TranslationHelper(requireActivity())
        translationHelper.setTranslationComplete(object : TranslationHelper.TranslationComplete {
            override fun translationCompleted(translation: String, language: String) {
                val translations = translation.split("\n")
                val translatedBlocks = mutableMapOf<Rect, String>()

                textBlocksWithCoords.entries.forEachIndexed { index, entry ->
                    if (index < translations.size) {
                        translatedBlocks[entry.key] = translations[index]
                    }
                }

                binding.translationOverlay.setTranslatedBlocks(textBlocksWithCoords, translatedBlocks)
                setLoading(false)
            }
        })

        // Combine all text blocks for translation
        val fullText = blocks.joinToString("\n") { it.text }
        translationHelper.initTranslation(
            text = fullText,
            outputCode = selectedTargetLanguage,
            inputCode = selectedSourceLanguage
        )
    }

    override fun onBlockSelected(originalText: String, translatedText: String) {
        selectedBlockOriginalText = originalText
        selectedBlockTranslatedText = translatedText
    }

}