package com.picker.chucklechooser.repo.data

import android.net.Uri

data class Document(
    val uri: Uri,
    val documentType: DocumentType
)

enum class DocumentType {
    MEDIA, LOCATION, CONTACT, AUDIO
}