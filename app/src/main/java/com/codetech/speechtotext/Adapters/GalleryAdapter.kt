package com.codetech.speechtotext.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.codetech.speechtotext.Utils.ImageList
import com.codetech.speechtotext.databinding.RecyclerImageListBinding

class GalleryAdapter(
    private val context: Context,
    private var items: List<ImageList>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    private var selectedPosition: Int = -1

    class GalleryViewHolder(val binding: RecyclerImageListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = RecyclerImageListBinding.inflate(LayoutInflater.from(context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val item = items[position]

        // Use Glide for efficient image loading
        Glide.with(context)
            .load(item.uri.toUri())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(300, 300) // Set a fixed size for images
            .centerCrop()
            .into(holder.binding.ImageView1)

        holder.binding.root.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION && selectedPosition != currentPosition) {
                if (selectedPosition != -1) {
                    items[selectedPosition].isChecked = false
                    notifyItemChanged(selectedPosition)
                }
                item.isChecked = true
                selectedPosition = currentPosition
                onItemClick(currentPosition)
                notifyItemChanged(currentPosition)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<ImageList>) {
        items = newItems
        notifyDataSetChanged()
    }

    fun getItem(position: Int): ImageList = items[position]
}
