package com.sneakers.sneakerschecker.screens.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.utils.CommonUtils

abstract class BaseFragment : Fragment(), Toolbar.OnMenuItemClickListener {
    protected var mToolBar: Toolbar? = null
    protected var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    protected var appBarLayout: AppBarLayout? = null

    protected abstract fun getLayoutId(): Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(getLayoutId(), container, false)
        mToolBar = view.findViewById(R.id.appBar)
        mCollapsingToolbarLayout = view.findViewById(R.id.collapsingToolBar)
        appBarLayout = view.findViewById(R.id.appBarLayout)
        initToolbarNav()
        return view
    }

    protected open fun getScreenTitleId(): Int {
        return 0
    }

    protected open fun getScreenTitle(): String {
        return ""
    }

    protected open fun getOptionMenu(): Int {
        return 0
    }

    protected open fun isShowBackButton(): Boolean {
        return false
    }

    protected open fun initToolbarNav() {
        mCollapsingToolbarLayout?.setCollapsedTitleTextAppearance(R.style.TextAppearance_TrueGrails_Title_Collapse)
        mCollapsingToolbarLayout?.setExpandedTitleTextAppearance(R.style.TextAppearance_TrueGrails_Title_Expand)
        appBarLayout?.outlineProvider = null

        if (appBarLayout?.layoutParams != null) {
            val params = appBarLayout?.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = AppBarLayout.Behavior()
            behavior.setDragCallback(object : AppBarLayout.Behavior.DragCallback() {
                override fun canDrag(appBarLayout: AppBarLayout): Boolean {
                    return false
                }
            })
            params.behavior = behavior
        }

        if (isShowBackButton()) {
            mToolBar?.setNavigationIcon(R.drawable.ic_back)
            mToolBar?.setNavigationOnClickListener {
                CommonUtils.hideKeyboard(activity)
                activity!!.onBackPressed()
            }
        }
        if (getScreenTitle().isNotEmpty()) {
            mCollapsingToolbarLayout?.title = getScreenTitle()
        } else if (getScreenTitleId() > 0) {
            mCollapsingToolbarLayout?.title = getString(getScreenTitleId())
        }
        showOptionMenu(mToolBar)
    }

    private fun showOptionMenu(toolbar: Toolbar?) {
        if (getOptionMenu() != 0) {
            toolbar?.menu?.clear()
            toolbar?.inflateMenu(getOptionMenu())
            toolbar?.setOnMenuItemClickListener(this)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return false
    }
}