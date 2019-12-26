package com.sneakers.sneakerschecker.screens.activity

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.sneakers.sneakerschecker.R
import com.sneakers.sneakerschecker.utils.CommonUtils

abstract class BaseActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {
    protected var mToolBar: Toolbar? = null
    protected var mCollapsingToolbarLayout: CollapsingToolbarLayout? = null
    protected var appBarLayout: AppBarLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutId())

        mToolBar = findViewById(R.id.appBar)
        setSupportActionBar(mToolBar)
        mCollapsingToolbarLayout = findViewById(R.id.collapsingToolBar)
        appBarLayout = findViewById(R.id.appBarLayout)
        initToolbarNav()
    }

    protected abstract fun getLayoutId(): Int

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
                CommonUtils.hideKeyboard(this)
                onBackPressed()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return false
    }
}