package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas

interface InvoiceTemplate {
    fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData)
}
