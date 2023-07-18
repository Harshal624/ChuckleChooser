package com.picker.chucklechooser.ui.util

import android.content.Context
import android.content.res.Resources
import android.util.TypedValue

object CommonUtils {
    fun dpToPx(resources: Resources, dp: Float): Int {
        val displayMetrics = resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics).toInt()
    }
}