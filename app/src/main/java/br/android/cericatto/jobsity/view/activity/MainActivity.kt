package br.android.cericatto.jobsity.view.activity

import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuItem
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.main.MainPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : ParentActivity() {

    //--------------------------------------------------
    // Constants
    //--------------------------------------------------

    companion object {
        const val LIST_POSITION_STATE = "list_position_state"
    }

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mMainPresenter: MainPresenterImpl
    var mListState: Parcelable? = null

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMainPresenter = MainPresenterImpl(this)
        mMainPresenter.init(savedInstanceState)
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(LIST_POSITION_STATE, activity_main__recycler_view.layoutManager?.onSaveInstanceState())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppConfiguration.MAIN_TO_SHOW_DETAILS_CODE) {
            mMainPresenter.showLoading(false)
            mMainPresenter.hideSearchView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMainPresenter.dispose()
    }

    //--------------------------------------------------
    // Menu Methods
    //--------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        initMenu(menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        mMainPresenter.checkOptionsItemSelected(item)
        return super.onOptionsItemSelected(item)
    }

    private fun initMenu(menu: Menu) {
        mMainPresenter.initMenu(menu)
    }
}