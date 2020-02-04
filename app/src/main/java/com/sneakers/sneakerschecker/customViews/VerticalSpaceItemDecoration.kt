package com.sneakers.sneakerschecker.customViews

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class VerticalSpaceItemDecoration constructor(private val verticalSpaceHeight: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemCount = state.itemCount

        if (parent.getChildAdapterPosition(view) == itemCount - 1) {
            outRect.bottom = verticalSpaceHeight * 5
        }
        outRect.top = verticalSpaceHeight
    }
}