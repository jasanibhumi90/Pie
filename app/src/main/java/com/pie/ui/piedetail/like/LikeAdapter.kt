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
            tvName.text = likemodel.first_name
            tvUserName.text = likemodel.last_name
            tvFollow.setOnClickListener(clickListener)
            tvFollow.tag = position
        }
    }


}