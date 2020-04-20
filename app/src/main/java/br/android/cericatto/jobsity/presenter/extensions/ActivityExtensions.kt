package br.android.cericatto.jobsity.presenter.extensions

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import android.view.animation.Animation
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.BuildConfig
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.retrofit.ApiService
import br.android.cericatto.jobsity.presenter.utils.NavigationUtils
import br.android.cericatto.jobsity.view.viewmodel.ShowsViewModel
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

//--------------------------------------------------
// Overall Methods
//--------------------------------------------------

fun AppCompatActivity.getViewModel(): ShowsViewModel {
    return ViewModelProviders.of(this).get(ShowsViewModel::class.java)
}

fun AppCompatActivity.initViewAnimation(view: View, animation: Animation) {
    view.startAnimation(animation)
}

fun AppCompatActivity.initViewAnimation(viewId: Int, animation: Animation) {
    findViewById<View>(viewId).startAnimation(animation)
}

fun Context.showToast(message: Int) {
    Toast.makeText(this, this.getString(message), Toast.LENGTH_LONG).show()
}

/*
fun String.trimStartEnd(): String {
    val text = this.trimStart()
    return text.trimEnd()
}
 */

fun Context.checkTextView(text: String?, textView: TextView) {
    val fieldNull = text == null
    var fieldEmpty = true
    if (!fieldNull) {
        if (text != null) {
            fieldEmpty = text.isEmpty()
        }
    }
    if (fieldNull || fieldEmpty) {
        textView.text = this.getEmptyField()
    } else {
        textView.text = text
    }
}

@Suppress("DEPRECATION")
fun Context.checkSpannedTextView(text: String?, textView: TextView) {
    val fieldNull = text == null
    var fieldEmpty = true
    if (!fieldNull) {
        if (text != null) {
            fieldEmpty = text.isEmpty()
        }
    }
    if (fieldNull || fieldEmpty) {
        textView.text = this.getEmptyField()
    } else {
        textView.text = Html.fromHtml(text)
    }
}

fun Context.getEmptyField(): String {
    return this.getString(R.string.activity_show_details__empty_field)
}

//--------------------------------------------------
// Intent Methods
//--------------------------------------------------

fun AppCompatActivity.openActivity(clazz: Class<*>) {
    val intent = Intent(this, clazz)
    this.startActivity(intent)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}

fun AppCompatActivity.openActivityForResult(code: Int, clazz: Class<*>) {
    val intent = Intent(this, clazz)
    this.startActivityForResult(intent, code)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}

/*
fun AppCompatActivity.openActivityExtras(clazz: Class<*>, key: String, value: Any) {
    val intent = Intent(this, clazz)
    val extras = getExtra(Bundle(), key, value)
    intent.putExtras(extras)

    this.startActivity(intent)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}
 */

fun AppCompatActivity.openActivityForResultWithExtras(clazz: Class<*>, code: Int, key: String, value: Any) {
    val intent = Intent(this, clazz)
    val extras = getExtra(Bundle(), key, value)
    intent.putExtras(extras)

    this.startActivityForResult(intent, code)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}

private fun getExtra(extras: Bundle, key: String, value: Any): Bundle {
    when (value) {
        is String -> extras.putString(key, value)
        is Int -> extras.putInt(key, value)
        is Long -> extras.putLong(key, value)
        is Boolean -> extras.putBoolean(key, value)
    }
    return extras
}

@Suppress("DEPRECATION")
fun Context.networkOn(): Boolean {
    var result = false
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        cm?.run {
            cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                result = when {
                    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                    else -> false
                }
            }
        }
    } else {
        cm?.run {
            cm.activeNetworkInfo?.run {
                if (type == ConnectivityManager.TYPE_WIFI) {
                    result = true
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    result = true
                }
            }
        }
    }
    return result
}

//--------------------------------------------------
// Retrofit Methods
//--------------------------------------------------

/*
fun Context.initTestService(): TestService {
    return initRetrofit().create(TestService::class.java)
}
 */

fun Context.initApiService(): ApiService {
    return initRetrofit().create(ApiService::class.java)
}

fun Context.initRetrofit(): Retrofit {
    return Retrofit.Builder()
        .baseUrl(AppConfiguration.BASE_URL)
        .addConverterFactory(initConverterFactory())
        .addCallAdapterFactory(initAdapterFactory())
        .client(initOkHttpClient())
        .build()
}

fun initConverterFactory(): MoshiConverterFactory {
    return MoshiConverterFactory.create()
}

fun initAdapterFactory(): RxJava2CallAdapterFactory {
    return RxJava2CallAdapterFactory.create()
}

private fun Context.initOkHttpClient(): OkHttpClient {
    val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Timber.d(message)
        }
    })

    logging.level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .addNetworkInterceptor(provideCacheInterceptor())
        .cache(provideCache())
        .build()
}

fun Context.provideCache(): Cache? {
    var cache: Cache? = null
    try {
        cache = Cache(File(cacheDir, "http-cache"), (10 * 1024 * 1024).toLong()) // 10 MB
    } catch (e: Exception) {
        Timber.e(e, "Could not create Cache!")
    }
    return cache
}

fun provideCacheInterceptor(): Interceptor {
    return object : Interceptor {
        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(chain.request())

            // re-write response header to force use of cache
            val cacheControl = CacheControl.Builder()
                .maxAge(10, TimeUnit.SECONDS)
                .build()

            return response.newBuilder()
                .header("Cache-Control", cacheControl.toString())
                .build()
        }
    }
}