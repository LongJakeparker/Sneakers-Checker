package com.sneakers.sneakerschecker

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.viewpager.widget.ViewPager
import com.google.firebase.iid.FirebaseInstanceId
import com.google.zxing.integration.android.IntentIntegrator
import com.sneakers.sneakerschecker.adapter.MainSliderAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.screens.activity.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer_menu.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import android.widget.LinearLayout
import com.sneakers.sneakerschecker.model.*
import android.R.attr.right
import android.R.attr.left
import android.animation.LayoutTransition
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TYPE_UNLINK: Int = 0
    private val TIME_AUTO_NEXT = 4000
    private val REQUEST_CODE_START_LOGIN_ACTIVITY = 1000
    private val REQUEST_CODE_START_CREATE_ACTIVITY = 1001

    private lateinit var sharedPref: SharedPref
    private var isExpanded: Boolean = false

    private var popupType: Int = -1

    private val mainSliderList = arrayListOf(
        MainSliderItem(R.string.text_explore_1, R.drawable.drawable_explore_pager_1),
        MainSliderItem(R.string.text_explore_2, R.drawable.drawable_explore_pager_2)
    )

    var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EventBus.getDefault().register(this)

        sharedPref = SharedPref(this)

        checkUserLogin()

        Thread {
            try {
                Web3Instance.setInstance(Web3j.build(HttpService(Constant.ETHEREUM_API_URL)))
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Connect Blockchain Failed", Toast.LENGTH_LONG).show() }
            }
        }.start()

        setViewPager()

        btnMenuMain.setOnClickListener(this)
        tvLogin.setOnClickListener(this)
        tvCreateNew.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
        ivLogout.setOnClickListener(this)
//        ibtnCopyMain.setOnClickListener(this)
        btnScanToken.setOnClickListener(this)
//        btnCollection.setOnClickListener(this)

        Log.e("FCM-TOKEN", FirebaseInstanceId.getInstance().token)
    }

    private fun checkUserLogin() {
        if (sharedPref.getString(Constant.USER_CREDENTIALS) != "") {
                notifyUserLogin()
        }
    }

    private fun setViewPager() {
        viewpagerMain.adapter = MainSliderAdapter(this, mainSliderList)
        viewpagerMain.setOnTouchListener { v, event -> true }
        viewpagerMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                startTimer()
            }

        })
        viewpagerMain.setPageTransformer(false) { view, position ->
            view.translationX = view.width * -position

            if (position <= -1.0F || position >= 1.0F) {
                view.alpha = 0.0F
            } else if (position == 0.0F) {
                view.alpha = 1.0F
            } else {
                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
                view.alpha = 1.0F - Math.abs(position)
            }
        }
        startTimer()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnMenuMain -> drawer_layout.openDrawer(GravityCompat.END)

            R.id.tvLogin -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY)
            }

            R.id.tvCreateNew -> {
                val intent = Intent(this, CreateNewActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_START_CREATE_ACTIVITY)
            }
//            R.id.ibtnCopyMain -> copyToClipboard(tvAddressMain.text.toString())
//
//            R.id.btnCollection -> {
//                drawer_layout.closeDrawer(GravityCompat.START)
//                CollectionActivity.start(this@MainActivity)
//            }
//
            R.id.tvLogout, R.id.ivLogout -> {
//                builder.setTitle("Log Out")
//                    .setMessage("Do you want to unlink your wallet?")
//                    .setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show()
            }

            R.id.btnScanToken -> goToScan()
        }
    }

    fun notifyUserLogin() {
//        tvUserName.text = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address
        tvUserAddress.text = sharedPref.getCredentials(Constant.USER_CREDENTIALS).address

        val qrCode = CommonUtils.generateQrCode(this, 1.0,
                                                sharedPref.getCredentials(Constant.USER_CREDENTIALS).address)

        if (qrCode != null) {
            ivQrAddress.setImageBitmap(qrCode)
        }

        ivAddressNonLogin.visibility = View.GONE
        lnNavigationItemUnLogin.visibility = View.GONE

        cvQrAddress.visibility = View.VISIBLE
        ivExpand.visibility = View.VISIBLE
        lnNavigationItemUnexpanded.visibility = View.VISIBLE
        tvLogout.visibility = View.VISIBLE

        ivExpand.setOnClickListener { expandMenu() }
    }

    private fun expandMenu() {
        if (!isExpanded) {
            val layoutTransition = (lnTopNavigation.parent as ConstraintLayout).layoutTransition
            layoutTransition.setDuration(300)
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            ivQrAddress.layoutParams.width = resources.getDimension(R.dimen.qr_code_size_expanded).toInt()
            ivQrAddress.layoutParams.height = resources.getDimension(R.dimen.qr_code_size_expanded).toInt()
            ivQrAddress.requestLayout()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, resources.getDimension(R.dimen.activity_margin_24dp).toInt(), 0, 0)
            tvUserName.layoutParams = params

            rlUserAddress.visibility = View.VISIBLE
            ivExpand.setImageResource(R.drawable.ic_expand_up)
            lnNavigationItemExpanded.visibility = View.VISIBLE

            lnNavigationItemUnexpanded.visibility = View.GONE
            tvLogout.visibility = View.GONE

            isExpanded = true
        }
        else {
            val layoutTransition = (lnTopNavigation.parent as ConstraintLayout).layoutTransition
            layoutTransition.setDuration(300)
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            ivQrAddress.layoutParams.width = resources.getDimension(R.dimen.qr_code_size_normal).toInt()
            ivQrAddress.layoutParams.height = resources.getDimension(R.dimen.qr_code_size_normal).toInt()
            ivQrAddress.requestLayout()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, resources.getDimension(R.dimen.activity_margin_16dp).toInt(), 0, 0)
            tvUserName.layoutParams = params

            rlUserAddress.visibility = View.GONE
            ivExpand.setImageResource(R.drawable.ic_expand_down)
            lnNavigationItemExpanded.visibility = View.GONE

            lnNavigationItemUnexpanded.visibility = View.VISIBLE
            tvLogout.visibility = View.VISIBLE

            isExpanded = false
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

        notifyUserLogout()
    }

    private fun notifyUserLogout() {

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

        when {
            requestCode == REQUEST_CODE_START_CREATE_ACTIVITY && resultCode == Activity.RESULT_OK -> {
                Handler().postDelayed({
                    ConfirmRegisterActivity.start(this)
                }, 500)
                notifyUserLogin()
            }

            requestCode == REQUEST_CODE_START_LOGIN_ACTIVITY && resultCode == Activity.RESULT_OK -> {
                notifyUserLogin()
            }
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
        countDownTimer!!.cancel()
    }

    private fun cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
            countDownTimer = null
        }
    }

    private fun startTimer() {
        cancelTimer()
        countDownTimer = object : CountDownTimer(TIME_AUTO_NEXT.toLong(), 500) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                var nextItem = viewpagerMain.currentItem + 1
                if (nextItem >= viewpagerMain.adapter!!.count) {
                    nextItem = viewpagerMain.adapter!!.count / 2
                }
                viewpagerMain.setCurrentItem(nextItem, true)
            }
        }
        countDownTimer!!.start()
    }
}
