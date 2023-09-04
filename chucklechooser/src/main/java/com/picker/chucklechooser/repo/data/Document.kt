package com.picker.chucklechooser.repo.data

import android.net.Uri

data class Document(
    val id: Long,
    val uri: Uri,
    val documentType: DocumentType
)
