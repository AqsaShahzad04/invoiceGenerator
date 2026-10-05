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
        canvas.drawColor(Color.WHITE)

        val margin = 36f

        val textFlags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG

        val greyPaint = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorGrey(context)
            textSize = 10f
            typeface = Typeface.SANS_SERIF
        }

        val titlePaint = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorPrimary(context)
            textSize = 28f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val boldPaint = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorPrimary(context)
            textSize = 11f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = ThemeUtils.getTextColorPrimary(context)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
        }

        val lightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D0D5DD")
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
        }

        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
        }

        var currentY = margin + 10f

        // --- Top Left: Logo & Workspace Name & Info ---
        val logoBitmap = data.logoBitmap
        var textLeftX = margin
        if (logoBitmap != null) {
            val logoDst = RectF(margin, currentY, margin + 50f, currentY + 50f)
            drawCircularLogo(canvas, logoBitmap, logoDst, bitmapPaint)
            textLeftX = margin + 60f
        }

        boldPaint.textSize = 16f
        canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", textLeftX, currentY + 18f, boldPaint)

        greyPaint.textSize = 9.5f
        var wsInfoY = currentY + 32f
        if (!data.workspace?.address.isNullOrEmpty()) {
            canvas.drawText(data.workspace!!.address, textLeftX, wsInfoY, greyPaint)
            wsInfoY += 13f
        }
        val wsContact = listOfNotNull(data.workspace?.phone, data.workspace?.email).joinToString(" | ")
        if (wsContact.isNotEmpty()) {
            canvas.drawText(wsContact, textLeftX, wsInfoY, greyPaint)
            wsInfoY += 13f
        }
        if (!data.workspace?.taxNumber.isNullOrEmpty()) {
            canvas.drawText("Tax/NTN: ${data.workspace?.taxNumber}", textLeftX, wsInfoY, greyPaint)
            wsInfoY += 13f
        }

        // --- Top Right: Invoice Title & Meta ---
        val rightX = width - margin
        titlePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("INVOICE", rightX, currentY + 26f, titlePaint)

        greyPaint.textAlign = Paint.Align.RIGHT
        boldPaint.textAlign = Paint.Align.RIGHT

        boldPaint.textSize = 12f
        canvas.drawText("Invoice #: ${data.invoice.invoiceNum}", rightX, currentY + 44f, boldPaint)
        canvas.drawText("Date: ${data.invoice.issueDate.format(dateFormatter)}", rightX, currentY + 58f, greyPaint)
        canvas.drawText("Due Date: ${data.invoice.dueDate.format(dateFormatter)}", rightX, currentY + 72f, greyPaint)

        boldPaint.textSize = 11f
        canvas.drawText("Status: ${data.invoice.status.uppercase()}", rightX, currentY + 86f, boldPaint)
        if (data.invoice.paidDate != null && data.invoice.status == "Paid") {
            canvas.drawText("Paid: ${data.invoice.paidDate.format(dateFormatter)}", rightX, currentY + 99f, greyPaint)
        }

        titlePaint.textAlign = Paint.Align.LEFT
        greyPaint.textAlign = Paint.Align.LEFT
        boldPaint.textAlign = Paint.Align.LEFT

        currentY = Math.max(wsInfoY + 15f, currentY + 108f)

        // --- Divider Line ---
        canvas.drawLine(margin, currentY, width - margin, currentY, lightLinePaint)
        currentY += 18f

        // --- Bill To Section & Total Due ---
        greyPaint.textSize = 9.5f
        canvas.drawText("BILL TO:", margin, currentY, greyPaint)
        boldPaint.textSize = 14f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 18f, boldPaint)

        var clientY = currentY + 32f
        if (data.client != null) {
            greyPaint.textSize = 9.5f
            if (!data.client.contactPerson.isNullOrEmpty()) {
                canvas.drawText("Attn: ${data.client.contactPerson}", margin, clientY, greyPaint)
                clientY += 13f
            }
            if (!data.client.address.isNullOrEmpty()) {
                canvas.drawText(data.client.address, margin, clientY, greyPaint)
                clientY += 13f
            }
            if (!data.client.phone.isNullOrEmpty()) {
                canvas.drawText("Phone: ${data.client.phone}", margin, clientY, greyPaint)
                clientY += 13f
            }
            if (!data.client.email.isNullOrEmpty()) {
                canvas.drawText("Email: ${data.client.email}", margin, clientY, greyPaint)
                clientY += 13f
            }
        }

        // Total Due (Right Side)
        greyPaint.textAlign = Paint.Align.RIGHT
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL DUE", rightX, currentY, greyPaint)
        boldPaint.textSize = 20f
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", rightX, currentY + 22f, boldPaint)

        greyPaint.textAlign = Paint.Align.LEFT
        boldPaint.textAlign = Paint.Align.LEFT

        currentY = Math.max(clientY + 15f, currentY + 65f)

        // --- Table Headers ---
        val tableTopY = currentY
        canvas.drawLine(margin, tableTopY, width - margin, tableTopY, linePaint)

        val col1 = margin + 12f                  // Item description
        val col2 = width - margin - 200f          // Price
        val col3 = width - margin - 110f          // Qty
        val col4 = width - margin - 12f           // Total

        boldPaint.textSize = 11f
        canvas.drawText("ITEM DESCRIPTION", col1, tableTopY + 16f, boldPaint)
        canvas.drawText("PRICE", col2, tableTopY + 16f, boldPaint)
        canvas.drawText("QTY", col3, tableTopY + 16f, boldPaint)

        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, tableTopY + 16f, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT

        val tableHeaderBottomY = tableTopY + 26f
        canvas.drawLine(margin, tableHeaderBottomY, width - margin, tableHeaderBottomY, linePaint)

        // --- Draw Table Items ---
        var itemY = tableHeaderBottomY + 22f
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEach { item ->
            boldPaint.textSize = 12f
            canvas.drawText(item.itemName, col1, itemY, boldPaint)

            greyPaint.textSize = 11f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col2, itemY, greyPaint)
            canvas.drawText("${item.itemQuantity.toInt()}", col3, itemY, greyPaint)

            boldPaint.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, itemY, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT

            itemY += 18f
            canvas.drawLine(margin, itemY, width - margin, itemY, lightLinePaint)
            itemY += 20f
        }

        val tableBottomY = Math.max(itemY - 10f, tableHeaderBottomY + 120f)
        canvas.drawLine(col2 - 12f, tableTopY, col2 - 12f, tableBottomY, lightLinePaint)
        canvas.drawLine(col3 - 12f, tableTopY, col3 - 12f, tableBottomY, lightLinePaint)
        canvas.drawLine(margin, tableBottomY, width - margin, tableBottomY, linePaint)

        currentY = tableBottomY + 25f

        // --- Totals Section (Right Side) ---
        val totalBoxWidth = 240f
        val totalLabelX = width - margin - totalBoxWidth
        val totalValX = width - margin

        greyPaint.textSize = 11f
        boldPaint.textSize = 12f

        // Sub Total
        canvas.drawText("SUB TOTAL", totalLabelX, currentY, greyPaint)
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", totalValX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT
        currentY += 20f

        // Tax
        if (data.invoice.taxPercentage > 0) {
            val taxAmount = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax (${data.invoice.taxPercentage.toInt()}%)", totalLabelX, currentY, greyPaint)
            boldPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmount)}", totalValX, currentY, boldPaint)
            boldPaint.textAlign = Paint.Align.LEFT
            currentY += 20f
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
            currentY += 20f
        }

        canvas.drawLine(totalLabelX, currentY, width - margin, currentY, linePaint)
        currentY += 20f

        // Grand Total
        boldPaint.textSize = 14f
        canvas.drawText("GRAND TOTAL", totalLabelX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", totalValX, currentY, boldPaint)
        boldPaint.textAlign = Paint.Align.LEFT

        // --- Bottom Section: Payment Info, Terms & Signature ---
        val footerY = height - margin - 110f

        // Payment Info (Left)
        boldPaint.textSize = 11f
        greyPaint.textSize = 10f

        canvas.drawText("PAYMENT METHOD", margin, footerY, boldPaint)
        val pmText = if (!data.paymentInfo.isNullOrEmpty()) data.paymentInfo else "Cash, Bank Transfer, Card"
        canvas.drawText(pmText, margin, footerY + 16f, greyPaint)

        // Terms & Condition (Left below Payment)
        val termsY = footerY + 40f
        canvas.drawText("NOTES", margin, termsY, boldPaint)
        val noteText = if (!data.invoice.endNote.isNullOrEmpty()) data.invoice.endNote else "Please reference invoice number when paying. Thank you!"
        canvas.drawText(noteText, margin, termsY + 16f, greyPaint)

        // Signature (Right)
        val sigBitmap = data.signatureBitmap
        if (sigBitmap != null) {
            val sigDst = RectF(width - margin - 120f, footerY - 15f, width - margin, footerY + 35f)
            canvas.drawBitmap(sigBitmap, null, sigDst, bitmapPaint)
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, lightLinePaint)
            greyPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, greyPaint)
            greyPaint.textAlign = Paint.Align.LEFT
        } else {
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, lightLinePaint)
            greyPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, greyPaint)
            greyPaint.textAlign = Paint.Align.LEFT
        }
    }
}
