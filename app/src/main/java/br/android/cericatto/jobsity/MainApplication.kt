package br.android.cericatto.jobsity

import android.app.Application
import br.android.cericatto.jobsity.model.retrofit.ApiService
import br.android.cericatto.jobsity.presenter.log.LineNumberDebugTree
import br.android.cericatto.jobsity.presenter.log.ReleaseTree
import timber.log.Timber

open class MainApplication : Application() {
    companion object {
        lateinit var service: ApiService
        var currentAdapterShowId = 0
    }

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) Timber.plant(LineNumberDebugTree())
        else Timber.plant(ReleaseTree())
    }
}