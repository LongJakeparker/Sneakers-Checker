package com.sneakers.sneakerschecker.screens.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.CollectionAdapter
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.fragment.InputPasswordDialog
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_collection.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.Type


class CollectionActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var sharedPref: SharedPref
    private lateinit var service: Retrofit
    private var listCollection: ArrayList<SneakerModel> = ArrayList()
    private var listContractCollection: ArrayList<SneakerContractModel> = ArrayList()
    private var userInfo: User? = null
    private var adapter: CollectionAdapter? = null

    companion object {
        fun start(activity: Activity) {
            val intent = Intent(activity, CollectionActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)
        EventBus.getDefault().register(this)

        sharedPref = SharedPref(this)

        userInfo = sharedPref.getUser(Constant.LOGIN_USER)?.user

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        getCollectionFromContract()

        btnScan.setOnClickListener(this)
        btnItemSale.setOnClickListener(this)
        btnScanNoData.setOnClickListener(this)
        btnBack.setOnClickListener(this)
        btnItemStolen.setOnClickListener(this)
        btnCloseReport.setOnClickListener(this)
        blurView.setOnClickListener(this)
        llConfirmStolen.setOnClickListener(this)
        btnConfirmStolen.setOnClickListener(this)
    }

    private fun getCollectionFromContract() {
        CommonUtils.toggleLoading(window.decorView.rootView, true)
        ContractRequest.getTableRowObservable(Constant.CONTRACT_TABLE_SNEAKER,
            userInfo?.id?.toLong()!!,
            true,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    val type: Type =
                        object : TypeToken<List<SneakerContractModel?>?>() {}.type
                    listContractCollection = Gson().fromJson((result as JSONArray).toString(), type)

                    getCollection()
                }
            })
    }

    private fun getCollection() {
        val accessToken = "Bearer " + sharedPref.getUser(Constant.LOGIN_USER)?.accessToken
        val param = HashMap<String, Any>()
        param["sneakerIdList"] = getListSneakerId()
        val call = service.create(MainApi::class.java)
            .getCollection(accessToken, userInfo?.id!!, param)
        call.enqueue(object : Callback<CollectionModel> {
            override fun onFailure(call: Call<CollectionModel>, t: Throwable) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                Toast.makeText(this@CollectionActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<CollectionModel>,
                response: Response<CollectionModel>
            ) {
                CommonUtils.toggleLoading(window.decorView.rootView, false)
                if (response.isSuccessful) {
                    listCollection.clear()
                    listCollection.addAll(response.body()?.collection!!)
                    checkActivateSneaker()
                    adapter = CollectionAdapter(listCollection, this@CollectionActivity)
                    viewPagerCollection.adapter = adapter
                    viewPagerCollection.addOnPageChangeListener(viewChangeListener)
                    if (listCollection.size > 0) {
                        btnScan.visibility = View.VISIBLE
                        showButtonStolen(0)
                        llViewNoData.visibility = View.GONE
                    } else {
                        llViewNoData.visibility = View.VISIBLE
                    }
                } else {
                    Toast.makeText(this@CollectionActivity, response.message(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })

    }

    private fun checkActivateSneaker() {
        for (collectionItem in listCollection) {
            val contractItem = binarySearch(
                listContractCollection,
                0,
                listContractCollection.size - 1,
                collectionItem.id!!
            )

            if (contractItem != null) {
                if (contractItem.status == Constant.ItemCondition.STOLEN) {
                    collectionItem.isCardActivate = false
                }
            }
        }
    }

    fun binarySearch(
        array: ArrayList<SneakerContractModel>,
        left: Int,
        right: Int,
        item: Long
    ): SneakerContractModel? {
        if (right >= left) {
            val mid: Int =
                left + (right - left) / 2 // Tương đương (l+r)/2 nhưng ưu điểm tránh tràn số khi l,r lớn
            // Nếu arr[mid] = x, trả về chỉ số và kết thúc.
            if (array[mid].id == item) return array[mid]
            // Nếu arr[mid] > x, thực hiện tìm kiếm nửa trái của mảng
            return if (array[mid].id!! > item) binarySearch(
                array,
                left,
                mid - 1,
                item
            ) else binarySearch(
                array,
                mid + 1,
                right,
                item
            )
            // Nếu arr[mid] < x, thực hiện tìm kiếm nửa phải của mảng
        }

        // Nếu không tìm thấy
        return null
    }

    private fun getListSneakerId(): java.util.ArrayList<Long> {
        val listId = ArrayList<Long>()
        for (i in 0 until listContractCollection.size) {
            listId.add(listContractCollection[i].id!!)
        }

        return listId
    }

    val viewChangeListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {

        }

        override fun onPageSelected(position: Int) {
            showButtonStolen(position)
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

    fun showButtonStolen(position: Int) {
        if (listCollection[position].isCardActivate) {
            cvFound.visibility = View.GONE
            rlBtnSaleAndStolen.visibility = View.VISIBLE
        } else {
            cvFound.visibility = View.VISIBLE
            rlBtnSaleAndStolen.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            llConfirmStolen -> {
            }

            btnScan, btnScanNoData -> CustomScanActivity.start(
                this,
                CustomScanActivity.ScanType.SCAN_GRAIL
            )

            btnItemSale -> GrailsTradingActivity.start(
                this,
                listCollection[viewPagerCollection.currentItem]
            )

            btnItemStolen -> {
                llConfirmStolen.visibility = View.VISIBLE
                blurView.visibility = View.VISIBLE
                blurView.isClickable = true
                blurView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in_view))
            }

            btnCloseReport, blurView -> closeReportView()

            btnConfirmStolen -> InputPasswordDialog.show(supportFragmentManager, "", "", object :
                InputPasswordDialog.PasscodeResultInterface {
                override fun onReceivePasscode(passcode: String) {
                    closeReportView()
                    reportSotlen(passcode)
                }

            })

            btnBack -> onBackPressed()
        }
    }

    fun closeReportView() {
        llConfirmStolen.visibility = View.GONE
        blurView.visibility = View.GONE
        blurView.isClickable = false
        blurView.startAnimation(
            AnimationUtils.loadAnimation(
                this,
                R.anim.fade_out_view
            )
        )
    }

    private fun reportSotlen(passcode: String) {
        CommonUtils.toggleLoading(window.decorView.rootView, true)
        val jsonData = ContractRequest.stolenSneakerJson(
            listCollection[viewPagerCollection.currentItem].id!!
        )

        ContractRequest.callEosApi(userInfo?.eosName!!,
            ContractRequest.METHOD_REPORT_STOLEN,
            jsonData,
            getString(R.string.format_eascrypt_password, passcode),
            userInfo?.encryptedPrivateKey,
            null,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    CommonUtils.toggleLoading(window.decorView.rootView, false)
                    if (e == null) {
                        Toast.makeText(
                            this@CollectionActivity,
                            "Transaction id: $result",
                            Toast.LENGTH_LONG
                        ).show()
                        listCollection[viewPagerCollection.currentItem].isCardActivate = false
                        adapter?.notifyDataSetChanged()

                    } else {
                        if (e.message == "pad block corrupted") {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(getString(R.string.msg_wrong_password))
                            alertDialogFragment.show(
                                supportFragmentManager,
                                AlertDialogFragment::class.java.simpleName
                            )
                        } else {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(e.message)
                            alertDialogFragment.show(
                                supportFragmentManager,
                                AlertDialogFragment::class.java.simpleName
                            )
                        }
                    }
                }

            })
    }

    @Subscribe
    fun onEvent(reloadCollectionEvent: ReloadCollectionEvent) {
        getCollectionFromContract()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}
