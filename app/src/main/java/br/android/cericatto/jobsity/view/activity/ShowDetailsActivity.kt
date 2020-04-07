package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.details.DetailsPresenterImpl

class ShowDetailsActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mDetailsPresenter: DetailsPresenterImpl

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_details)

        mDetailsPresenter = DetailsPresenterImpl(this)
        mDetailsPresenter.getExtras()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDetailsPresenter.dispose()
    }
}