package br.android.cericatto.jobsity.view.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import br.android.cericatto.jobsity.model.Cache
import br.android.cericatto.jobsity.model.api.Shows

class ShowsViewModel(application: Application) : AndroidViewModel(application) {
    var favorites = mutableListOf<Shows>()
    var favoriteMenuClicked = false
    var lastId: Int = 0
    var page: Int = 0

    fun getShowsList(): MutableList<Shows> {
        return Cache.shows
    }

    fun setFavoritesList(list: MutableList<Shows>) {
        favorites = list
    }

    fun getFavoritesList(): MutableList<Shows> {
        return favorites
    }
}