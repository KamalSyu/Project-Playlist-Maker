package com.practicum.playlistmaker.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.TypedValue

class DashedRoundedBorderDrawable(
    private val context: Context,
    private val strokeWidth: Float = 1f,
    private val dashLength: Float = 30f,
    private val dashGap: Float = 30f,
    private val color: Int = Color.parseColor("#AEAFB4"),
    private val cornerRadiusDp: Float = 8f
) : Drawable() {

    private val cornerRadius = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        cornerRadiusDp,
        context.resources.displayMetrics
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        pathEffect = DashPathEffect(floatArrayOf(dashLength, dashGap), 0f)
        color = this@DashedRoundedBorderDrawable.color
    }

    override fun draw(canvas: Canvas) {
        val rect = RectF(bounds)
        rect.inset(strokeWidth / 2f, strokeWidth / 2f)

        val path = Path()
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
