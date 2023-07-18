package com.picker.chucklechooser.ui

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.picker.chucklechooser.repo.MediaRepository
import com.picker.chucklechooser.repo.data.AlbumItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class ChucklePickerViewModel(
    private val mediaRepository: MediaRepository,
    private val resources: Resources
) : ViewModel() {

    private val _selectedAlbumType: MutableStateFlow<AlbumItem> =
        MutableStateFlow(AlbumItem.getAll(resources))

    private val _allAlbums: MutableList<AlbumItem> = mutableListOf()
    val allAlbums get() = _allAlbums

    val selectedItemType = _selectedAlbumType.asLiveData()

    val mediaPage = _selectedAlbumType.flatMapLatest { item ->
        mediaRepository.getMediaPage(albumItem = item)
            .cachedIn(viewModelScope)
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

    class ChucklePickerViewModelFactory(
        private val mediaRepository: MediaRepository,
        private val resources: Resources
    ): ViewModelProvider.Factory {
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