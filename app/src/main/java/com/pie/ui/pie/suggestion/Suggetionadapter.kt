package com.pie.ui.pie.suggestion

import android.view.View
import com.bumptech.glide.Glide
import com.pie.R
import com.pie.model.LikeModel
import com.pie.model.PostModel
import com.pie.model.Suggestion
import com.pie.ui.base.BaseAdapter
import com.pie.utils.AppLogger
import kotlinx.android.synthetic.main.suggestion_raw_layout.view.*


class Suggetionadapter(
    layout: Int,
    private val clickListener: View.OnClickListener
) : BaseAdapter<Suggestion>(layout), BaseAdapter.OnBind<Suggestion> {

    init {
        setOnBinding(this)
    }

    override fun onBind(view: View, position: Int, data: Suggestion) {
        view.run {
            Glide.with(mContext).load(data.profile_pic)
                .error(resources.getDrawable(R.drawable.profile_pic)).into(ivProfilepic)
            tvFirstName.text = data.first_name
            tvUserName.text = data.user_name
            llSuggestion.tag=position
            llSuggestion.setOnClickListener(clickListener)

        }
    }


}