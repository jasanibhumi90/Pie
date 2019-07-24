package com.pie.ui.pie


import android.os.Bundle
import android.view.View
import androidx.viewpager.widget.ViewPager
import com.pie.R
import com.pie.ui.base.BaseActivity
import com.pie.utils.AppConstant.Companion.ARG_POS
import com.pie.utils.AppConstant.Companion.EXTRA_IMAGE_DATA
import kotlinx.android.synthetic.main.activity_fullmage.*

class ImageSliderActivity : BaseActivity(), View.OnClickListener {
    var pos: Int = 0
    var arImages: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fullmage)
        tvClose.setOnClickListener(this)
        init()
    }

    private fun init() {
        arImages = intent.getSerializableExtra(EXTRA_IMAGE_DATA) as ArrayList<String>
        pos = intent.getIntExtra(ARG_POS, 0)

        viewFullPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                tvIndicator.text = ((position + 1).toString() + "/" + arImages.size)

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        setFullSlider(arImages, pos)
    }

    fun setFullSlider(arImages: ArrayList<String>, pos: Int) {
        if (arImages.size > 0) {
            val viewPagerAdapter =
                FullImageAdapter(arImages)
            viewFullPager!!.adapter = viewPagerAdapter
            viewFullPager.currentItem = pos
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.tvClose -> {
                finish()
            }
        }
    }


}