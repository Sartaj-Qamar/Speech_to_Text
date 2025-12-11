package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.VM.SpeechToTextViewModel
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.FragmentSpeechToTextResponseBinding


class SpeechToTextResponseFragment : Fragment() {

    private lateinit var binding: FragmentSpeechToTextResponseBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TranslationAdapter
    private val translationList = mutableListOf<Translation>()
    private lateinit var viewModel: SpeechToTextViewModel
    private var spokenText: String? = null // Store spoken text as a class property

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSpeechToTextResponseBinding.inflate(inflater, container, false)

        spokenText = arguments?.getString("spokenText")

        recyclerView = binding.recyclerView
        adapter = TranslationAdapter(translationList, requireContext())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        viewModel = ViewModelProvider(this).get(SpeechToTextViewModel::class.java)

        observeViewModel()
        return binding.root
    }

    private fun observeViewModel() {
        viewModel.translatedTextLiveData.observe(viewLifecycleOwner) { translationString ->
            val newTranslation = Translation(
                sourceLang = "en",
                targetLang = "es",
                inputText = spokenText ?: "",
                resultText = translationString, // The translated text received from the ViewModel
                timestamp = System.currentTimeMillis()
            )

            // Update the list and notify the adapter
            translationList.add(newTranslation)
            adapter.notifyItemInserted(translationList.size - 1)
        }
    }
}
