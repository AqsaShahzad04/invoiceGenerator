package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.format.DateTimeFormatter

class MinimalTemplateRenderer : InvoiceTemplate {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")

    override fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData) {
        canvas.drawColor(Color.WHITE)

        val margin = 36f
        val orangeColor = Color.parseColor("#E05A10")
        val darkColor = Color.parseColor("#171817")
        val greyColor = Color.parseColor("#5C625E")

        val textFlags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG

        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orangeColor
            style = Paint.Style.FILL
        }

        val textBold = Paint(textFlags).apply {
            color = darkColor
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val textGrey = Paint(textFlags).apply {
            color = greyColor
            textSize = 10f
            typeface = Typeface.SANS_SERIF
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkColor
            strokeWidth = 1.2f
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

        // --- Top Right Orange Accent Block ---
        val accentPath = Path().apply {
            moveTo(width - 55f, 0f)
            lineTo(width, 0f)
            lineTo(width, 110f)
            lineTo(width - 55f, 55f)
            close()
        }
        canvas.drawPath(accentPath, orangePaint)

        // --- Bottom Left Orange Vertical Bar Accent ---
        val leftAccentPath = Path().apply {
            moveTo(0f, height - 160f)
            lineTo(14f, height - 160f)
            lineTo(14f, height)
            lineTo(0f, height)
            close()
        }
        canvas.drawPath(leftAccentPath, orangePaint)

        var currentY = margin + 10f

        // --- Top Left: Workspace Logo & Title & Details ---
        val logo = data.logoBitmap
        var nameX = margin + 12f
        if (logo != null) {
            val logoDst = RectF(margin + 12f, currentY, margin + 57f, currentY + 45f)
            drawCircularLogo(canvas, logo, logoDst, bitmapPaint)
            nameX = margin + 67f
        } else if (data.isSample) {
            val logoDst = RectF(margin + 12f, currentY, margin + 57f, currentY + 45f)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F2F4F7")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(logoDst, 8f, 8f, bgPaint)
            val logoTextPaint = Paint(textFlags).apply {
                color = orangeColor
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("LOGO", logoDst.centerX(), logoDst.centerY() + 3f, logoTextPaint)
            nameX = margin + 67f
        }

        textBold.textSize = 15f
        textBold.color = orangeColor
        canvas.drawText(data.workspace?.name ?: "Workspace Name", nameX, currentY + 16f, textBold)
        textBold.color = darkColor

        textGrey.textSize = 9.5f
        var wsY = currentY + 30f
        if (!data.workspace?.address.isNullOrEmpty()) {
            canvas.drawText(data.workspace!!.address, nameX, wsY, textGrey)
            wsY += 13f
        }
        val contactList = listOfNotNull(data.workspace?.phone, data.workspace?.email)
        if (contactList.isNotEmpty()) {
            canvas.drawText(contactList.joinToString(" | "), nameX, wsY, textGrey)
            wsY += 13f
        }
        if (!data.workspace?.taxNumber.isNullOrEmpty()) {
            canvas.drawText("Tax/NTN: ${data.workspace?.taxNumber}", nameX, wsY, textGrey)
            wsY += 13f
        }

        // --- Top Right: "INVOICE" Title & Meta ---
        val titlePaint = Paint(textFlags).apply {
            color = orangeColor
            textSize = 26f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        val rightX = width - margin - 15f
        canvas.drawText("INVOICE", rightX, currentY + 22f, titlePaint)

        textBold.textAlign = Paint.Align.RIGHT
        textGrey.textAlign = Paint.Align.RIGHT

        textBold.textSize = 11f
        canvas.drawText("Invoice #: ${data.invoice.invoiceNum}", rightX, currentY + 38f, textBold)

        textGrey.textSize = 9.5f
        canvas.drawText("Issue Date: ${data.invoice.issueDate.format(dateFormatter)}", rightX, currentY + 51f, textGrey)
        canvas.drawText("Due Date: ${data.invoice.dueDate.format(dateFormatter)}", rightX, currentY + 64f, textGrey)

        textBold.textSize = 10f
        textBold.color = if (data.invoice.status == "Paid") Color.parseColor("#007A78") else orangeColor
        canvas.drawText("Status: ${data.invoice.status.uppercase()}", rightX, currentY + 77f, textBold)
        textBold.color = darkColor

        if (data.invoice.paidDate != null && data.invoice.status == "Paid") {
            canvas.drawText("Paid: ${data.invoice.paidDate.format(dateFormatter)}", rightX, currentY + 90f, textGrey)
        }

        textBold.textAlign = Paint.Align.LEFT
        textGrey.textAlign = Paint.Align.LEFT

        currentY = Math.max(wsY + 15f, currentY + 98f)

        // --- Divider ---
        canvas.drawLine(margin + 12f, currentY, width - margin, currentY, lightLinePaint)
        currentY += 16f

        // --- Details Row: Bill To ---
        textGrey.textSize = 9.5f
        canvas.drawText("BILL TO:", margin + 12f, currentY, textGrey)

        textBold.textSize = 13f
        canvas.drawText(data.invoice.clientBusinessName, margin + 12f, currentY + 16f, textBold)

        var clientY = currentY + 30f
        textGrey.textSize = 9.5f
        if (data.client != null) {
            if (!data.client.contactPerson.isNullOrEmpty()) {
                canvas.drawText("Attn: ${data.client.contactPerson}", margin + 12f, clientY, textGrey)
                clientY += 13f
            }
            if (!data.client.address.isNullOrEmpty()) {
                canvas.drawText(data.client.address, margin + 12f, clientY, textGrey)
                clientY += 13f
            }
            if (!data.client.phone.isNullOrEmpty()) {
                canvas.drawText("Phone: ${data.client.phone}", margin + 12f, clientY, textGrey)
                clientY += 13f
            }
            if (!data.client.email.isNullOrEmpty()) {
                canvas.drawText("Email: ${data.client.email}", margin + 12f, clientY, textGrey)
                clientY += 13f
            }
        }

        currentY = Math.max(clientY + 12f, currentY + 60f)

        // --- Table Headers: ITEM DESCRIPTION | PRICE | QTY | TOTAL ---
        canvas.drawLine(margin + 12f, currentY, width - margin, currentY, linePaint)

        val col1 = margin + 16f                  // Description
        val col2 = width - margin - 190f          // Price
        val col3 = width - margin - 100f          // Qty
        val col4 = width - margin - 12f           // Total

        textBold.textSize = 10f
        canvas.drawText("ITEM DESCRIPTION", col1, currentY + 16f, textBold)
        canvas.drawText("PRICE", col2, currentY + 16f, textBold)
        canvas.drawText("QTY", col3, currentY + 16f, textBold)

        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, currentY + 16f, textBold)
        textBold.textAlign = Paint.Align.LEFT

        currentY += 24f
        canvas.drawLine(margin + 12f, currentY, width - margin, currentY, linePaint)

        // --- Table Rows ---
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEach { item ->
            currentY += 20f

            textBold.textSize = 11.5f
            canvas.drawText(item.itemName, col1, currentY, textBold)

            textGrey.textSize = 10.5f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col2, currentY, textGrey)
            canvas.drawText("${item.itemQuantity.toInt()}", col3, currentY, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, currentY, textBold)

            textBold.textAlign = Paint.Align.LEFT

            currentY += 12f
            canvas.drawLine(margin + 12f, currentY, width - margin, currentY, lightLinePaint)
        }

        currentY += 18f

        // --- Totals Section: SUB TOTAL -> DISCOUNT -> TAX -> GRAND TOTAL ---
        val tableRightX = width - margin - 12f
        val labelX = width - margin - 220f

        textGrey.textSize = 10.5f
        canvas.drawText("Sub Total", labelX, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", tableRightX, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 18f

        if (data.invoice.discountValue > 0) {
            val discAmt = if (data.invoice.discountType == "Percent") subtotal * (data.invoice.discountValue / 100) else data.invoice.discountValue
            canvas.drawText("Discount", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${data.currencySymbol} ${String.format("%.2f", discAmt)}", tableRightX, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        if (data.invoice.taxPercentage > 0) {
            val taxAmt = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax (${data.invoice.taxPercentage.toInt()}%)", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmt)}", tableRightX, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        currentY += 8f

        // GRAND TOTAL Orange Box
        val boxWidth = 240f
        val boxLeft = width - margin - boxWidth
        val grandTotalRect = RectF(boxLeft, currentY, width - margin, currentY + 30f)
        canvas.drawRect(grandTotalRect, orangePaint)

        val whitePaint = Paint(textFlags).apply {
            color = Color.WHITE
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("GRAND TOTAL", boxLeft + 12f, currentY + 20f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", width - margin - 12f, currentY + 20f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        // --- Bottom Section: Payment Info, Notes & Signature ---
        val footerY = height - margin - 100f
        textBold.textSize = 11f
        textGrey.textSize = 9.5f

        canvas.drawText("Payment Method", margin + 20f, footerY, textBold)
        val pmText = if (!data.paymentInfo.isNullOrEmpty()) data.paymentInfo else "Cash, Bank Transfer, Card"
        canvas.drawText(pmText, margin + 20f, footerY + 16f, textGrey)

        val termsY = footerY + 38f
        canvas.drawText("Notes", margin + 20f, termsY, textBold)
        val noteText = if (!data.invoice.endNote.isNullOrEmpty()) data.invoice.endNote else "Please reference invoice number when paying. Thank you!"
        canvas.drawText(noteText, margin + 20f, termsY + 16f, textGrey)

        // Signature
        val sig = data.signatureBitmap
        if (sig != null) {
            val sigDst = RectF(width - margin - 120f, footerY - 15f, width - margin, footerY + 35f)
            canvas.drawBitmap(sig, null, sigDst, bitmapPaint)
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, lightLinePaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        } else {
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, lightLinePaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        }
    }
}
