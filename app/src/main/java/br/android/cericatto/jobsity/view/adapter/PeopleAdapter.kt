package br.android.cericatto.jobsity.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.Person
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.activity.PersonSearchActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.item_person.view.*

class PeopleAdapter(
    private val mActivity: PersonSearchActivity,
    private val mDataList: MutableList<Person>
) : RecyclerView.Adapter<PeopleAdapter.PersonViewHolder>() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private lateinit var mLayoutInflater: LayoutInflater

    //--------------------------------------------------
    // Recycler View
    //--------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonViewHolder {
        if (!::mLayoutInflater.isInitialized) {
            mLayoutInflater = LayoutInflater.from(parent.context)
        }
        return PersonViewHolder(mLayoutInflater.inflate(R.layout.item_person, parent, false))
    }

    override fun getItemCount() = mDataList.size

    override fun onBindViewHolder(holder: PersonViewHolder, position: Int) {
        val item = mDataList[position]
        holder.container.setOnClickListener {
            mActivity.showToast(R.string.item_person__lack_of_time)
        }

        setImage(holder, item)
        setName(holder, item)
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun setName(holder: PersonViewHolder, item: Person?) {
        if (!item!!.name.isNullOrBlank() || !item!!.name.isNullOrEmpty())
            holder.nameTextView.text = item.name
    }

    private fun setImage(holder: PersonViewHolder, item: Person?) {
        val imageView = holder.showImageView
        val textView = holder.nameTextView

        val imageNull = item!!.image == null
        val originalImageNull: Boolean
        val originalImageEmpty: Boolean
        if (imageNull) textView.text = ""
        else {
            originalImageNull = item.image!!.medium == null
            if (originalImageNull) textView.text = ""
            else {
                originalImageEmpty = item.image.medium!!.isEmpty()
                if (originalImageEmpty) textView.text = ""
                else {
                    Glide.with(imageView)
                        .load(item.image.medium)
                        .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                        .into(imageView)
                }
            }
        }
    }

    //--------------------------------------------------
    // View Holder
    //--------------------------------------------------

    inner class PersonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container = itemView.item_person__card_view!!
        val showImageView = itemView.item_person__image_view!!
        val nameTextView = itemView.item_person__name_text_view!!
    }
}