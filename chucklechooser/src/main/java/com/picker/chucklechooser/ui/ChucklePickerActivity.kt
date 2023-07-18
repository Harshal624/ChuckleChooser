package com.picker.chucklechooser.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.picker.chucklechooser.ChucklePicker
import com.picker.chucklechooser.R
import com.picker.chucklechooser.databinding.ActivityChucklePickerBinding
import com.picker.chucklechooser.repo.GalleryRepository
import com.picker.chucklechooser.repo.MediaRepository
import com.picker.chucklechooser.ui.file.FilesFragment
import com.picker.chucklechooser.ui.gallery.GalleryFragment

/**
 * Camera, Image, Video, Gallery, Audio, Location, Favourites, Allow selection from multiple sources, Contacts,
 */
class ChucklePickerActivity : AppCompatActivity() {

    private lateinit var viewModel: ChucklePickerViewModel

    private lateinit var optionAdapter: OptionAdapter

    private lateinit var binding: ActivityChucklePickerBinding

    private var allowGallery: Boolean = true
    private var allowFiles: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChucklePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this, ChucklePickerViewModel.ChucklePickerViewModelFactory(
                mediaRepository = MediaRepository(
                    GalleryRepository(contentResolver = contentResolver, resources = resources)
                ),
                resources = resources
            )
        )[ChucklePickerViewModel::class.java]

        val fragments = mutableListOf<Fragment>()
        allowGallery = savedInstanceState?.getBoolean(ChucklePicker.EXTRA_SHOW_GALLERY)
            ?: intent?.getBooleanExtra(ChucklePicker.EXTRA_SHOW_GALLERY, true) ?: true
        allowFiles = savedInstanceState?.getBoolean(ChucklePicker.EXTRA_SHOW_FILES)
            ?: intent?.getBooleanExtra(ChucklePicker.EXTRA_SHOW_FILES, true) ?: true

        val tabTitles = mutableListOf<String>()

        if (allowGallery) {
            fragments.add(GalleryFragment())
            tabTitles.add(resources.getString(R.string.title_gallery))
        }

        if (allowFiles) {
            fragments.add(FilesFragment())
            tabTitles.add(resources.getString(R.string.title_files))
        }

        if (fragments.isEmpty()) {
            throw IllegalArgumentException("At least one option (Gallery, Files, Location, or Contacts) must be enabled.")
        }

        optionAdapter = OptionAdapter(
            this,
            fragments
        )

        binding.pager.adapter = optionAdapter

        intent?.getBooleanExtra(ChucklePicker.EXTRA_DISABLE_SWIPE_GESTURE, false)?.let { disable ->
            binding.pager.isUserInputEnabled = !disable
        }

        TabLayoutMediator(binding.tabLayout, binding.pager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ChucklePicker.EXTRA_SHOW_GALLERY, allowGallery)
        outState.putBoolean(ChucklePicker.EXTRA_SHOW_FILES, allowFiles)
    }
}