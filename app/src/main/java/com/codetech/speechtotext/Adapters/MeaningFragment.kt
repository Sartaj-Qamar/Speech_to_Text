package com.codetech.speechtotext.Adapters

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.codetech.speechtotext.databinding.MeaningRecyclerRowBinding
import com.codetech.speechtotext.models.Meaning
import java.util.Locale

class MeaningFragment : Fragment() {

    private var _binding: MeaningRecyclerRowBinding? = null
    private val binding get() = _binding!!

    private lateinit var textToSpeech: TextToSpeech

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MeaningRecyclerRowBinding.inflate(inflater, container, false)

        textToSpeech = TextToSpeech(requireContext()) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
            }
        }

        binding.volume.setOnClickListener {
            readTextAloud()
        }

        val meaning: Meaning? = arguments?.getParcelable(ARG_MEANING)
        if (meaning != null) {
            binding.partOfSpeechTextview.text = meaning.partOfSpeech.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
            binding.definitionsTextview.text = meaning.definitions.joinToString("\n\n") {
                it.definition
            }

            if (meaning.synonyms.isNotEmpty()) {
                binding.synonymsTitleTextview.visibility = View.VISIBLE
                binding.synonymsTextview.visibility = View.VISIBLE
                binding.synonymsTextview.text = meaning.synonyms.joinToString(", ")
            } else {
                binding.synonymsTitleTextview.visibility = View.GONE
                binding.synonymsTextview.visibility = View.GONE
            }

            if (meaning.antonyms.isNotEmpty()) {
                binding.antonymsTitleTextview.visibility = View.VISIBLE
                binding.antonymsTextview.visibility = View.VISIBLE
                binding.antonymsTextview.text = meaning.antonyms.joinToString(", ")
            } else {
                binding.antonymsTitleTextview.visibility = View.GONE
                binding.antonymsTextview.visibility = View.GONE
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun readTextAloud() {
        val definitionsText = binding.definitionsTextview.text.toString()
        val synonymsText = binding.synonymsTextview.text.toString()

        val textToRead = "$definitionsText. $synonymsText."

        if (textToRead.isNotEmpty()) {
            textToSpeech.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    companion object {
        private const val ARG_MEANING = "arg_meaning"

        fun newInstance(meaning: Meaning): MeaningFragment {
            val fragment = MeaningFragment()
            val args = Bundle().apply {
                putParcelable(ARG_MEANING, meaning)
            }
            fragment.arguments = args
            return fragment
        }
    }
    private fun pauseSpeech() {
        textToSpeech.stop()
    }

    override fun onPause() {
        super.onPause()
        pauseSpeech()
    }
}
