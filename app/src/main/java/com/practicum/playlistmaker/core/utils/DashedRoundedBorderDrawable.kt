package com.practicum.playlistmaker.core.utils


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable

class DashedRoundedBorderDrawable(
    private val strokeWidth: Float = 1f,
    private val dashLength: Float = 30f,
    private val dashGap: Float = 30f,
    private val color: Int = Color.parseColor("#AEAFB4"),
    private val cornerRadius: Float = 8f
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        pathEffect = DashPathEffect(floatArrayOf(dashLength, dashGap), 0f)
        color = this@DashedRoundedBorderDrawable.color
    }

    override fun draw(canvas: Canvas) {
        val rect = RectF(bounds)
        // Отступ на половину толщины, чтобы обводка была внутри границ View
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