package br.android.cericatto.jobsity.presenter.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.CancellationSignal
import android.widget.Toast
import androidx.core.app.ActivityCompat
import br.android.cericatto.jobsity.view.activity.LoginActivity
import br.android.cericatto.jobsity.view.activity.MainActivity
import br.android.cericatto.jobsity.view.activity.ShowDetailsActivity

@SuppressLint("ByteOrderMark")
@Suppress("DEPRECATION")
class FingerprintHelper(private val mActivity: LoginActivity) : FingerprintManager.AuthenticationCallback() {

    //--------------------------------------------------
    // Constants
    //--------------------------------------------------

    lateinit var mCancellationSignal: CancellationSignal

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    fun startAuth(manager: FingerprintManager, cryptoObject: FingerprintManager.CryptoObject) {
        mCancellationSignal = CancellationSignal()
        val permission = ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.USE_FINGERPRINT)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            return
        }
        manager.authenticate(cryptoObject, mCancellationSignal, 0, this, null)
    }

    //--------------------------------------------------
    // Override Methods
    //--------------------------------------------------

    override fun onAuthenticationError(errMsgId: Int, errString: CharSequence) {
        Toast.makeText(mActivity, "Authentication error\n$errString.", Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationHelp(helpMsgId: Int, helpString: CharSequence) {
        Toast.makeText(mActivity, "Authentication help\n$helpString.", Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationFailed() {
        Toast.makeText(mActivity,"Authentication failed.", Toast.LENGTH_LONG).show()
    }

    override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult) {
        Toast.makeText(mActivity, "Authentication succeeded.", Toast.LENGTH_LONG).show()
        mActivity.openActivity(MainActivity::class.java)
    }
}