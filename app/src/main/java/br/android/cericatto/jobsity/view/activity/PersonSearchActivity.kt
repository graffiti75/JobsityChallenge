package br.android.cericatto.jobsity.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Person
import br.android.cericatto.jobsity.presenter.extensions.networkOn
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.PeopleAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_person_search.*
import timber.log.Timber

@SuppressLint("LogNotTimber")
class PersonSearchActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mSearchView: SearchView
    private val mComposite = CompositeDisposable()

    private lateinit var mPeopleAdapter: PeopleAdapter
    private var mPeopleList: MutableList<Person> = arrayListOf()
    private lateinit var mPersonName: String

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_search)

        setToolbar(toolbarId = R.id.id_toolbar, homeEnabled = true,
            titleId = R.string.activity_person_search__title)
    }

    override fun onDestroy() {
        super.onDestroy()
        dispose()
    }

    private fun dispose() {
        mComposite.dispose()
    }

    //--------------------------------------------------
    // Menu Methods
    //--------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_person, menu)
        initMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun initMenu(menu: Menu) {
        val searchItem = menu.findItem(R.id.person_search)
        mSearchView = searchItem.actionView as SearchView
        mSearchView.queryHint = getString(R.string.person_search_hint)
        mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                try {
                    if (!networkOn()) showToast(R.string.no_internet)
                    else {
                        showLoading()
                        Handler().postDelayed({
                            activity_person_search__empty_recycler_view.visibility = View.GONE
                            clearPeopleList(query)
                            searchPeople(query)
                        }, 500)
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
    // Data Decision Methods
    //--------------------------------------------------

    private fun clearPeopleList(query: String) {
        mPersonName = query
        mPeopleList.clear()
    }

    private fun searchPeople(query: String) {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service
            val observable = service.searchPeople(query)
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        val list: MutableList<Person> = it.map {
                            data -> data.person } as MutableList<Person>
                        getPeopleOnSuccess(list)
                    },
                    {
                        Timber.i("searchPeople() -> On error: $it")
                    },
                    {
                        Timber.i("searchPeople() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun getPeopleOnSuccess(list: MutableList<Person>) {
        if (list.isEmpty()) {
            showEmptyRecyclerView()
        } else {
            mPeopleList.addAll(list)
            setAdapter(mPeopleList)
        }
        showLoading(false)
    }

    private fun setAdapter(list: MutableList<Person>) {
        mPeopleAdapter = PeopleAdapter(this, list)
        activity_person_search__recycler_view.adapter = mPeopleAdapter
    }

    //--------------------------------------------------
    // Loading Methods
    //--------------------------------------------------

    private fun showLoading(loading: Boolean = true) {
        Log.i(AppConfiguration.TAG, "showLoading() -> loading: $loading.")
        if (loading) {
            activity_person_search__recycler_view.visibility = View.GONE
            activity_person_search__loading.visibility = View.VISIBLE
        } else {
            activity_person_search__recycler_view.visibility = View.VISIBLE
            activity_person_search__loading.visibility = View.GONE
        }
    }

    private fun showEmptyRecyclerView(empty: Boolean = true) {
        Log.i(AppConfiguration.TAG, "showEmptyRecyclerView() -> empty: $empty.")
        if (empty) {
            activity_person_search__recycler_view.visibility = View.GONE
            activity_person_search__empty_recycler_view.visibility = View.VISIBLE
        } else {
            activity_person_search__recycler_view.visibility = View.VISIBLE
            activity_person_search__empty_recycler_view.visibility = View.GONE
        }
    }
}