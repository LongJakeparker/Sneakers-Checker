package com.sneakers.sneakerschecker

import android.content.*
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import com.sneakers.sneakerschecker.model.GenerateQrCode
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.Web3Instance
import com.sneakers.sneakerschecker.screens.activity.AuthenticationActivity
import com.sneakers.sneakerschecker.screens.activity.CollectionActivity
import com.sneakers.sneakerschecker.screens.activity.SneakerInfoActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer_menu.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.admin.Admin
import org.web3j.protocol.http.HttpService


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TYPE_UNLINK: Int = 0

    private lateinit var web3: Web3j

    private lateinit var sharedPref: SharedPref

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private var popupType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        builder = AlertDialog.Builder(this)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        //builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()
        dialog.show()

        sharedPref = SharedPref(this)

        tvAddressMain.text = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address

        val qrCode = GenerateQrCode.accountId(this, 0.55)

        if (qrCode != null) {
            ivQrCodeMain.setImageBitmap(qrCode)
        }

        Thread {
            try {
                web3 = Web3j.build(HttpService(Constant.ETHEREUM_API_URL))
                Web3Instance.setInstance(web3)
//                if (Web3Instance.getInstance() == null) {
//                    web3 = Admin.build(HttpService(Constant.ETHEREUM_API_URL))
//                    Web3Instance.setInstance(web3)
//                }
//                else {
//                    web3 = Web3Instance.getInstance() as Admin
//                }

                dialog.dismiss()
            } catch (e: Exception) {
                dialog.dismiss()
                runOnUiThread { Toast.makeText(this, "Connect Blockchain Failed", Toast.LENGTH_LONG).show() }
            }
        }.start()

        btnMenuMain.setOnClickListener(this)
        btnUnlinkWalletMain.setOnClickListener(this)
        ibtnCopyMain.setOnClickListener(this)
        btnScanMain.setOnClickListener(this)
        btnCollection.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.START)

            R.id.ibtnCopyMain -> copyToClipboard(tvAddressMain.text.toString())

            R.id.btnCollection -> {
                drawer_layout.closeDrawer(GravityCompat.START)
                CollectionActivity.start(this@MainActivity)
            }

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

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText("simple text", text)
        clipboard.primaryClip = clip
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun goToScan() {
        val intentIntegrator = IntentIntegrator(this)
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)
        intentIntegrator.initiateScan()
    }

    private fun UnlinkWallet() {
        sharedPref.clearPref()
        sharedPref.setBool(true, Constant.ACCOUNT_UNLINK)
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                SneakerInfoActivity.start(this@MainActivity, result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
