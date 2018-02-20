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
    var hasProgressItem : Boolean
    var currentSearchTerm : String
    var lastPageNumber: Int
    var lastPageLoaded: Boolean
}

class Presenter(private val modelInteractor: ModelInteraction,
                override var currentSearchTerm: String,
                override var lastPageNumber: Int) : Presentation {

    override var lastPageLoaded: Boolean = false
    override var hasProgressItem: Boolean = false

    override fun clearData() {
        lastPageLoaded = false
        lastPageNumber = 1
        currentSearchTerm = ""
        hasProgressItem = false
        modelInteractor.clearData()
    }

    override fun getImageDataCount() =
         modelInteractor.getImageDataCount() + if (hasProgressItem) 1 else 0

    override fun getImageData(position: Int) = modelInteractor.getImageData(position)

    override fun getFirstPage(searchTerm: String) : Single<Int> {
        lastPageLoaded = false
        lastPageNumber = 1
        currentSearchTerm = searchTerm.toLowerCase()
        return modelInteractor
                .getImageDataPage(currentSearchTerm)
                .subscribeOn(Schedulers.io())
    }

    override fun getNextPage(): Single<Int> =
            modelInteractor
                    .getImageDataPage(currentSearchTerm, lastPageNumber + 1)
                    .doOnSuccess({ ++lastPageNumber }) // increment only if we actually got the next page (we could get an error or there may not be another page)
                    .doOnError({ lastPageLoaded = true }) // TODO: (enhancement) this could happen because of bad network or last page was already loaded.
                    // TODO:  we need a way to identify which one it was. This requires us to change the NetworkAdapter to tell us that.
                    // TODO:  we'll probably need to do that by means of a custom converter.
                    .doFinally({ hasProgressItem = false })
                    .subscribeOn(Schedulers.io())
}

// Extension function that constructs the URI to the image best suited for the list of photos
fun ImageData.getListImageUrl() : String {
    if (isPhoto) {
        val lastSeg = Uri.parse(link).lastPathSegment
        if (!lastSeg.isNullOrBlank()) {
            return link.replace(lastSeg, lastSeg.replace(".", "h."))
        }
    }
    return link
}