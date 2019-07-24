package com.pie.ui.dashboard

import android.os.Bundle
import android.view.View
import com.pie.R
import com.pie.ui.base.BaseActivity
import com.pie.ui.login.LoginActivity
import com.pie.ui.register.RegisterActivity
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_forgot_pass.*
import org.jetbrains.anko.startActivity

class DashboardActivity : BaseActivity(), View.OnClickListener {
    var vEmail = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        tvLogin.setOnClickListener(this)
        tvRegister.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvLogin -> {
                startActivity<LoginActivity>()
            }
            R.id.tvRegister -> {
                startActivity<RegisterActivity>()
            }
        }

    }

/*private fun forgotPass() {
    val data = HashMap<String, Any>()
    data[getString(R.string.param_email)] = vEmail


    callApi(requestInterface.forgotPass(data), true)
        ?.subscribe({ onForgotPass(it) }) { onResponseFailure(it, true) }
        ?.let { mCompositeDisposable.add(it) }
}

private fun onForgotPass(resp: BaseResponse<Any>) {
    if (super.onStatusFalse(resp, true)) return
    AppLogger.e("json++ =>", gson.toJson(resp.data))
    if (resp.success) {
        startActivity<ResetPasswordActivity>(AppConstant.EXTRA_EMAIL to vEmail)
        finish()
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
}*/


}
