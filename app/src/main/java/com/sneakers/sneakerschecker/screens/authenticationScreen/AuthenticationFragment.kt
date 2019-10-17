package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator

import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.Web3Instance
import com.sneakers.sneakerschecker.screens.activity.SneakerInfoActivity
import kotlinx.android.synthetic.main.fragment_authentication.view.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import java.security.Security

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class AuthenticationFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null
    private lateinit var sharedPref: SharedPref
    private lateinit var credentials: Credentials

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_authentication, container, false)

        setupBouncyCastle()

        Thread {
            try {
                Web3Instance.setInstance(Web3j.build(HttpService(Constant.ETHEREUM_API_URL)))
            } catch (e: Exception) {
                activity?.runOnUiThread { Toast.makeText(activity, "Connect Blockchain Failed", Toast.LENGTH_LONG).show() }
            }
        }.start()

        sharedPref = context?.let { SharedPref(it) }!!
        createAppCredentials()

        fragmentView!!.btnNewWalletAuthen.setOnClickListener(this)
        fragmentView!!.btnRestoreWalletAuthen.setOnClickListener(this)
        fragmentView!!.btnValidate.setOnClickListener(this)

        return fragmentView
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider.javaClass == BouncyCastleProvider::class.java) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun createAppCredentials() {
        try {
            val keyPair = Keys.createEcKeyPair()
            credentials = Credentials.create(keyPair)
            sharedPref.setCredentials(credentials, Constant.APP_CREDENTIALS)

        } catch (e: Exception) {
            Log.e( "Error: " , e.message)
        }
    }

    override fun onClick(v: View?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        when (v?.id) {
            R.id.btnValidate -> {
                goToScan()
            }

            R.id.btnNewWalletAuthen -> {
//                transaction.replace(R.id.authentication_layout, CreateNewFragment())
//                    .addToBackStack(null)
//                    .commit()
            }

            R.id.btnRestoreWalletAuthen -> {
//                transaction.replace(R.id.authentication_layout, ImportPrivateKeyFragment())
//                    .addToBackStack(null)
//                    .commit()
            }
        }
    }

    private fun goToScan() {
        val intentIntegrator = IntentIntegrator.forSupportFragment(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        intentIntegrator.setPrompt(resources.getString(R.string.scan_tutorial_scan_sneaker_id))
        intentIntegrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                SneakerInfoActivity.start(activity as Activity, result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
