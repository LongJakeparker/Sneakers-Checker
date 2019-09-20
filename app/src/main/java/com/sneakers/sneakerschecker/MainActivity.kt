package com.sneakers.sneakerschecker

import android.content.*
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.BusEventMessage
import com.sneakers.sneakerschecker.model.GenerateQrCode
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.Web3Instance
import com.sneakers.sneakerschecker.screens.activity.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer_menu.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TYPE_UNLINK: Int = 0
    private val REQUEST_CODE_START_LOGIN_ACTIVITY = 1000
    private val REQUEST_CODE_START_CREATE_ACTIVITY = 1001

    private lateinit var sharedPref: SharedPref

    private var popupType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        sharedPref = SharedPref(this)

//        tvAddressMain.text = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address

//        val qrCode = GenerateQrCode.accountId(this, 0.55)

//        if (qrCode != null) {
//            ivQrCodeMain.setImageBitmap(qrCode)
//        }

        Thread {
            try {
                Web3Instance.setInstance(Web3j.build(HttpService(Constant.ETHEREUM_API_URL)))
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Connect Blockchain Failed", Toast.LENGTH_LONG).show() }
            }
        }.start()

        btnMenuMain.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
        tvCreateNew.setOnClickListener(this)
//        tvLogout.setOnClickListener(this)
//        ibtnCopyMain.setOnClickListener(this)
        btnScanToken.setOnClickListener(this)
//        btnCollection.setOnClickListener(this)

        Log.e("FCM-TOKEN", FirebaseInstanceId.getInstance().token)
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.END)

            R.id.tvLogin -> {
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY)
            }

            R.id.tvCreateNew -> {
                val intent = Intent(this, CreateNewActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivityForResult(intent, REQUEST_CODE_START_CREATE_ACTIVITY)
            }
//            R.id.ibtnCopyMain -> copyToClipboard(tvAddressMain.text.toString())
//
//            R.id.btnCollection -> {
//                drawer_layout.closeDrawer(GravityCompat.START)
//                CollectionActivity.start(this@MainActivity)
//            }
//
//            R.id.btnUnlinkWalletMain -> {
//                popupType = TYPE_UNLINK
//                builder.setTitle("Log Out")
//                    .setMessage("Do you want to unlink your wallet?")
//                    .setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show()
//            }

            R.id.btnScanToken -> goToScan()
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
        intentIntegrator.setPrompt(resources.getString(R.string.scan_tutorial_scan_sneaker_id))
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

    @Subscribe
    fun getMessage(refreshWorkMessage: BusEventMessage) {
        if (refreshWorkMessage.message == Constant.BusMessage.MESS_CLOSE_CHECK_SCREEN) {
//            builder.setTitle("Transfer Status")
//                .setMessage("Your transfer was succeed")
//                .setPositiveButton("OK", dialogClickListener).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
