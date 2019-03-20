package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer

import com.sneakers.sneakerschecker.R
import android.widget.Toast
import com.sneakers.sneakerschecker.MainActivity
import com.sneakers.sneakerschecker.api.AuthenticationApi
import com.sneakers.sneakerschecker.model.RetrofitClientInstance
import com.sneakers.sneakerschecker.model.SignIn
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import okhttp3.Credentials
import okhttp3.ResponseBody
import retrofit2.Retrofit
import android.R.string
import android.os.Environment
import android.support.v4.app.FragmentManager
import android.support.v7.widget.GridLayoutManager
import com.google.gson.TypeAdapter
import com.google.gson.Gson
import android.util.Log
import android.widget.EditText
import com.sneakers.sneakerschecker.adapter.PhraseAdapter
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.CipherException
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Security
import kotlin.concurrent.thread


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

    private var web3: Web3j? = null
    private var credentials: org.web3j.crypto.Credentials? = null
    private lateinit var mnemonic: String

    private lateinit var bundle: Bundle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBouncyCastle()
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider::class.equals(BouncyCastleProvider::class.java)) {
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_register_user_info, container, false)

        val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, 1)

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

    fun connectEthereumNode() {
        web3 = Web3j.build( HttpService(Constant.ETHEREUM_API_KEY))
//        try {
//            var clientVersion = web3!!.web3ClientVersion().sendAsync().get()
//            if(!clientVersion.hasError()){
//                //Connected
//
//            }
//            else {
//                //Show Error
//            }
//        }
//        catch (e: Exception) {
//            //Show Error
//            Log.e("TAG", e.toString())
//        }
        generateWallet(bundle.getString("password"))
    }

    fun generateWallet(password: String) {
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!path.exists()) {
                path.mkdir()
            }
            var wallet = WalletUtils.generateBip39Wallet(password, File(path.toString()))
            val keystoreFileName = wallet.filename
            Log.e("TAG", "generateWallet: $keystoreFileName")

            // my mnemonic
            mnemonic = wallet.mnemonic
            Log.e("TAG", "generateWallet: $mnemonic")

            credentials = WalletUtils.loadBip39Credentials(password, mnemonic)

            val sharedPref = SharedPref(this.context!!)
            sharedPref.setString(credentials!!.address, Constant.WALLET_ADDRESS)
            sharedPref.setString(credentials!!.ecKeyPair.publicKey.toString(), Constant.WALLET_PUBLIC_KEY)
            sharedPref.setString(credentials!!.ecKeyPair.privateKey.toString(), Constant.WALLET_PRIVATE_KEY)
            sharedPref.setString(mnemonic, Constant.WALLET_MNEMONIC)

        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchProviderException) {
            e.printStackTrace()
        } catch (e: InvalidAlgorithmParameterException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CipherException) {
            e.printStackTrace()
        }

    }

    private fun UserRegister() {
        if (etFirstName.text.isEmpty() || etLastName.text.isEmpty()) {
            Toast.makeText(context, "All fields need to be filled", Toast.LENGTH_LONG).show()
        }
        else {
            dialog.show()

            connectEthereumNode()

            var data = HashMap<String, String>()
            data.put("email", bundle.getString("username"))
            data.put("password", bundle.getString("password"))
            data.put("firstName", etFirstName.text.toString().trim())
            data.put("lastName", etLastName.text.toString().trim())

            /*Create handle for the RetrofitInstance interface*/
            val call = service.create(AuthenticationApi::class.java!!).signUpApi(data)
            call.enqueue(object : Callback<ResponseBody> {

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 201) {
                        RequestLogIn()
                    }
                    else if (response.code() == 400) {
                        dialog.dismiss()
                        Toast.makeText(context, "Email has used", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(context, "Response Code: " + response.code(), Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    dialog.dismiss()
                    Toast.makeText(context, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun RequestLogIn() {

        val authToken = Credentials.basic(Constant.AUTH_TOKEN_USERNAME, Constant.AUTH_TOKEN_PASSWORD)
        val call = service.create(AuthenticationApi::class.java!!)
                            .signInApi(authToken, Constant.GRANT_TYPE_PASSWORD, bundle.getString("username"),bundle.getString("password"))
        call.enqueue(object : Callback<SignIn> {

            override fun onResponse(call: Call<SignIn>, response: Response<SignIn>) {
                dialog.dismiss()

                if (response.code() == 200) {
                    activity!!.supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.authentication_layout, CreatePhraseFragment())
                        .commit()
                }
                else if (response.code() == 400) {
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
