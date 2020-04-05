package br.android.cericatto.jobsity.model

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/shows")
    fun getShowsList(
        @Query("page") page: Int = 0
    ): Observable<MutableList<Shows>>
}