package com.picker.chucklechooser

import android.app.Application
import timber.log.Timber
import timber.log.Timber.*


class BaseApp: Application() {
    override fun onCreate() {
        super.onCreate()

        // TODO Plant if debug build
        Timber.plant(DebugTree())
    }
}