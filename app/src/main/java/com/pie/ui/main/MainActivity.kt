package com.pie.ui.main

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.pie.ui.base.BaseActivity

import androidx.fragment.app.FragmentPagerAdapter
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.ui.base.BaseFragment
import com.pie.ui.home.HomeFragment
import com.pie.ui.pie.PieFragment
import com.pie.ui.profile.ProfileActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar_main.view.*
import org.jetbrains.anko.startActivity


class MainActivity : BaseActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar.ivProfile.setOnClickListener(this)
        pref.getLoginData()?.let {
            Glide.with(this).load(it.profile_pic).into(toolbar.ivProfile)
        }
        showFragment(HomeFragment())
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivProfile->{
                startActivity<ProfileActivity>()
            }
        }
    }

    fun showFragment(fragment: BaseFragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
            android.R.anim.fade_out)
        fragmentTransaction.replace(R.id.container, fragment, null)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container)
        if(fragment is HomeFragment){
            finishAffinity()
        }else {
            super.onBackPressed()
        }
    }
}
