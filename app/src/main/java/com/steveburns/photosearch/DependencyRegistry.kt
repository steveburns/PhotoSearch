package com.steveburns.photosearch

import com.steveburns.photosearch.model.*

class DependencyRegistry {

    companion object {
        // MainActivity dependencies
        fun inject(activity: MainActivity, searchTerm: String, lastPageNumber: Int) {
            val cacheInteractor = CacheInteractor()
            val networkInteractor = NetworkInteractor()
            val modelInteractor = ModelInteractor(cacheInteractor, networkInteractor)
            val presenter = Presenter(modelInteractor, searchTerm, lastPageNumber)
            val navigationCoordinator = NavigationCoordinator()
            activity.provide(presenter, navigationCoordinator)
        }

        // FullscreenActivity dependencies
        fun inject(activity: FullscreenActivity) {
            val cacheInteractor = CacheInteractor()
            val networkInteractor = NetworkInteractor()
            val modelInteractor = ModelInteractor(cacheInteractor, networkInteractor)
            val presenter = FullScreenPresenter(modelInteractor)
            activity.provide(presenter)
        }
    }
}