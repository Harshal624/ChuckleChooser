package com.picker.chucklechooser.ui.gallery

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.picker.chucklechooser.R
import com.picker.chucklechooser.databinding.ItemGalleryBinding
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.ui.util.CommonUtils

class GalleryPagingAdapter(
    private val resources: Resources,
    private val clickListener: (MediaItem) -> Unit
): PagingDataAdapter<MediaItem, GalleryPagingAdapter.GalleryViewHolder>(
    diff()
) {

    private val selectedImgDpInPx = CommonUtils.dpToPx(resources = resources, dp = 12F)

    companion object {

        const val KEY_IS_SELECTED = "isSelected"

        fun diff() = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.isSelected == newItem.isSelected
            }

            override fun getChangePayload(oldItem: MediaItem, newItem: MediaItem): Any? {

                if (oldItem.isSelected != newItem.isSelected) {
                    return Bundle().apply {
                        putBoolean(KEY_IS_SELECTED, newItem.isSelected)
                    }
                }

                return super.getChangePayload(oldItem, newItem)
            }
        }
    }

    override fun onBindViewHolder(
        holder: GalleryViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            payloads.firstOrNull()?.let {
                val bundle = it as? Bundle
                if (bundle != null && bundle.containsKey(KEY_IS_SELECTED)) {
                    holder.updateSelection(isSelected = bundle.getBoolean(KEY_IS_SELECTED, false))
                }
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
            binding.ivSelection.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { item ->
                        clickListener(item)
                    }
                }
            }
            Glide.with(binding.iv).load(mediaItem.mediaUri)
                .placeholder(R.drawable.baseline_image_24).apply(
                RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE).centerCrop()
            ).into(binding.iv)
            updateSelection(isSelected = mediaItem.isSelected)
        }

        fun updateSelection(isSelected: Boolean) {
            binding.ivSelection.setImageResource(
                if (isSelected) R.drawable.baseline_radio_button_checked_24 else R.drawable.baseline_radio_button_unchecked_24
            )
            val params: LayoutParams = binding.iv.layoutParams as LayoutParams
            if (isSelected) {
                params.setMargins(selectedImgDpInPx,selectedImgDpInPx,selectedImgDpInPx,selectedImgDpInPx)
            } else {
                params.setMargins(0,0,0,0)
            }
            binding.iv.layoutParams = params
        }
    }
}