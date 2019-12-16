package com.sneakers.sneakerschecker

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.sneakers.sneakerschecker.model.SharedPref

class TrueGrailsApplication : Application() {
    private var isAppBackground = false
    private lateinit var sharedPref: SharedPref

    companion object {
        var mInstance: TrueGrailsApplication? = null
    }

    override fun onCreate() {
        super.onCreate()
        sharedPref = SharedPref(this)
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    fun isAppBackground(): Boolean {
        isAppBackground = sharedPref.getBool(SharedPref.IS_APP_BACKGROUND)
        return isAppBackground
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityStarted(activity: Activity) {}
        override fun onActivityResumed(activity: Activity) {
            isAppBackground = false
            sharedPref.setBool(isAppBackground, SharedPref.IS_APP_BACKGROUND)
        }

        override fun onActivityPaused(activity: Activity) {
            isAppBackground = true
            sharedPref.setBool(isAppBackground, SharedPref.IS_APP_BACKGROUND)
        }

        override fun onActivityStopped(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    fun isAppRunning(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        return activityManager.appTasks
            .filter { it.taskInfo != null }
            .filter { it.taskInfo.baseActivity != null }
            .any { it.taskInfo.baseActivity.packageName == packageName }
    }

    init {
        mInstance = this
    }
}