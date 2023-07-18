package com.picker.chucklechooser.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.picker.chucklechooser.R
import com.picker.chucklechooser.databinding.FragmentGalleryBinding
import com.picker.chucklechooser.repo.data.AlbumItem
import com.picker.chucklechooser.ui.ChucklePickerViewModel
import timber.log.Timber

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityViewModel: ChucklePickerViewModel

    private lateinit var galleryAdapter: GalleryPagingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activityViewModel = ViewModelProvider(requireActivity())[ChucklePickerViewModel::class.java]
        setUpOptions()
        setUpAdapter()
    }

    private fun setUpOptions() {

        activityViewModel.selectedItemType.observe(viewLifecycleOwner) { album ->
            binding.tvOptionTitle.text = album.name
        }

        binding.tvOptionTitle.setOnClickListener { view ->
            val contextMenu = PopupMenu(requireContext(), view)
            contextMenu.inflate(R.menu.context_menu_gallery)
            activityViewModel.allAlbums.let { albums ->
                albums.forEachIndexed { index, albumItem ->
                    contextMenu.menu.add(0, index, 0, albumItem.name)
                    contextMenu.setOnMenuItemClickListener { item ->
                        activityViewModel.updateAlbumSelection(albums[item.itemId])
                        false
                    }
                }
            }
            contextMenu.show()
        }
    }

    private fun setUpAdapter() {
        galleryAdapter = GalleryPagingAdapter()
        binding.rv.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = galleryAdapter
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            activityViewModel.mediaPage.collect { data ->
                galleryAdapter.submitData(data)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}