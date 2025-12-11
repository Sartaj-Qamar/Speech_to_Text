package com.codetech.speechtotext.Fragments

import TranslationRepository
import TranslationViewModel
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Activity.MainActivity
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.Fragments.SpeechToTranslateFragment.Companion.REQUEST_CODE_SPEECH_INPUT
import com.codetech.speechtotext.Helper.SharedPrefHelper
import com.codetech.speechtotext.Helper.TranslationHelper
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.DrawView
import com.codetech.speechtotext.Utils.PermissionUtils
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.Utils.gone
import com.codetech.speechtotext.Utils.hideSystemUI
import com.codetech.speechtotext.Utils.isNetworkConnected
import com.codetech.speechtotext.Utils.manageBottomNavOnKeyboardState
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentTextToTranslateBinding
import com.codetech.speechtotext.models.StrokeManager
import com.codetech.speechtotext.models.TranslationData
import com.codetech.speechtotext.viewmodels.TranslationViewModelFactory


class TextToTranslateFragment : Fragment() {

    private var _binding: FragmentTextToTranslateBinding? = null
    private val binding get() = _binding!!
    private var selectedTargetLanguage = "zh-CN"
    private var selectedSourceLanguage = "en-US"

    //  private lateinit var languageLayout: LinearLayout
    private lateinit var translationAdapter: TranslationAdapter
    private val translationList = mutableListOf<Translation>()
    private lateinit var EditImageView: ImageView
    private lateinit var editText: EditText

    private lateinit var btnRecognize: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var btnRedo: ImageButton
    private lateinit var drawView: DrawView
    private lateinit var textView: EditText

    private var isFirstClick = true
    private var isEditIconVisible = true
    private var isEditImageViewClicked = false
    private val sharedPrefHelper: SharedPrefHelper by lazy { SharedPrefHelper(requireContext()) }
    private lateinit var viewModel: TranslationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTextToTranslateBinding.inflate(inflater, container, false)

        setupViewModel()
        setupRecyclerView()


//        setupRightIconClick()
        setupEditImageViewClick()
        // StrokeManager.updateRecognitionLanguage()


        btnRecognize = binding.next
        btnClear = binding.brushColorButton
        btnRedo = binding.redoButton
        drawView = binding.drawingArea
        textView = binding.editTextView



        btnRecognize.setOnClickListener {
            StrokeManager.recognize(textView, "")
        }



        btnClear.setOnClickListener {
            drawView.clear()
            StrokeManager.clear()
            textView.setText("")
        }

        binding.spaceButton.setOnClickListener {
            drawView.clear()
            StrokeManager.recognize(textView, selectedSourceLanguage)
        }

        btnRedo.setOnClickListener {
            drawView.undo()
        }


        EditImageView = binding.editTextIcon
        editText = binding.editTextView




        binding.languageSelection1.setOnClickListener { showLanguageSelectionPopup() }
        binding.languageSelection2.setOnClickListener { showLanguageSelectionPopup2() }
        binding.iconNext.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            binding.iconNext.startAnimation(rotateAnimation)
            swapLanguageSelections()
        }

        binding.translateButton.setOnClickListener {
//            binding.translateTextRecyclerView.visibility = View.GONE
            if (requireContext().isNetworkConnected()) {
                translateText()
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        setupEditText()

        binding.micIcon.setOnClickListener { checkMicrophonePermission() }

        autoTranslateIfTextPresent()
        // observeKeyboardVisibility(binding.root)

        return binding.root
    }

    private fun checkMicrophonePermission() {

        val micPermission = arrayOf(Manifest.permission.RECORD_AUDIO)
        PermissionUtils.checkPermission(
            requireContext(), permissionArray = micPermission,
            object : PermissionUtils.OnPermissionListener {
                override fun onPermissionSuccess() {
                    startSpeechRecognition()
                }
            }

        )
    }

    private fun observeKeyboardVisibility(rootView: View) {
        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect = android.graphics.Rect()
            rootView.getWindowVisibleDisplayFrame(rect)
            val screenHeight = rootView.rootView.height
            val keypadHeight = screenHeight - rect.bottom

            // Check if the keyboard is open (assuming keyboard height is > 100px)
            binding.micIcon.visibility = if (keypadHeight > 100) View.GONE else View.VISIBLE
        }
    }


    private fun setupRecyclerView() {
        translationAdapter = TranslationAdapter(translationList, requireContext())
        binding.translateTextRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = translationAdapter
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = TranslationRepository(database.translationDao())
        viewModel = ViewModelProvider(this, TranslationViewModelFactory(repository))
            .get(TranslationViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }


//    private fun setupRightIconClick() {
//        (activity as? MainActivity)?.setRightIconClickListener {
//            onRightIconClicked()
//        }
//    }


    private var hasClickedRightIcon = false // New variable to track if the right icon has been clicked

    private fun setupEditImageViewClick() {
        binding.editTextIcon.setOnClickListener {
            val bundle = Bundle().apply {
                putString(TextDrawTranslateFragment.ARG_SOURCE, TextDrawTranslateFragment.SOURCE_EDIT_ICON)
            }
            findNavController().navigate(R.id.textDrawTranslateFragment, bundle)

        }
    }

//    private fun onRightIconClicked() {
//        if (isEditImageViewClicked) {
//            if (isEditIconVisible) {
//                (activity as? MainActivity)?.updateRightIcon(R.drawable.edit_text_icon)
//                binding.drawLayout.visibility = View.GONE
//                binding.editTextView.isFocusableInTouchMode = true
//                binding.editTextView.isFocusable = true
//                binding.editTextView.isEnabled = true
//                binding.languageSelection1.visibility = View.VISIBLE
//                binding.languageSelection2.visibility = View.VISIBLE
//                binding.micIcon.visibility = View.VISIBLE
//
//                // Update constraints for mic icon visibility
//                val constraintSet = ConstraintSet()
//                constraintSet.clone(binding.root)
//                constraintSet.connect(
//                    R.id.language_layout,
//                    ConstraintSet.BOTTOM,
//                    R.id.micIcon,
//                    ConstraintSet.TOP
//                )
//                constraintSet.applyTo(binding.root)
//
//                // Show keyboard
////                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
////                imm?.showSoftInput(binding.editTextView, InputMethodManager.SHOW_IMPLICIT)
//
//            } else {
//                (activity as? MainActivity)?.updateRightIcon(R.drawable.ic_keyboard)
//                binding.drawLayout.visibility = View.VISIBLE
//                binding.translateButton.visibility = View.VISIBLE
//                binding.editTextView.isFocusable = false
//                binding.editTextView.isFocusableInTouchMode = false
//                binding.editTextView.isEnabled = false
//                binding.micIcon.visibility = View.GONE
//
//
//                // Update constraints for draw layout visibility
//                val constraintSet = ConstraintSet()
//                constraintSet.clone(binding.root)
//                constraintSet.connect(
//                    R.id.language_layout,
//                    ConstraintSet.BOTTOM,
//                    R.id.draw_layout,
//                    ConstraintSet.TOP
//                )
//                constraintSet.applyTo(binding.root)
//
//                // Hide keyboard
//                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
//                imm?.hideSoftInputFromWindow(binding.editTextView.windowToken, 0)
//
//            }
//            isEditIconVisible = !isEditIconVisible
//        }
//    }


    private fun autoTranslateIfTextPresent() {
        val initialText = binding.editTextView.text.toString()
        if (initialText.isNotEmpty() && initialText != "Type your Text in Here...") {
            binding.translateTextRecyclerView.visibility = View.GONE
            translateText()
        }
    }

    private fun translateText() {
        val sourceText = binding.editTextView.text.toString()
        if (sourceText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter some text to translate", Toast.LENGTH_SHORT).show()
            return
        }

        val translationHelper = TranslationHelper(requireActivity())
        translationHelper.setTranslationComplete(object : TranslationHelper.TranslationComplete {
            override fun translationCompleted(translation: String, language: String) {

                // Clear the list to ensure only one item exists
                translationList.clear()

                val translationData = Translation(
                    sourceLang = selectedSourceLanguage,
                    targetLang = selectedTargetLanguage,
                    inputText = sourceText,
                    resultText = translation,
                    timestamp = System.currentTimeMillis()
                )

                translationAdapter.addTranslation(translationData)

                // Notify the adapter about the updated list
                translationAdapter.notifyDataSetChanged()


                //sharedPrefHelper.saveTranslationToHistory(translationData)
                // viewModel.addTranslation(translationData)
                binding.editTextView.setText("")

                binding.translateTextRecyclerView.visibility = View.VISIBLE
                binding.editTextIcon.visibility = View.VISIBLE

            }


        })

        translationHelper.initTranslation(sourceText, selectedTargetLanguage, selectedSourceLanguage)
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
                binding.editTextView.text = Editable.Factory.getInstance().newEditable(result[0])
                translateText()
                binding.translateTextRecyclerView.visibility = View.VISIBLE
                binding.editTextView.visibility = View.GONE
                binding.editTextIcon.visibility = View.GONE
            }
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
//        hideSystemUI()

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView1.text = selectedCountryName
            selectedSourceLanguage = getLanguageCode(selectedCountryName)
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

        val popupWindow = PopupWindow(
            dialogBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            animationStyle = R.style.PopupWindowAnimationFromBottom
        }

        dialogBinding.root.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = dialogBinding.root.measuredHeight
        popupWindow.showAsDropDown(binding.languageSelection2, 0, -(binding.languageSelection2.height + popupHeight))

        hideSystemUI()

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView2.text = selectedCountryName
            selectedTargetLanguage = getLanguageCode(selectedCountryName)
            popupWindow.dismiss()
        }

    }


    private fun getLanguageCode(countryName: String): String {
        return when (countryName) {
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

    private fun copyToClipboard(text: String) {
        if (text.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Translate Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Text copied", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No text to copy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTranslateText() {
//        binding.translateText.visibility = View.GONE
        translationAdapter.clearTranslations() // Clear the adapter data
        binding.translateTextRecyclerView.visibility = View.GONE // Hide RecyclerView
        Toast.makeText(requireContext(), "Result field is cleared", Toast.LENGTH_SHORT).show()
    }

    private fun hideSystemUI() {
        requireActivity().window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                )
    }


    private fun setupEditText() {
        var isFirstClick = true

        binding.editTextView.setOnClickListener {
            binding.translateButton.visibility = View.VISIBLE
            val bundle = Bundle().apply {
                putString(TextDrawTranslateFragment.ARG_SOURCE, TextDrawTranslateFragment.SOURCE_EDIT_TEXT)
            }
            findNavController().navigate(R.id.textDrawTranslateFragment, bundle)
        }

        // Disable focusing behavior
        binding.editTextView.apply {
            isFocusable = false
            isFocusableInTouchMode = false
        }
    }

    override fun onResume() {
        super.onResume()
        //   languageLayout = requireActivity().findViewById(R.id.bottomMenu)


        //binding.root.manageBottomNavOnKeyboardState(requireContext(), languageLayout, binding.micIcon)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
