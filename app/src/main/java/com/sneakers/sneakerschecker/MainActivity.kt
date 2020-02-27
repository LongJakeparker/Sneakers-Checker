package com.sneakers.sneakerschecker

import android.animation.LayoutTransition
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.sneakers.sneakerschecker.`interface`.IDialogListener
import com.sneakers.sneakerschecker.adapter.SneakerBoardAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.customViews.VerticalSpaceItemDecoration
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.activity.*
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.screens.fragment.dialog.ConfirmDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer_menu.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : BaseActivity(), View.OnClickListener {
    private val TIME_AUTO_NEXT = 4000
    private val REQUEST_CODE_START_LOGIN_ACTIVITY = 1000

    private lateinit var sharedPref: SharedPref
    private var isExpanded: Boolean = false
    private lateinit var service: Retrofit
    private var listSneaker = ArrayList<SneakerBoardModel?>()
    private var sneakerBoardAdapter: SneakerBoardAdapter? = null

    private val mainSliderList = arrayListOf(
        MainSliderItem(R.string.text_explore_1, R.drawable.drawable_explore_pager_1),
        MainSliderItem(R.string.text_explore_2, R.drawable.drawable_explore_pager_2)
    )

    var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = SharedPref(this)
        service = RetrofitClientInstance().getRetrofitInstance()!!

        checkUserLogin()

        getListSneakerBoard()

        val isObtained = intent?.getBooleanExtra(Constant.EXTRA_IS_OBTAINED, false)
        if (isObtained != null && isObtained) {
            val sneakerInfo = intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel
            ObtainGrailActivity.start(baseContext, sneakerInfo, true)
        }

        tvLogin.setOnClickListener(this)
        tvCreateNew.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
        ivLogout.setOnClickListener(this)
        rlUserPhone.setOnClickListener(this)
        btnScanToken.setOnClickListener(this)
        tvCollection.setOnClickListener(this)
        ivCollection.setOnClickListener(this)
        tvCardManage.setOnClickListener(this)
        ivCardManage.setOnClickListener(this)
        tvSetting.setOnClickListener(this)
        ivSetting.setOnClickListener(this)
        tvProfile.setOnClickListener(this)
        ivProfile.setOnClickListener(this)

        Log.e("FCM-TOKEN", sharedPref.getString(Constant.FCM_TOKEN))
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_sneaker_board
    }

    override fun onResume() {
        super.onResume()
        checkUserLogin()
    }

    private fun getBrainTreeClientToken() {
        val accessToken = "Bearer " + CommonUtils.getCurrentUser(this)?.accessToken
        val call = service.create(MainApi::class.java)
            .getBrainTreeClientToken(accessToken, CommonUtils.getCurrentUser(this)?.user?.id!!)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    supportFragmentManager,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body()?.string())
                    val brainTreeClientToken = jsonObject.getString("clientToken")
                    sharedPref.setString(brainTreeClientToken, Constant.BRAINTREE_TOKEN)
                } else {
                    if (response.code() == 401) {
                        logOut()
                    } else {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance(response.message())
                        alertDialogFragment.show(
                            supportFragmentManager,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }
                }
            }

        })
    }

    private fun checkUserLogin() {
        if (!CommonUtils.isNonLoginUser(this)) {
            notifyUserLogin()
        }
    }

//    private fun setViewPager() {
//        viewpagerMain.adapter = MainSliderAdapter(this, mainSliderList)
//        viewpagerMain.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
//            override fun onPageScrollStateChanged(state: Int) {
//            }
//
//            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
//
//            }
//
//            override fun onPageSelected(position: Int) {
//                startTimer()
//            }
//
//        })
//        viewpagerMain.setPageTransformer(false) { view, position ->
//            view.translationX = view.width * -position
//
//            if (position <= -1.0F || position >= 1.0F) {
//                view.alpha = 0.0F
//            } else if (position == 0.0F) {
//                view.alpha = 1.0F
//            } else {
//                // position is between -1.0F & 0.0F OR 0.0F & 1.0F
//                view.alpha = 1.0F - Math.abs(position)
//            }
//        }
//        startTimer()
//    }

    private fun setListAdapter() {
        val layoutManager = LinearLayoutManager(this)
        rvContent.layoutManager = layoutManager
        sneakerBoardAdapter = SneakerBoardAdapter(listSneaker)
        sneakerBoardAdapter?.setCopyListener(object : SneakerBoardAdapter.CopyListener {
            override fun onSelectItem(copyData: String?) {
                CommonUtils.copyToClipboard(this@MainActivity, copyData!!)
            }

        })
        sneakerBoardAdapter?.setListener(object : SneakerBoardAdapter.Listener {
            override fun onSelectItem(itemId: Long?, position: Int) {
                getOwnerId(itemId, position)
            }

            override fun onNotifyLogin() {
                val intent = Intent(this@MainActivity, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY)
            }

        })
        rvContent.adapter = sneakerBoardAdapter
        rvContent.isFocusable = false
        rvContent.addItemDecoration(VerticalSpaceItemDecoration(resources.getDimensionPixelOffset(R.dimen.activity_margin_16dp)))
    }

    private fun getListSneakerBoard() {
        val call = service.create(MainApi::class.java)
            .getListSneakerAvailable()
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.message)
                alertDialogFragment.show(
                    supportFragmentManager,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val jsonObject = JSONObject(response.body()?.string())
                    val jsonArrayList = jsonObject.getJSONArray("availableTrade")
                    var userListType = object : TypeToken<List<SneakerBoardModel>>() {}.type
                    listSneaker = Gson().fromJson(jsonArrayList.toString(), userListType)
                    setListAdapter()
                }
            }

        })
    }

    private fun getOwnerId(itemId: Long?, position: Int) {
        if (itemId == null)
            return

        ContractRequest.getTableRowObservable(ContractRequest.getQueryTableRow(Constant.CONTRACT_TABLE_SNEAKER,
            itemId),
            object : ContractRequest.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    if (e == null) {
                        val sneakerContractModel =
                            Gson().fromJson((result as JSONArray).getJSONObject(0).toString(), SneakerContractModel::class.java)
                        getOwnerInfo(sneakerContractModel.owner_id, position)
                    } else {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance(e.localizedMessage)
                        alertDialogFragment.show(
                            supportFragmentManager,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }
                }
            })
    }

    private fun getOwnerInfo(ownerId: Int?, position: Int) {
        if (ownerId == null)
            return

        val call = service.create(MainApi::class.java)
            .getUserInformation(ownerId)
        call.enqueue(object : Callback<CollectorModel> {
            override fun onFailure(call: Call<CollectorModel>, t: Throwable) {
                val alertDialogFragment =
                    AlertDialogFragment.newInstance(t.localizedMessage)
                alertDialogFragment.show(
                    supportFragmentManager,
                    AlertDialogFragment::class.java.simpleName
                )
            }

            override fun onResponse(
                call: Call<CollectorModel>,
                response: Response<CollectorModel>
            ) {
                if (response.isSuccessful) {
                    if (response.body() != null) {
                        val userInfo = response.body()!!.collector!!

                        listSneaker[position]?.OwnerInfo = userInfo
                        sneakerBoardAdapter?.notifyItemChanged(position)
                    }
                } else {
                    val alertDialogFragment =
                        AlertDialogFragment.newInstance(response.message())
                    alertDialogFragment.show(
                        supportFragmentManager,
                        AlertDialogFragment::class.java.simpleName
                    )
                }
            }

        })
    }

    override fun onClick(v: View?) {
        when (v) {

            tvLogin -> {
                val intent = Intent(this, LoginActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_START_LOGIN_ACTIVITY)
                drawer_layout.closeDrawer(GravityCompat.END)
            }

            tvCreateNew -> {
                CreateNewActivity.start(this)
                drawer_layout.closeDrawer(GravityCompat.END)
            }
            rlUserPhone -> {
                CommonUtils.copyToClipboard(this, tvUserPhone.text.toString())
                tvCopied.visibility = View.VISIBLE
            }

            tvCollection, ivCollection -> {
                CollectionActivity.start(this@MainActivity)
                drawer_layout.closeDrawer(GravityCompat.END)
            }

            tvLogout, ivLogout -> {
                val confirmDialogFragment = ConfirmDialogFragment.newInstance(
                    resources.getString(R.string.dialog_title_logout),
                    resources.getString(R.string.msg_logout), true
                )
                confirmDialogFragment.setListener(object : IDialogListener {
                    override fun onDialogFinish(tag: String, ok: Boolean, result: Bundle) {
                        if (ok) {
                            logOut()
                        }
                    }

                    override fun onDialogCancel(tag: String) {

                    }
                })
                confirmDialogFragment.show(
                    supportFragmentManager,
                    ConfirmDialogFragment::class.java.simpleName
                )
            }

            btnScanToken -> CustomScanActivity.start(this, CustomScanActivity.ScanType.SCAN_GRAIL)

            tvCardManage, ivCardManage -> {
                ManageCardActivity.start(this)
                drawer_layout.closeDrawer(GravityCompat.END)
            }

            tvProfile, ivProfile -> {
                UserProfileActivity.start(this)
                drawer_layout.closeDrawer(GravityCompat.END)
            }
        }
    }

    fun notifyUserLogin() {
        val userInfo = sharedPref.getUser(Constant.LOGIN_USER)
        tvUserName.text = userInfo?.user?.username
        tvUserPhone.text = userInfo?.user?.userIdentity

        if (!userInfo?.user?.avatar.isNullOrEmpty()) {
            Picasso.get().load(userInfo?.user?.avatar).into(ivAvatar)
        }

        ivAvatarNonLogin.visibility = View.GONE
        lnNavigationItemUnLogin.visibility = View.GONE

        ivAvatar.visibility = View.VISIBLE
        ivExpand.visibility = View.VISIBLE
        lnNavigationItemUnexpanded.visibility = View.VISIBLE
        tvLogout.visibility = View.VISIBLE

        ivExpand.setOnClickListener { expandMenu() }

        getBrainTreeClientToken()
    }

    private fun expandMenu() {
        if (!isExpanded) {
            val layoutTransition = (lnTopNavigation.parent as ConstraintLayout).layoutTransition
            layoutTransition.setDuration(300)
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            ivAvatar.layoutParams.width =
                resources.getDimension(R.dimen.avatar_size_expanded).toInt()
            ivAvatar.layoutParams.height =
                resources.getDimension(R.dimen.avatar_size_expanded).toInt()
            ivAvatar.requestLayout()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, resources.getDimension(R.dimen.activity_margin_24dp).toInt(), 0, 0)
            tvUserName.layoutParams = params

            rlUserPhone.visibility = View.VISIBLE
            ivExpand.setImageResource(R.drawable.ic_expand_up)

            lnNavigationItemUnexpanded.visibility = View.GONE
            tvLogout.visibility = View.GONE

            lnNavigationItemExpanded.visibility = View.VISIBLE

            isExpanded = true
        } else {
            val layoutTransition = (lnTopNavigation.parent as ConstraintLayout).layoutTransition
            layoutTransition.setDuration(300)
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
            ivAvatar.layoutParams.width = resources.getDimension(R.dimen.avatar_size_normal).toInt()
            ivAvatar.layoutParams.height =
                resources.getDimension(R.dimen.avatar_size_normal).toInt()
            ivAvatar.requestLayout()

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, resources.getDimension(R.dimen.activity_margin_16dp).toInt(), 0, 0)
            tvUserName.layoutParams = params

            rlUserPhone.visibility = View.GONE
            ivExpand.setImageResource(R.drawable.ic_expand_down)
            lnNavigationItemExpanded.visibility = View.GONE

            lnNavigationItemUnexpanded.visibility = View.VISIBLE
            tvLogout.visibility = View.VISIBLE
            tvCopied.visibility = View.GONE

            isExpanded = false
        }
    }

    private fun logOut() {
        sharedPref.setUser(null, Constant.LOGIN_USER)
        sharedPref.setBool(true, Constant.ACCOUNT_UNLINK)

        notifyUserLogout()
    }

    private fun notifyUserLogout() {
        isExpanded = true
        expandMenu()

        tvUserName.text = getString(R.string.text_user_name_non_login)

        ivAvatarNonLogin.visibility = View.VISIBLE
        lnNavigationItemUnLogin.visibility = View.VISIBLE

        ivAvatar.visibility = View.GONE
        ivExpand.visibility = View.GONE
        lnNavigationItemUnexpanded.visibility = View.GONE
        tvLogout.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            (requestCode == REQUEST_CODE_START_LOGIN_ACTIVITY) &&
                    resultCode == Activity.RESULT_OK -> {
                notifyUserLogin()
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        countDownTimer!!.cancel()
//    }

//    private fun cancelTimer() {
//        if (countDownTimer != null) {
//            countDownTimer!!.cancel()
//            countDownTimer = null
//        }
//    }

//    private fun startTimer() {
//        cancelTimer()
//        countDownTimer = object : CountDownTimer(TIME_AUTO_NEXT.toLong(), 500) {
//            override fun onTick(millisUntilFinished: Long) {}
//
//            override fun onFinish() {
//                var nextItem = viewpagerMain.currentItem + 1
//                if (nextItem >= viewpagerMain.adapter!!.count) {
//                    nextItem = viewpagerMain.adapter!!.count / 2
//                }
//                viewpagerMain.setCurrentItem(nextItem, true)
//            }
//        }
//        countDownTimer!!.start()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_menu) {
            drawer_layout.openDrawer(GravityCompat.END)
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
