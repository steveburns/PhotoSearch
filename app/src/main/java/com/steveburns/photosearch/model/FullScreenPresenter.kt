package com.steveburns.photosearch.model

interface FullScreenPresentation {
    fun getImageData(position: Int) : ImageData?
}

class FullScreenPresenter(private val modelInteractor: ModelInteraction) : FullScreenPresentation {

    override fun getImageData(position: Int) = modelInteractor.getImageData(position)
}