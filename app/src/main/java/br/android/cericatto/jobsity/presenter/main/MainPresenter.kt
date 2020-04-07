package br.android.cericatto.jobsity.presenter.main

import android.os.Bundle
import android.view.Menu
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
    fun updateVisibilities(loading: Boolean = true)

    /*
     * Pagination Methods.
     */

    fun setRecyclerViewScrollListener()
    fun onScrollChanged()
}