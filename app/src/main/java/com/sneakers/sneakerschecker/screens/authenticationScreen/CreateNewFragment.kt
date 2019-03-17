package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.web3j.crypto.CipherException
import org.web3j.crypto.WalletUtils
import java.nio.file.Files.exists
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Environment
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.fragment_create_new.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import java.io.File
import java.io.IOException
import android.util.Log
import android.widget.Button
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security.insertProviderAt
import java.security.Security.removeProvider
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import org.web3j.crypto.Credentials
import java.security.*




// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class CreateNewFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private var btnNewWallet: Button? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_new, container, false)

        btnNewWallet = fragmentView!!.findViewById(R.id.btnNewWalletCreate)

        btnNewWallet!!.setOnClickListener(this)

        return fragmentView
    }

    override fun onClick(v: View?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction();
        transaction.replace(R.id.authentication_layout, CreatePhraseFragment())
            .commit()
    }
}
