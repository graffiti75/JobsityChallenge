package br.android.cericatto.jobsity.presenter.details

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
import br.android.cericatto.jobsity.view.activity.ShowDetailsActivity
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

class DetailsPresenterImpl(activity: ShowDetailsActivity) : DetailsPresenter {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    var mActivity = activity

    private val mComposite = CompositeDisposable()
    private var mShowId = 0

    private lateinit var mDatabase: AppDatabase
    private lateinit var mShowsDao: ShowsDao
    private var mFavorite = false
    private lateinit var mCurrentShow: Shows

    //--------------------------------------------------
    // Override Methods
    //--------------------------------------------------

    /*
     * Main Methods.
     */

    override fun getExtras() {
        val extras = mActivity.intent.extras
        val json = extras!!.getString(AppConfiguration.CURRENT_SHOW_EXTRA)
        val jsonType = object : TypeToken<Shows>() {}.type
        mCurrentShow = Gson().fromJson(json, jsonType)
        mActivity.setToolbar(R.id.id_toolbar, homeEnabled = true, title = mCurrentShow.name)
        mShowId = mCurrentShow.id
        setDetailsData()
    }

    override fun setDetailsData() {
        initDatabase()
        checkCurrentShow()
        initLayout()
    }

    override fun dispose() {
        mComposite.dispose()
    }

    /*
     * Layout Methods.
     */

    override fun initLayout() {
        Glide.with(mActivity.activity_show_details__image_view)
            .load(mCurrentShow.image.original)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(mActivity.activity_show_details__image_view)

        setTextViews()

        mActivity.activity_show_details__favorite_image_view.setOnClickListener {
            checkShowIsFavorite()
        }

        getEpisodes()
    }

    override fun setTextViews() {
        mActivity.activity_show_details__name_text_view.text = mCurrentShow.name

        mActivity.activity_show_details__schedule_days_text_view.text =
            mCurrentShow.schedule.days?.joinToString(separator = ", ") { it }

        if (mCurrentShow.schedule.time.isEmpty()) {
            mActivity.activity_show_details__schedule_time_text_view.visibility = View.GONE
        } else {
            mActivity.activity_show_details__schedule_time_text_view.text = mCurrentShow.schedule.time
        }

        mActivity.activity_show_details__genres_text_view.text =
            mCurrentShow.genres?.joinToString(separator = ", ") { it }
        mActivity.activity_show_details__summary_text_view.text = Html.fromHtml(mCurrentShow.summary)
    }

    override fun updateDrawable() {
        var drawableId = R.drawable.ic_favorite_border
        if (mFavorite) {
            drawableId = R.drawable.ic_favorite
        }
        mActivity.activity_show_details__favorite_image_view.setBackgroundResource(drawableId)
    }

    override fun setAdapter(list: MutableList<Episode>) {
        mActivity.activity_show_details__recycler_view.adapter = EpisodesAdapter(mActivity, list)
    }

    /*
     * Database Methods.
     */

    override fun initDatabase() {
        mDatabase = AppDatabase.getInstance(mActivity.applicationContext)
        mShowsDao = mDatabase.showsDao()
    }

    override fun checkCurrentShow() {
        mShowsDao.getShow(mCurrentShow.id).observe(mActivity, Observer {
            if (it != null) {
                mFavorite = it.favorite
                mCurrentShow = it
                updateDrawable()
            }
        })
    }

    override fun checkShowIsFavorite() {
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

    override fun updateVisibilities(loading: Boolean) {
        if (loading) {
            mActivity.activity_show_details__container.visibility = View.GONE
            mActivity.activity_show_details__loading.visibility = View.VISIBLE
        } else {
            mActivity.activity_show_details__container.visibility = View.VISIBLE
            mActivity.activity_show_details__loading.visibility = View.GONE
        }
    }

    /*
     * Episode Methods.
     */

    override fun getEpisodes() {
        if (!mActivity.networkOn()) mActivity.showToast(R.string.no_internet)
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

    override fun getEpisodesOnSuccess(list: MutableList<Episode>) {
        setAdapter(list)
        updateVisibilities(false)
    }
}