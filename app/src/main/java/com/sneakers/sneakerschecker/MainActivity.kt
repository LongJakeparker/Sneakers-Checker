package com.sneakers.sneakerschecker

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
import android.support.v7.app.AlertDialog
import com.sneakers.sneakerschecker.contracts.TrueGrailToken
import org.web3j.crypto.WalletUtils
import org.web3j.tx.Contract
import java.math.BigInteger
import android.R.attr.label
import android.content.*
import android.content.Context.CLIPBOARD_SERVICE




class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val GAS_PRICE:BigInteger = BigInteger.valueOf(20000000000L)
    private val GAS_LIMIT:BigInteger = BigInteger.valueOf(6721975L)

    private val CONTRACT_ADDRESS:String = "0x9A8c82A07cB94730BD10074A4C2758c7c28038dc"

    private val TYPE_UNLINK: Int = 0

    private lateinit var contract: TrueGrailToken

    private lateinit var sharedPref: SharedPref
    private var credentials: org.web3j.crypto.Credentials? = null

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private var popupType: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        builder = AlertDialog.Builder(this)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()
        dialog.show()

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

            credentials = WalletUtils.loadBip39Credentials(sharedPref.getString(Constant.WALLET_PASSPHRASE), sharedPref.getString(Constant.WALLET_MNEMONIC))

            contract = TrueGrailToken.load(
                CONTRACT_ADDRESS, web3, credentials, GAS_PRICE, GAS_LIMIT
            )

            dialog.dismiss()

            Log.e("TAG", "Connect: $clientVersion")
        }.start()

        btnMenuMain.setOnClickListener(this)
        btnUnlinkWalletMain.setOnClickListener(this)
        ibtnCopyMain.setOnClickListener(this)
        btnScanMain.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.START)

            R.id.ibtnCopyMain -> copyToClipboard(tvAddressMain.text.toString())

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
        sharedPref.setBool(true, Constant.WALLET_UNLINK)
        val intent = Intent(this, AuthenticationActivity::class.java)
        startActivity(intent)
        this.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents != null) {
                var ownerId = contract.ownerOf(BigInteger(result.contents)).sendAsync().get()
                var productId = contract.tokenMetadata(BigInteger(result.contents)).sendAsync().get()
                Toast.makeText(this, "$ownerId + $productId", Toast.LENGTH_LONG).show()
                Log.e("TAG", "ownerId: $ownerId + productId: $productId")
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
