package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.format.DateTimeFormatter

class ModernTemplateRenderer : InvoiceTemplate {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy")

    override fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData) {
        canvas.drawColor(Color.WHITE)

        val margin = 36f
        val tealSecondary = Color.parseColor("#007A78")
        val darkHeader = ThemeUtils.getTextColorPrimary(context)

        val textFlags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG

        val whitePaint = Paint(textFlags).apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.SANS_SERIF
        }

        val tealPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealSecondary
            style = Paint.Style.FILL
        }

        val textDark = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorPrimary(context)
            textSize = 11f
            typeface = Typeface.SANS_SERIF
        }

        val textBold = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorPrimary(context)
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val textGrey = Paint(textFlags).apply {
            color = ThemeUtils.getTextColorGrey(context)
            textSize = 10f
            typeface = Typeface.SANS_SERIF
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D0D5DD")
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
        }

        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
        }

        // --- Top Header Banners ---
        val headerHeight = 105f

        val leftPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.48f, 0f)
            lineTo(width * 0.40f, headerHeight)
            lineTo(0f, headerHeight)
            close()
        }
        val darkPaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkHeader
            style = Paint.Style.FILL
        }
        canvas.drawPath(leftPath, darkPaintFill)

        val rightPath = Path().apply {
            moveTo(width * 0.42f, 0f)
            lineTo(width, 0f)
            lineTo(width, headerHeight)
            lineTo(width * 0.34f, headerHeight)
            close()
        }
        canvas.drawPath(rightPath, tealPaint)

        // Logo & Workspace Name in Left Dark Section
        val logo = data.logoBitmap
        if (logo != null) {
            val logoDst = RectF(20f, 20f, 65f, 65f)
            drawCircularLogo(canvas, logo, logoDst, bitmapPaint)
            whitePaint.textSize = 15f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", 75f, 44f, whitePaint)
        } else if (data.isSample) {
            val logoDst = RectF(20f, 20f, 65f, 65f)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#333333")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(logoDst, 8f, 8f, bgPaint)
            val logoTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                isLinearText = true
                isSubpixelText = true
            }
            canvas.drawText("LOGO", logoDst.centerX(), logoDst.centerY() + 3f, logoTextPaint)
            whitePaint.textSize = 15f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", 75f, 44f, whitePaint)
        } else {
            whitePaint.textSize = 16f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", 20f, 44f, whitePaint)
        }

        // Top Right: "INVOICE" Title & Dates
        whitePaint.textAlign = Paint.Align.RIGHT
        whitePaint.textSize = 26f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE", width - 20f, 32f, whitePaint)

        whitePaint.textSize = 9.5f
        whitePaint.typeface = Typeface.DEFAULT
        canvas.drawText("Invoice #: ${data.invoice.invoiceNum}", width - 20f, 48f, whitePaint)
        canvas.drawText("Date: ${data.invoice.issueDate.format(dateFormatter)}", width - 20f, 61f, whitePaint)
        canvas.drawText("Due: ${data.invoice.dueDate.format(dateFormatter)}", width - 20f, 74f, whitePaint)
        if (data.invoice.paidDate != null && data.invoice.status == "Paid") {
            canvas.drawText("Paid: ${data.invoice.paidDate.format(dateFormatter)}", width - 20f, 87f, whitePaint)
        }
        whitePaint.textAlign = Paint.Align.LEFT

        var currentY = headerHeight + 25f

        // --- Bill To & Workspace Info Row ---
        textBold.color = tealSecondary
        textBold.textSize = 11f
        canvas.drawText("INVOICE TO:", margin, currentY, textBold)
        textBold.color = ThemeUtils.getTextColorPrimary(context)

        textBold.textSize = 14f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 18f, textBold)

        var clientY = currentY + 34f
        if (data.client != null) {
            textGrey.textSize = 9.5f
            if (!data.client.contactPerson.isNullOrEmpty()) { canvas.drawText("Attn: ${data.client.contactPerson}", margin, clientY, textGrey); clientY += 13f }
            if (!data.client.address.isNullOrEmpty()) { canvas.drawText(data.client.address, margin, clientY, textGrey); clientY += 13f }
            if (!data.client.phone.isNullOrEmpty()) { canvas.drawText("Phone: ${data.client.phone}", margin, clientY, textGrey); clientY += 13f }
            if (!data.client.email.isNullOrEmpty()) { canvas.drawText("Email: ${data.client.email}", margin, clientY, textGrey); clientY += 13f }
        }

        // Right side: Payment Status badge & Workspace contact
        textGrey.textAlign = Paint.Align.RIGHT
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS:", width - margin, currentY, textGrey)
        textBold.textSize = 13f
        textBold.color = if (data.invoice.status == "Paid") tealSecondary else Color.parseColor("#E05A10")
        canvas.drawText(data.invoice.status.uppercase(), width - margin, currentY + 18f, textBold)
        textBold.color = ThemeUtils.getTextColorPrimary(context)

        if (data.workspace != null) {
            textGrey.textSize = 9.5f
            var wsY = currentY + 34f
            if (!data.workspace.address.isNullOrEmpty()) { canvas.drawText(data.workspace.address, width - margin, wsY, textGrey); wsY += 13f }
            val contact = listOfNotNull(data.workspace.phone, data.workspace.email).joinToString(" | ")
            if (contact.isNotEmpty()) { canvas.drawText(contact, width - margin, wsY, textGrey); wsY += 13f }
            if (!data.workspace.taxNumber.isNullOrEmpty()) { canvas.drawText("Tax/NTN: ${data.workspace.taxNumber}", width - margin, wsY, textGrey); wsY += 13f }
        }

        textGrey.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        currentY = Math.max(clientY + 15f, currentY + 80f)

        // --- Table Headers (Teal Banner) ---
        val tableHeaderRect = RectF(margin, currentY, width - margin, currentY + 26f)
        canvas.drawRect(tableHeaderRect, tealPaint)

        whitePaint.textSize = 10f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col0 = margin + 12f
        val col1 = margin + 45f
        val col2 = width - margin - 200f
        val col3 = width - margin - 110f
        val col4 = width - margin - 12f

        canvas.drawText("NO.", col0, currentY + 17f, whitePaint)
        canvas.drawText("ITEM DESCRIPTION", col1, currentY + 17f, whitePaint)
        canvas.drawText("PRICE", col2, currentY + 17f, whitePaint)
        canvas.drawText("QTY", col3, currentY + 17f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, currentY + 17f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        currentY += 26f

        // --- Items Rows ---
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEachIndexed { index, item ->
            currentY += 22f
            val itemNoStr = String.format("%02d", index + 1)

            textGrey.textSize = 10f
            canvas.drawText(itemNoStr, col0, currentY, textGrey)

            textBold.textSize = 12f
            canvas.drawText(item.itemName, col1, currentY, textBold)

            textGrey.textSize = 11f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col2, currentY, textGrey)
            canvas.drawText("${item.itemQuantity.toInt()}", col3, currentY, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT

            currentY += 12f
            canvas.drawLine(margin, currentY, width - margin, currentY, gridPaint)
        }

        currentY += 20f

        // --- Totals Box ---
        val boxWidth = 200f
        val boxLeft = width - margin - boxWidth

        val taxAmount = if (data.invoice.taxPercentage > 0) subtotal * (data.invoice.taxPercentage / 100) else 0.0
        val discAmount = if (data.invoice.discountValue > 0) {
            if (data.invoice.discountType == "Percent") subtotal * (data.invoice.discountValue / 100) else data.invoice.discountValue
        } else 0.0

        // Subtotal
        textGrey.textSize = 11f
        canvas.drawText("Subtotal:", boxLeft + 12f, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", width - margin - 12f, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 18f

        // Discount
        if (discAmount > 0) {
            canvas.drawText("Discount:", boxLeft + 12f, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${data.currencySymbol} ${String.format("%.2f", discAmount)}", width - margin - 12f, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        // Tax
        if (taxAmount > 0) {
            canvas.drawText("Tax (${data.invoice.taxPercentage.toInt()}%):", boxLeft + 12f, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmount)}", width - margin - 12f, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        // Total Teal Box Fill
        val totalBoxRect = RectF(boxLeft, currentY, width - margin, currentY + 28f)
        canvas.drawRect(totalBoxRect, tealPaint)

        whitePaint.textSize = 12f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL:", boxLeft + 12f, currentY + 18f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", width - margin - 12f, currentY + 18f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        // --- Bottom Section: Payment Info & Terms ---
        val footerY = height - 100f
        textBold.textSize = 11f
        textGrey.textSize = 10f

        canvas.drawText("PAYMENT METHOD", margin, footerY, textBold)
        val pmText = if (!data.paymentInfo.isNullOrEmpty()) data.paymentInfo else "Cash, Bank Transfer, Card"
        canvas.drawText(pmText, margin, footerY + 16f, textGrey)

        val termsY = footerY + 38f
        canvas.drawText("NOTES", margin, termsY, textBold)
        val noteText = if (!data.invoice.endNote.isNullOrEmpty()) data.invoice.endNote else "Please reference invoice number when paying. Thank you!"
        canvas.drawText(noteText, margin, termsY + 16f, textGrey)

        // Bottom Banners
        val bottomPath = Path().apply {
            moveTo(0f, height - 35f)
            lineTo(width * 0.55f, height - 35f)
            lineTo(width * 0.50f, height)
            lineTo(0f, height)
            close()
        }
        canvas.drawPath(bottomPath, tealPaint)

        val bottomDarkPath = Path().apply {
            moveTo(0f, height - 12f)
            lineTo(width, height - 12f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        canvas.drawPath(bottomDarkPath, darkPaintFill)

        // Signature
        val sig = data.signatureBitmap
        if (sig != null) {
            val sigDst = RectF(width - margin - 120f, footerY - 15f, width - margin, footerY + 35f)
            canvas.drawBitmap(sig, null, sigDst, bitmapPaint)
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, gridPaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        } else {
            canvas.drawLine(width - margin - 130f, footerY + 40f, width - margin, footerY + 40f, gridPaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 54f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        }
    }
}
