package com.pie.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.pie.R
import com.pie.ui.base.BaseActivity
import com.pie.ui.editprofile.EditProfileActivity
import com.pie.ui.pie.PieFragment
import com.pie.utils.AppConstant.Companion.REQUEST_EDIT_PROFILE
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult


class ProfileActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setData()
        ivBack.setOnClickListener(this)
        tvEditProfile.setOnClickListener(this)
        val adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        pager.setAdapter(adapter)
        tablayout.setupWithViewPager(pager)
        tablayout.setTabGravity(TabLayout.GRAVITY_FILL)
    }

    private fun setData() {
        pref.getLoginData()?.let {
            if (it.profile_pic.isNotEmpty()) {
                Glide.with(this).load(it.profile_pic).into(ivProfile)
            }
            tvName.text = (it.first_name + " " + it.last_name)
            tvUsername.text = (it.user_name)

            if(it.profile_status.isNotEmpty())
                tvBio.text=it.profile_status
            else tvBio.visibility=View.GONE
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                finish()
            }

            R.id.tvEditProfile -> {
                startActivityForResult<EditProfileActivity>(REQUEST_EDIT_PROFILE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== REQUEST_EDIT_PROFILE){
            if(resultCode== Activity.RESULT_OK ){
                setData()
            }
        }
    }

    inner class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

        // This determines the fragment for each tab

        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> PieFragment()
                1 -> PieFragment()
                2 -> PieFragment()
                else -> PieFragment()
            }
           //return PieFragment()
        }

        override fun getCount(): Int {

            return 4
        }

        // This determines the title for each tab

        override fun getPageTitle(position: Int): CharSequence? {

            // Generate title based on item position

            when (position) {

                0 ->

                    return mContext.getString(R.string.pies)

                1 ->

                    return mContext.getString(R.string.chats)

                2 ->

                    return mContext.getString(R.string.guest)


                else ->

                    return null
            }
        }
    }
}
