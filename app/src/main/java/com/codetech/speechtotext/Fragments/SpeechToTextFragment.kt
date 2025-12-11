package com.codetech.speechtotext.Fragments

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.LanguageSelectionAdapter
import com.codetech.speechtotext.Dialog.showExitAppDialog
import com.codetech.speechtotext.Helper.SharedPrefHelper
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.PermissionUtils
import com.codetech.speechtotext.Utils.SelectionOfCountry
import com.codetech.speechtotext.application.BaseFragment
import com.codetech.speechtotext.databinding.DialogLanguageSelectionBinding
import com.codetech.speechtotext.databinding.FragmentSpeechToTextBinding

class SpeechToTextFragment : BaseFragment<FragmentSpeechToTextBinding>(FragmentSpeechToTextBinding::inflate) {

    override lateinit var binding: FragmentSpeechToTextBinding
    private lateinit var speechRecognizer: SpeechRecognizer
    private var selectedLanguage = "en-US"
    private var REQUEST_MICROPHONE_PERMISSION = 102
    private val REQUEST_CODE_SPEECH_INPUT = 1

    private val sharedPrefHelper: SharedPrefHelper by lazy { SharedPrefHelper(requireContext()) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                showExitAppDialog(requireActivity())
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        binding = FragmentSpeechToTextBinding.inflate(inflater, container, false)


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())

        binding.countryLanguageSelection.setOnClickListener {
            showLanguageSelectionPopup()
        }

        binding.copyText.setOnClickListener {
            copyToClipboard(binding.micText.text.toString())
        }

        binding.deleteText.setOnClickListener {
            deleteMicText(binding.micText.text.toString())
        }


        binding.mic.setOnClickListener {
            checkMicrophonePermission()
        }


        return binding.root

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
        popupWindow.animationStyle = R.style.PopupWindowAnimationFromTop
        popupWindow.showAsDropDown(binding.countryLanguageSelection, 0, -binding.countryLanguageSelection.height)

        adapter.setOnItemClickListener { selectedCountryName ->
            binding.textView1.text = selectedCountryName

            val selectedCountry = SelectionOfCountry.countries.find { it.name == selectedCountryName }
            selectedLanguage = when (selectedCountry?.name) {
                "USA" -> "en-US"       // English (United States)
                "Canada" -> "fr-CA"    // French (Canada)
                "Mexico" -> "es-MX"    // Spanish (Mexico)
                "France" -> "fr-FR"    // French (France)
                "Germany" -> "de-DE"   // German (Germany)
                "Spain" -> "es-ES"     // Spanish (Spain)
                "Italy" -> "it-IT"     // Italian (Italy)
                "India" -> "hi-IN"     // Hindi (India)
                "China" -> "zh-CN"     // Chinese (China)
                "Japan" -> "ja-JP"     // Japanese (Japan)
                "Russia" -> "ru-RU"    // Russian (Russia)
                "Brazil" -> "pt-BR"    // Portuguese (Brazil)
                "Argentina" -> "es-AR" // Spanish (Argentina)
                "Australia" -> "en-AU" // English (Australia)
                "South Africa" -> "af-ZA" // Afrikaans (South Africa)
                "Saudi Arabia" -> "ar-SA" // Arabic (Saudi Arabia)
                "United Kingdom" -> "en-GB" // English (United Kingdom)
                "South Korea" -> "ko-KR"   // Korean (South Korea)
                "Egypt" -> "ar-EG"         // Arabic (Egypt)
                "Turkey" -> "tr-TR"        // Turkish (Turkey)
                else -> "en-US"            // Default to English (United States)
            }

//            selectedCountry?.let {
//                binding.imageCountry.setImageResource(it.imageResId)
//            }

            popupWindow.dismiss()
        }
    }


    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage)

        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text  ${binding.textView1.text}")

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
                binding.micText.text = result[0]
            }
        }
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


    private fun copyToClipboard(text: String) {
        if (text.isNotEmpty()) {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Mic Text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "Text copied ", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No text to copy", Toast.LENGTH_SHORT).show()
        }
    }


    private fun deleteMicText(text: String) {

        if (text.isEmpty()) {
            Toast.makeText(requireContext(), "No text found", Toast.LENGTH_SHORT).show()
        } else {
            binding.micText.text = ""
            Toast.makeText(requireContext(), "Text deleted", Toast.LENGTH_SHORT).show()
        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}
