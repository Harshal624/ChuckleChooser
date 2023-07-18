package com.picker.chucklechooser.ui

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import com.picker.chucklechooser.repo.MediaRepository
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.Document
import com.picker.chucklechooser.repo.data.DocumentType
import com.picker.chucklechooser.repo.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChucklePickerViewModel(
    private val mediaRepository: MediaRepository,
    private val resources: Resources
) : ViewModel() {

    private val _selectedAlbumType: MutableStateFlow<AlbumItem> =
        MutableStateFlow(AlbumItem.getAll(resources))

    private val _allAlbums: MutableList<AlbumItem> = mutableListOf()
    val allAlbums get() = _allAlbums

    val selectedItemType = _selectedAlbumType.asLiveData()

    private val _selectedDocuments: MutableStateFlow<List<Document>> =
        MutableStateFlow(emptyList())

    val selectedDocuments = _selectedDocuments.asLiveData()

    val mediaPage = _selectedAlbumType.flatMapLatest { item ->
        _selectedDocuments.flatMapLatest { documentList ->
            mediaRepository.getMediaPage(albumItem = item)
                .map { mediaItemList ->
                    mediaItemList.map { item ->
                        item.copy(isSelected = documentList.find { doc -> doc.uri == item.mediaUri } != null)
                    }
                }
        }
    }.cachedIn(viewModelScope)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _allAlbums.clear()
            _allAlbums.addAll(mediaRepository.getMediaAlbums())
        }
    }

    fun updateAlbumSelection(albumItem: AlbumItem) {
        _selectedAlbumType.value = albumItem
    }

    fun updateDocumentSelection(selectedItem: MediaItem) {
        val selectedDocs = mutableListOf<Document>()
        selectedDocs.addAll(_selectedDocuments.value)
        val existingDocument = selectedDocs.find { doc -> doc.uri == selectedItem.mediaUri }
        if (existingDocument == null) {
            selectedDocs.add(
                Document(
                    uri = selectedItem.mediaUri,
                    documentType = DocumentType.MEDIA
                )
            )
        } else {
            selectedDocs.remove(existingDocument)
        }
        _selectedDocuments.value = selectedDocs
    }

    class ChucklePickerViewModelFactory(
        private val mediaRepository: MediaRepository,
        private val resources: Resources
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ChucklePickerViewModel::class.java)) {
                return ChucklePickerViewModel(
                    mediaRepository = mediaRepository,
                    resources = resources
                ) as T
            }
            throw IllegalStateException("ViewModel not found")
        }
    }
}