package br.android.cericatto.jobsity.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Episode
import br.android.cericatto.jobsity.presenter.extensions.openActivityForResultWithExtras
import br.android.cericatto.jobsity.view.activity.ShowDetailsActivity
import br.android.cericatto.jobsity.view.activity.ShowEpisodeActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.item_episodes.view.*

class EpisodesAdapter(
    private val mActivity: ShowDetailsActivity,
    private val mDataList: MutableList<Episode>
) : RecyclerView.Adapter<EpisodesAdapter.EpisodesViewHolder>() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mLayoutInflater: LayoutInflater

    //--------------------------------------------------
    // Recycler View
    //--------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodesViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        return EpisodesViewHolder(mLayoutInflater.inflate(R.layout.item_episodes, parent, false))
    }

    override fun getItemCount() = mDataList.size

    override fun onBindViewHolder(holder: EpisodesViewHolder, position: Int) {
        val item = mDataList[position]

        val isOddSeason = item.season!! % 2 == 1
        var drawable = ContextCompat.getDrawable(holder.container.context, R.color.colorAccentLight)
        if (isOddSeason) drawable = ContextCompat.getDrawable(holder.container.context, R.color.colorAccentClear)
        holder.container.background = drawable

        holder.container.setOnClickListener {
            val json: String = Gson().toJson(item)
            mActivity.openActivityForResultWithExtras(
                ShowEpisodeActivity::class.java, AppConfiguration.SHOW_DETAILS_TO_EPISODE_DETAILS_CODE,
                AppConfiguration.CURRENT_EPISODE_EXTRA, json
            )
        }

        val season = " " + item.season
        holder.seasonTextView.text = mActivity.getString(R.string.item_episodes__season, season)
        val number = " " + item.number
        holder.numberTextView.text = mActivity.getString(R.string.item_episodes__number, number)
        val name = " " + item.name
        holder.nameTextView.text = mActivity.getString(R.string.item_episodes__name, name)
    }

    //--------------------------------------------------
    // View Holder
    //--------------------------------------------------

    inner class EpisodesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.item_episodes__card_view!!
        val seasonTextView = itemView.item_episodes__season_text_view!!
        val numberTextView = itemView.item_episodes__number_text_view!!
        val nameTextView = itemView.item_episodes__name_text_view!!
    }
}