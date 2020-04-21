package br.android.cericatto.jobsity.model.retrofit

import br.android.cericatto.jobsity.model.api.*
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("/shows")
    fun getShowsList(
        @Query("page") page: Int = 0
    ): Observable<MutableList<Shows>>

    @GET("/search/shows")
    fun search(
        @Query("q") keyword: String
    ): Observable<MutableList<SearchShow>>

    @GET("/shows/{showId}/episodes")
    fun getEpisodes(
        @Path("showId") showId: Int
    ): Observable<MutableList<Episode>>

    @GET("/search/people")
    fun searchPeople(
        @Query("q") keyword: String
    ): Observable<MutableList<SearchPeople>>

    @GET("/people/{peopleId}/castcredits")
    fun castCredits(
        @Path("peopleId") peopleId: String
    ): Observable<MutableList<CastCredits>>

    @GET("/shows/{showId}")
    fun getShow(
        @Path("showId") showId: String
    ): Observable<Shows>
}