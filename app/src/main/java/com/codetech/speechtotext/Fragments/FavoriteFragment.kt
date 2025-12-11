package com.codetech.speechtotext.Fragments//class FavoriteFragment : Fragment() {
//    private var _binding: FragmentFavoriteBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var translationAdapter: TranslationAdapter
//    private lateinit var viewModel: TranslationViewModel
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        setupViewModel()
//        setupRecyclerView()
//        observeFavorites()
//
//        binding.backButton.setOnClickListener {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }
//    }
//
//    private fun setupViewModel() {
//        val database = AppDatabase.getDatabase(requireContext())
//        val repository = TranslationRepository(database.translationDao())
//        viewModel = ViewModelProvider(this, TranslationViewModelFactory(repository))
//            .get(TranslationViewModel::class.java)
//    }
//
//    private fun observeFavorites() {
//        viewModel.favorites.observe(viewLifecycleOwner) { favorites ->
//            if (favorites.isEmpty()) {
//                showEmptyState()
//            } else {
//                showFavorites(favorites)
//            }
//        }
//        viewModel.loadFavorites()
//    }
//
//    private fun showFavorites(favorites: List<Translation>) {
//        binding.apply {
//            emptyFavImage.visibility = View.GONE
//            emptyTitleText.visibility = View.GONE
//            emptyDescText.visibility = View.GONE
//            translateTextRecyclerView.visibility = View.VISIBLE
//            translationAdapter.updateTranslations(favorites)
//        }
//    }
//}