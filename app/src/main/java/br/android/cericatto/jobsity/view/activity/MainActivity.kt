package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.view.View
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Shows
import br.android.cericatto.jobsity.presenter.utils.extensions.initApiService
import br.android.cericatto.jobsity.presenter.utils.extensions.networkOn
import br.android.cericatto.jobsity.presenter.utils.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.ShowsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setToolbar(R.id.id_toolbar,true, R.string.activity_main__title)
        MainApplication.service = initApiService()
        setDataListItems()
    }

    override fun onDestroy() {
        super.onDestroy()
        mComposite.dispose()
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun setDataListItems() {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service

            val observable = service.getShowsList()
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(
                    {
                        setAdapter(it)
                    },
                    {
                        Timber.i("setDataListItems() -> On error: $it")
                    },
                    {
                        Timber.i("setDataListItems() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun setAdapter(list: MutableList<Shows>) {
        activity_main__recycler_view.adapter = ShowsAdapter(list)

        activity_main__recycler_view.visibility = View.VISIBLE
        activity_main__loading.visibility = View.GONE
    }
}
