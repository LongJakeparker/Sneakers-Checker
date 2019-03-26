package com.sneakers.sneakerschecker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.GenerateQrCode
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.screens.activity.AuthenticationActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer_menu.*
import org.web3j.protocol.http.HttpService
import org.web3j.protocol.Web3j
import android.util.Log
import android.content.DialogInterface
import android.support.v7.app.AlertDialog


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TYPE_UNLINK: Int = 0

    private lateinit var sharedPref: SharedPref

    private lateinit var builder: AlertDialog.Builder
    private var popupType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        builder = AlertDialog.Builder(this)

        sharedPref = SharedPref(this)

        tvAddressMain.text = sharedPref.getString(Constant.WALLET_ADDRESS)

        val qrCode = GenerateQrCode.WalletAddress(this, 0.55)

        if (qrCode != null) {
            ivQrCodeMain.setImageBitmap(qrCode)
        }

        Thread {
            val web3 = Web3j.build(HttpService(Constant.ETHEREUM_API_KEY))
            val web3ClientVersion = web3.web3ClientVersion().send()
            val clientVersion = web3ClientVersion.web3ClientVersion

            Log.e("TAG", "Connect: $clientVersion")
        }.start()

        btnMenuMain.setOnClickListener(this)
        btnUnlinkWalletMain.setOnClickListener(this)
        btnScanMain.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.START)

            R.id.btnUnlinkWalletMain -> {
                popupType = TYPE_UNLINK
                builder.setMessage("Do you want to unlink your wallet?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show()
            }

            R.id.btnScanMain -> goToScan()
        }
    }

    var dialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    when (popupType) {
                        TYPE_UNLINK -> UnlinkWallet()
                    }
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                }
            }
        }

    private fun goToScan() {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        intentIntegrator.initiateScan()
    }

    private fun UnlinkWallet() {
        sharedPref.clearPref()
        sharedPref.setBool(true, Constant.WALLET_UNLINK)
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                Toast.makeText(this, result.contents, Toast.LENGTH_LONG).show()
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
