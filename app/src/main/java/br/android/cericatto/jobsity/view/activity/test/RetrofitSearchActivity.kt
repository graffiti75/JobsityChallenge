package br.android.cericatto.jobsity.view.activity.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.OkHttpProvider
import br.android.cericatto.jobsity.model.api.SearchShow
import br.android.cericatto.jobsity.model.retrofit.TestService
import kotlinx.android.synthetic.main.activity_retrofit_search.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class RetrofitSearchActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrofit_search)

        val retrofit = Retrofit.Builder()
            .baseUrl(AppConfiguration.BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(OkHttpProvider.instance)
            .build()

        val githubService = retrofit.create(TestService::class.java)
        githubService.search(AppConfiguration.TEST_SEARCH_NAME).enqueue(object: Callback<MutableList<SearchShow>> {
            override fun onFailure(call: Call<MutableList<SearchShow>>, t: Throwable) {
                activity_retrofit_search__result.text = getString(R.string.error_searching_shows)
            }

            override fun onResponse(call: Call<MutableList<SearchShow>>, response: Response<MutableList<SearchShow>>) {
                if (response.isSuccessful) {
                    activity_retrofit_search__result.text = response.body()?.get(0)?.show?.name
                } else {
                    activity_retrofit_search__result.text = getString(R.string.error_searching_shows)
                }
            }
        })
    }
}
