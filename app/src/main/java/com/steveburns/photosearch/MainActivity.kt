package com.steveburns.photosearch

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.widget.Toast
import com.steveburns.photosearch.model.*
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val SEARCH_TERM_KEY = "SEARCH_TERM_KEY"
    private val LAST_PAGE_LOADED_KEY = "LAST_PAGE_LOADED_KEY"

    private lateinit var presenter: Presentation
    private lateinit var navigationCoordinator: NavigationCoordination
    private val compositeDisposable = CompositeDisposable()
    private lateinit var queryTextChangedEmitter: ObservableEmitter<String>
    private var requestingNextPage = false
    private var lastQueryText = ""

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)

        val savedSearchTerm = state?.getString(SEARCH_TERM_KEY) ?: ""
        val lastPageLoaded = state?.getInt(LAST_PAGE_LOADED_KEY, 1) ?: 1
        DependencyRegistry.inject(this, savedSearchTerm, lastPageLoaded)

        setupTextChangeObservable()

        // setup RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = createPhotosAdapter(this)
        val infiniteScrollListener = getOnScrollListener(recyclerView)
        recyclerView.addOnScrollListener(infiniteScrollListener)
    }

    // DependencyRegistry calls this method
    fun provide(presenter: Presentation, navigationCoordinator: NavigationCoordination) {
        this.presenter = presenter
        this.navigationCoordinator = navigationCoordinator
    }

    private fun createPhotosAdapter(activity: Activity) : PhotosAdapter {
        return object : PhotosAdapter(activity, presenter) {
            override fun onItemClicked(position: Int) {
                navigationCoordinator.searchItemWasTapped(activity, position)
            }
        }
    }

    private fun setupTextChangeObservable() {
        val disposable = Observable.create<String>({ emitter -> queryTextChangedEmitter = emitter })
                .debounce(250, TimeUnit.MILLISECONDS)
                .subscribe({
                    requestFirstPage(it)
                },
                { throwable ->
                    System.out.println(throwable.message)
                })

        compositeDisposable.add(disposable)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString(SEARCH_TERM_KEY, presenter.currentSearchTerm)
        outState?.putInt(LAST_PAGE_LOADED_KEY, presenter.lastPageNumber)
        super.onSaveInstanceState(outState)
    }

    private fun getOnScrollListener(recyclerView: RecyclerView): InfiniteScrollListener {
        return object : InfiniteScrollListener(recyclerView.layoutManager as LinearLayoutManager) {
            override fun onLoadMore() {

                if (requestingNextPage || presenter.lastPageLoaded) {
                    return
                }

                if (presenter.getImageDataCount() > 0) {
                    // Tell presenter to have a progress item.
                    presenter.hasProgressItem = true
                }

                Handler().post({
                    recyclerView.adapter.notifyItemInserted(presenter.getImageDataCount())
                })

                // request another page of data.
                requestNextPage()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.menu, menu)
            val searchItem = menu.findItem(R.id.action_search)
            val searchView = MenuItemCompat.getActionView(searchItem) as SearchView
            searchView.setOnQueryTextListener(getQueryTextListener())
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun getQueryTextListener() : SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // let SearchView perform the default action (close the keyboard)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                lastQueryText = newText ?: ""
                if (newText != null && newText.length > 2) {
                    queryTextChangedEmitter.onNext(newText)
                } else {
                    // We need to reset some stuff
                    presenter.clearData()
                    recyclerView.adapter.notifyDataSetChanged()
                }
                return true // let caller know we handled it
            }

        }
    }

    private fun requestFirstPage(searchTerm: String) {
        val adapter = recyclerView.adapter
        val disposable = presenter.getFirstPage(searchTerm)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            if (it > 0) {
                                adapter.notifyDataSetChanged()
                            }
                        },
                        {
                            handleError(it)
                        })
        compositeDisposable.add(disposable)
    }

    private fun requestNextPage() {
        if (!requestingNextPage) {
            requestingNextPage = true
            val adapter = recyclerView.adapter
            val curSize = adapter.itemCount
            val disposable = presenter.getNextPage()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                requestingNextPage = false
                                if (it > 0) {
                                    adapter.notifyItemRangeInserted(curSize, it)
                                }
                            },
                            {
                                requestingNextPage = false
                                adapter.notifyDataSetChanged() // remove spinner item
                                handleError(it)
                            })
            compositeDisposable.add(disposable)
        }
    }

    private fun handleError(throwable: Throwable) {
        if (lastQueryText == presenter.currentSearchTerm) {
            val toast = Toast.makeText(this, "There was an error. Check your network", Toast.LENGTH_SHORT)
            toast.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
