package com.pie.ui.profile.piemate

import android.view.View
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.model.LikeModel
import com.pie.model.Piemate
import com.pie.ui.base.BaseAdapter
import kotlinx.android.synthetic.main.like_row_layout.view.*


class PiematesAdapter(
    layout: Int,
    private val clickListener: View.OnClickListener
) : BaseAdapter<Piemate>(layout), BaseAdapter.OnBind<Piemate> {

    init {
        setOnBinding(this)
    }

    override fun onBind(view: View, position: Int, item: Piemate) {
        view.run {
            Glide.with(mContext).load(item.profile_pic)
                .error(resources.getDrawable(R.drawable.profile_pic)).into(ivProfile)
            tvName.text = mContext.resources.getString(R.string.full_name,item.first_name,item.last_name)
            tvUserName.text = item.user_name
            tvFollow.text=mContext.resources.getString(R.string.piemate)
          tvFollow.setTextColor(mContext.resources.getColor(R.color.colorWhite))
            tvFollow.background=mContext.resources.getDrawable(R.drawable.bg_btn_blue)
            /*if(item.followstatus=="0"){
                tvFollow.visibility=View.VISIBLE
                tvFollow.setOnClickListener(clickListener)
                tvFollow.tag = position
            }else if(item.followstatus=="1"){
                tvFollow.visibility=View.VISIBLE
                tvFollow.text=mContext.getString(R.string.piemate)

                tvFollow.setOnClickListener(clickListener)
                tvFollow.tag = position
            }*/

        }
    }


}