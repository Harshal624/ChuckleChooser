package com.picker.chucklechooser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.picker.chucklechooser.repo.GalleryRepository
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem
import timber.log.Timber

class GalleryPagingDataSource(
    private val albumItem: AlbumItem,
    private val repository: GalleryRepository
) : PagingSource<Int, MediaItem>() {

    var initialLoadSize: Int = 0

    /**
     * TODO Implement this
     */
    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        Timber.d("Load called. Key: ${params.key}")
        try {
            val nextPageNumber = params.key ?: 1
            if (params.key == null) {
                initialLoadSize = params.loadSize
            }

            val offset = if (nextPageNumber == 2)
                initialLoadSize
            else
                ((nextPageNumber - 1) * params.loadSize) + (initialLoadSize - params.loadSize)

            val mediaItemList =
                repository.fetchAllMedia(albumItem = albumItem, offset = offset, pageSize = params.loadSize)

            val count = mediaItemList.size

            val nextKey = if (count < params.loadSize) null else nextPageNumber + 1

            Timber.d("Total count: $count, offset: $offset, next page number: $nextPageNumber, next key: $nextKey, loadSize: ${params.loadSize}")

            /**
             * TODO Add support for placeholders and calculate itemsAfter and itemsBefore
             */
            return LoadResult.Page(
                data = mediaItemList,
                prevKey = null, // Only paging forward (for now).
                nextKey = nextKey
            )
        } catch (e: Exception) {
            Timber.e("Exception: $e")
            return LoadResult.Error(e)
        }
    }
}