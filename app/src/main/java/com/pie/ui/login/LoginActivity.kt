package com.pie.ui.login

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.LoginModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.forgotpass.ForgotPassActivity
import com.pie.ui.main.MainActivity
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.startActivity

class LoginActivity : BaseActivity(), View.OnClickListener {
    private var vEmail: String = ""
    private var vPassword: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        tvLogin.setOnClickListener(this)

        tvForgotPass.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLogin -> {
                if (isValid()) {
                    loginAPI()
                }
            }

            R.id.tvForgotPass -> {
                startActivity<ForgotPassActivity>()
            }
        }
    }

    private fun isValid(): Boolean {
        vEmail = etEmail.text.toString().trim()
        vPassword = etPassword.text.toString().trim()

        if (vEmail.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_email))
            return false
        } else if (!AppGlobal.isEmailValid(vEmail)) {
            sneakerError(this, resources.getString(R.string.error_valid_email))
            return false
        } else if (vPassword.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_password))
            return false
        }
        return true
    }

    private fun loginAPI() {
        if (AppGlobal.isNetworkConnected(this)) run {

            val data = HashMap<String, String>()
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_email)] = vEmail
            data[getString(R.string.param_password)] = vPassword
            data[getString(R.string.param_device_type)] = "A"
            data[getString(R.string.param_deviceid)] = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_login)
            service[getString(R.string.request)] = request
            callApi(requestInterface.login(service), true)
                ?.subscribe({ onLogin(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onLogin(resp: BaseResponse<LoginModel>) {
        if (super.onStatusFalse(resp, true)) return
            resp.data?.let {
                pref.setLogin(true)
                pref.setToken(resp.token)
                pref.setLoginData(it)
                startActivity<MainActivity>()
            }
    }

}
