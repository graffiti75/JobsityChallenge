package br.android.cericatto.jobsity.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Cache
import br.android.cericatto.jobsity.model.api.Shows
import br.android.cericatto.jobsity.presenter.extensions.initApiService
import br.android.cericatto.jobsity.presenter.extensions.networkOn
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.ShowsAdapter
import br.android.cericatto.jobsity.view.viewmodel.ShowsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

@SuppressLint("LogNotTimber")
class MainActivity : ParentActivity() {

    //--------------------------------------------------
    // Constants
    //--------------------------------------------------

    companion object {
        const val LIST_POSITION_STATE = "list_position_state"
    }

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mSearchView: SearchView
    private var mIsToolbarMenuSearch = false

    private lateinit var mShowsAdapter: ShowsAdapter
    private var mShowsList: MutableList<Shows> = arrayListOf()
    private var mListState: Parcelable? = null

    private val mComposite = CompositeDisposable()

    private lateinit var mShowName: String
    private lateinit var mViewModel: ShowsViewModel

    private lateinit var mScrollListener: RecyclerView.OnScrollListener

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainApplication.service = initApiService()
        mViewModel = ViewModelProviders.of(this).get(ShowsViewModel::class.java)

        setToolbar(R.id.id_toolbar, titleId = R.string.activity_main__title)
        checkSavedInstanceState(savedInstanceState)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(LIST_POSITION_STATE, activity_main__recycler_view.layoutManager?.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        mComposite.dispose()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConfiguration.MAIN_TO_DETAILS_CODE) {
            updateVisibilities(false)
        }
    }

    //--------------------------------------------------
    // Menu
    //--------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        initMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.search)
        mSearchView = searchItem.actionView as SearchView
        mSearchView.queryHint = getString(R.string.search_hint)
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                try {
                    if (!networkOn()) showToast(R.string.no_internet)
                    else {
                        updateVisibilities()
                        clearMoviesList(query)
                        searchShows(query)
                    }
                } catch (e: Exception) {
                    showToast(R.string.search_error)
                }
                searchItem.collapseActionView()
                return false
            }
            override fun onQueryTextChange(newText: String) = true
        })
    }

    //--------------------------------------------------
    // Data Response Methods
    //--------------------------------------------------

    private fun checkSavedInstanceState(savedInstanceState: Bundle?) {
        Log.i(AppConfiguration.TAG, "checkSavedInstanceState().")
        mListState = savedInstanceState?.getParcelable(LIST_POSITION_STATE)
        if (savedInstanceState == null) {   // Get shows from API.
            Log.i(AppConfiguration.TAG, "checkSavedInstanceState() -> savedInstanceState is null.")
            getShows()
        } else {                            // Get cached shows from ViewModel.
            Log.i(AppConfiguration.TAG, "checkSavedInstanceState() -> savedInstanceState is NOT null.")
            val viewModel = ViewModelProviders
                .of(this)
                .get(ShowsViewModel::class.java)
            val moviesList = viewModel.getShowsList()
            showCachedMovies(moviesList)
        }
    }

    private fun showCachedMovies(list: MutableList<Shows>) {
        Log.i(AppConfiguration.TAG, "showCachedMovies().")
        if (list.isNotEmpty()) mShowsList = list

        if (activity_main__recycler_view.adapter == null) {
            Log.i(AppConfiguration.TAG, "showCachedMovies() -> Adapter is null.")
            setAdapter(mShowsList)
            activity_main__recycler_view.adapter = mShowsAdapter
        } else {
            Log.i(AppConfiguration.TAG, "showCachedMovies() -> Adapter is NOT null.")
            mShowsAdapter.notifyDataSetChanged()
        }
        activity_main__recycler_view.layoutManager!!.onRestoreInstanceState(mListState)
        setRecyclerViewScrollListener()
        updateVisibilities(false)
    }

    private fun getShows() {
        Log.i(AppConfiguration.TAG, "getShows() -> Page is ${mViewModel.page}.")
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service
            val observable = service.getShowsList(mViewModel.page)
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        mViewModel.lastId = it[it.size - 1].id
                        mIsToolbarMenuSearch = false
                        getShowsOnSuccess(it, false)
                    },
                    {
                        Timber.i("getShows() -> On error: $it")
                    },
                    {
                        Timber.i("getShows() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun searchShows(query: String) {
//        if (mShowsViewModel.page <= mShowsViewModel.pagesNeeded) {
            val subscription = MainApplication.service.search(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        val list: MutableList<Shows> = it.map {
                            data -> data.show
                        } as MutableList<Shows>
                        mIsToolbarMenuSearch = true
                        getShowsOnSuccess(list)
                    },
                    {
                        Timber.i("searchShows() -> On error: $it")
                    },
                    {
                        Timber.i("searchShows() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
//        }
    }

    //--------------------------------------------------
    // Data Decision Methods
    //--------------------------------------------------

    fun clearMoviesList(query: String) {
        mShowName = query
        mShowsList.clear()
        Cache.clearCacheShows()
        mViewModel.page = 1
    }

    private fun getShowsOnSuccess(list: MutableList<Shows>, searchPerformed: Boolean = true) {
        if (searchPerformed) {
            mShowsList.addAll(list)
        } else {
            setRecyclerViewScrollListener()
            if (mViewModel.page > 0) {
                mShowsAdapter.updateList(list)
            } else {
                mShowsList.addAll(list)
                setAdapter(mShowsList)
            }
        }
        activity_main__pagination_loading.visibility = View.GONE
        Cache.cacheShows(mShowsList)
        updateVisibilities(false)
    }

    private fun setAdapter(list: MutableList<Shows>) {
        mShowsAdapter = ShowsAdapter(this, list)
        activity_main__recycler_view.adapter = mShowsAdapter
    }

    private fun updateVisibilities(loading: Boolean = true) {
        if (loading) {
            activity_main__recycler_view.visibility = View.GONE
            activity_main__loading.visibility = View.VISIBLE
        } else {
            activity_main__recycler_view.visibility = View.VISIBLE
            activity_main__loading.visibility = View.GONE
        }
    }

    //--------------------------------------------------
    // Pagination Methods
    //--------------------------------------------------

    private fun setRecyclerViewScrollListener() {
        Log.i(AppConfiguration.TAG, "setRecyclerViewScrollListener().")
        mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrollChanged()
            }
        }
        activity_main__recycler_view.addOnScrollListener(mScrollListener)
    }

    private fun onScrollChanged() {
        val currentAdapterShowId = MainApplication.currentAdapterShowId
        Log.i(AppConfiguration.TAG, "onScrollChanged -> currentAdapterShowId: $currentAdapterShowId ")
        val noMoreScrolling = currentAdapterShowId == mViewModel.lastId
        if (noMoreScrolling) {
            if (!networkOn()) showToast(R.string.no_internet)
            else {
                mViewModel.page++
                if (!mIsToolbarMenuSearch) {
                    activity_main__pagination_loading.visibility = View.VISIBLE
                    getShows()
                }
                Log.i(AppConfiguration.TAG, "onScrollChanged -> Calling removeOnScrollListener.")
                activity_main__recycler_view!!.removeOnScrollListener(mScrollListener)
            }
        }
    }
}
