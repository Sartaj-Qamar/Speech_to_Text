package com.codetech.speechtotext.Fragments

import TranslationRepository
import TranslationViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.databinding.FragmentHistoryBinding
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.viewmodels.TranslationViewModelFactory

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var translationAdapter: TranslationAdapter
    private lateinit var viewModel: TranslationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeTranslations()

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewModel() {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = TranslationRepository(database.translationDao())
        viewModel = ViewModelProvider(this, TranslationViewModelFactory(repository))
            .get(TranslationViewModel::class.java)
    }

    private fun observeTranslations() {
        viewModel.translations.observe(viewLifecycleOwner) { translations ->
            if (translations.isEmpty()) {
                showEmptyState()
            } else {
                showHistory(translations)
            }
        }
        viewModel.loadTranslations()
    }

    private fun setupRecyclerView() {
        translationAdapter = TranslationAdapter(
            mutableListOf(),
            requireContext(),
            onEmptyList = {
                showEmptyState()
            },
//            onFavoriteChanged = { updatedTranslation ->
//                viewModel.loadTranslations()
//            }
        )
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = translationAdapter
        }
    }

    private fun showEmptyState() {
        binding.apply {
            historyRecyclerView.visibility = View.GONE
            emptyTitleImage.visibility = View.VISIBLE
            emptyTitleText.visibility = View.VISIBLE
            emptyDescText.visibility = View.VISIBLE
        }
    }

    private fun showHistory(translations: List<Translation>) {
        binding.apply {
            emptyTitleImage.visibility = View.GONE
            emptyTitleText.visibility = View.GONE
            emptyDescText.visibility = View.GONE
            historyRecyclerView.visibility = View.VISIBLE
            translationAdapter.updateTranslations(translations)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
