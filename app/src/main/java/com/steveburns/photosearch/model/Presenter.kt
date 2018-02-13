package com.steveburns.photosearch.model

import android.net.Uri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface Presentation {
    fun getImageDataCount() : Int
    fun getImageData(position: Int) : ImageData?
    fun getFirstPage(searchTerm: String) : Single<Int>
    fun getNextPage() : Single<Int>
}

class Presenter(private val modelInteractor: ModelInteraction) : Presentation {
    override fun getImageDataCount() = modelInteractor.getImageDataCount()

    var lastPageNumber = 1
    var currentSearchTerm = ""

    override fun getImageData(position: Int) = modelInteractor.getImageData(position)

    override fun getFirstPage(searchTerm: String) : Single<Int> {
        lastPageNumber = 1
        currentSearchTerm = searchTerm.toLowerCase()
        return modelInteractor
                .getImageDataPage(currentSearchTerm)
                .subscribeOn(Schedulers.io())
    }

    override fun getNextPage() =
            modelInteractor
                    .getImageDataPage(currentSearchTerm, ++lastPageNumber)
                    .subscribeOn(Schedulers.io())
}

fun ImageData.getListImageUrl() : String {
    if (isPhoto) {
        val uri = Uri.parse(link)
        val lastSeg = uri.lastPathSegment
        if (!lastSeg.isNullOrBlank()) {
            val newLastSeg = lastSeg.replace(".", "h.")
            return link.replace(lastSeg, newLastSeg)
        }
    }
    return link
}