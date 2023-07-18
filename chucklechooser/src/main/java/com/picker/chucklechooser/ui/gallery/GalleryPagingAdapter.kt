package com.picker.chucklechooser.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.picker.chucklechooser.R
import com.picker.chucklechooser.databinding.ItemGalleryBinding
import com.picker.chucklechooser.repo.data.MediaItem

class GalleryPagingAdapter : PagingDataAdapter<MediaItem, GalleryPagingAdapter.GalleryViewHolder>(
    diff()
) {

    companion object {
        fun diff() = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        getItem(position)?.let { item ->
            holder.bind(mediaItem = item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = ItemGalleryBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GalleryViewHolder(binding = binding)
    }

    inner class GalleryViewHolder(
        private val binding: ItemGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: MediaItem) {
            Glide.with(binding.iv).load(mediaItem.mediaUri).placeholder(R.drawable.baseline_image_24).apply(
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE)
            ).into(binding.iv)
        }
    }
}