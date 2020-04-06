package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Episode
import br.android.cericatto.jobsity.model.api.Shows
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

class ShowDetailsActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()
    private var mShowId = 0
    private var mFavorite = false

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
    // Methods
    //--------------------------------------------------

    private fun getExtras() {
        val extras = intent.extras
        val json = extras!!.getString(AppConfiguration.CURRENT_SHOW_EXTRA)
        val jsonType = object : TypeToken<Shows>(){}.type
        var currentShow: Shows = Gson().fromJson(json, jsonType)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = currentShow.name)
        mShowId = currentShow.id
        initLayout(currentShow)
    }

    private fun initLayout(currentShow: Shows) {
        Glide.with(activity_show_details__image_view)
            .load(currentShow.image.original)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(activity_show_details__image_view)

        activity_show_details__name_text_view.text = currentShow.name

        activity_show_details__schedule_days_text_view.text = currentShow.schedule.days?.joinToString(separator = ", ") { it }

        if (currentShow.schedule.time.isEmpty()) {
            activity_show_details__schedule_time_text_view.visibility = View.GONE
        } else {
            activity_show_details__schedule_time_text_view.text = currentShow.schedule.time
        }

        activity_show_details__genres_text_view.text = currentShow.genres?.joinToString(separator = ", ") { it }
        activity_show_details__summary_text_view.text = Html.fromHtml(currentShow.summary)

        activity_show_details__favorite_image_view.setOnClickListener {
            mFavorite = !mFavorite
            var drawableId = R.drawable.ic_favorite_border
            if (mFavorite) {
                drawableId = R.drawable.ic_favorite
            }
            it.setBackgroundResource(drawableId)
        }

        getEpisodes()
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
    // Episodes Methods
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
//        Handler().postDelayed({
            updateVisibilities(false)
//        }, 2000)
    }

    private fun setAdapter(list: MutableList<Episode>) {
        activity_show_details__recycler_view.adapter = EpisodesAdapter(this, list)
    }
}
