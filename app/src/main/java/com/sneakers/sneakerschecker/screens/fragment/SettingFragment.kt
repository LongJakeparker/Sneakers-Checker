package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.view.View
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment: BaseFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvChangePassword.setOnClickListener {
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.fragment_enter_from_right,
                R.anim.fragment_exit_to_left,
                R.anim.fragment_enter_from_left,
                R.anim.fragment_exit_to_right
            )
                .replace(R.id.fl_transfer_content, ChangePasswordFragment())
                .addToBackStack(null).commit()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_setting
    }

    override fun isShowBackButton(): Boolean {
        return true
    }

    override fun getScreenTitleId(): Int {
        return R.string.label_button_setting
    }

}