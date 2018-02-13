package com.steveburns.photosearch.model

import android.net.Uri
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

interface Presentation {
    fun getImageDataCount() : Int
    fun getImageData(position: Int) : ImageData?
    fun getFirstPage(searchTerm: String) : Single<Int>
    fun getNextPage() : Single<Int>
    fun clearData()
    fun addProgressItem()
}

class Presenter(private val modelInteractor: ModelInteraction) : Presentation {

    private var lastPageNumber = 1
    private var currentSearchTerm = ""
    private var hasProgressItem = false

    override fun clearData() {
        lastPageNumber = 1
        currentSearchTerm = ""
        hasProgressItem = false
        modelInteractor.clearData()
    }

    override fun addProgressItem() {
        hasProgressItem = true
    }

    override fun getImageDataCount() =
         modelInteractor.getImageDataCount() + if (hasProgressItem) 1 else 0

    override fun getImageData(position: Int) = modelInteractor.getImageData(position)

    override fun getFirstPage(searchTerm: String) : Single<Int> {
        lastPageNumber = 1
        currentSearchTerm = searchTerm.toLowerCase()
        return modelInteractor
                .getImageDataPage(currentSearchTerm)
                .subscribeOn(Schedulers.io())
    }

    override fun getNextPage(): Single<Int> =
            modelInteractor
                    .getImageDataPage(currentSearchTerm, ++lastPageNumber)
                    .doFinally { hasProgressItem = false }
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