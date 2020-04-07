package br.android.cericatto.jobsity.presenter.details

import br.android.cericatto.jobsity.model.api.Episode

interface DetailsPresenter {

    /*
     * Main Methods.
     */

    fun getExtras()
    fun setDetailsData()
    fun dispose()

    /*
     * Layout Methods.
     */

    fun initLayout()
    fun setTextViews()
    fun updateDrawable()
    fun setAdapter(list: MutableList<Episode>)

    /*
     * Database Methods.
     */

    fun initDatabase()
    fun checkCurrentShow()
    fun checkShowIsFavorite()
    fun updateVisibilities(loading: Boolean = true)

    /*
     * Episode Methods.
     */

    fun getEpisodes()
    fun getEpisodesOnSuccess(list: MutableList<Episode>)
}