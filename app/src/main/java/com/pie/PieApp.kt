package com.pie

import android.app.Activity
import android.app.Application
import com.bumptech.glide.request.target.ViewTarget
import com.pie.data.prefs.AppPreferencesHelper
import com.pie.utils.AppLifecycleHandler
import com.pie.utils.AppLogger
import com.pie.utils.FTActivityLifecycleCallbacks


class PieApp : Application(), AppLifecycleHandler.LifecycleDelegate {

    private lateinit var appPreferencesHelper: AppPreferencesHelper
    private var lifeCycleHandler: AppLifecycleHandler? = null
    val mFTActivityLifecycleCallbacks = FTActivityLifecycleCallbacks()

    companion object {
        private lateinit var mInstance: PieApp
        @Synchronized
        fun currentActivity(): Activity? {

            return mInstance.mFTActivityLifecycleCallbacks.currentActivity
        }

        @Synchronized
        fun getInstance(): PieApp {
            return mInstance
        }
    }

    override fun onCreate() {
        super.onCreate()
        ViewTarget.setTagId(R.id.glide_tag)
        mInstance = this

//        ViewTarget.setTagId(R.id.glide_tag)
        appPreferencesHelper = AppPreferencesHelper(this, getString(R.string.app_name))
        registerActivityLifecycleCallbacks(mFTActivityLifecycleCallbacks)
        lifeCycleHandler = AppLifecycleHandler(this)
        registerLifecycleHandler(lifeCycleHandler!!)

    }

    fun getAppPreferencesHelper(): AppPreferencesHelper {
        return appPreferencesHelper
    }

    override fun onAppBackgrounded() {
        AppLogger.e("Tag", "onAppBackgrounded")
    }

    override fun onAppForegrounded() {
        AppLogger.e("Tag", "onAppForegrounded")
    }

    private fun registerLifecycleHandler(lifeCycleHandler: AppLifecycleHandler) {
        registerComponentCallbacks(lifeCycleHandler)
        registerActivityLifecycleCallbacks(lifeCycleHandler)
    }

    /* override fun attachBaseContext(base: Context?) {
         base?.let {
             super.attachBaseContext(LocaleHelper.onAttach(it))
         }
     }*/
}