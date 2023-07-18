package com.picker.chucklechooser.repo.util

import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.repo.data.MediaItem

interface MediaRepo {
     fun fetchAlbums(): List<AlbumItem>
     fun fetchAllMedia(albumItem: AlbumItem, pageSize: Int, offset: Int): List<MediaItem>
}