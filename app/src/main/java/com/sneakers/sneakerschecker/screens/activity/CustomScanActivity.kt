package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.activity_custom_scan.*

class CustomScanActivity : AppCompatActivity() {

    private val TAG = CustomScanActivity::class.java.simpleName
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }

            lastText = result.text
            tvCode.text = lastText

            beepManager!!.playBeepSoundAndVibrate()

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_scan)

        barcodeView = findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.initializeFromIntent(intent)
        barcodeView!!.decodeSingle(callback)

        beepManager = BeepManager(this)
    }

    override fun onResume() {
        super.onResume()

        barcodeView!!.resume()
    }

    override fun onPause() {
        super.onPause()

        barcodeView!!.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
