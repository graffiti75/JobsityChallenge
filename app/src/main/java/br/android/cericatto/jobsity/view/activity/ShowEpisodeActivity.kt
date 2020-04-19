package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.os.Handler
import android.view.View
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Episode
import br.android.cericatto.jobsity.presenter.extensions.checkSpannedTextView
import br.android.cericatto.jobsity.presenter.extensions.checkTextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_show_episode.*

class ShowEpisodeActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()
    private lateinit var mCurrentEpisode: Episode

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_episode)

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
        val json = extras!!.getString(AppConfiguration.CURRENT_EPISODE_EXTRA)
        val jsonType = object : TypeToken<Episode>(){}.type
        mCurrentEpisode = Gson().fromJson(json, jsonType)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = mCurrentEpisode.name!!)
        initLayout()
    }

    private fun initLayout() {
        setNumber()
        setSeason()
        setSummary()
        setImage()

        Handler().postDelayed({
            updateVisibilities()
        }, 1000)
    }

    private fun setImage() {
        val imageView = activity_show_episode__image_view

        val imageNull = mCurrentEpisode.image == null
        val originalImageNull: Boolean
        val originalImageEmpty: Boolean
        if (!imageNull) {
            originalImageNull = mCurrentEpisode.image!!.original == null
            if (!originalImageNull) {
                originalImageEmpty = mCurrentEpisode.image!!.original!!.isEmpty()
                if (originalImageEmpty) {
                    Glide.with(imageView)
                        .load(mCurrentEpisode.image!!.original)
                        .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                        .into(imageView)
                }
            }
        }
    }

    private fun setNumber() {
        checkTextView(text = mCurrentEpisode.number.toString(),
            textView = activity_show_episode__number_text_view)
        /*
        val numberEmpty = currentEpisode.number.toString().isEmpty()
        if (numberEmpty) {
            activity_show_episode__number_text_view.text = getEmptyField()
        } else {
            activity_show_episode__number_text_view.text = currentEpisode.number.toString()
        }
         */
    }

    private fun setSeason() {
        checkTextView(text = mCurrentEpisode.season.toString(),
            textView = activity_show_episode__season_text_view)
        /*
        val seasonEmpty = currentEpisode.season.toString().isEmpty()
        if (seasonEmpty) {
            activity_show_episode__season_text_view.text = getEmptyField()
        } else {
            activity_show_episode__season_text_view.text = currentEpisode.season.toString()
        }
         */
    }

    private fun setSummary() {
        checkSpannedTextView(text = mCurrentEpisode.summary,
            textView = activity_show_episode__summary_text_view)
        /*
        val summaryNull = currentEpisode.summary == null
        var summaryEmpty = true
        if (!summaryNull) {
            summaryEmpty = currentEpisode.summary.isEmpty()
        }
        if (summaryNull || summaryEmpty) {
            activity_show_episode__summary_text_view.text = getEmptyField()
        } else {
            activity_show_episode__summary_text_view.text = Html.fromHtml(currentEpisode.summary)
        }
         */
    }

    private fun updateVisibilities() {
        activity_show_episode__container.visibility = View.VISIBLE
        activity_show_episode__loading.visibility = View.GONE
    }
}