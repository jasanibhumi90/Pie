package com.pie.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.app.utils.RxBus
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.pie.R
import com.pie.model.*
import com.pie.ui.base.BaseActivity
import com.pie.ui.editprofile.EditProfileActivity
import com.pie.ui.event.EventFragment
import com.pie.ui.meal.MealsFragment
import com.pie.ui.pie.PieFragment
import com.pie.ui.profile.piemate.MyPiematesActivity
import com.pie.utils.AppConstant
import com.pie.utils.AppConstant.Companion.ARG_FOLLOW_STATUS

import com.pie.utils.AppConstant.Companion.ARG_PIEMATE
import com.pie.utils.AppConstant.Companion.ARG_PIE_DATA
import com.pie.utils.AppConstant.Companion.ARG_PIE_MATE_LIST
import com.pie.utils.AppConstant.Companion.ARG_PIE_PROFILE_ID
import com.pie.utils.AppConstant.Companion.REQUEST_EDIT_PROFILE
import com.pie.utils.AppLogger
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_profile.*
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.startActivityForResult


class ProfileActivity : BaseActivity(), View.OnClickListener {

    private var pieList = ArrayList<PostModel>()
    private var profileId: String = "0"
    private val NUM_PAGES = 3
    private var piemateList=ArrayList<Piemate>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        if (intent.hasExtra(ARG_PIE_PROFILE_ID)) {
            profileId = intent.getStringExtra(ARG_PIE_PROFILE_ID)
            init()
            clickListener()
            // setData()
        }
    }

    fun followStatus(status: String): String {
        var followStatus = ""
        followStatus = if (status == "0") {
            getString(R.string.follow)
        } else if (status == "1") {
            getString(R.string.following)
        } else getString(R.string.piemate)
        return followStatus
    }

    private fun init(){
        getPieProfile()
        mCompositeDisposable.add(
            RxBus.listen(Bundle::class.java).observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io()).subscribe { bundle ->
                if (bundle.containsKey(ARG_PIEMATE)) {
                    try {
                        val piemate = bundle.getString(ARG_PIEMATE)
                        val pos = bundle.getInt(AppConstant.ARG_POSITION)
                        val data = piemateList[pos]
                        data.followstatus = bundle.getString(ARG_FOLLOW_STATUS)!!
                        tvPiemates.text = piemate
                    }catch (e:Exception){
                        AppLogger.d("tag","$e")
                    }
                }
            })
    }

    private fun setPagerAdapter(){
        val adapter = SimpleFragmentPagerAdapter(this, supportFragmentManager)
        pager.adapter = adapter
        tablayout.setupWithViewPager(pager)
        //tablayout.tabGravity = TabLayout.GRAVITY_FILL
        llprofile.visibility = View.VISIBLE
    }

    private fun getPieProfile() {
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
            ?.subscribe({ onPeofileResponse(it) }) { onResponseFailure(it, true) }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onPeofileResponse(resp: BaseResponse<Profile>) {
        if (super.onStatusFalse(resp, true)) return
        resp.data?.let {
            pref.setLoginData(it)
            tvName.text = it.first_name + " " + it.last_name
            tvUsername.text = it.user_name
            if (it.profile_pic.isNotEmpty())
                setImage(ivProfile, it.profile_pic)
            tvPies.text = it.countpies
            tvPiemates.text = it.countpiemate
            tvMeals.text = it.mealsorevent
            it.pie_list?.let {
                if (it.size > 0) {
                    pieList.addAll(it)
                    setPagerAdapter()
                }
            }
            piemateList= it.piemate!!
            if (profileId != pref.getLoginData()!!.user_id)
                tvEditProfile.text = followStatus(it.followstatus)
        }
    }

    private fun followUser() {
        val request = java.util.HashMap<String, Any>()
        val service = java.util.HashMap<String, Any>()
        val data = java.util.HashMap<String, Any>()
        val auth = java.util.HashMap<String, Any>()
        data[getString(R.string.param_follow_id)] = profileId
        auth[getString(R.string.param_id)] = pref.getLoginData()?.user_id.toString()
        auth[getString(R.string.param_token)] = pref.getToken()
        request[getString(R.string.data)] = data
        service[getString(R.string.service)] = getString(R.string.service_pies_follow)
        service[getString(R.string.request)] = request
        service[getString(R.string.auth)] = auth
        callApi(requestInterface.followPie(service), true)
            ?.subscribe({
                onFollowUser(it)
            }) {
                onResponseFailure(it, true)
            }
            ?.let { mCompositeDisposable.add(it) }

    }

    private fun onFollowUser(resp: FollowResponse) {
        if (super.onStatusFalseFollow(resp, true)) return
        tvEditProfile.text=followStatus (resp.followstatus)
    }

    private fun clickListener() {
        ivBack.setOnClickListener(this)
        tvEditProfile.setOnClickListener(this)
        llPiemate.setOnClickListener(this)
    }

    private fun setData() {
        pref.getLoginData()?.let {
            if (it.profile_pic.isNotEmpty()) {
                Glide.with(this).load(it.profile_pic).into(ivProfile)
            }
            tvName.text = getString(R.string.full_name, it.first_name, it.last_name)
            tvUsername.text = (it.user_name)

            if (it.profile_status.isNotEmpty())
                tvBio.text = it.profile_status
            else tvBio.visibility = View.GONE
        }
    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.ivBack -> {
                finish()
            }
            R.id.tvEditProfile -> {
                if (profileId != pref.getLoginData()?.user_id) followUser()
                else startActivityForResult<EditProfileActivity>(REQUEST_EDIT_PROFILE)
            }
            R.id.llPiemate -> {
                AppLogger.e("tag", "data.." + gson.toJson(piemateList).toString())
                startActivity<MyPiematesActivity>(ARG_PIE_MATE_LIST to piemateList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_EDIT_PROFILE) {
            if (resultCode == Activity.RESULT_OK) {
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
                 0-> newInstance()
                1 -> MealsFragment()
                else -> EventFragment()}

        }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getCount(): Int {
            return NUM_PAGES
        }

        override fun getPageTitle(position: Int): CharSequence? {
              when (position) {
                  0 -> return mContext.getString(R.string.pies)
                  1 -> return mContext.getString(R.string.meal)
                else -> return mContext.getString(R.string.events)
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
