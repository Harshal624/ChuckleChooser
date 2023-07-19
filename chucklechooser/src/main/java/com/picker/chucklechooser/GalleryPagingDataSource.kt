package com.picker.chucklechooser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.picker.chucklechooser.repo.GalleryRepository
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GalleryPagingDataSource(
    private val albumItem: AlbumItem,
    private val repository: GalleryRepository
) : PagingSource<Int, MediaItem>() {

    override val keyReuseSupported: Boolean
        get() = false

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? {
        val anchorPosition = state.anchorPosition

        if (anchorPosition != null) {
            val closestPosition = state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
            Timber.d("getRefreshKey called. Anchor pos: $anchorPosition, closest position to position: $closestPosition")
            return closestPosition
        }

        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        return withContext(Dispatchers.IO) {
            try {

                val isPrepend = params is LoadParams.Prepend
                val isAppend = params is LoadParams.Append
                val isRefresh = params is LoadParams.Refresh

                val mode = if (isPrepend) "Prepend" else if (isAppend) "Append" else "Refresh"

                val currentPage = params.key ?: 0

                val mediaItemList = repository.fetchAllMedia(
                    albumItem = albumItem,
                    page = currentPage,
                    pageSize = params.loadSize
                )

                val prevPage = if (currentPage > 0) currentPage - 1 else null

                val nextPage = if (mediaItemList.isNotEmpty()) currentPage + 1 else null

                Timber.d("Mode: $mode, previous page: $prevPage, current page: $currentPage, nextPage: $nextPage. List size: ${mediaItemList.size}")

                LoadResult.Page(
                    data = mediaItemList,
                    prevKey = prevPage,
                    nextKey = nextPage
                )
            } catch (e: Exception) {
                Timber.e("Exception: $e")
                LoadResult.Error(e)
            }
        }
    }
}