package com.sneakers.sneakerschecker.customViews

import android.content.Context
import android.util.AttributeSet
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.viewpager.widget.ViewPager
import android.view.MotionEvent



class CustomMainViewPager(context: Context, attrs: AttributeSet?) : ViewPager(context, attrs) {
    private var mScroller: FixedSpeedScroller? = null

    init {
        try {
            val viewpager = ViewPager::class.java
            val scroller = viewpager.getDeclaredField("mScroller")
            scroller.isAccessible = true
            mScroller = FixedSpeedScroller(getContext(), DecelerateInterpolator())
            scroller.set(this, mScroller)
        } catch (ignored: Exception) {
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Never allow swiping to switch between pages
        return false
    }

    /*
     * Set the factor by which the duration will change
     */
    fun setScrollDuration(duration: Int) {
        mScroller!!.setScrollDuration(duration)
    }

    private class FixedSpeedScroller(context: Context?, interpolator: Interpolator?) : Scroller(context, interpolator) {

        private var mDuration: Int = 1500

        @Override
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration)
        }

        @Override
        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration)
        }

        fun setScrollDuration(duration: Int) {
            mDuration = duration
        }
    }
}