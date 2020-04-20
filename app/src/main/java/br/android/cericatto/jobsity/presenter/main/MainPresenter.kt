package br.android.cericatto.jobsity.presenter.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import br.android.cericatto.jobsity.model.api.Shows

interface MainPresenter {

    /*
     * Init Methods.
     */

    fun init(savedInstanceState: Bundle?)
    fun dispose()

    /*
     * Menu Methods.
     */

    fun initMenu(menu: Menu)
    fun checkOptionsItemSelected(item: MenuItem)
    fun hideSearchView()
    fun getFavorites()
    fun checkSavedInstanceState(savedInstanceState: Bundle?)

    /*
     * Data Response Methods.
     */

    fun showCachedMovies(list: MutableList<Shows>)
    fun getShows()
    fun searchShows(query: String)

    /*
     * Data Decision Methods.
     */

    fun clearMoviesList(query: String)
    fun getShowsOnSuccess(list: MutableList<Shows>, searchPerformed: Boolean = true)
    fun setAdapter(list: MutableList<Shows>)
    fun initDatabase()
    fun showLoading(loading: Boolean = true)
    fun showEmptyRecyclerView(empty: Boolean = true)

    /*
     * Pagination Methods.
     */

    fun setRecyclerViewScrollListener()
    fun onScrollChanged()
}