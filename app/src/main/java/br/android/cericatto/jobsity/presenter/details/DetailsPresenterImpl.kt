package br.android.cericatto.jobsity.presenter.details

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
import br.android.cericatto.jobsity.presenter.extensions.checkSpannedTextView
import br.android.cericatto.jobsity.presenter.extensions.getEmptyField
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
        mActivity.setToolbar(R.id.id_toolbar, homeEnabled = true, title = mCurrentShow.name!!)
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
        setTextViews()
        setImage()

        mActivity.activity_show_details__favorite_image_view.setOnClickListener {
            checkShowIsFavorite()
        }

        getEpisodes()
    }

    override fun setTextViews() {
        setSchedule()
        setGender()
        setSummary()
    }

    override fun setImage() {
        val imageView = mActivity.activity_show_details__image_view

        val imageNull = mCurrentShow.image == null
        val originalImageNull: Boolean
        val originalImageEmpty: Boolean
        if (!imageNull) {
            originalImageNull = mCurrentShow.image!!.original == null
            if (!originalImageNull) {
                originalImageEmpty = mCurrentShow.image!!.original!!.isEmpty()
                if (!originalImageEmpty) {
                    Glide.with(imageView)
                        .load(mCurrentShow.image!!.original)
                        .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                        .into(imageView)
                }
            }
        }
    }

    override fun setSchedule() {
        val scheduleDaysNull = mCurrentShow.schedule.days == null
        val scheduleTimeNull = mCurrentShow.schedule.time == null
        var scheduleDaysEmpty = true
        var scheduleTimeEmpty  = true
        if (!scheduleDaysNull) {
            scheduleDaysEmpty = mCurrentShow.schedule.days!!.isEmpty()
        }
        if (!scheduleTimeNull) {
            scheduleTimeEmpty  = mCurrentShow.schedule.time!!.isEmpty()
        }

        val invalidFields = scheduleDaysEmpty || scheduleTimeEmpty || scheduleDaysNull || scheduleTimeNull
        if (invalidFields) {
            mActivity.activity_show_details__schedule_days_text_view.text = mActivity.getEmptyField()
            mActivity.activity_show_details__schedule_time_text_view.visibility = View.GONE
        } else {
            mActivity.activity_show_details__schedule_days_text_view.text =
                mCurrentShow.schedule.days?.joinToString(separator = ", ") { it }
            mActivity.activity_show_details__schedule_time_text_view.text = mCurrentShow.schedule.time
        }
    }

    override fun setGender() {
        val genderNull = mCurrentShow.genres == null
        var genderEmpty = true
        if (!genderNull) {
            genderEmpty = mCurrentShow.genres!!.isEmpty()
        }

        val invalidFields = genderEmpty || genderNull
        if (invalidFields) {
            mActivity.activity_show_details__genres_text_view.text = mActivity.getEmptyField()
        } else {
            mActivity.activity_show_details__genres_text_view.text =
                mCurrentShow.genres?.joinToString(separator = ", ") { it }
        }
    }

    @Suppress("DEPRECATION")
    override fun setSummary() {
        mActivity.checkSpannedTextView(text = mCurrentShow.summary,
            textView = mActivity.activity_show_details__summary_text_view)
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
        mShowsDao = AppDatabase.getInstance(mActivity.applicationContext)
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

    override fun showLoading() {
        mActivity.activity_show_details__container.visibility = View.VISIBLE
        mActivity.activity_show_details__loading.visibility = View.GONE
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
        val listEmpty = list.isEmpty()
        if (listEmpty) {
            mActivity.activity_show_details__recycler_view.visibility = View.GONE
            mActivity.activity_show_details__empty_recycler_text_view.visibility = View.VISIBLE
        } else {
            mActivity.activity_show_details__recycler_view.visibility = View.VISIBLE
            mActivity.activity_show_details__empty_recycler_text_view.visibility = View.GONE
            setAdapter(list)
        }
        showLoading()
    }
}