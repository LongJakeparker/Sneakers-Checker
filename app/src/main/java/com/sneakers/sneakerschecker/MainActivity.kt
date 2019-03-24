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

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sharedPref: SharedPref

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPref = SharedPref(this)

        tvAddressMain.text = sharedPref.getString(Constant.WALLET_ADDRESS)

        val qrCode = GenerateQrCode.WalletAddress(this, 0.55)

        if (qrCode != null) {
            ivQrCodeMain.setImageBitmap(qrCode)
        }

        btnMenuMain.setOnClickListener(this)
        btnUnlinkWalletMain.setOnClickListener(this)
        btnScanMain.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.START)

            R.id.btnUnlinkWalletMain -> UnlinkWallet()

            R.id.btnScanMain -> goToScan()
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
