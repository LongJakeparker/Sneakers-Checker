package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SharedPref
import com.sneakers.sneakerschecker.model.SignIn
import com.sneakers.sneakerschecker.model.SignUp
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.security.Security


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 */
class RegisterUserInfoFragment : Fragment(), View.OnClickListener {

    private var fragmentView: View? = null

    private var btnNewWallet: Button? = null
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var service: Retrofit

    private lateinit var bundle: Bundle

    private lateinit var sharedPref: SharedPref
    private lateinit var credentials: Credentials

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_register_user_info, container, false)

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, 1)

        sharedPref = context?.let { SharedPref(it) }!!

        btnNewWallet = fragmentView!!.findViewById(R.id.btnCreateNewCreate)
        etFirstName = fragmentView!!.findViewById(R.id.etFirstNameCreate)
        etLastName = fragmentView!!.findViewById(R.id.etLastNameCreate)

        if (this.arguments != null) {
            bundle = this.arguments!!
        }

        setupBouncyCastle()

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        builder = AlertDialog.Builder(context)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(com.sneakers.sneakerschecker.R.layout.layout_loading_dialog)
        dialog = builder.create()

        btnNewWallet!!.setOnClickListener(this)

        return fragmentView
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnCreateNewCreate -> {
                newAccount()
            }
        }
    }

    fun newAccount() {
        try {
            dialog.show()
            val keyPair = Keys.createEcKeyPair()
            credentials = Credentials.create(keyPair)
            sharedPref.setCredentials(credentials, Constant.USER_CREDENTIALS)

            UserRegister()
        } catch (e: Exception) {
            dialog.dismiss()
            Log.e("Error: ", e.message)
        }
    }

    private fun UserRegister() {
        if (etFirstName.text.isEmpty() || etLastName.text.isEmpty()) {
            Toast.makeText(context, "All fields need to be filled", Toast.LENGTH_LONG).show()
        } else {

            var data = HashMap<String, String>()
            data.put("email", bundle.getString("username"))
            data.put("password", bundle.getString("password"))
            data.put("firstName", etFirstName.text.toString().trim())
            data.put("lastName", etLastName.text.toString().trim())
            data.put("networkAddress", credentials.address)
            data.put("registrationToken", sharedPref.getString(Constant.FCM_TOKEN))

            /*Create handle for the RetrofitInstance interface*/
            val call = service.create(AuthenticationApi::class.java).signUpApi(data)
            call.enqueue(object : Callback<SignUp> {

                override fun onResponse(call: Call<SignUp>, response: Response<SignUp>) {
                    if (response.code() == 201) {
                        //newAccount(response.body()!!.passwordHash)
                        RequestLogIn()
                    } else if (response.code() == 400) {
                        dialog.dismiss()
                        Toast.makeText(context, "Email has used", Toast.LENGTH_SHORT).show()
                    } else {
                        dialog.dismiss()
                        Toast.makeText(context, "Response Code: " + response.code(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SignUp>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(context, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun RequestLogIn() {

        val authToken = okhttp3.Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java)
            .signInApi(
                authToken,
                Constant.GRANT_TYPE_PASSWORD,
                bundle.getString("username"),
                bundle.getString("password")
            )
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                dialog.dismiss()

                if (response.code() == 200) {
                    sharedPref.setUser(response.body()!!, Constant.LOGIN_USER)

                    activity!!.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

//                    val transaction = activity!!.supportFragmentManager.beginTransaction()
//                    transaction.replace(R.id.authentication_layout, ConfirmRegisterFragment())
//                        .commit()
                } else if (response.code() == 400) {
                    Log.d("TAG", "onResponse - Status : " + response.errorBody()!!.string())
                }
            }

            override fun onFailure(call: Call<SignIn>, t: Throwable) {
                dialog.dismiss()
                Toast.makeText(context, "Something went wrong when login", Toast.LENGTH_SHORT).show()
            }

        })
    }
}
