package br.android.cericatto.jobsity.view.activity

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import br.android.cericatto.jobsity.presenter.utils.NavigationUtils

open class ParentActivity : AppCompatActivity() {

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onBackPressed() {
        super.onBackPressed()
        NavigationUtils.animate(this, NavigationUtils.Animation.BACK)
    }

    //--------------------------------------------------
    // Menu
    //--------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    fun setToolbar(toolbarId: Int, homeEnabled: Boolean = false) {
        setSupportActionBar(findViewById(toolbarId))
        supportActionBar?.setDisplayHomeAsUpEnabled(homeEnabled)
    }

    fun setToolbar(toolbarId: Int, homeEnabled: Boolean = false, titleId: Int) {
        setSupportActionBar(findViewById(toolbarId))
        supportActionBar?.setDisplayHomeAsUpEnabled(homeEnabled)
        supportActionBar?.title = getString(titleId)
    }

    fun setToolbar(toolbarId: Int, homeEnabled: Boolean = false, title: String) {
        setSupportActionBar(findViewById(toolbarId))
        supportActionBar?.setDisplayHomeAsUpEnabled(homeEnabled)
        supportActionBar?.title = title
    }
}