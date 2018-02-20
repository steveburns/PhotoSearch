package com.steveburns.photosearch

import android.app.Activity
import android.content.Intent


interface NavigationCoordination {
    fun searchItemWasTapped(activity: Activity, position: Int)
}

class NavigationCoordinator : NavigationCoordination {
    override fun searchItemWasTapped(activity: Activity, position: Int) {
        val intent = Intent(activity, FullscreenActivity::class.java)
        intent.putExtra(FullscreenActivity.ITEM_POSITION_KEY, position)
        activity.startActivity(intent)
    }
}