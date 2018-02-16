package com.steveburns.photosearch

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

abstract class InfiniteScrollListener(private var layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    // The minimum amount of items to have below your current scroll position before loading more.
    private val visibleThreshold = 2

    // This happens many times a second during a scroll, so be wary of the code you place here.
    override fun onScrolled(view: RecyclerView?, dx: Int, dy: Int) {

        // See if we have breached the visibleThreshold and need to load more data.
        if (layoutManager.findLastVisibleItemPosition() + visibleThreshold > layoutManager.itemCount) {
            onLoadMore()
        }
    }

    // Defines the process for actually loading more data based on page
    abstract fun onLoadMore()
}