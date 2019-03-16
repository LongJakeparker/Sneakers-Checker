package com.sneakers.sneakerschecker.screens.authenticationScreen

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sneakers.sneakerschecker.R
import android.os.Handler


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class SplashFragment : Fragment() {

    /** Duration of wait  */
    private val SPLASH_DISPLAY_LENGTH: Long = 4000

    private var fragmentView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment

        fragmentView = inflater.inflate(R.layout.fragment_splash, container, false)

        Handler().postDelayed({
            val transaction = activity!!.supportFragmentManager.beginTransaction();
            transaction.replace(R.id.authentication_layout, CreateNewFragment())
                .commit()
        }, SPLASH_DISPLAY_LENGTH)

        return fragmentView
    }
}
