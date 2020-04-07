package br.android.cericatto.jobsity.view.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.lifecycle.Observer
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Episode
import br.android.cericatto.jobsity.model.api.Shows
import br.android.cericatto.jobsity.model.db.AppDatabase
import br.android.cericatto.jobsity.model.db.AppExecutors
import br.android.cericatto.jobsity.model.db.ShowsDao
import br.android.cericatto.jobsity.presenter.extensions.networkOn
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.EpisodesAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_show_details.*
import timber.log.Timber

@SuppressLint("LogNotTimber")
class ShowDetailsActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()
    private var mShowId = 0

    private lateinit var mDatabase: AppDatabase
    private lateinit var mShowsDao: ShowsDao
    private var mFavorite = false
    private lateinit var mCurrentShow: Shows

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_details)

        getExtras()
    }

    override fun onDestroy() {
        super.onDestroy()
        mComposite.dispose()
    }

    //--------------------------------------------------
    // Main Methods
    //--------------------------------------------------

    private fun getExtras() {
        val extras = intent.extras
        val json = extras!!.getString(AppConfiguration.CURRENT_SHOW_EXTRA)
        val jsonType = object : TypeToken<Shows>() {}.type
        mCurrentShow = Gson().fromJson(json, jsonType)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = mCurrentShow.name)
        mShowId = mCurrentShow.id
        setDetailsData()
    }

    private fun setDetailsData() {
        initDatabase()
        checkCurrentShow()
        initLayout()
    }

    //--------------------------------------------------
    // Layout Methods
    //--------------------------------------------------

    private fun initLayout() {
        Glide.with(activity_show_details__image_view)
            .load(mCurrentShow.image.original)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(activity_show_details__image_view)

        setTextViews()

        activity_show_details__favorite_image_view.setOnClickListener {
            checkShowIsFavorite()
        }

        getEpisodes()
    }

    private fun setTextViews() {
        activity_show_details__name_text_view.text = mCurrentShow.name

        activity_show_details__schedule_days_text_view.text =
            mCurrentShow.schedule.days?.joinToString(separator = ", ") { it }

        if (mCurrentShow.schedule.time.isEmpty()) {
            activity_show_details__schedule_time_text_view.visibility = View.GONE
        } else {
            activity_show_details__schedule_time_text_view.text = mCurrentShow.schedule.time
        }

        activity_show_details__genres_text_view.text =
            mCurrentShow.genres?.joinToString(separator = ", ") { it }
        activity_show_details__summary_text_view.text = Html.fromHtml(mCurrentShow.summary)
    }

    private fun updateDrawable() {
        var drawableId = R.drawable.ic_favorite_border
        if (mFavorite) {
            drawableId = R.drawable.ic_favorite
        }
        activity_show_details__favorite_image_view.setBackgroundResource(drawableId)
    }

    private fun setAdapter(list: MutableList<Episode>) {
        activity_show_details__recycler_view.adapter = EpisodesAdapter(this, list)
    }

    //--------------------------------------------------
    // Database Methods
    //--------------------------------------------------

    private fun initDatabase() {
        mDatabase = AppDatabase.getInstance(applicationContext)
        mShowsDao = mDatabase.showsDao()
    }

    private fun checkCurrentShow() {
        mShowsDao.getShow(mCurrentShow.id).observe(this, Observer {
            if (it != null) {
                mFavorite = it.favorite
                mCurrentShow = it
                updateDrawable()
            }
        })
    }

    private fun checkShowIsFavorite() {
        if (!mFavorite) {
            AppExecutors.instance?.diskIO()?.execute {
                mShowsDao.insert(mCurrentShow.copy(favorite = !mFavorite))
            }
        } else {
            AppExecutors.instance?.diskIO()?.execute {
                mShowsDao.delete(mCurrentShow)
                mFavorite = !mFavorite
                updateDrawable()
            }
        }
    }

    private fun updateVisibilities(loading: Boolean = true) {
        if (loading) {
            activity_show_details__container.visibility = View.GONE
            activity_show_details__loading.visibility = View.VISIBLE
        } else {
            activity_show_details__container.visibility = View.VISIBLE
            activity_show_details__loading.visibility = View.GONE
        }
    }

    //--------------------------------------------------
    // Episode Methods
    //--------------------------------------------------

    private fun getEpisodes() {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service
            val observable = service.getEpisodes(mShowId)
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        getEpisodesOnSuccess(it)
                    },
                    {
                        Timber.i("getEpisodes() -> On error: $it")
                    },
                    {
                        Timber.i("getEpisodes() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun getEpisodesOnSuccess(list: MutableList<Episode>) {
        setAdapter(list)
        updateVisibilities(false)
    }
}