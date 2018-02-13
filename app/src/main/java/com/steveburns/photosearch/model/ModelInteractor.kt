package com.steveburns.photosearch.model

import io.reactivex.Single

interface ModelInteraction {
    fun getImageData(position: Int) : ImageData?
    fun getImageDataPage(searchTerm: String, page: Int = 1) : Single<Int>
    fun getImageDataCount(): Int
    fun clearData()
}

class ModelInteractor(
        private val cacheInteractor: CacheInteraction,
        private val networkInteractor: NetworkInteraction) : ModelInteraction {

    private var lastRequestedSearchTerm = ""

    override fun clearData() {
        lastRequestedSearchTerm = ""
        cacheInteractor.clearData()
    }

    override fun getImageDataCount() = cacheInteractor.getCount()

    override fun getImageData(position: Int) = cacheInteractor.get(position)

    override fun getImageDataPage(searchTerm: String, page: Int): Single<Int> {
        lastRequestedSearchTerm = searchTerm
        return networkInteractor
                .getImageDataPage(searchTerm, page)
                .map({
                    when {
                        lastRequestedSearchTerm != searchTerm -> 0 // search term changed while we were fetching page, ignore results
                        page == 1 -> cacheInteractor.replaceWith(it)
                        else -> cacheInteractor.appendTo(it)
                    }
                })
    }

}