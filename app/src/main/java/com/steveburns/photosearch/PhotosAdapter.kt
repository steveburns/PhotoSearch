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

class PhotosAdapter(private val context: Context, private val presenter: Presentation) : RecyclerView.Adapter<PhotosAdapterViewHolder>() {

    override fun getItemCount(): Int {
        return presenter.getImageDataCount()
    }

    override fun onBindViewHolder(holder: PhotosAdapterViewHolder?, position: Int) {
        val imageData = presenter.getImageData(position)

        if (holder != null && imageData != null) {
            holder.titleText.text = imageData.title

            if (imageData.isPhoto) {
                // TODO: Can we fix the image control to be at least a minimum size???
                // TODO: We might want that since sometimes we don't get an image
                // TODO: Log those so we can see what we have and why it's happening
                Picasso.with(context).load(imageData.getListImageUrl()).into(holder.imageView)
            } else {
                // TODO: Replace with something else besides the launcher icon
                Picasso.with(context).load(R.mipmap.ic_launcher).into(holder.imageView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): PhotosAdapterViewHolder {
        val context = parent?.context
        val inflater = LayoutInflater.from(context)

        val contactView = inflater.inflate(R.layout.photo_item_card, parent, false)

        return PhotosAdapterViewHolder(contactView)
    }
}



class PhotosAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView = itemView.findViewById(R.id.image_id) as ImageView
    val titleText = itemView.findViewById(R.id.title_text) as TextView
}
