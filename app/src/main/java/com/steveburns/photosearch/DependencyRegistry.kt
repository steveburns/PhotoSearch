package com.steveburns.photosearch

import com.steveburns.photosearch.model.CacheInteractor
import com.steveburns.photosearch.model.ModelInteractor
import com.steveburns.photosearch.model.NetworkInteractor
import com.steveburns.photosearch.model.Presenter

class DependencyRegistry {

    companion object {
        // MainActivity dependencies
        fun inject(activity: MainActivity, searchTerm: String, lastPageNumber: Int) {
            val cacheInteractor = CacheInteractor()
            val networkInteractor = NetworkInteractor()
            val modelInteractor = ModelInteractor(cacheInteractor, networkInteractor)
            val presenter = Presenter(modelInteractor, searchTerm, lastPageNumber)
            activity.provide(presenter)
        }

    }
}