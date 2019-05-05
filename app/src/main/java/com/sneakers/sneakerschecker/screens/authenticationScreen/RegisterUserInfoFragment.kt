package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import okhttp3.Credentials
import org.web3j.protocol.admin.Admin
import org.web3j.protocol.http.HttpService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 */
class RegisterUserInfoFragment : Fragment(), View.OnClickListener {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var fragmentView: View? = null

    private var btnNewWallet: Button? = null
    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText

    private lateinit var builder: AlertDialog.Builder
    private lateinit var dialog: AlertDialog
    private lateinit var service: Retrofit

    private lateinit var bundle: Bundle

    private lateinit var sharedPref: SharedPref

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

        //Get instant retrofit
        service = RetrofitClientInstance().getRetrofitInstance()!!

        builder = AlertDialog.Builder(context)
        builder.setCancelable(false) // if you want user to wait for some process to finish,
        builder.setView(R.layout.layout_loading_dialog)
        dialog = builder.create()

        btnNewWallet!!.setOnClickListener(this)

        return fragmentView
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnCreateNewCreate -> {
                UserRegister()
            }
        }
    }

    fun newAccount(password: String) {
        Thread {
            try {
                val admin = Admin.build(HttpService(Constant.ETHEREUM_API_URL))
                //Web3Instance.setInstance(admin)
                val rxNewAccount = admin.personalNewAccount(password)
                    .flowable()
                    .subscribeOn(Schedulers.io())
                    .subscribe({ response -> sharedPref.setString(response.result, Constant.ACCOUNT_ID) },
                        {throwable ->
                            Log.e("TAG", "Throwable " + throwable.message)},
                        {
                            RequestLogIn()
                        }
                    )
                compositeDisposable.add(rxNewAccount)
            } catch (e: Exception) {
                dialog.dismiss()
                activity?.runOnUiThread {
                    Toast.makeText(activity, "Connect Blockchain Failed", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun UserRegister() {
        if (etFirstName.text.isEmpty() || etLastName.text.isEmpty()) {
            Toast.makeText(context, "All fields need to be filled", Toast.LENGTH_LONG).show()
        } else {
            dialog.show()

            var data = HashMap<String, String>()
            data.put("email", bundle.getString("username"))
            data.put("password", bundle.getString("password"))
            data.put("firstName", etFirstName.text.toString().trim())
            data.put("lastName", etLastName.text.toString().trim())

            /*Create handle for the RetrofitInstance interface*/
            val call = service.create(AuthenticationApi::class.java!!).signUpApi(data)
            call.enqueue(object : Callback<SignUp> {

                override fun onResponse(call: Call<SignUp>, response: Response<SignUp>) {
                    if (response.code() == 201) {
                        //newAccount(response.body()!!.passwordHash)
                        newAccount(bundle.getString("password"))
                    } else if (response.code() == 400) {
                        dialog.dismiss()
                        Toast.makeText(context, "Email has used", Toast.LENGTH_SHORT).show()
                    } else {
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

        val authToken = Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
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
                    sharedPref.setUser(response.body()!!, Constant.WALLET_USER)

                    val intent = Intent(activity, MainActivity::class.java)
                    startActivity(intent)
                    activity!!.finish()
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
