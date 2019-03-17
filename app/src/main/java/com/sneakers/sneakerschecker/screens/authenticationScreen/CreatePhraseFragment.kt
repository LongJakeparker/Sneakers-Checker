package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.adapter.PhraseAdapter
import com.sneakers.sneakerschecker.constant.Constant
import com.sneakers.sneakerschecker.model.SharedPref
import kotlinx.android.synthetic.main.fragment_create_phrase.*
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.web3j.crypto.CipherException
import org.web3j.crypto.Credentials
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class CreatePhraseFragment : Fragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters

    private var fragmentView: View? = null
    private lateinit var recyclerPhrase: RecyclerView
    private lateinit var btnNext: Button

    private var web3: Web3j? = null
    private var credentials: Credentials? = null
    private lateinit var mnemonic: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupBouncyCastle()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.fragment_create_phrase, container, false)

        recyclerPhrase = fragmentView!!.findViewById(R.id.layout_list_phrase)
        btnNext = fragmentView!!.findViewById(R.id.btnNextCreatePhrase)

        val permissions = arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        requestPermissions(permissions, 1)

        // FIXME: Add your own API key here
        web3 = Web3j.build( HttpService("https://rinkeby.infura.io/v3/e85cba30a04e48548170fdb23f9d5fc9"))
        try {
            var clientVersion: Web3ClientVersion = web3!!.web3ClientVersion().sendAsync().get()
            if(!clientVersion.hasError()){
                //Connected
                generateWallet("123456")

                val seed = mnemonic.split(" ")

                recyclerPhrase!!.layoutManager = GridLayoutManager(context, Constant.GRID_COLUMN)
                recyclerPhrase!!.adapter = PhraseAdapter(seed, context!!)
            }
            else {
                //Show Error
            }
        }
        catch (e: Exception) {
            //Show Error
            Log.e("TAG", e.toString())
        }

        btnNext.setOnClickListener(this)

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
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
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

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btnNextCreatePhrase -> {
                val transaction = activity!!.supportFragmentManager.beginTransaction();
                transaction.replace(R.id.authentication_layout, ConfirmPhraseFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
