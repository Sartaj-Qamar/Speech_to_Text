package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.Utils.Constants.isFromOCRCamera
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.FragmentOCRToTextTranslateBinding

var translationList = mutableListOf<Translation>()

class OCRToTextTranslateFragment : Fragment() {
    private lateinit var binding: FragmentOCRToTextTranslateBinding
    private lateinit var translationAdapter: TranslationAdapter

    private val database by lazy { AppDatabase.getDatabase(requireContext()) }
    private val translationDao by lazy { database.translationDao() }

    private var recognizedText: String? = null
    private var translatedText: String? = null
    private var selectedSourceLanguage: String? = null
    private var selectedTargetLanguage: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOCRToTextTranslateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        recognizedText = arguments?.getString("recognizedText") ?: ""
        translatedText = arguments?.getString("translatedText") ?: ""
        selectedSourceLanguage = arguments?.getString("sourceLanguage") ?: "en-US"
        selectedTargetLanguage = arguments?.getString("targetLanguage") ?: "zh-CN"

        if (recognizedText!!.isNotEmpty() && isFromOCRCamera) {
            Log.e("loggee", "inside iffffff ")
            Log.e("loggee11", "onViewCreated: ${recognizedText}")
            translateText(recognizedText!!, translatedText!!, selectedSourceLanguage, selectedTargetLanguage)
        }
    }

    private fun setupRecyclerView() {
        binding.translateTextRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        translationAdapter = TranslationAdapter(translationList, requireContext())
        binding.translateTextRecyclerView.adapter = translationAdapter
    }

    private fun translateText(recognizeText: String, translatedText: String, sourceLanguage: String?, targetLanguage: String?) {
        if (recognizeText.isEmpty()) {
            return
        }
        translationList.clear()

        val translationData = Translation(
            sourceLang = sourceLanguage,
            targetLang = targetLanguage,
            inputText = recognizeText,
            resultText = translatedText,
            timestamp = System.currentTimeMillis()
        )
        isFromOCRCamera = false


        //Add new translation in the list
        translationAdapter.addTranslation(translationData)
        translationAdapter.notifyDataSetChanged()

    }

//    private fun translateText(recognizeText: String, sourceLanguage: String?, targetLanguage: String?) {
//        if (recognizeText.isEmpty()) {
//            Toast.makeText(requireContext(), "Please enter some text to translate", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        val translationHelper = TranslationHelper(requireActivity())
//        translationHelper.setTranslationComplete(object : TranslationHelper.TranslationComplete {
//            override fun translationCompleted(translation: String, language: String) {
//
//                // Clear the list to ensure only one item exists
//                translationList.clear()
//
//                val translationData = Translation(
//                    sourceLang = sourceLanguage,
//                    targetLang = targetLanguage,
//                    inputText = recognizeText,
//                    resultText = translation,
//                    timestamp = System.currentTimeMillis()
//                )
//                isFromOCRCamera = false
//
//                // Add the new translation to the list
//                translationAdapter.addTranslation(translationData)
//
//                translationAdapter.notifyDataSetChanged()
//
//                //  translationAdapter.notifyItemInserted(translationList.size - 1)
//                binding.translateTextRecyclerView.visibility = View.VISIBLE
//
//            }
//        })
//
//        translationHelper.initTranslation(recognizeText, targetLanguage!!, sourceLanguage!!)
//    }


}

