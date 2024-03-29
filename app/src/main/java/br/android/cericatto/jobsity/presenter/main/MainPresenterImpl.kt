package br.android.cericatto.jobsity.presenter.main

import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Cache
import br.android.cericatto.jobsity.model.api.Shows
import br.android.cericatto.jobsity.model.db.AppDatabase
import br.android.cericatto.jobsity.model.db.ShowsDao
import br.android.cericatto.jobsity.presenter.extensions.*
import br.android.cericatto.jobsity.view.activity.MainActivity
import br.android.cericatto.jobsity.view.activity.PersonSearchActivity
import br.android.cericatto.jobsity.view.adapter.ShowsAdapter
import br.android.cericatto.jobsity.view.viewmodel.ShowsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainPresenterImpl(activity: MainActivity) : MainPresenter {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    var mActivity = activity

    private lateinit var mSearchView: SearchView
    private var mIsToolbarMenuSearch = false

    private lateinit var mShowsAdapter: ShowsAdapter
    private var mShowsList: MutableList<Shows> = arrayListOf()

    private val mComposite = CompositeDisposable()

    private lateinit var mShowName: String
    private lateinit var mViewModel: ShowsViewModel
    private lateinit var mShowsDao: ShowsDao

    private lateinit var mScrollListener: RecyclerView.OnScrollListener

    //--------------------------------------------------
    // Override Methods
    //--------------------------------------------------

    /*
     * Init Methods.
     */

    override fun init(savedInstanceState: Bundle?) {
        MainApplication.service = mActivity.initApiService()
        mViewModel = ViewModelProviders.of(mActivity).get(ShowsViewModel::class.java)

        initDatabase()
        mActivity.setToolbar(R.id.id_toolbar, titleId = R.string.activity_main__title)
        checkSavedInstanceState(savedInstanceState)
    }

    override fun dispose() {
        mComposite.dispose()
    }

    /*
     * Menu Methods.
     */

    override fun initMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.search)
        mSearchView = searchItem.actionView as SearchView
        mSearchView.queryHint = mActivity.getString(R.string.search_hint)
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                try {
                    if (!mActivity.networkOn()) mActivity.showToast(R.string.no_internet)
                    else {
                        showLoading()
                        clearMoviesList(query)
                        searchShows(query)
                    }
                } catch (e: Exception) {
                    mActivity.showToast(R.string.search_error)
                }
                searchItem.collapseActionView()
                return false
            }
            override fun onQueryTextChange(newText: String) = true
        })
    }

    override fun checkOptionsItemSelected(item: MenuItem) {
        when (item.itemId) {
            R.id.update -> {
                mActivity.getViewModel().favoriteMenuClicked = false
                mActivity.getViewModel().searchMenuClicked = false
                hideSearchView()
                mShowsList.clear()
                showEmptyRecyclerView(false)
                showLoading()
                getShows()
            }
            R.id.favorites -> {
                showLoading()
                mActivity.getViewModel().favoriteMenuClicked = true
                mActivity.getViewModel().searchMenuClicked = false
                hideSearchView()
                getFavorites()
            }
            R.id.people_search -> {
                mActivity.openActivity(PersonSearchActivity::class.java)
            }
        }
    }

    override fun hideSearchView() {
        mSearchView.isIconified = true
        mSearchView.onActionViewCollapsed()
    }

    override fun getFavorites() {
        showLoading()
        Handler().postDelayed({
            val list = mActivity.getViewModel().getFavoritesList()
            if (list.isEmpty()) {
                showEmptyRecyclerView()
            } else {
                showEmptyRecyclerView(false)
                mShowsList.clear()
                mShowsList.addAll(list)
                setAdapter(mShowsList)
                showLoading(false)
            }
        }, 1000)
    }

    override fun checkSavedInstanceState(savedInstanceState: Bundle?) {
        mActivity.mListState = savedInstanceState?.getParcelable(MainActivity.LIST_POSITION_STATE)

        /*
         * Get shows from API.
         */
        if (savedInstanceState == null) {
            getShows()
        }

        /*
         * Get cached shows from ViewModel.
         */
        else {
            val viewModel = ViewModelProviders
                .of(mActivity)
                .get(ShowsViewModel::class.java)
            val moviesList = viewModel.getShowsList()
            showCachedMovies(moviesList)
        }
    }

    /*
     * Data Response Methods.
     */

    override fun showCachedMovies(list: MutableList<Shows>) {
        if (list.isNotEmpty()) mShowsList = list

        if (mActivity.activity_main__recycler_view.adapter == null) {
            setAdapter(mShowsList)
            mActivity.activity_main__recycler_view.adapter = mShowsAdapter
        } else {
            mShowsAdapter.notifyDataSetChanged()
        }
        mActivity.activity_main__recycler_view.layoutManager!!.onRestoreInstanceState(mActivity.mListState)
        setRecyclerViewScrollListener()
        showLoading(false)
    }

    override fun getShows() {
        if (!mActivity.networkOn()) mActivity.showToast(R.string.no_internet)
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

    override fun searchShows(query: String) {
        val subscription = MainApplication.service.search(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val list: MutableList<Shows> = it.map {
                        data -> data.show } as MutableList<Shows>
                    mIsToolbarMenuSearch = true
                    mActivity.getViewModel().searchMenuClicked = true
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
    }

    /*
     * Data Decision Methods.
     */

    override fun clearMoviesList(query: String) {
        mShowName = query
        mShowsList.clear()
        Cache.clearCacheShows()
    }

    override fun getShowsOnSuccess(list: MutableList<Shows>, searchPerformed: Boolean) {
        if (searchPerformed) {
            mShowsList.addAll(list)
            setAdapter(mShowsList)
        } else {
            setRecyclerViewScrollListener()
            if (mViewModel.page > 0) {
                mShowsAdapter.updateList(list)
            } else {
                mShowsList.addAll(list)
                setAdapter(mShowsList)
            }
        }
        mActivity.activity_main__pagination_loading.visibility = View.GONE
        Cache.cacheShows(mShowsList)
        showLoading(false)
    }

    override fun setAdapter(list: MutableList<Shows>) {
        mShowsAdapter = ShowsAdapter(mActivity, list)
        mActivity.activity_main__recycler_view.adapter = mShowsAdapter
    }

    override fun initDatabase() {
        mShowsDao = AppDatabase.getInstance(mActivity.applicationContext)
        mShowsDao.getFavorites().observe(mActivity, Observer {
            if (it.isNotEmpty()) {
                mActivity.getViewModel().setFavoritesList(it)
                if (mActivity.getViewModel().favoriteMenuClicked) {
                    if (!mActivity.getViewModel().searchMenuClicked) {
                        getFavorites()
                    }
                }
            } else {
                mActivity.getViewModel().setFavoritesList(it)
                if (mActivity.getViewModel().favoriteMenuClicked) {
                    if (!mActivity.getViewModel().searchMenuClicked) {
                        showEmptyRecyclerView()
                    }
                }
            }
        })
    }

    /*
     * Loading Methods.
     */

    override fun showLoading(loading: Boolean) {
        if (loading) {
            mActivity.activity_main__recycler_view.visibility = View.GONE
            mActivity.activity_main__loading.visibility = View.VISIBLE
        } else {
            mActivity.activity_main__recycler_view.visibility = View.VISIBLE
            mActivity.activity_main__loading.visibility = View.GONE
        }
    }

    override fun showEmptyRecyclerView(empty: Boolean) {
        if (empty) {
            mActivity.activity_main__recycler_view.visibility = View.GONE
            mActivity.activity_main__empty_recycler_view.visibility = View.VISIBLE
        } else {
            mActivity.activity_main__recycler_view.visibility = View.VISIBLE
            mActivity.activity_main__empty_recycler_view.visibility = View.GONE
        }
    }

    /*
     * Pagination Methods.
     */

    override fun setRecyclerViewScrollListener() {
        mScrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                onScrollChanged()
            }
        }
        mActivity.activity_main__recycler_view.addOnScrollListener(mScrollListener)
    }

    override fun onScrollChanged() {
        val currentAdapterShowId = MainApplication.currentAdapterShowId
        val noMoreScrolling = currentAdapterShowId == mViewModel.lastId
        if (noMoreScrolling) {
            if (!mActivity.networkOn()) mActivity.showToast(R.string.no_internet)
            else {
                mViewModel.page++
                if (!mIsToolbarMenuSearch) {
                    mActivity.activity_main__pagination_loading.visibility = View.VISIBLE
                    getShows()
                }
                mActivity.activity_main__recycler_view!!.removeOnScrollListener(mScrollListener)
            }
        }
    }
}