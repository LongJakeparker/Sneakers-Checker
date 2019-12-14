package com.sneakers.sneakerschecker.screens.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.contract.ContractRequest
import com.sneakers.sneakerschecker.model.*
import com.sneakers.sneakerschecker.screens.activity.ObtainGrailActivity
import com.sneakers.sneakerschecker.screens.fragment.dialog.AlertDialogFragment
import com.sneakers.sneakerschecker.utils.CommonUtils
import kotlinx.android.synthetic.main.fragment_confirm_transfer.*
import kotlinx.android.synthetic.main.fragment_create_transfer.ivLimited
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailBrand
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailName
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailReleaseDate
import kotlinx.android.synthetic.main.fragment_create_transfer.tvGrailSize
import org.greenrobot.eventbus.EventBus

class ConfirmTransferFragment : Fragment(), View.OnClickListener {
    private var fragmentView: View? = null
    private var sneaker: SneakerModel? = null
    private var receiverEosName: String? = null
    private var receiverName: String? = null
    private var currentUser: User? = null
    private var password: String? = null
    private var receiverEosId: Int? = null

    private lateinit var sharedPref: SharedPref

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_confirm_transfer, container, false)

        sharedPref = context?.let { SharedPref(it) }!!
        currentUser = sharedPref.getUser(Constant.LOGIN_USER)?.user

        sneaker = activity?.intent?.getSerializableExtra(Constant.EXTRA_SNEAKER) as SneakerModel
        receiverName = arguments?.getString(Constant.EXTRA_RECEIVER_NAME)
        receiverEosName = arguments?.getString(Constant.EXTRA_RECEIVER_EOS_NAME)
        receiverEosId = arguments?.getInt(Constant.EXTRA_RECEIVER_ID, 0)


        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadSneakerInfo()
        loadReceiverInfo()

        btnBack.setOnClickListener(this)
        btnConfirm.setOnClickListener(this)
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

    private fun loadReceiverInfo() {
        tvReceiverName.text = receiverName
        tvReceiverEosName.text = receiverEosName
    }

    override fun onClick(v: View?) {
        when (v) {
            btnBack -> activity?.onBackPressed()

            btnConfirm -> {
                InputPasswordDialog.show(
                    this@ConfirmTransferFragment, fragmentManager!!,
                    resources.getString(R.string.dialog_title_confirm_passcode),
                    resources.getString(R.string.dialog_message_confirm_passcode)
                )
            }
        }
    }

    private fun createTransaction() {
        CommonUtils.toggleLoading(fragmentView, true)
        val jsonData = ContractRequest.transferSneakerJson(
            sneaker?.id!!,
            receiverEosId!!
        )

        ContractRequest.callEosApi(currentUser?.eosName!!,
            ContractRequest.METHOD_TRANSFER,
            jsonData,
            getString(R.string.format_eascrypt_password, password),
            currentUser?.encryptedPrivateKey,
            null,
            object : ContractRequest.Companion.EOSCallBack {
                override fun onDone(result: Any?, e: Throwable?) {
                    CommonUtils.toggleLoading(fragmentView, false)
                    if (e == null) {
                        Toast.makeText(context, "Transaction id: $result", Toast.LENGTH_LONG).show()
                        ObtainGrailActivity.start(activity!!, sneaker!!)
                        EventBus.getDefault().post(ReloadCollectionEvent())
                        activity?.finish()
                    } else {
                        if (e.message == "pad block corrupted") {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(getString(R.string.msg_wrong_password))
                            alertDialogFragment.show(
                                fragmentManager!!,
                                AlertDialogFragment::class.java.simpleName
                            )
                        } else {
                            val alertDialogFragment =
                                AlertDialogFragment.newInstance(e.message)
                            alertDialogFragment.show(
                                fragmentManager!!,
                                AlertDialogFragment::class.java.simpleName
                            )
                        }
                        password = ""
                    }
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            if (data?.extras?.containsKey(Constant.EXTRA_USER_PASSWORD)!!) {
                password = data.extras?.getString(Constant.EXTRA_USER_PASSWORD)
                createTransaction()
            }
        }
    }
}