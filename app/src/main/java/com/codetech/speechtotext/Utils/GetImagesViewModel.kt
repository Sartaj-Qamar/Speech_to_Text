package com.codetech.speechtotext.Utils

import android.app.Application
import android.content.ContentUris
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GetImagesViewModel(application: Application) : AndroidViewModel(application) {

    private val _galleryItems = MutableLiveData<List<ImageList>>()
    val galleryItems: LiveData<List<ImageList>> = _galleryItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentPage = 0
    private val pageSize = 20
    private var hasMoreItems = true
    private var allItems = mutableListOf<ImageList>()

    init {
        _isLoading.value = false
        loadGalleryItems()
    }

    fun loadMoreImages() {
        if (!_isLoading.value!! && hasMoreItems) {
            currentPage++
            loadGalleryItems()
        }
    }

    private fun loadGalleryItems() {
        _isLoading.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = fetchGalleryItems(getApplication())
                withContext(Dispatchers.Main) {
                    allItems.addAll(items)
                    val startIndex = currentPage * pageSize
                    val endIndex = minOf(startIndex + pageSize, allItems.size)
                    
                    if (startIndex < allItems.size) {
                        val pagedItems = allItems.subList(0, endIndex)
                        _galleryItems.value = pagedItems
                        hasMoreItems = endIndex < allItems.size
                    } else {
                        hasMoreItems = false
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    hasMoreItems = false
                }
            }
        }
    }

    private suspend fun fetchGalleryItems(application: Application): List<ImageList> {
        val galleryItems = mutableListOf<ImageList>()
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        val cursor = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dataColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                galleryItems.add(ImageList(id.toInt(), contentUri.toString()))
            }
        }

        return galleryItems
    }
}
