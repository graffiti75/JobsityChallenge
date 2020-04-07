package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Episode
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
        var currentEpisode: Episode = Gson().fromJson(json, jsonType)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = currentEpisode.name)
        initLayout(currentEpisode)
    }

    private fun initLayout(currentEpisode: Episode) {
        Glide.with(activity_show_episode__image_view)
            .load(currentEpisode.image.original)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(activity_show_episode__image_view)

        activity_show_episode__name_text_view.text = currentEpisode.name
        activity_show_episode__number_text_view.text = currentEpisode.number.toString()
        activity_show_episode__season_text_view.text = currentEpisode.season.toString()
        activity_show_episode__summary_text_view.text = Html.fromHtml(currentEpisode.summary)

        Handler().postDelayed({
            updateVisibilities(false)
        }, 1000)
    }

    private fun updateVisibilities(loading: Boolean = true) {
        if (loading) {
            activity_show_episode__container.visibility = View.GONE
            activity_show_episode__loading.visibility = View.VISIBLE
        } else {
            activity_show_episode__container.visibility = View.VISIBLE
            activity_show_episode__loading.visibility = View.GONE
        }
    }
}
