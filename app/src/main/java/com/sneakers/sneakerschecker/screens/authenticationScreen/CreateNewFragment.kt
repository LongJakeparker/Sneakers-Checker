package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import org.web3j.crypto.CipherException
import org.web3j.crypto.WalletUtils
import java.nio.file.Files.exists
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.Environment
import com.sneakers.sneakerschecker.R
import kotlinx.android.synthetic.main.fragment_create_new.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Web3ClientVersion
import org.web3j.protocol.http.HttpService
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import android.util.Log
import android.widget.Button
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security.insertProviderAt
import java.security.Security.removeProvider
import java.security.Security
import android.Manifest.permission
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import org.web3j.crypto.Credentials


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
    private var web3: Web3j? = null

    private var btnNewWallet: Button? = null

    private var credentials: Credentials? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupBouncyCastle()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_new, container, false)

        val permissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, 1)

        btnNewWallet = fragmentView!!.findViewById(R.id.btnNewWalletCreate)

        btnNewWallet!!.setOnClickListener(this)

        return fragmentView
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
        removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        insertProviderAt(BouncyCastleProvider(), 1)
    }

    override fun onClick(v: View?) {
        // FIXME: Add your own API key here
        web3 = Web3j.build( HttpService("https://rinkeby.infura.io/v3/e85cba30a04e48548170fdb23f9d5fc9"))
            try {
                var clientVersion: Web3ClientVersion = web3!!.web3ClientVersion().sendAsync().get()
            if(!clientVersion.hasError()){
                //Connected
                generateWallet("123456")
            }
            else {
                //Show Error
            }
        }
        catch (e: Exception) {
            //Show Error
        }
    }

    fun generateWallet(password: String) {
        val fileName: String
        try {
            val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!path.exists()) {
                path.mkdir()
            }
            fileName = WalletUtils.generateLightNewWalletFile(password, File(path.toString()))
            Log.e("TAG", "generateWallet: $path/$fileName")
            credentials = WalletUtils.loadCredentials(
                password,
                path.toString() + "/" + fileName
            )
            Log.e(
                "TAG",
                "generateWallet: " + credentials!!.getAddress() + " " + credentials!!.getEcKeyPair().getPublicKey()
            )
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
        finally {
            val sharedPref = SharedPref(this.context!!)
            sharedPref.setString(credentials!!.getAddress(), Constant.WALLET_ADDRESS)
            val transaction = activity!!.supportFragmentManager.beginTransaction();
            transaction.replace(R.id.authentication_layout, CreatePhraseFragment())
                .commit()
        }

    }
}
