package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.zxing.integration.android.IntentIntegrator

import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.fragment_authentication.view.*

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_authentication, container, false)

        fragmentView!!.btnNewWalletAuthen.setOnClickListener(this)
        fragmentView!!.btnRestoreWalletAuthen.setOnClickListener(this)

        return fragmentView
    }

    override fun onClick(v: View?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        when (v?.id) {
            R.id.btnValidate -> {
                goToScan()
            }

            R.id.btnNewWalletAuthen -> {
                transaction.replace(R.id.authentication_layout, CreateNewFragment())
                    .addToBackStack(null)
                    .commit()
            }

            R.id.btnRestoreWalletAuthen -> {
                transaction.replace(R.id.authentication_layout, RestoreFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    private fun goToScan() {
        val intentIntegrator = IntentIntegrator(activity)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        intentIntegrator.initiateScan()
    }
}
