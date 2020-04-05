package br.android.cericatto.jobsity.presenter.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.BuildConfig
import br.android.cericatto.jobsity.model.ApiService
import br.android.cericatto.jobsity.presenter.utils.NavigationUtils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

//--------------------------------------------------
// Overall Methods
//--------------------------------------------------

fun Context.showToast(message: Int) {
    Toast.makeText(this, this.getString(message), Toast.LENGTH_LONG).show()
}

fun AppCompatActivity.openActivity(clazz: Class<*>) {
    val intent = Intent(this, clazz)
    this.startActivity(intent)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}

fun AppCompatActivity.openActivityExtras(clazz: Class<*>, key: String, value: Any) {
    val intent = Intent(this, clazz)
    val extras = getExtra(Bundle(), key, value)
    intent.putExtras(extras)

    this.startActivity(intent)
    NavigationUtils.animate(this, NavigationUtils.Animation.GO)
}

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

fun String.trimStartEnd(): String {
    var text = this.trimStart()
    return text.trimEnd()
}

fun Long.getDate(dateFormat: String): String? {
    val formatter = SimpleDateFormat(dateFormat)
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return formatter.format(calendar.time)
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