package br.android.cericatto.jobsity.view.activity

import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.extensions.initViewAnimation
import br.android.cericatto.jobsity.presenter.extensions.openActivity
import br.android.cericatto.jobsity.presenter.extensions.showToast
import kotlinx.android.synthetic.main.activity_pin.*

class PINActivity : ParentActivity(), View.OnClickListener {

    //--------------------------------------------------
    // Attributes
    //--------------------------------------------------

    private var mPassword = ""

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        initLayout()
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun initLayout() {
        activity_pin__one_text_view.setOnClickListener(this)
        activity_pin__two_text_view.setOnClickListener(this)
        activity_pin__three_text_view.setOnClickListener(this)
        activity_pin__four_text_view.setOnClickListener(this)
        activity_pin__five_text_view.setOnClickListener(this)
        activity_pin__six_text_view.setOnClickListener(this)
        activity_pin__seven_text_view.setOnClickListener(this)
        activity_pin__eight_text_view.setOnClickListener(this)
        activity_pin__nine_text_view.setOnClickListener(this)
        activity_pin__zero_text_view.setOnClickListener(this)

        activity_pin__delete_text_view.setOnClickListener(this)
        activity_pin__confirm_button.setOnClickListener(this)
    }

    private fun dialAction(viewId: Int, animation: Animation, dial: String) {
        initViewAnimation(viewId, animation)
        mPassword += dial
        updateViews()
    }

    private fun updateViews() {
        activity_pin__password_edit_text.setText(mPassword)
        val length = mPassword.length
        var background = ContextCompat.getDrawable(this, R.drawable.border_square_fill_grey)
        if (length in 4..6) background = ContextCompat.getDrawable(this, R.drawable.border_square_fill_blue)
        activity_pin__confirm_button.background = background
    }

    private fun confirmButtonAction() {
        val length = mPassword.length
        if (length !in 4..6)
            showToast(R.string.activity_pin__invalid_password_length)
        else
            openActivity(MainActivity::class.java)
    }

    //--------------------------------------------------
    // Listeners
    //--------------------------------------------------

    override fun onClick(view: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in_95)
        when (view.id) {
            R.id.activity_pin__delete_text_view -> {
                initViewAnimation(view.id, animation)
                mPassword = mPassword.substring(0, mPassword.length - 1)
                updateViews()
            }
            R.id.activity_pin__confirm_button -> {
                initViewAnimation(view.id, animation)
                confirmButtonAction()
            }
            R.id.activity_pin__one_text_view -> dialAction(view.id, animation, "1")
            R.id.activity_pin__two_text_view -> dialAction(view.id, animation, "2")
            R.id.activity_pin__three_text_view -> dialAction(view.id, animation, "3")
            R.id.activity_pin__four_text_view -> dialAction(view.id, animation, "4")
            R.id.activity_pin__five_text_view -> dialAction(view.id, animation, "5")
            R.id.activity_pin__six_text_view -> dialAction(view.id, animation, "6")
            R.id.activity_pin__seven_text_view -> dialAction(view.id, animation, "7")
            R.id.activity_pin__eight_text_view -> dialAction(view.id, animation, "8")
            R.id.activity_pin__nine_text_view -> dialAction(view.id, animation, "9")
            R.id.activity_pin__zero_text_view -> dialAction(view.id, animation, "0")
        }
    }
}