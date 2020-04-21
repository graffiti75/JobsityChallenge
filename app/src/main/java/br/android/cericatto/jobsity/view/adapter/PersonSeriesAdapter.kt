package br.android.cericatto.jobsity.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.CastCredits
import kotlinx.android.synthetic.main.item_cast_credits.view.*

class PersonSeriesAdapter(
    private val mDataList: MutableList<CastCredits>
) : RecyclerView.Adapter<PersonSeriesAdapter.PersonSeriesViewHolder>() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mLayoutInflater: LayoutInflater

    //--------------------------------------------------
    // Recycler View
    //--------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonSeriesViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        return PersonSeriesViewHolder(mLayoutInflater.inflate(R.layout.item_cast_credits, parent, false))
    }

    override fun getItemCount() = mDataList.size

    override fun onBindViewHolder(holder: PersonSeriesViewHolder, position: Int) {
        val item = mDataList[position]

        val isOddSeason = position % 2 == 1
        var drawable = ContextCompat.getDrawable(holder.container.context, R.color.colorAccentLight)
        if (isOddSeason) drawable = ContextCompat.getDrawable(holder.container.context, R.color.colorAccentClear)
        holder.container.background = drawable

        holder.container.setOnClickListener {
            // TODO
        }

        setLink(holder, item)
    }

    private fun setLink(holder: PersonSeriesViewHolder, item: CastCredits) {
        val textView = holder.hrefTextView

        val linksNull = item.links == null
        val showsNull: Boolean
        val hrefNull: Boolean
        if (linksNull) textView.text = ""
        else {
            showsNull = item.links!!.show == null
            if (showsNull) textView.text = ""
            else {
                hrefNull = item.links.show!!.href == null
                if (hrefNull) textView.text = ""
                else {
                    val link = item.links.show.href
                    holder.hrefTextView.text = link
                }
            }
        }
    }

    //--------------------------------------------------
    // View Holder
    //--------------------------------------------------

    inner class PersonSeriesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.item_cast_credits__card_view!!
        val hrefTextView = itemView.item_cast_credits__show_text_view!!
    }
}