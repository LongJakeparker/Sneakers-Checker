package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.MainApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SneakerModel
import com.sneakers.sneakerschecker.screens.activity.CustomScanActivity
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_create_transfer.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class CreateTransferFragment : Fragment(), View.OnClickListener {
    private val REQUEST_CODE_SCAN_EOS_NAME = 1002

    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverName: String = ""
    private var receiverId: Int? = null
    private lateinit var service: Retrofit

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_transfer, container, false)

        sneaker = activity?.intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel

        service = RetrofitClientInstance().getRetrofitInstance()!!

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSneakerInfo()

        btnClose.setOnClickListener(this)
        ivScanEosName.setOnClickListener(this)
        btnContinue.setOnClickListener(this)
        root.setOnClickListener(this)
        etReceiverEosName.addTextChangedListener(textWatcher)
    }

    private fun loadSneakerInfo() {
        tvGrailName.text = sneaker?.model
        tvGrailBrand.text = sneaker?.brand
        tvGrailSize.text = sneaker?.size.toString()
        tvGrailReleaseDate.text = sneaker?.releaseDate

        if (sneaker?.limitedEdition!!) {
            ivLimited.visibility = View.VISIBLE
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isNotEmpty()) {
                ivScanEosName.setImageResource(R.drawable.ic_close_has_bg)
            } else {
                ivScanEosName.setImageResource(R.drawable.ic_scan_has_bg)
            }
            btnContinue.isEnabled = validateData()
        }
    }

    private fun validateData(): Boolean {
        return !(etReceiverEosName.text.isEmpty())
    }

    override fun onClick(v: View?) {
        when (v) {
            btnClose -> activity?.finish()

            ivScanEosName -> {
                if (etReceiverEosName.text.isEmpty()) {
                    CustomScanActivity.startForResult(
                        this,
                        CustomScanActivity.ScanType.SCAN_EOS_NAME,
                        REQUEST_CODE_SCAN_EOS_NAME
                    )
                } else {
                    etReceiverEosName.text.clear()
                }
            }

            root -> CommonUtils.hideKeyboard(activity)

            btnContinue -> checkEosName()
        }
    }

    private fun checkEosName() {
        CommonUtils.toggleLoading(fragmentView, true)
        val call = service.create(MainApi::class.java)
            .getUserNameByEosName(etReceiverEosName.text.toString().trim())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                CommonUtils.toggleLoading(fragmentView, false)
                Toast.makeText(
                    context,
                    t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                CommonUtils.toggleLoading(fragmentView, false)
                when {
                    response.isSuccessful -> {
                        try {
                            val jsonObject = JSONObject(response.body()?.string())
                            receiverName = jsonObject.getString("username")
                            receiverId = jsonObject.getInt("id")
                            confirmTransaction()
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }

                    response.code() == 400 -> {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance(getString(R.string.msg_eos_name_not_valid))
                        alertDialogFragment.show(
                            fragmentManager!!,
                            AlertDialogFragment::class.java.simpleName
                        )
                    }

                    else -> {
                        val alertDialogFragment =
                            AlertDialogFragment.newInstance("Response Code ${response.code()}: ${response.message()}")
                        alertDialogFragment.show(
                            fragmentManager!!,
                            AlertDialogFragment::class.java.simpleName
                        )
//                        Toast.makeText(
//                            context,
//                            "Response Code ${response.code()}: ${response.message()}",
//                            Toast.LENGTH_SHORT
//                        ).show()
                    }
                }
            }

        })
    }

    private fun confirmTransaction() {
        val confirmTransferFragment = ConfirmTransferFragment()
        val bundle = Bundle()
        bundle.putSerializable(Constant.EXTRA_SNEAKER, sneaker)
        bundle.putString(Constant.EXTRA_RECEIVER_NAME, receiverName)
        bundle.putInt(Constant.EXTRA_RECEIVER_ID, receiverId!!)
        bundle.putString(
            Constant.EXTRA_RECEIVER_EOS_NAME,
            etReceiverEosName.text.toString().trim()
        )
        confirmTransferFragment.arguments = bundle
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fragment_enter_from_right,
            R.anim.fragment_exit_to_left,
            R.anim.fragment_enter_from_left,
            R.anim.fragment_exit_to_right
        )
            .replace(R.id.fl_transfer_content, confirmTransferFragment)
            .addToBackStack(null).commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == REQUEST_CODE_SCAN_EOS_NAME && resultCode == Activity.RESULT_OK -> {
                val eosName = data?.extras?.getString(Constant.EXTRA_USER_EOS_NAME)
                etReceiverEosName.setText(eosName)
                ivScanEosName.setImageResource(R.drawable.ic_close_has_bg)
            }
        }
    }
}