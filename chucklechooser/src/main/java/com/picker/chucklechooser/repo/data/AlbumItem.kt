package com.picker.chucklechooser.repo.data

import android.content.res.Resources
import com.picker.chucklechooser.R


data class AlbumItem(val name: String, val albumType: AlbumType, val bucketId: String) {
    override fun equals(other: Any?): Boolean {
        if (other !is AlbumItem) {
            return false
        }
        return other.bucketId == bucketId
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + albumType.hashCode()
        result = 31 * result + bucketId.hashCode()
        return result
    }

    companion object {
        fun getAll(resources: Resources): AlbumItem {
            return AlbumItem(
                name = resources.getString(R.string.label_all),
                albumType = AlbumType.ALL,
                bucketId = "0"

            )
        }
    }
}