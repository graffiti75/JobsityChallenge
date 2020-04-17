package br.android.cericatto.jobsity.model.retrofit

import br.android.cericatto.jobsity.model.api.SearchShow
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TestService {
    @GET("/search/shows")
    fun search(
        @Query("q") keyword: String
    ): Call<MutableList<SearchShow>>
}