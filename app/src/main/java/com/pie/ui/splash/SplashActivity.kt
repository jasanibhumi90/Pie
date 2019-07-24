package com.pie.ui.splash

import android.os.Bundle
import android.view.View
import com.pie.R
import com.pie.ui.base.BaseActivity
import com.pie.ui.dashboard.DashboardActivity
import com.pie.ui.login.LoginActivity
import com.pie.ui.main.MainActivity
import com.pie.utils.PermissionUtils
import org.jetbrains.anko.startActivity

class SplashActivity : BaseActivity(), View.OnClickListener, PermissionUtils.OnPermissionResponse {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBlack))
        setContentView(R.layout.activity_splash)

        if (pref.isLogin()) {
            handler.postDelayed({
                startActivity<MainActivity>()
                finish()
            }, 2000)
        } else {
            handler.postDelayed({
                startActivity<DashboardActivity>()
                finish()
            }, 2000)
        }

    }

    override fun onClick(v: View?) {
        when (v?.id) {
        }
    }

    override fun onPermissionGranted(requestCode: Int) {

    }

    override fun onPermissionDenied(requestCode: Int) {

    }


}
