package com.picker.chucklechooser.repo

import android.content.ContentResolver
import android.content.res.Resources
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.repo.util.MediaRepo

class AudioRepository(
    private val contentResolver: ContentResolver,
    private val resources: Resources
): MediaRepo {
    override fun fetchAlbums(): List<AlbumItem> {
        return emptyList()
    }

    override fun fetchAllMedia(albumItem: AlbumItem, pageSize: Int, offset: Int): List<MediaItem> {
        return emptyList()
    }

}