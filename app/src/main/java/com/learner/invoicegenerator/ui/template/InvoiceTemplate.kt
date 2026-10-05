package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import com.learner.invoicegenerator.utils.ThemeUtils
import kotlin.math.min

interface InvoiceTemplate {
    fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData)

    fun setContext(ctx: Context) {}

    fun drawCircularLogo(canvas: Canvas, bitmap: Bitmap, dst: RectF, paint: Paint) {
        if (bitmap.width <= 0 || bitmap.height <= 0) return
        val minDim = min(bitmap.width, bitmap.height)
        val srcLeft = (bitmap.width - minDim) / 2
        val srcTop = (bitmap.height - minDim) / 2

        val cropped = Bitmap.createBitmap(bitmap, srcLeft, srcTop, minDim, minDim)
        val shader = BitmapShader(cropped, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val matrix = Matrix()
        val scale = dst.width() / minDim.toFloat()
        matrix.setScale(scale, scale)
        matrix.postTranslate(dst.left, dst.top)
        shader.setLocalMatrix(matrix)

        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
            this.shader = shader
        }
        val radius = min(dst.width(), dst.height()) / 2f
        canvas.drawCircle(dst.centerX(), dst.centerY(), radius, circlePaint)
    }
}
