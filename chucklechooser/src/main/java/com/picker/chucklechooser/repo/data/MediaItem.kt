package com.picker.chucklechooser.repo.data

import android.net.Uri

data class MediaItem(
    val mediaType: MediaType,
    val duration: Int,
    val size: Int,
    val displayName: String?,
    val id: Long,
    val mediaUri: Uri,
    val isSelected: Boolean = false
)

enum class MediaType {
    IMAGE, VIDEO
}