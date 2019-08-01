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
import com.pie.model.BaseResponse
import com.pie.model.Piemate
import com.pie.model.PostModel
import com.pie.model.Profile
import com.pie.ui.base.BaseActivity
import com.pie.ui.editprofile.EditProfileActivity
import com.pie.ui.pie.PieFragment
import com.pie.ui.profile.piemate.MyPiematesActivity
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIE_MATE_LIST
import com.pie.utils.AppConstant.Companion.ARG_PIE_PROFILE_ID
import com.pie.utils.AppConstant.Companion.REQUEST_EDIT_PROFILE
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult

class ProfileActivity : BaseActivity(), View.OnClickListener {

    private var pieList=ArrayList<PostModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if(intent.hasExtra(ARG_PIE_PROFILE_ID)) {
            init(intent.getStringExtra(ARG_PIE_PROFILE_ID))
            clickListener()
            if(intent.getStringExtra(ARG_PIE_PROFILE_ID)!=pref.getLoginData()!!.user_id)
                tvEditProfile.visibility=View.GONE
            // setData()
        }
    }

    private fun setPagerAdapter() {
        val adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        pager.adapter = adapter
        tablayout.setupWithViewPager(pager)
        tablayout.tabGravity = TabLayout.GRAVITY_FILL
        llprofile.visibility=View.VISIBLE

    }
    private fun init(profileId: String) {
        val request = HashMap<String, Any>()
        val service = HashMap<String, Any>()
        val data = HashMap<String, Any>()
        val auth = HashMap<String, Any>()
        data[getString(R.string.param_profile_id)] = profileId

        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()

        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_get_profile_by_id)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.getProfile(service), true)
            ?.subscribe({ onPeofileResponse(it,profileId) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }


    }

    private fun onPeofileResponse(resp: BaseResponse<Profile>,profileId:String) {
        if(super.onStatusFalse(resp,true))return
        resp.data?.let {
            tvName.text=it.first_name+" "+it.last_name
            tvUsername.text=it.user_name
            if(it.profile_pic.isNotEmpty())
                setImage(ivProfile,it.profile_pic)
            tvPies.text=it.countpies
            tvPiemates.text=it.countpiemate
            tvMeals.text=it.mealsorevent
            it.pie_list?.let {
                if(it.size>0){
                    pieList.addAll(it)
                    setPagerAdapter()
                }
            }
            llPiemate.tag=it.piemate
        }


    }

    private fun clickListener(){
        ivBack.setOnClickListener(this)
        tvEditProfile.setOnClickListener(this)
        llPiemate.setOnClickListener(this)
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
            R.id.llPiemate->{
                val list=p0.tag as ArrayList<Piemate>
                AppLogger.e("tag","data.."+gson.toJson(list).toString())
                startActivity<MyPiematesActivity>(ARG_PIE_MATE_LIST to list)
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

    inner class SimpleFragmentPagerAdapter(
        private val mContext: Context,
        fm: FragmentManager
    ) :
        FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {

            return when (position) {
                0 -> newInstance()
                1 -> PieFragment()
                2 -> PieFragment()
                else -> PieFragment()
            }
        }

        override fun getCount(): Int {
            return 4
        }
        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return mContext.getString(R.string.pies)
                1 -> return mContext.getString(R.string.meal)
                2 -> return mContext.getString(R.string.events)
                else -> return null
            }
        }
    }
    fun newInstance(): PieFragment {
        val frag = PieFragment()
        val args = Bundle()
        args.putSerializable(ARG_PIE_DATA, pieList)
        frag.arguments = args
        return frag
    }
}
