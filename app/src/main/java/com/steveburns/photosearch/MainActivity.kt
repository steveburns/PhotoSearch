package com.steveburns.photosearch

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
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

    lateinit var presenter: Presentation
    val compositeDisposable = CompositeDisposable()
    lateinit var queryTextChangedEmitter: ObservableEmitter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPhotos()
    }

    private fun setupPhotos() {
        // There's less byte code if you access it just once using Kotlin synthetic.
        //  Look at the Kotlin Bytecode.
        val rv = recyclerView

        rv.setHasFixedSize(true)

        // TODO: Move this to a Dependency Registry!!!
        val cacheInteractor = CacheInteractor()
        val networkInteractor = NetworkInteractor()
        val modelInteractor = ModelInteractor(cacheInteractor, networkInteractor)
        presenter = Presenter(modelInteractor)

        setupTextChangeObservable()

        val adapter = PhotosAdapter(this, presenter)
        rv.adapter = adapter
        rv.addOnScrollListener(getOnScrollListener(rv))
    }

    private fun setupTextChangeObservable() {
        val disposable = Observable.create<String>({ emitter -> queryTextChangedEmitter = emitter })
                .debounce(250, TimeUnit.MILLISECONDS)
                .subscribe({
                    requestFirstPage(it)
                },
                { throwable ->
                    // TODO: put some good logging here
                    System.out.println(throwable.message)
                })

        compositeDisposable.add(disposable)
    }

    private fun getOnScrollListener(recyclerView: RecyclerView): RecyclerView.OnScrollListener {
        return object : InfiniteScrollListener(recyclerView.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                System.out.println("onLoadMore, page: $page, totalItems: $totalItemsCount")

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
            val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
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
        val adapter = recyclerView.adapter
        val curSize = adapter.itemCount
        val disposable = presenter.getNextPage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (it > 0) {
                            adapter.notifyItemRangeInserted(curSize, it)
                        }
                    },
                    {
                        handleError(it)
                    })
        compositeDisposable.add(disposable)
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
