package com.pie.ui.editprofile

import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.ui.base.BaseActivity
import kotlinx.android.synthetic.main.editprofile.*

class EditProfileActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.editprofile)
        pref.getLoginData()?.let {
            if (it.profile_pic.isNotEmpty()) {
                Glide.with(this).load(it.profile_pic).into(ivProPic)
            }
            etFirstName.setText(it.first_name)
            etLastName.setText(it.last_name)
            etEmailAddress.setText(it.email_id)
            etMobileNo.setText(it.phone_no)
        }
        ivBack.setOnClickListener(this)
        //tvDob.setOnClickListener(this)

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                finish()
            }

           /* R.id.tvDob -> {
                finish()
            }*/
        }
    }

}
