package com.codetech.speechtotext.Fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.Helper.TranslationHelper
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.DrawView
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.Utils.SelectionOfCountry.getLanguageCode
import com.codetech.speechtotext.Utils.isNetworkConnected
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentTextDrawTranslateBinding
import com.codetech.speechtotext.models.StrokeManager


class TextDrawTranslateFragment : Fragment() {

    private lateinit var binding: FragmentTextDrawTranslateBinding
    private lateinit var translationAdapter: TranslationAdapter
    private lateinit var btnRecognize: ImageButton
    private lateinit var btnClear: ImageButton
    private lateinit var undoBtn: ImageButton
    private lateinit var drawView: DrawView
    private lateinit var editText: EditText
    private lateinit var btnSpace: ImageButton
    private lateinit var loader: LottieAnimationView


    var translationList = mutableListOf<Translation>()
    private var source: String? = null
    private var isEditTextIcon = false // Track current icon state
    private var selectedTargetLanguage = "zh-CN"
    private var selectedSourceLanguage = "en-US"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        source = arguments?.getString(ARG_SOURCE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTextDrawTranslateBinding.inflate(inflater, container, false)
        btnRecognize = binding.next
        btnClear = binding.clearText
        undoBtn = binding.undoButton
        drawView = binding.drawingArea
        btnSpace = binding.spaceButton
        loader = binding.loader

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        setupRecyclerView()


        // Connect DrawView with EditText
        drawView.setEditText(binding.editTextView)

        // Add text change listener to track cursor position
        binding.editTextView.setOnSelectionChangedListener { selStart, selEnd ->
            drawView.setEditText(binding.editTextView)
        }
    }

    override fun onResume() {
        super.onResume()

        binding.editTextView.clearFocus()

        if (binding.drawLayout.visibility == View.VISIBLE) {
            // Hide the keyboard
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.editTextView.windowToken, 0)

            // Ensure the EditText does not show the keyboard on focus
            binding.editTextView.showSoftInputOnFocus = false
        }

    }
    //

    private fun setupUI() {
        when (source) {
            SOURCE_EDIT_TEXT -> {
                // Configure UI for edit text source
                binding.customToolbar.rightIcon.setImageResource(R.drawable.edit_text_icon)
                isEditTextIcon = true

                // Set focus to the EditText and show the keyboard
                binding.editTextView.requestFocus()
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(binding.editTextView, InputMethodManager.SHOW_IMPLICIT)
            }

            SOURCE_EDIT_ICON -> {
                // Configure UI for edit icon source
                binding.customToolbar.rightIcon.setImageResource(R.drawable.ic_keyboard)
                binding.drawLayout.visibility = View.VISIBLE
                // Set focus to the EditText
                binding.editTextView.requestFocus()
                // Disable keyboard input and allow only cursor placement
                binding.editTextView.showSoftInputOnFocus = false

            }
        }
    }

    private fun setupClickListeners() {
        binding.customToolbar.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.customToolbar.rightIcon.setOnClickListener {
            changeToolbarRightIcon()
        }

        binding.translateButton.setOnClickListener {
            if (requireContext().isNetworkConnected()) {
                translateText()
            } else {
                Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            }
        }

        binding.languageSelection1.setOnClickListener { showLanguageSelectionPopup() }
        binding.languageSelection2.setOnClickListener { showLanguageSelectionPopup2() }
        binding.iconNext.setOnClickListener {
            val rotateAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_animation)
            binding.iconNext.startAnimation(rotateAnimation)
            swapLanguageSelections()
        }

        btnRecognize.setOnClickListener {
            drawView.moveDrawingsToLeft()
            StrokeManager.recognize(binding.editTextView, selectedSourceLanguage)
        }


        btnClear.setOnClickListener {
            drawView.clear()
            deleteChar()
        }

        btnSpace.setOnClickListener {
            drawView.clear()
            insertSpace()
        }

        undoBtn.setOnClickListener {
            drawView.undo()
        }
    }

    private fun changeToolbarRightIcon() {
        if (isEditTextIcon) {
            // Hide keyboard
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.editTextView.windowToken, 0)

            binding.customToolbar.rightIcon.setImageResource(R.drawable.ic_keyboard)
            binding.drawLayout.visibility = View.VISIBLE

            // Disable keyboard input and allow only cursor placement
            binding.editTextView.showSoftInputOnFocus = false

        } else {
            binding.customToolbar.rightIcon.setImageResource(R.drawable.edit_text_icon)
            binding.drawLayout.visibility = View.GONE

            // Allow full keyboard input
            binding.editTextView.showSoftInputOnFocus = true

            // Set focus to the EditText and show the keyboard
            binding.editTextView.requestFocus()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showSoftInput(binding.editTextView, InputMethodManager.SHOW_IMPLICIT)
        }
        // Toggle the state
        isEditTextIcon = !isEditTextIcon
    }

    private fun setupRecyclerView() {
        translationAdapter = TranslationAdapter(translationList, requireContext())
        binding.translateTextRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = translationAdapter
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

        StrokeManager.downloadLanguage(selectedSourceLanguage, loader)
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
        //  hideSystemUI()

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView1.text = selectedCountryName
            selectedSourceLanguage = getLanguageCode(selectedCountryName)
            StrokeManager.downloadLanguage(selectedSourceLanguage, loader)
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

        //  hideSystemUI()

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView2.text = selectedCountryName
            selectedTargetLanguage = getLanguageCode(selectedCountryName)
            popupWindow.dismiss()
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

                // Create the translation object
                val translationData = Translation(
                    sourceLang = selectedSourceLanguage,
                    targetLang = selectedTargetLanguage,
                    inputText = sourceText,
                    resultText = translation,
                    timestamp = System.currentTimeMillis()
                )

                // Add the new translation to the list
                translationAdapter.addTranslation(translationData)

                // Notify the adapter about the updated list
                translationAdapter.notifyDataSetChanged()

                // Ensure the RecyclerView is visible
                binding.translateTextRecyclerView.visibility = View.VISIBLE
            }
        })

        translationHelper.initTranslation(sourceText, selectedTargetLanguage, selectedSourceLanguage)
    }


    private fun insertSpace() {
        // Get the current text from the EditText
        val currentText = binding.editTextView.text

        // Get the current cursor position
        val cursorPosition = binding.editTextView.selectionStart

        // Insert a space at the current cursor position
        currentText?.insert(cursorPosition, " ")

        // Set the modified text back to the EditText
        binding.editTextView.setText(currentText)

        // Move the cursor to the position after the inserted space
        binding.editTextView.setSelection(cursorPosition + 1)
    }

    private fun deleteChar() {
        // Get the current text from the EditText
        val currentText = binding.editTextView.text

        // Get the current cursor position
        val cursorPosition = binding.editTextView.selectionStart

        // Check if there's a character before the cursor to remove
        if (cursorPosition > 0) {
            // Remove the character before the cursor
            currentText?.delete(cursorPosition - 1, cursorPosition)

            // Update the EditText text
            binding.editTextView.setText(currentText)

            // Move the cursor to the position after the deletion
            binding.editTextView.setSelection(cursorPosition - 1)
        }
    }


    companion object {
        const val ARG_SOURCE = "source"
        const val SOURCE_EDIT_TEXT = "edit_text"
        const val SOURCE_EDIT_ICON = "edit_icon"
    }

}

// Add this extension function
private fun EditText.setOnSelectionChangedListener(listener: (Int, Int) -> Unit) {
    setOnClickListener {
        listener(selectionStart, selectionEnd)
    }

    addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
            listener(selectionStart, selectionEnd)
        }
    })
}