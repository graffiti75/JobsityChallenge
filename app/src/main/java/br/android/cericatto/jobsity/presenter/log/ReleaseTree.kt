package br.android.cericatto.jobsity.presenter.log

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {}

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) return false
        return true // Only log WARN, ERROR, WTF
    }
}