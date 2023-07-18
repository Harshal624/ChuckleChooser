package com.picker.chucklechooser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val picker = ChucklePicker(
            context = this,
            disableSwipeGesture = true,
            showGallery = true,
            showFiles = true
        )
        val intent = picker.createIntent()
        startActivity(intent)
        finish()
    }
}