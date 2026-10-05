package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val A4_WIDTH_PTS = 595
    private const val A4_HEIGHT_PTS = 842
    private const val SCALE_FACTOR = 3.0f

    fun generatePdf(
        context: Context,
        data: InvoiceRenderData,
        templateId: Int
    ): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val bitmapWidth = (A4_WIDTH_PTS * SCALE_FACTOR).toInt()
        val bitmapHeight = (A4_HEIGHT_PTS * SCALE_FACTOR).toInt()
        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)

        val bitmapCanvas = Canvas(bitmap)
        bitmapCanvas.drawColor(Color.WHITE)
        bitmapCanvas.scale(SCALE_FACTOR, SCALE_FACTOR)

        val template = TemplateFactory.getTemplate(templateId)
        template.draw(bitmapCanvas, A4_WIDTH_PTS.toFloat(), A4_HEIGHT_PTS.toFloat(), data)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)
        val pageBounds = RectF(0f, 0f, A4_WIDTH_PTS.toFloat(), A4_HEIGHT_PTS.toFloat())
        page.canvas.drawBitmap(bitmap, null, pageBounds, paint)

        pdfDocument.finishPage(page)
        bitmap.recycle()

        val invoicesDir = File(context.filesDir, "invoices")
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs()
        }

        val safeInvoiceNum = data.invoice.invoiceNum.replace("#", "").replace("/", "_")
        val fileName = "Invoice_${safeInvoiceNum}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(invoicesDir, fileName)

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }
}
