package com.picker.chucklechooser

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.picker.chucklechooser.repo.GalleryRepository
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.repo.util.PagingUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.math.abs

class GalleryPagingDataSource(
    private val albumItem: AlbumItem,
    private val repository: GalleryRepository
) : PagingSource<Int, MediaItem>() {

    override val keyReuseSupported: Boolean
        get() = false

    override val jumpingSupported: Boolean
        get() = true

    override fun getRefreshKey(state: PagingState<Int, MediaItem>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        return abs(anchorPosition / PagingUtil.PAGE_SIZE_GALLERY)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaItem> {
        return withContext(Dispatchers.IO) {
            try {

                val isPrepend = params is LoadParams.Prepend
                val isAppend = params is LoadParams.Append
                val isRefresh = params is LoadParams.Refresh

                val mode = if (isPrepend) "Prepend" else if (isAppend) "Append" else "Refresh"

                val currentPage = params.key ?: 0

                val offset = currentPage * params.loadSize

                val mediaItemList = repository.fetchAllMedia(
                    albumItem = albumItem,
                    offset = offset,
                    pageSize = params.loadSize
                )

                val prevPage = if (currentPage > 0) currentPage - 1 else null

                val nextPage = if (mediaItemList.isNotEmpty()) currentPage + 1 else null

                val totalMediaCount = repository.getMediaCount(albumItem = albumItem)

                val itemsAfterCurrentPage = totalMediaCount - (offset + mediaItemList.size)

                Timber.d("Mode: $mode, previous page: $prevPage, current page: $currentPage, nextPage: $nextPage. List size: ${mediaItemList.size}, itemsAfter: $itemsAfterCurrentPage, itemsBefore: $offset, totalMediaCount: $totalMediaCount")

                LoadResult.Page(
                    data = mediaItemList,
                    prevKey = prevPage,
                    nextKey = nextPage,
                    itemsBefore = offset,
                    itemsAfter = if (itemsAfterCurrentPage < 0) 0 else itemsAfterCurrentPage
                )
            } catch (e: Exception) {
                Timber.e("Exception: $e")
                LoadResult.Error(e)
            }
        }
    }
}