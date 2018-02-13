package com.steveburns.photosearch.model

import com.steveburns.photosearch.network.ImageDataResponse
import com.steveburns.photosearch.network.NetworkAdapter
import com.steveburns.photosearch.network.NetworkImageData
import io.reactivex.Single

interface NetworkInteraction {
    fun getImageDataPage(searchTerm: String, page: Int) : Single<List<ImageData>>
}

class NetworkInteractor : NetworkInteraction {

    override fun getImageDataPage(searchTerm: String, page: Int) =
        NetworkAdapter.imgurService
                .getPhotoPage(page, searchTerm)
                .map { imageDataListFrom(it) }

    private fun imageDataListFrom(imageDataResponse: ImageDataResponse) : List<ImageData> {
        return imageDataResponse.data
                // some do not have images
                .filter { it.images != null && it.images.isNotEmpty() }
                .map { imageDataFrom(it) }
    }

    private fun imageDataFrom(networkImageData: NetworkImageData) : ImageData {
        val image = networkImageData.images.first()
        return ImageData(networkImageData.title, image.type.startsWith("image"), image.link)
    }

}