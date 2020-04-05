package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.text.Html
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Shows
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

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
        initLayout(currentShow)
    }

    private fun initLayout(currentShow: Shows) {
        Glide.with(activity_details__image_view)
            .load(currentShow.image.medium)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(activity_details__image_view)

        activity_details__name_text_view.text = currentShow.name
        activity_details__schedule_days_text_view.text = currentShow.schedule.days?.joinToString(separator = ", ") { it }
        activity_details__schedule_time_text_view.text = currentShow.schedule.time
        activity_details__genres_text_view.text = currentShow.genres?.joinToString(separator = ", ") { it }
        activity_details__summary_text_view.text = Html.fromHtml(currentShow.summary)
    }
}