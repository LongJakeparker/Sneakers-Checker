package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sneakers.sneakerschecker.R
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sneakers.sneakerschecker.model.Validation


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class CreateNewFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private var btnNewWallet: Button? = null
    private lateinit var etUserName: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_new, container, false)

        btnNewWallet = fragmentView!!.findViewById(R.id.btnNextCreate)
        etUserName = fragmentView!!.findViewById(R.id.etUserNameCreate)
        etPassword = fragmentView!!.findViewById(R.id.etPasswordCreate)
        etConfirmPassword = fragmentView!!.findViewById(R.id.etConfirmPasswordCreate)

        btnNewWallet!!.setOnClickListener(this)

        return fragmentView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnNextCreate -> nextRegister()
        }
    }

    private fun callValidatePassword() {
        val password = etPassword.text.toString()

        if (!Validation.validatePassword(password)!!) {
            etPassword.error = "Password need to be included [@#$%^&*][0-9][a-z][A-Z]"
        }
    }

    private fun callValidateEmail() {
        val email = etUserName.text.toString()

        if (!Validation.validateEmail(email)!!) {
            etUserName.error = "Please enter a valid email address"
        }
    }

    fun nextRegister() {
        if (etUserName.text.isEmpty() || etPassword.text.isEmpty() || etConfirmPassword.text.isEmpty()) {
            Toast.makeText(context, "All fields need to be filled", Toast.LENGTH_LONG).show()
        }
        else {
            callValidateEmail()
            callValidatePassword()
            if (Validation.validateEmail(etUserName.text.toString())!! && Validation.validatePassword(etPassword.text.toString())!!) {
                if (etPassword.text.toString().trim() == etConfirmPassword.text.toString().trim()) {
                    val bundle = Bundle()
                    bundle.putString("username", etUserName.text.toString().trim())
                    bundle.putString("password", etPassword.text.toString().trim())

                    val registerUserInfoFragment = RegisterUserInfoFragment()
                    registerUserInfoFragment.arguments = bundle

                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.authentication_layout, registerUserInfoFragment)
                        .addToBackStack(null)
                        .commit()
                }
                else {
                    etConfirmPassword.error = "Confirm password not match"
                    etPassword.error = null
                    etUserName.error = null
                }
            }
        }
    }
}
