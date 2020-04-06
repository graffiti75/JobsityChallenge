package br.android.cericatto.jobsity.view.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.model.Cache
import br.android.cericatto.jobsity.model.api.Shows
import kotlin.math.ceil

class ShowsViewModel(application: Application) : AndroidViewModel(application) {
    /*
     - The endpoint https://api.tvmaze.com/shows?page=0 contains 250 movies per page
     */
    var lastId: Int = 0
    var page: Int = 0
    var noMoreScrolling: Boolean = false

    fun getShowsList(): MutableList<Shows> {
        return Cache.shows
    }
}