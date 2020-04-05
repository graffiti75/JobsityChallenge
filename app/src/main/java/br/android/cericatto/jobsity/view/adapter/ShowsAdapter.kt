package br.android.cericatto.jobsity.view.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.Shows
import br.android.cericatto.jobsity.presenter.extensions.openActivityForResultWithExtras
import br.android.cericatto.jobsity.view.activity.DetailsActivity
import br.android.cericatto.jobsity.view.activity.MainActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_shows.view.*

class ShowsAdapter(
    private val mActivity: MainActivity,
    private val mDataList: List<Shows>
) : RecyclerView.Adapter<ShowsAdapter.ShowsViewHolder>() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mLayoutInflater: LayoutInflater

    //--------------------------------------------------
    // Recycler View
    //--------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShowsViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        return ShowsViewHolder(mLayoutInflater.inflate(R.layout.item_shows, parent, false))
    }

    override fun getItemCount() = mDataList.size

    override fun onBindViewHolder(holder: ShowsViewHolder, position: Int) {
        val item = mDataList[position]

        val url = item.image.medium

        holder.container.setOnClickListener {
            val json: String = Gson().toJson(item)
            mActivity.openActivityForResultWithExtras(
                DetailsActivity::class.java, AppConfiguration.MAIN_TO_DETAILS_CODE,
                AppConfiguration.CURRENT_SHOW_EXTRA, json
            )
        }

        Glide.with(holder.showImageView)
            .load(url)
            .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
            .into(holder.showImageView)

        holder.nameTextView.text = item.name
        holder.daysTextView.text = item.schedule.days?.joinToString(separator = ", ") { it }
        holder.timeTextView.text = item.schedule.time
        holder.genresTextView.text = item.genres?.joinToString(separator = ", ") { it }

        holder.summaryTextView.text = Html.fromHtml(item.summary)
    }

    //--------------------------------------------------
    // View Holder
    //--------------------------------------------------

    inner class ShowsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.item_shows__card_view!!
        val showImageView = itemView.item_shows__image_view!!
        val nameTextView = itemView.item_shows__name_text_view!!
        val daysTextView = itemView.item_shows__schedule_days_text_view!!
        val timeTextView = itemView.item_shows__schedule_time_text_view!!
        val genresTextView = itemView.item_shows__genres_text_view!!
        val summaryTextView = itemView.item_shows__summary_text_view!!
    }
}