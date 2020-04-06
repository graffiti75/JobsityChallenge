package br.android.cericatto.jobsity.model

import br.android.cericatto.jobsity.model.api.Shows

object Cache {
    var shows = mutableListOf<Shows>()

    fun cacheShows(newList: MutableList<Shows>) {
        this.shows.addAll(newList)
    }

    fun clearCacheShows() {
        this.shows.clear()
    }
}
