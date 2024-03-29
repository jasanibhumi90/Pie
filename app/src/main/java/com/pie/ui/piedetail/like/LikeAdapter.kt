package com.pie.ui.piedetail.like

import android.view.View
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.model.LikeModel
import com.pie.model.PostModel
import com.pie.ui.base.BaseAdapter
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.like_row_layout.view.*


class LikeAdapter(
    layout: Int,
    private val clickListener: View.OnClickListener
) : BaseAdapter<LikeModel>(layout), BaseAdapter.OnBind<LikeModel> {

    init {
        setOnBinding(this)
    }

    override fun onBind(view: View, position: Int, likemodel: LikeModel) {
        view.run {

            Glide.with(mContext).load(likemodel.profile_pic)
                .error(resources.getDrawable(R.drawable.profile_pic)).into(ivProfile)
            tvName.text = resources.getString(R.string.full_name,likemodel.first_name,likemodel.last_name)
            tvUserName.text = likemodel.user_name
            if(likemodel.followstatus=="0"){
                tvFollow.text=resources.getString(R.string.follow)
                tvFollow.visibility=View.VISIBLE
            }else if(likemodel.followstatus=="1"){
                tvFollow.text=resources.getString(R.string.following)
                tvFollow.visibility=View.VISIBLE
            }else if(likemodel.followstatus=="2"){
                tvFollow.text=resources.getString(R.string.piemate)
                tvFollow.background=mContext.resources.getDrawable(R.drawable.bg_btn_blue)
                tvFollow.visibility=View.VISIBLE
            }else{
                tvFollow.visibility=View.VISIBLE
            }
            tvFollow.setOnClickListener(clickListener)
            tvFollow.tag = position
        }
    }


}