package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val A4_WIDTH_PTS = 595
    private const val A4_HEIGHT_PTS = 842

    fun generatePdf(
        context: Context,
        data: InvoiceRenderData,
        templateId: Int
    ): File {
        val pdfDocument = PdfDocument()

        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH_PTS, A4_HEIGHT_PTS, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val pageCanvas = page.canvas
        pageCanvas.drawColor(Color.WHITE)

        val template = TemplateFactory.getTemplate(templateId)
        template.draw(pageCanvas, A4_WIDTH_PTS.toFloat(), A4_HEIGHT_PTS.toFloat(), data)

        pdfDocument.finishPage(page)

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
