package br.android.cericatto.jobsity.view.activity

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import br.android.cericatto.jobsity.R
import br.android.cericatto.jobsity.presenter.extensions.BiometricChecker
import br.android.cericatto.jobsity.presenter.extensions.FingerprintHelper
import br.android.cericatto.jobsity.presenter.extensions.openActivity
import br.android.cericatto.jobsity.presenter.extensions.showToast
import java.io.IOException
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKey
import javax.security.cert.CertificateException

/**
 * Source:
 * https://blog.mindorks.com/authentication-using-fingerprint-in-android-tutorial
 */
@Suppress("DEPRECATION")
class FingerprintActivity : AppCompatActivity() {

    //--------------------------------------------------
    // Constants
    //--------------------------------------------------

    private val KEY_NAME = "my_key"

    private lateinit var mFingerprintManager: FingerprintManager
    private lateinit var mKeyguardManager: KeyguardManager

    private lateinit var mKeyStore: KeyStore
    private lateinit var mKeyGenerator: KeyGenerator

    private lateinit var mCipher: Cipher
    private lateinit var mCryptoObject: FingerprintManager.CryptoObject

    private lateinit var mBiometricChecker: BiometricChecker

    //--------------------------------------------------
    // Activity Life Cycle
    //--------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fingerprint)

        mBiometricChecker = BiometricChecker.getInstance(this)
        if (!mBiometricChecker.hasBiometrics) {
            showToast(R.string.activity_fingerprint__device_does_not_support_fingerprint)
            openActivity(PINActivity::class.java)
        } else if (checkLockScreen()) {
            generateKey()
            if (initCipher()) {
                mCipher.let {
                    mCryptoObject = FingerprintManager.CryptoObject(it)
                }
                val helper = FingerprintHelper(this)
                if (mFingerprintManager != null && mCryptoObject != null) {
                    helper.startAuth(mFingerprintManager, mCryptoObject)
                }
            }
        }
    }

    //--------------------------------------------------
    // Methods
    //--------------------------------------------------

    private fun checkLockScreen(): Boolean {
        mKeyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
        mFingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        var text = getString(R.string.activity_fingerprint__lock_screen_not_enabled)
        if (!mKeyguardManager.isKeyguardSecure) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
            return false
        }

        val permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT)
        text = getString(R.string.activity_fingerprint__fingerprint_permission_not_enabled)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
            return false
        }

        text = getString(R.string.activity_fingerprint__no_fingerprint_registered)
        if (!mFingerprintManager.hasEnrolledFingerprints()) {
            Toast.makeText(this, text, Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun generateKey() {
        try {
            mKeyStore = KeyStore.getInstance("AndroidKeyStore")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            mKeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        } catch (e: NoSuchProviderException) {
            throw RuntimeException("Failed to get KeyGenerator instance", e)
        }

        try {
            mKeyStore.load(null)
            mKeyGenerator.init(KeyGenParameterSpec.Builder(KEY_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build())
            mKeyGenerator.generateKey()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: InvalidAlgorithmParameterException) {
            throw RuntimeException(e)
        } catch (e: CertificateException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun initCipher(): Boolean {
        try {
            mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to get Cipher", e)
        } catch (e: NoSuchPaddingException) {
            throw RuntimeException("Failed to get Cipher", e)
        }

        try {
            mKeyStore.load(null)
            val key = mKeyStore.getKey(KEY_NAME, null) as SecretKey
            mCipher.init(Cipher.ENCRYPT_MODE, key)
            return true
        } catch (e: KeyPermanentlyInvalidatedException) {
            return false
        } catch (e: KeyStoreException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: CertificateException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: UnrecoverableKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to init Cipher", e)
        } catch (e: InvalidKeyException) {
            throw RuntimeException("Failed to init Cipher", e)
        }
    }
}