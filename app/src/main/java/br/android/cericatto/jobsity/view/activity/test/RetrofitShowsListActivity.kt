package br.android.cericatto.jobsity.view.activity.test

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.extensions.initApiService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_retrofit_shows_list.*

class RetrofitShowsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_retrofit_shows_list)

        val service = initApiService()
        val observable = service.getShowsList()
        val subscription = observable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    activity_retrofit_shows_list__result.text = it[0].name
                },
                {
                    activity_retrofit_shows_list__result.text = it.message
                },
                // OnCompleted
                {}
            )
        val composite = CompositeDisposable()
        composite.add(subscription)
    }
}