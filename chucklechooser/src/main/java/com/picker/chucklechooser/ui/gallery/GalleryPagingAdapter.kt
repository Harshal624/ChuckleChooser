package com.picker.chucklechooser.ui.gallery

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.picker.chucklechooser.R
import com.picker.chucklechooser.databinding.ItemGalleryBinding
import com.picker.chucklechooser.databinding.ItemImagePlaceholderBinding
import com.picker.chucklechooser.repo.data.Document
import com.picker.chucklechooser.repo.data.MediaItem
import com.picker.chucklechooser.ui.util.CommonUtils
import timber.log.Timber

class GalleryPagingAdapter(
    private val resources: Resources,
    private val selectedDocumentList: MutableList<Document>,
    private val clickListener: (Int, MediaItem) -> Unit         // Adapter position and Media Item
) : PagingDataAdapter<MediaItem, RecyclerView.ViewHolder>(
    diff()
) {

    private val selectedImgDpInPx = CommonUtils.dpToPx(resources = resources, dp = 8F)

    companion object {

        fun diff() = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem): Boolean {
                return oldItem.mediaUri == newItem.mediaUri
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (holder is GalleryViewHolder) {
                payloads.firstOrNull()?.let {
                    val isSelected = it as? Boolean
                    if (isSelected != null) {
                        Timber.d("onBindViewHolder payload. Is selected: $isSelected")
                        holder.updateSelection(isSelected = isSelected)
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GalleryViewHolder) {
            getItem(position)?.let { item ->
                holder.bind(mediaItem = item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == -1) {
            val binding = ItemImagePlaceholderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ImagePlaceholderViewHolder(binding = binding)
        } else {
            val binding = ItemGalleryBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            GalleryViewHolder(binding = binding)
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position) == null) {
            -1  // Placeholder
        } else {
            1   // Actual item
        }
    }

    inner class ImagePlaceholderViewHolder(
        binding: ItemImagePlaceholderBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class GalleryViewHolder(
        private val binding: ItemGalleryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(mediaItem: MediaItem) {
            Timber.d("bind called for position: $bindingAdapterPosition")
            binding.root.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    getItem(bindingAdapterPosition)?.let { item ->
                        clickListener(bindingAdapterPosition, item)
                    }
                }
            }
            Glide.with(binding.iv).load(mediaItem.mediaUri)
                .placeholder(R.drawable.baseline_image_24).apply(
                    RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.RESOURCE).centerCrop()
                ).into(binding.iv)
            updateSelection(isSelected = selectedDocumentList.find { it.id == mediaItem.id && it.documentType == mediaItem.mediaType } != null)
        }

        internal fun updateSelection(isSelected: Boolean) {
            binding.ivSelection.setImageResource(
                if (isSelected) R.drawable.baseline_radio_button_checked_24 else R.drawable.baseline_radio_button_unchecked_24
            )
            binding.vSelection.isVisible = isSelected
            val params: LayoutParams = binding.iv.layoutParams as LayoutParams
            if (isSelected) {
                params.setMargins(
                    selectedImgDpInPx,
                    selectedImgDpInPx,
                    selectedImgDpInPx,
                    selectedImgDpInPx
                )
            } else {
                params.setMargins(0, 0, 0, 0)
            }
            binding.iv.layoutParams = params
        }
    }

    fun onSelectedDocumentsUpdated(documents: List<Document>) {
        selectedDocumentList.clear()
        selectedDocumentList.addAll(documents)
    }
}