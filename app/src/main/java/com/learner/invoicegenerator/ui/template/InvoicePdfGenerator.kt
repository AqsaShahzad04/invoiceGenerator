package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.graphics.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {

    private const val A4_WIDTH = 595
    private const val A4_HEIGHT = 842

    fun generatePdf(
        context: Context,
        data: InvoiceRenderData,
        templateId: Int
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val template = TemplateFactory.getTemplate(templateId)
        template.draw(page.canvas, A4_WIDTH.toFloat(), A4_HEIGHT.toFloat(), data)

        pdfDocument.finishPage(page)

        val invoicesDir = File(context.filesDir, "invoices")
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs()
        }

        val fileName = "Invoice_${data.invoice.invoiceNum.replace("#", "")}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(invoicesDir, fileName)

        FileOutputStream(outputFile).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()

        return outputFile
    }
}
