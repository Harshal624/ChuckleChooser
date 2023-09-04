package com.picker.chucklechooser.repo.data

sealed class DocumentType {
    sealed class MediaType: DocumentType() {
        object Image: MediaType()
        object Video: MediaType()
    }
}