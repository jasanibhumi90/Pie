package com.pie.ui.pie

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.pie.PieApp
import com.pie.R
import kotlinx.android.synthetic.main.listitem_full_image.view.*


class FullImageAdapter(arImages: ArrayList<String>) : PagerAdapter() {
    var context = PieApp.getInstance()
    var images: ArrayList<String>? = arImages
    private var layoutInflater: LayoutInflater? = null
    var onClickListener: View.OnClickListener? = null

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`

    }

    override fun getCount(): Int {
        return images!!.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater!!.inflate(R.layout.listitem_full_image, null)
        Glide.with(context).load(images?.get(position)).into(view.ivImage)
        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        try {
            val vp = container as ViewPager
            val view = `object` as View
            vp.removeView(view)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}