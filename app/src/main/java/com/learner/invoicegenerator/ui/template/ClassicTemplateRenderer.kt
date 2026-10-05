package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import java.time.format.DateTimeFormatter

class ClassicTemplateRenderer : InvoiceTemplate {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy")

    override fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData) {
        // Background
        canvas.drawColor(Color.WHITE)

        val margin = 36f
        val contentWidth = width - (margin * 2)

        // Paints
        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        val greyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5C625E")
            textSize = 9f
            typeface = Typeface.DEFAULT
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val boldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        val lightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5E5E5")
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        var currentY = margin + 10f

        // --- Top Left: Logo & Workspace Name ---
        val logoBitmap = data.logoBitmap
        if (logoBitmap != null) {
            val logoDst = RectF(margin, currentY, margin + 40f, currentY + 40f)
            canvas.drawBitmap(logoBitmap, null, logoDst, null)
            val nameX = margin + 48f
            boldPaint.textSize = 14f
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", nameX, currentY + 18f, boldPaint)
            if (!data.workspace?.taxNumber.isNullOrEmpty()) {
                greyPaint.textSize = 8f
                canvas.drawText("NTN: ${data.workspace?.taxNumber}", nameX, currentY + 32f, greyPaint)
            }
        } else {
            boldPaint.textSize = 16f
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", margin, currentY + 18f, boldPaint)
            if (!data.workspace?.taxNumber.isNullOrEmpty()) {
                greyPaint.textSize = 9f
                canvas.drawText("NTN: ${data.workspace?.taxNumber}", margin, currentY + 32f, greyPaint)
            }
        }

        // --- Top Right: Invoice Header ---
        val rightX = width - margin
        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Invoice", rightX, currentY + 22f, titlePaint)

        greyPaint.textAlign = Paint.Align.RIGHT
        boldPaint.textAlign = Paint.Align.RIGHT

        val dateStr = data.invoice.issueDate.format(dateFormatter)
        canvas.drawText("Invoice#  ${data.invoice.invoiceNum}", rightX, currentY + 38f, boldPaint)
        canvas.drawText("Date:  $dateStr", rightX, currentY + 50f, greyPaint)

        titlePaint.textAlign = Paint.Align.LEFT
        greyPaint.textAlign = Paint.Align.LEFT
        boldPaint.textAlign = Paint.Align.LEFT

        currentY += 75f

        // --- Bill To & Total Due ---
        greyPaint.textSize = 9f
        canvas.drawText("Bill To:", margin, currentY, greyPaint)
        boldPaint.textSize = 12f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 15f, boldPaint)
        if (data.client != null) {
            greyPaint.textSize = 9f
            if (!data.client.address.isNullOrEmpty()) {
                canvas.drawText(data.client.address, margin, currentY + 28f, greyPaint)
            }
            if (!data.client.phone.isNullOrEmpty()) {
                canvas.drawText(data.client.phone, margin, currentY + 40f, greyPaint)
            }
        }

        // Total Due Right side
        greyPaint.textAlign = Paint.Align.RIGHT
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("Total Due:", rightX, currentY, greyPaint)
        boldPaint.textSize = 15f
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", rightX, currentY + 18f, boldPaint)

        greyPaint.textAlign = Paint.Align.LEFT
        boldPaint.textAlign = Paint.Align.LEFT

        currentY += 65f

        // --- Table Headers ---
        val tableTopY = currentY
        canvas.drawLine(margin, tableTopY, width - margin, tableTopY, linePaint)

        val col1 = margin + 10f                  // Description
        val col2 = width - margin - 180f          // Price
        val col3 = width - margin - 100f          // Qty
        val col4 = width - margin - 10f           // Total

        boldPaint.textSize = 9f
        canvas.drawText("ITEM DESCRIPTION", col1, tableTopY + 14f, boldPaint)
        canvas.drawText("PRICE", col2, tableTopY + 14f, boldPaint)
        canvas.drawText("QTY", col3, tableTopY + 14f, boldPaint)

        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, tableTopY + 14f, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT

        val tableHeaderBottomY = tableTopY + 22f
        canvas.drawLine(margin, tableHeaderBottomY, width - margin, tableHeaderBottomY, linePaint)

        // Draw Items
        var itemY = tableHeaderBottomY + 18f
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEach { item ->
            boldPaint.textSize = 10f
            canvas.drawText(item.itemTitle, col1, itemY, boldPaint)

            greyPaint.textSize = 9f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col2, itemY, greyPaint)
            canvas.drawText("${item.itemQuantity}", col3, itemY, greyPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, itemY, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT

            itemY += 12f
            if (!item.itemDescription.isNullOrEmpty()) {
                greyPaint.textSize = 8f
                canvas.drawText(item.itemDescription, col1, itemY, greyPaint)
                itemY += 12f
            }

            itemY += 6f
            canvas.drawLine(margin, itemY, width - margin, itemY, lightLinePaint)
            itemY += 16f
        }

        // Vertical dividing lines for table columns
        val tableBottomY = itemY - 10f
        canvas.drawLine(col2 - 10f, tableTopY, col2 - 10f, tableBottomY, lightLinePaint)
        canvas.drawLine(col3 - 10f, tableTopY, col3 - 10f, tableBottomY, lightLinePaint)
        canvas.drawLine(margin, tableBottomY, width - margin, tableBottomY, linePaint)

        currentY = tableBottomY + 20f

        // --- Totals Section ---
        val totalLabelX = width - margin - 150f
        val totalValX = width - margin

        greyPaint.textSize = 9f
        boldPaint.textSize = 10f

        // Sub Total
        canvas.drawText("SUB TOTAL", totalLabelX, currentY, greyPaint)
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", totalValX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT
        currentY += 16f

        // Tax
        if (data.invoice.taxPercentage > 0) {
            val taxAmount = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax (${data.invoice.taxPercentage}%)", totalLabelX, currentY, greyPaint)
            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmount)}", totalValX, currentY, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        // Discount
        if (data.invoice.discountValue > 0) {
            val discAmount = if (data.invoice.discountType == "Percent") {
                subtotal * (data.invoice.discountValue / 100)
            } else {
                data.invoice.discountValue
            }
            canvas.drawText("Discount", totalLabelX, currentY, greyPaint)
            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${data.currencySymbol} ${String.format("%.2f", discAmount)}", totalValX, currentY, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        canvas.drawLine(totalLabelX, currentY, width - margin, currentY, linePaint)
        currentY += 16f

        // Grand Total
        boldPaint.textSize = 12f
        canvas.drawText("Grand Total", totalLabelX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", totalValX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT

        // --- Bottom Section: Notes & Signature ---
        val footerY = height - margin - 80f

        // Notes / Status Left
        greyPaint.textSize = 8f
        boldPaint.textSize = 9f

        canvas.drawText("Status: ${data.invoice.status}", margin, footerY, boldPaint)
        if (!data.invoice.endNote.isNullOrEmpty()) {
            canvas.drawText("Note: ${data.invoice.endNote}", margin, footerY + 14f, greyPaint)
        }

        // Signature Right
        val sigBitmap = data.signatureBitmap
        if (sigBitmap != null) {
            val sigDst = RectF(width - margin - 100f, footerY - 10f, width - margin, footerY + 30f)
            canvas.drawBitmap(sigBitmap, null, sigDst, null)
            canvas.drawLine(width - margin - 110f, footerY + 32f, width - margin, footerY + 32f, lightLinePaint)
            greyPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 44f, greyPaint)
            greyPaint.textAlign = Paint.Align.LEFT
        }
    }
}
