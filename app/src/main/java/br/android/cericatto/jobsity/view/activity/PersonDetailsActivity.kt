package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.view.View
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.MainApplication
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.model.api.CastCredits
import br.android.cericatto.jobsity.model.api.Person
import br.android.cericatto.jobsity.presenter.extensions.networkOn
import br.android.cericatto.jobsity.presenter.extensions.openActivityExtras
import br.android.cericatto.jobsity.presenter.extensions.showToast
import br.android.cericatto.jobsity.view.adapter.PersonSeriesAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_person_details.*
import timber.log.Timber

class PersonDetailsActivity : ParentActivity() {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private val mComposite = CompositeDisposable()
    private var mPersonId = ""
    private lateinit var mCurrentPerson: Person

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_person_details)
        getExtras()
    }

    //--------------------------------------------------
    // Main Methods
    //--------------------------------------------------

    private fun getExtras() {
        val extras = intent.extras
        val json = extras!!.getString(AppConfiguration.CURRENT_PERSON_EXTRA)
        val jsonType = object : TypeToken<Person>() {}.type
        mCurrentPerson = Gson().fromJson(json, jsonType)
        setToolbar(R.id.id_toolbar, homeEnabled = true, title = mCurrentPerson.name!!)
        mPersonId = mCurrentPerson.id
        initLayout()
    }

    private fun initLayout() {
        setImage()
        getPersonSeries()
    }

    private fun setImage() {
        val imageView = activity_person_details__image_view

        val imageNull = mCurrentPerson.image == null
        val originalImageNull: Boolean
        val originalImageEmpty: Boolean
        if (!imageNull) {
            originalImageNull = mCurrentPerson.image!!.original == null
            if (!originalImageNull) {
                originalImageEmpty = mCurrentPerson.image!!.original!!.isEmpty()
                if (!originalImageEmpty) {
                    Glide.with(imageView)
                        .load(mCurrentPerson.image!!.original)
                        .apply(RequestOptions().placeholder(R.drawable.ic_image_placeholder))
                        .into(imageView)
                }
            }
        }
    }

    private fun getPersonSeries() {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service
            val observable = service.castCredits(mPersonId)
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        getPersonSeriesOnSuccess(it)
                    },
                    {
                        Timber.i("getPersonSeries() -> On error: $it")
                    },
                    {
                        Timber.i("getPersonSeries() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }

    private fun getPersonSeriesOnSuccess(list: MutableList<CastCredits>) {
        val listEmpty = list.isEmpty()
        if (listEmpty) {
            activity_person_details__recycler_view.visibility = View.GONE
            activity_person_details__empty_recycler_text_view.visibility = View.VISIBLE
        } else {
            activity_person_details__recycler_view.visibility = View.VISIBLE
            activity_person_details__empty_recycler_text_view.visibility = View.GONE
            setAdapter(list)
        }
        hideLoading()
    }

    private fun setAdapter(list: MutableList<CastCredits>) {
        activity_person_details__recycler_view.adapter = PersonSeriesAdapter(this, list)
    }

    private fun hideLoading() {
        activity_person_details__container.visibility = View.VISIBLE
        activity_person_details__loading.visibility = View.GONE
    }

    //--------------------------------------------------
    // Callback Methods
    //--------------------------------------------------

    fun getCurrentShow(showId: String) {
        getShow(showId)
    }

    private fun getShow(showId: String) {
        if (!networkOn()) showToast(R.string.no_internet)
        else {
            val service = MainApplication.service
            val observable = service.getShow(showId)
            val subscription = observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        val url = it.officialSite!!
                        val keys = arrayOf(AppConfiguration.CURRENT_PERSON_EXTRA,
                            AppConfiguration.CURRENT_URL_EXTRA)
                        val values = arrayOf(mCurrentPerson.name!!, url)
                        openActivityExtras(WebViewActivity::class.java, keys, values)
                    },
                    {
                        Timber.i("getShow() -> On error: $it")
                    },
                    {
                        Timber.i("getShow() -> On Completed.")
                    }
                )
            mComposite.add(subscription)
        }
    }
}