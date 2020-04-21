package br.android.cericatto.jobsity.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import kotlinx.android.synthetic.main.activity_web_view.*

class WebViewActivity : ParentActivity() {

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        getExtras()
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun getExtras() {
        val extras = intent.extras
        val name = extras!!.getString(AppConfiguration.CURRENT_PERSON_EXTRA)
        val url = extras!!.getString(AppConfiguration.CURRENT_URL_EXTRA)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = name!!)
        if (!url.isNullOrEmpty()) {
            callWebView(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun callWebView(url: String) {
        val loading = activity_web_view__loading
        val webView = activity_web_view__web_view

        webView.visibility = View.VISIBLE
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = MyWebViewClient(loading)
        webView.loadUrl(url)
    }

    //--------------------------------------------------
    // WebView Client
    //--------------------------------------------------

    private class MyWebViewClient(val loading: View): WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loading.visibility = View.GONE
        }
    }
}