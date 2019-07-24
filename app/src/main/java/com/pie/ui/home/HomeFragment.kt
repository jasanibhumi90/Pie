package com.pie.ui.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pie.R
import com.pie.model.BaseResponse
import com.pie.model.PostModel
import com.pie.ui.base.BaseFragment
import com.pie.ui.createpie.CreatePieActivity
import com.pie.ui.pie.PieFragment
import com.pie.utils.AppConstant
import com.pie.utils.AppGlobal
import kotlinx.android.synthetic.main.activity_create_pie.*
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult
import java.util.HashMap

class HomeFragment : BaseFragment(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = SimpleFragmentPagerAdapter(activity!!, childFragmentManager)
        pager.setAdapter(adapter)
        tablayout.setupWithViewPager(pager)

    }



    inner class SimpleFragmentPagerAdapter(private val mContext: Context, fm: FragmentManager) :
        FragmentPagerAdapter(fm) {

        // This determines the fragment for each tab

        override fun getItem(position: Int): Fragment {

            /* return if (position == 0) {

                 PieFragment()
             } else if (position == 1) {

                 PieFragment()
             } else if (position == 2) {

                 PieFragment()
             }  else if (position == 3) {

                 PieFragment()
             }else if (position == 4) {

                 PieFragment()
             } else {

                 PieFragment()
             }*/
            return PieFragment()
        }

        // This determines the number of tabs

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

                3 ->

                    return mContext.getString(R.string.host)

                4 ->

                    return mContext.getString(R.string.more)


                else ->

                    return null
            }
        }
    }

    override fun onClick(p0: View?) {

    }




}
