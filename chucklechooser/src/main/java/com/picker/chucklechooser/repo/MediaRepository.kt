package com.picker.chucklechooser.repo

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.picker.chucklechooser.GalleryPagingDataSource
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.repo.util.PagingUtil
import kotlinx.coroutines.flow.Flow

class MediaRepository(
    private val galleryRepository: GalleryRepository
) {

    fun getMediaPage(albumItem: AlbumItem): Flow<PagingData<MediaItem>> {
        return Pager(config = PagingConfig(
            pageSize = PagingUtil.PAGE_SIZE_GALLERY,
            enablePlaceholders = true,
            initialLoadSize = PagingUtil.PAGE_SIZE_GALLERY,
            jumpThreshold = PagingUtil.PAGE_SIZE_GALLERY * 3
        ), pagingSourceFactory = {
            GalleryPagingDataSource(
                albumItem = albumItem,
                repository = galleryRepository
            )
        }).flow
    }

    fun getMediaAlbums(): List<AlbumItem> = galleryRepository.fetchAlbums()
}