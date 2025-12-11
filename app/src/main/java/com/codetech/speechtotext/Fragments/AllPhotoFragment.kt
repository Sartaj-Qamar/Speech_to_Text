package com.codetech.speechtotext.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codetech.speechtotext.Adapters.GalleryAdapter
import com.codetech.speechtotext.R
import com.codetech.speechtotext.Utils.GetImagesViewModel
import com.codetech.speechtotext.Utils.ImageList
import com.codetech.speechtotext.databinding.FragmentAllPhotoBinding

class AllPhotoFragment : Fragment() {

    private lateinit var binding: FragmentAllPhotoBinding
    private lateinit var photoFolder: GalleryAdapter
    private lateinit var gridLayoutManager: GridLayoutManager

    private lateinit var selectedTargetLanguage: String
    private lateinit var selectedSourceLanguage: String
    private val photoList = mutableListOf<ImageList>()
    private val getImagesViewModel: GetImagesViewModel by viewModels()
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAllPhotoBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupScrollListener()
        observeViewModel()

        binding.camera.setOnClickListener {
            findNavController().navigate(R.id.OCRFragment)
        }

        selectedSourceLanguage = arguments?.getString("sourceLanguage").toString()
        selectedTargetLanguage = arguments?.getString("targetLanguage").toString()

        return binding.root
    }

    private fun setupRecyclerView() {
        gridLayoutManager = GridLayoutManager(requireContext(), 3)
        photoFolder = GalleryAdapter(requireContext(), photoList, ::onItemClick)

        binding.imageList.apply {
            layoutManager = gridLayoutManager
            adapter = photoFolder
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
    }

    private fun setupScrollListener() {
        binding.imageList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = gridLayoutManager.childCount
                val totalItemCount = gridLayoutManager.itemCount
                val firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition()

                if (!isLoading && (visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                    getImagesViewModel.loadMoreImages()
                }
            }
        })
    }

    private fun observeViewModel() {
        getImagesViewModel.galleryItems.observe(viewLifecycleOwner) { galleryItems ->
            isLoading = false
            val updatedPhotoList = galleryItems.map { ImageList(it.id, it.uri) }
            photoFolder.updateData(updatedPhotoList)
        }

    }

    private fun onItemClick(position: Int) {
        val selectedImage = photoFolder.getItem(position)


        val bundle = Bundle().apply {
            putString("imageUri", selectedImage.uri)
            putString("sourceLanguage", selectedSourceLanguage)
            putString("targetLanguage", selectedTargetLanguage)
        }
        findNavController().navigate(R.id.scanOCRFragment, bundle)
    }
}
