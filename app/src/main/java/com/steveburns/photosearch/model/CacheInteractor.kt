package com.steveburns.photosearch.model

interface CacheInteraction {
    fun get(position: Int) : ImageData?
    fun replaceWith(listImageData: List<ImageData>): Int
    fun appendTo(listImageData: List<ImageData>): Int
    fun getCount() : Int
}

class CacheInteractor : CacheInteraction {

    companion object {
        val imageDataList = mutableListOf<ImageData>()
    }

    override fun getCount() = imageDataList.size

    override fun get(position: Int) = imageDataList.elementAtOrNull(position)

    override fun replaceWith(listImageData: List<ImageData>): Int {
        imageDataList.clear()
        return appendTo(listImageData)
    }

    override fun appendTo(listImageData: List<ImageData>): Int {
        imageDataList.addAll(listImageData)
        return listImageData.count()
    }

}