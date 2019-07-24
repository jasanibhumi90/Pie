package com.pie.ui.forgotpass

import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.LoginModel
import com.pie.ui.base.BaseActivity
import com.pie.ui.login.LoginActivity
import com.pie.ui.main.MainActivity
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import kotlinx.android.synthetic.main.activity_forgot_pass.etEmail
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.*

class ForgotPassActivity : BaseActivity(), View.OnClickListener {
    var vEmail = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_pass)
        tvSend.setOnClickListener(this)
        ivBack.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvSend -> {
               if(isValid()){
                   forgotPass()
               }
            }
            R.id.ivBack -> {
                finish()
            }
        }

    }
    private fun isValid(): Boolean {
        vEmail = etEmail.text.toString().trim()

        if (vEmail.isEmpty()) {
            sneakerError(this, resources.getString(R.string.error_empty_email))
            return false
        } else if (!AppGlobal.isEmailValid(vEmail)) {
            sneakerError(this, resources.getString(R.string.error_valid_email))
            return false
        }
        return true
    }
    private fun forgotPass() {
        if (AppGlobal.isNetworkConnected(this)) run {

            val data = HashMap<String, String>()
            val request = HashMap<String, Any>()
            val service = HashMap<String, Any>()
            data[getString(R.string.param_email)] = vEmail

            request[getString(R.string.data)] = data
            service[getString(R.string.service)] = getString(R.string.service_forgot_password)
            service[getString(R.string.request)] = request
            callApi(requestInterface.forgotpass(service), true)
                ?.subscribe({ onForgotPass(it) }) { onResponseFailure(it, true) }
                ?.let { mCompositeDisposable.add(it) }

        } else {
            Toast.makeText(this, resources.getString(R.string.msg_no_internet), Toast.LENGTH_LONG).show()
        }
    }

    private fun onForgotPass(resp: BaseResponse<Any>) {
        if (super.onStatusFalse(resp, true)) return
        longToast(resp.message)
        startActivity(intentFor<LoginActivity>().clearTask().newTask())

    }
}
