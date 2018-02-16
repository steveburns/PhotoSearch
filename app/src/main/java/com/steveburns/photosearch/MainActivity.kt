package com.steveburns.photosearch

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
    private val compositeDisposable = CompositeDisposable()
    private lateinit var queryTextChangedEmitter: ObservableEmitter<String>
    private lateinit var infiniteScrollListener: InfiniteScrollListener
    private var requestingNextPage = false


    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContentView(R.layout.activity_main)

        val savedSearchTerm = state?.getString(SEARCH_TERM_KEY) ?: ""
        val lastPageLoaded = state?.getInt(LAST_PAGE_LOADED_KEY, 1) ?: 1
        DependencyRegistry.inject(this, savedSearchTerm, lastPageLoaded)

        setupTextChangeObservable()

        // setup RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = PhotosAdapter(this, presenter)
        infiniteScrollListener = getOnScrollListener(recyclerView)
        recyclerView.addOnScrollListener(infiniteScrollListener)
    }

    // DependencyRegistry calls this method
    fun provide(presenter: Presentation) {
        this.presenter = presenter
        System.out.println("Presenter count of Loaded Images: ${presenter.getImageDataCount()}, term: ${presenter.currentSearchTerm}, page: ${presenter.lastPageNumber}")
    }

    private fun setupTextChangeObservable() {
        val disposable = Observable.create<String>({ emitter -> queryTextChangedEmitter = emitter })
                .debounce(250, TimeUnit.MILLISECONDS)
                .subscribe({
                    System.out.println("Requesting First Page for: $it")
                    requestFirstPage(it)
                },
                { throwable ->
                    // TODO: put some good logging here
                    System.out.println(throwable.message)
                })

        compositeDisposable.add(disposable)
    }

    // As property without the subscribe
//    private val textChangeObservable: Observable<String>
//        get() = Observable.create<String>({ emitter -> queryTextChangedEmitter = emitter })
//                    .debounce(250, TimeUnit.MILLISECONDS)

    // As method without subscribe
//    private fun setupTextChangeObservable() : Observable<String> {
//        return Observable.create<String>({ emitter -> queryTextChangedEmitter = emitter })
//                .debounce(250, TimeUnit.MILLISECONDS)
//    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putString(SEARCH_TERM_KEY, presenter.currentSearchTerm)
        outState?.putInt(LAST_PAGE_LOADED_KEY, presenter.lastPageNumber)
        super.onSaveInstanceState(outState)
    }

    private fun getOnScrollListener(recyclerView: RecyclerView): InfiniteScrollListener {
        return object : InfiniteScrollListener(recyclerView.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {

                if (requestingNextPage) return

                // TODO: some posts complained that this method gets called multiple times. Check it out.
                // TODO: https://gist.github.com/ssinss/e06f12ef66c51252563e
                // TODO: Look for this post: zfdang commented on Mar 25, 2016
                // TODO: Notice how he put a synchronize block in the onScrolled method.
                // TODO: I verified that it is and requestingNextPage flag fixes any bugs arising from it.
//                System.out.println("onLoadMore, page: $page, totalItems: $totalItemsCount")

                // Tell presenter to have a progress item.
                presenter.hasProgressItem = true
                Handler().post({
                    System.out.println("Act like we have a progress item...")
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

            // TODO: haha this just causes the search to be rerun on a rotation, but doesn't set
            // TODO:   the text that's displayed in the SearchView. Lame!
//            if (presenter.currentSearchTerm.isNotEmpty()) {
//                // TODO: the "submit" parameter tells the control where or not to submit or just set.
//                searchView.setQuery(presenter.currentSearchTerm, false)
//            }


            // TODO: according to setSearchableInfo code header we don't need this
//            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
//            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun getQueryTextListener() : SearchView.OnQueryTextListener {
        return object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                System.out.println("Query Submit tapped. Text is: $query")
                return false // let SearchView perform the default action (close the keyboard)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                System.out.println("Query Text changed to: $newText")
                if (newText != null && newText.length > 2) {
                    queryTextChangedEmitter.onNext(newText)
                } else {
                    // We need to reset some stuff
                    presenter.clearData()
                    infiniteScrollListener.reset()
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
                            System.out.println("DONE getting First Page for: $searchTerm")
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
                                System.out.println("DONE getting Next Page")
                                if (it > 0) {
                                    adapter.notifyItemRangeInserted(curSize, it)
                                }
                            },
                            {
                                requestingNextPage = false
                                handleError(it)
                            })
            compositeDisposable.add(disposable)
        }
    }

    private fun handleError(throwable: Throwable) {
        // TODO: put some good logging here
        System.out.println(throwable.message)
        val toast = Toast.makeText(this@MainActivity, "There was an error. Check your network", Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onDestroy() {

        // TODO: Should this be done in onDestroy?????
        super.onDestroy()
        compositeDisposable.clear()
    }
}
