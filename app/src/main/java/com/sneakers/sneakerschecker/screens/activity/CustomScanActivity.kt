package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.BeepManager
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import kotlinx.android.synthetic.main.activity_custom_scan.*
import kotlinx.android.synthetic.main.include_bottom_view_scan.*

class CustomScanActivity : AppCompatActivity(), View.OnClickListener {
    class ScanType {
        companion object {
            val SCAN_GRAIL = 1
            val SCAN_PRIVATE_KEY = 2
            val SCAN_ADDRESS = 3
            val SCAN_CLAIM_TOKEN = 4
        }
    }
    private val VIEW_CHANGE_DURATION = 300L
    private val TAG = CustomScanActivity::class.java.simpleName
    private var barcodeView: DecoratedBarcodeView? = null
    private var beepManager: BeepManager? = null
    private var lastText: String? = null
    private var scanType: Int? = null
    private var hadScanResult: Boolean = false
    private var isExpanded: Boolean = false

    private val callback = object : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult) {
            if (result.text == null || result.text == lastText) {
                // Prevent duplicate scans
                return
            }

            lastText = result.text

            beepManager!!.playBeepSoundAndVibrate()

        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>) {}
    }

    companion object {
        fun start(activity: Activity, scanType: Int) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            activity.startActivity(intent)
        }

        fun startForResult(activity: Activity, scanType: Int, requestCode: Int) {
            val intent = Intent(activity, CustomScanActivity::class.java)
            intent.putExtra(Constant.EXTRA_SCAN_TYPE, scanType)
            activity.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_custom_scan)

        scanType = intent?.getIntExtra(Constant.EXTRA_SCAN_TYPE, 0)

        barcodeView = findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView!!.barcodeView.decoderFactory = DefaultDecoderFactory(formats)
        barcodeView!!.initializeFromIntent(intent)
        barcodeView!!.decodeContinuous(callback)

        beepManager = BeepManager(this)

        setupBottomView()

        rlRootBottomView.setOnClickListener(this)
        btnCloseBottomView.setOnClickListener(this)
        blurView.setOnClickListener(this)
        btnClose.setOnClickListener(this)
    }

    private fun setupBottomView() {
        when (scanType) {
            ScanType.SCAN_GRAIL -> {
                setContentBottomView(R.string.activity_title_scan_grail, R.string.header_guide_scan_grail,
                    R.drawable.ic_guideline_scan_grail, R.string.title_guide_scan_grail,
                    R.string.content_guide_scan_grail)
            }

            ScanType.SCAN_PRIVATE_KEY -> {
                setContentBottomView(R.string.activity_title_scan_private_key, R.string.header_guide_scan_private_key,
                    R.drawable.ic_guideline_scan_private_key, R.string.title_guide_scan_private_key,
                    R.string.content_guide_scan_private_key)
            }
        }
    }

    private fun setContentBottomView(activityTitle: Int, header: Int, image: Int, title: Int, content: Int) {
        tvTitle.text = resources.getText(activityTitle)
        tvHeaderGuide.text = resources.getText(header)
        ivGuideImage.setImageResource(image)
        tvTitleGuide.text = resources.getText(title)
        tvContentGuide.text = resources.getText(content)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.rlRootBottomView -> {
                if (!isExpanded) {
                    expandBottomView()
                }
            }
            R.id.btnCloseBottomView, R.id.blurView -> expandBottomView()

            R.id.btnClose -> finish()
        }
    }

    private fun expandBottomView() {
        if (!hadScanResult) {
            if (!isExpanded) {
                tvHeaderGuide.visibility = View.GONE

                rlRootGuide.visibility = View.VISIBLE
                ivGuideImage.visibility = View.VISIBLE

                blurView.visibility = View.VISIBLE
                blurView.isClickable = true
                blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_view))

                barcodeView?.pause()
                isExpanded = true
            }
            else {
                tvHeaderGuide.visibility = View.VISIBLE

                rlRootGuide.visibility = View.GONE
                ivGuideImage.visibility = View.GONE

                blurView.visibility = View.GONE
                blurView.isClickable = false
                blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out_view))

                barcodeView?.resume()
                isExpanded = false
            }
        }
    }

    override fun onResume() {
        super.onResume()

        barcodeView?.resume()
    }

    override fun onPause() {
        super.onPause()

        barcodeView?.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }
}
