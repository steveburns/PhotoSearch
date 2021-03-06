package com.steveburns.photosearch

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import com.steveburns.photosearch.model.Presentation
import com.steveburns.photosearch.model.getListImageUrl
import android.widget.ProgressBar



abstract class PhotosAdapter(private val context: Context, private val presenter: Presentation) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1

    override fun getItemViewType(position: Int): Int {
        return if (presenter.getImageData(position) == null) { VIEW_TYPE_LOADING } else { VIEW_TYPE_ITEM }
    }

    override fun getItemCount(): Int {
        return presenter.getImageDataCount()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {

        if (holder is PhotosAdapterViewHolder) {
            val imageData = presenter.getImageData(position)
            if (imageData != null) {
                holder.titleText.text = imageData.title
                holder.itemView.setOnClickListener({
                    onItemClicked(position)
                })

                if (imageData.isPhoto) {
                    Picasso.with(context).load(imageData.getListImageUrl()).into(holder.imageView)
                } else {
                    Picasso.with(context).load(R.mipmap.ic_launcher).into(holder.imageView)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val context = parent?.context
        val inflater = LayoutInflater.from(context)

        return if (viewType == VIEW_TYPE_ITEM) {
            val contactView = inflater.inflate(R.layout.photo_item_card, parent, false)
            PhotosAdapterViewHolder(contactView)
        } else {
            val progressView = inflater.inflate(R.layout.progressbar_item, parent, false)
            LoadingViewHolder(progressView)
        }
    }

    abstract fun onItemClicked(position: Int)
}



class PhotosAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.findViewById(R.id.image_id) as ImageView
    val titleText = itemView.findViewById(R.id.title_text) as TextView
}

private class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar) as ProgressBar
}
