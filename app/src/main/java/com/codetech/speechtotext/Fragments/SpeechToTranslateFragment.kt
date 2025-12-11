package com.codetech.speechtotext.Fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.Helper.TranslationHelper
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentSpeechToTranslateBinding


class SpeechToTranslateFragment : Fragment() {
    private lateinit var binding: FragmentSpeechToTranslateBinding

    private var selectedSourceLanguage = "en-US"
    private var selectedTargetLanguage = "en-US"
    private var isKeyboardOpen = false
    private var originalConstraintSet: ConstraintSet? = null

    private var lastKeyboardHeight = 0

    companion object {
        const val REQUEST_CODE_SPEECH_INPUT = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSpeechToTranslateBinding.inflate(inflater, container, false)

        binding.iconNext.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            binding.iconNext.startAnimation(rotateAnimation)
            swapLanguageSelections()
        }

        binding.editTextIcon.setOnClickListener {
            findNavController().navigate(R.id.textToTranslateFragment)
        }

        binding.languageSelection1.setOnClickListener {
            showLanguageSelectionPopup()
        }

        binding.languageSelection2.setOnClickListener {
            showLanguageSelectionPopup2()
        }

        binding.mic.setOnClickListener {
            startSpeechRecognition()
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInitialViews()
        setupKeyboardListener()
    }

    private fun setupInitialViews() {
        binding.apply {

            val toolbar = (activity as AppCompatActivity).supportActionBar
            toolbar?.title = "Translate"
            toolbar?.setDisplayHomeAsUpEnabled(true)
            setHasOptionsMenu(true)

            val constraintLayout = root
            originalConstraintSet = ConstraintSet().apply {
                clone(constraintLayout)
            }
        }
    }

    private fun setupKeyboardListener() {
        val rootView = requireView().rootView
        val constraintLayout = binding.root
        val languageLayout = binding.languageLayout

        rootView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            private val rect = Rect()
            private var lastVisibleHeight = 0

            override fun onGlobalLayout() {
                rootView.getWindowVisibleDisplayFrame(rect)
                val visibleHeight = rect.bottom - rect.top

                if (visibleHeight == lastVisibleHeight) return
                lastVisibleHeight = visibleHeight

                val screenHeight = rootView.height
                val keyboardHeight = screenHeight - rect.bottom
                val isKeyboardNowOpen = keyboardHeight > screenHeight * 0.15

                handleKeyboardVisibilityChange(
                    isKeyboardNowOpen = isKeyboardNowOpen,
                    keyboardHeight = keyboardHeight,
                    constraintLayout = constraintLayout,
                    languageLayout = languageLayout
                )
            }
        })
    }

    private fun handleKeyboardVisibilityChange(
        isKeyboardNowOpen: Boolean,
        keyboardHeight: Int,
        constraintLayout: ConstraintLayout,
        languageLayout: View
    ) {
        if (isKeyboardNowOpen) {
            showLanguageLayoutWithKeyboard(
                keyboardHeight = keyboardHeight,
                constraintLayout = constraintLayout,
                languageLayout = languageLayout
            )
        } else {
            hideLanguageLayoutWithKeyboard(
                constraintLayout = constraintLayout,
                languageLayout = languageLayout
            )
        }

        isKeyboardOpen = isKeyboardNowOpen
        lastKeyboardHeight = keyboardHeight
    }

    private fun showLanguageLayoutWithKeyboard(
        keyboardHeight: Int,
        constraintLayout: ConstraintLayout,
        languageLayout: View
    ) {
        languageLayout.visibility = View.VISIBLE
        binding.mic.visibility = View.GONE

        ConstraintSet().apply {
            clone(constraintLayout)

            clear(R.id.languageLayout, ConstraintSet.BOTTOM)

            connect(
                R.id.languageLayout,
                ConstraintSet.BOTTOM,
                ConstraintSet.PARENT_ID,
                ConstraintSet.BOTTOM,
                keyboardHeight
            )

            val transition = AutoTransition().apply {
                duration = 200
            }
            TransitionManager.beginDelayedTransition(constraintLayout, transition)

            applyTo(constraintLayout)
        }
    }

    private fun hideLanguageLayoutWithKeyboard(
        constraintLayout: ConstraintLayout,
        languageLayout: View
    ) {
        binding.mic.visibility = View.VISIBLE
        languageLayout.visibility = View.VISIBLE

        val transition = AutoTransition().apply {
            duration = 200
        }
        TransitionManager.beginDelayedTransition(constraintLayout, transition)

        originalConstraintSet?.applyTo(constraintLayout)
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

    private fun startSpeechRecognition() {
        val currentSourceLanguage = SelectionOfCountry.getLanguageCode(binding.textView1.text.toString())

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentSourceLanguage)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text in ${binding.textView1.text}")
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!result.isNullOrEmpty()) {
                val spokenText = result[0]
                translateText(spokenText)
            }
        }
    }

    private fun translateText(sourceText: String) {
        if (sourceText.isNotEmpty()) {
            val currentSourceLanguage = SelectionOfCountry.getLanguageCode(binding.textView1.text.toString())
            val currentTargetLanguage = SelectionOfCountry.getLanguageCode(binding.textView2.text.toString())

            val translationHelper = TranslationHelper(requireActivity())
            translationHelper.setTranslationComplete(object : TranslationHelper.TranslationComplete {
                override fun translationCompleted(translation: String, language: String) {
                    binding.editTextView.text = Editable.Factory.getInstance().newEditable(translation)
                }
            })

            translationHelper.initTranslation(
                text = sourceText,
                outputCode = currentTargetLanguage,
                inputCode = currentSourceLanguage
            )
        } else {
            Toast.makeText(requireContext(), "Please speak something to translate", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        view?.rootView?.viewTreeObserver?.removeOnGlobalLayoutListener(null)
    }
}