package com.sneakers.sneakerschecker

import android.app.Activity
import android.content.*
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.util.Log
import android.view.View
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
import android.animation.LayoutTransition
import androidx.constraintlayout.widget.ConstraintLayout
import com.sneakers.sneakerschecker.`interface`.IDialogListener
import com.sneakers.sneakerschecker.screens.fragment.ConfirmDialogFragment
import android.view.WindowManager
import android.os.Build
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import java.security.Security


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private val TIME_AUTO_NEXT = 4000
    private val REQUEST_CODE_START_LOGIN_ACTIVITY = 1000
    private val REQUEST_CODE_START_CREATE_ACTIVITY = 1001

    private lateinit var sharedPref: SharedPref
    private var isExpanded: Boolean = false

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

        setupBouncyCastle()

        if (sharedPref.getString(Constant.APP_CREDENTIALS) == "") {
            createAppCredentials()
        }

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
        rlUserAddress.setOnClickListener(this)
        btnScanToken.setOnClickListener(this)
        tvCollection.setOnClickListener(this)
        ivCollection.setOnClickListener(this)

        Log.e("FCM-TOKEN", FirebaseInstanceId.getInstance().token)
    }

    private fun checkUserLogin() {
        if (!CommonUtils.isNonLoginUser(this)) {
                notifyUserLogin()
        }
    }

    private fun createAppCredentials() {
        try {
            val keyPair = Keys.createEcKeyPair()
            val credentials = Credentials.create(keyPair)
            sharedPref.setCredentials(credentials, Constant.APP_CREDENTIALS)

        } catch (e: Exception) {
            Log.e("Error: ", e.message)
        }
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider.javaClass == BouncyCastleProvider::class.java) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }

    private fun setViewPager() {
        viewpagerMain.adapter = MainSliderAdapter(this, mainSliderList)
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
                drawer_layout.closeDrawer(GravityCompat.END)
            }

            R.id.tvCreateNew -> {
                val intent = Intent(this, CreateNewActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_START_CREATE_ACTIVITY)
                drawer_layout.closeDrawer(GravityCompat.END)
            }
            R.id.rlUserAddress -> {
                CommonUtils.copyToClipboard(this, tvUserAddress.text.toString())
                tvCopied.visibility = View.VISIBLE
            }

            R.id.tvCollection, R.id.ivCollection -> {
                drawer_layout.closeDrawer(GravityCompat.END)
                CollectionActivity.start(this@MainActivity)
            }

            R.id.tvLogout, R.id.ivLogout -> {
                val confirmDialogFragment = ConfirmDialogFragment.newInstance(resources.getString(R.string.dialog_title_logout),
                    resources.getString(R.string.msg_logout), true)
                confirmDialogFragment.setListener(object : IDialogListener {
                    override fun onDialogFinish(tag: String, ok: Boolean, result: Bundle) {
                        if (ok) {
                            logOut()
                        }
                    }
                    override fun onDialogCancel(tag: String) {

                    }
                })
                confirmDialogFragment.show(supportFragmentManager, ConfirmDialogFragment::class.java.simpleName)
            }

            R.id.btnScanToken -> CustomScanActivity.start(this, CustomScanActivity.ScanType.SCAN_GRAIL)
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

            lnNavigationItemUnexpanded.visibility = View.GONE
            tvLogout.visibility = View.GONE

            lnNavigationItemExpanded.visibility = View.VISIBLE

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
            tvCopied.visibility = View.GONE

            isExpanded = false
        }
    }

    private fun logOut() {
        sharedPref.clearPref()
        sharedPref.setBool(true, Constant.ACCOUNT_UNLINK)

        notifyUserLogout()
    }

    private fun notifyUserLogout() {
        isExpanded = true
        expandMenu()

        ivAddressNonLogin.visibility = View.VISIBLE
        lnNavigationItemUnLogin.visibility = View.VISIBLE

        cvQrAddress.visibility = View.GONE
        ivExpand.visibility = View.GONE
        lnNavigationItemUnexpanded.visibility = View.GONE
        tvLogout.visibility = View.GONE
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
