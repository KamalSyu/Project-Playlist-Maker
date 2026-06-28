package com.practicum.playlistmaker.core.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class FirstItemTopMarginDecoration(private val topMarginPx: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        if (position == 0) {
            outRect.top = topMarginPx
        } else {
            outRect.top = 0
        }
        outRect.bottom = 0
        outRect.left = 0
        outRect.right = 0
    }
}
