package com.picker.chucklechooser

import android.content.Context
import android.content.Intent
import com.picker.chucklechooser.ui.ChucklePickerActivity

class ChucklePicker(
    private val context: Context,
    private val pickLimit: Int = 10,
    private val disableSwipeGesture: Boolean = false,
    private val showGallery: Boolean = true,
    private val showFiles: Boolean = true
) {
    companion object {
        const val EXTRA_PICK_LIMIT = "pickLimit"
        const val EXTRA_DISABLE_SWIPE_GESTURE = "disableSwipeGesture"
        const val EXTRA_SHOW_GALLERY = "showGallery"
        const val EXTRA_SHOW_FILES = "showFiles"
    }

    fun createIntent(): Intent {
        val intent = Intent(context, ChucklePickerActivity::class.java)
        intent.putExtra(EXTRA_PICK_LIMIT, pickLimit)
        intent.putExtra(EXTRA_DISABLE_SWIPE_GESTURE, disableSwipeGesture)
        intent.putExtra(EXTRA_SHOW_GALLERY, showGallery)
        intent.putExtra(EXTRA_SHOW_FILES, showFiles)
        return intent
    }
}