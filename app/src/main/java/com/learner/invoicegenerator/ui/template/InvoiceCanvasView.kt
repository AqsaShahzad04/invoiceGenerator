package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class InvoiceCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var renderData: InvoiceRenderData? = null
        set(value) {
            field = value
            invalidate()
        }

    var templateId: Int = 1
        set(value) {
            field = value
            invalidate()
        }

    // Standard A4 dimensions in points
    private val a4Width = 595f
    private val a4Height = 842f

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width * (a4Height / a4Width)).toInt()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = renderData ?: return

        val viewWidth = width.toFloat()
        val scale = viewWidth / a4Width

        canvas.save()
        canvas.scale(scale, scale)

        val template = TemplateFactory.getTemplate(templateId)
        template.draw(canvas, a4Width, a4Height, data)

        canvas.restore()
    }
}
