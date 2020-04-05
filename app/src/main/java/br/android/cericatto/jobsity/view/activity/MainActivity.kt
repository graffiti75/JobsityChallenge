package br.android.cericatto.jobsity.view.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Shows
import br.android.cericatto.jobsity.presenter.extensions.initApiService
import br.android.cericatto.jobsity.presenter.extensions.networkOn
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.ShowsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mSearchView: SearchView
    private var mShowsList: MutableList<Shows> = arrayListOf()

    private val mComposite = CompositeDisposable()

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbar(R.id.id_toolbar, titleId = R.string.activity_main__title)
        MainApplication.service = initApiService()
        setDataListItems()
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
                        mShowsList.clear()
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
    // Methods
    //--------------------------------------------------

    private fun setDataListItems() {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service

            val observable = service.getShowsList()
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        getShowsOnSuccess(it, false)
                    },
                    {
                        Timber.i("setDataListItems() -> On error: $it")
                    },
                    {
                        Timber.i("setDataListItems() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun searchShows(query: String) {
        val subscription = MainApplication.service.search(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val list: MutableList<Shows> = it.map { data -> data.show } as MutableList<Shows>
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

    private fun getShowsOnSuccess(list: MutableList<Shows>, searchPerformed: Boolean = true) {
        if (searchPerformed) {
            mShowsList.addAll(list)
            setAdapter(mShowsList)
        } else {
            setAdapter(list)
        }
        updateVisibilities(false)
    }

    private fun setAdapter(list: MutableList<Shows>) {
        activity_main__recycler_view.adapter = ShowsAdapter(this, list)
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
}
