package br.android.cericatto.jobsity.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.android.cericatto.jobsity.AppConfiguration
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.extensions.openActivityForResult
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initLayout()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppConfiguration.LOGIN_TO_FINGERPRINT_CODE -> onBackPressed()
            AppConfiguration.LOGIN_TO_PIN_CODE -> onBackPressed()
        }
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun initLayout() {
        activity_login__fingerprint_button.setOnClickListener {
            openActivityForResult(AppConfiguration.LOGIN_TO_FINGERPRINT_CODE,
                FingerprintActivity::class.java)
        }
        activity_login__pin_button.setOnClickListener {
            openActivityForResult(AppConfiguration.LOGIN_TO_PIN_CODE,
                PINActivity::class.java)
        }
    }
}