package com.practicum.playlistmaker.core.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class GridSpacingItemDecoration(
    private val spanCount: Int,
    private val edgeSpacing: Int,      // 16 dp
    private val columnSpacing: Int     // 8 dp
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val column = position % spanCount
        val row = position / spanCount

        outRect.left = if (column == 0) edgeSpacing else columnSpacing
        outRect.right = if (column == spanCount - 1) edgeSpacing else columnSpacing

        outRect.top = edgeSpacing

    }
}
