package com.codetech.speechtotext.Fragments

import TranslationRepository
import TranslationViewModel
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codetech.speechtotext.Adapters.TranslationAdapter
import com.codetech.speechtotext.data_source.AppDatabase
import com.codetech.speechtotext.data_source.Translation
import com.codetech.speechtotext.databinding.FragmentFavouriteBinding
import com.codetech.speechtotext.viewmodels.TranslationViewModelFactory


class FavouriteFragment : Fragment() {

    private var _binding: FragmentFavouriteBinding? = null
    private val binding get() = _binding!!
    private lateinit var translationAdapter: TranslationAdapter

    //private lateinit var sharedPrefHelper: SharedPrefHelper
    private lateinit var viewModel: TranslationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavouriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        observeFavorites()

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

    private fun setupRecyclerView() {
        translationAdapter = TranslationAdapter(
            mutableListOf(),
            requireContext(),
            isFavouriteFragment = true,
            onEmptyList = {
                showEmptyState()
            },
//            onFavoriteChanged = { updatedTranslation ->
//                viewModel.loadFavorites() // Refresh favorites list
//            }
        )
        binding.translateTextRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = translationAdapter
        }
    }

    private fun observeFavorites() {
        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            if (favorites.isEmpty()) {
                showEmptyState()
            } else {
                showFavorites(favorites)
            }
        }
        viewModel.loadFavorites()
    }

//    private fun loadFavorites() {
//        val favorites = sharedPrefHelper.getHistory().filter { it.isFavorite }
//        updateUI(favorites)
//    }

//    private fun updateUI(favorites: List<TranslationData>) {
//        if (favorites.isEmpty()) {
//            showEmptyState()
//        } else {
//            showFavorites(favorites)
//        }
//    }

    private fun showEmptyState() {
        binding.apply {
            emptyFavImage.visibility = View.VISIBLE
            emptyTitleText.visibility = View.VISIBLE
            emptyDescText.visibility = View.VISIBLE
            translateTextRecyclerView.visibility = View.GONE
        }
    }

    private fun showFavorites(favorites: List<Translation>) {
        binding.apply {
            emptyFavImage.visibility = View.GONE
            emptyTitleText.visibility = View.GONE
            emptyDescText.visibility = View.GONE
            translateTextRecyclerView.visibility = View.VISIBLE
            translationAdapter.updateTranslations(favorites)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
