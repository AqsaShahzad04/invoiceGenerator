package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.format.DateTimeFormatter

class BoldTemplateRenderer : InvoiceTemplate {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy")

    override fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData) {
        canvas.drawColor(Color.WHITE)

        val margin = 36f
        val darkCharcoal = Color.parseColor("#22252A")
        val goldYellow = Color.parseColor("#F5B027")
        val silverGrey = Color.parseColor("#7A808A")
        val lightRowBg = Color.parseColor("#F8F9FA")

        val textFlags = Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG or Paint.LINEAR_TEXT_FLAG

        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkCharcoal
            style = Paint.Style.FILL
        }

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldYellow
            style = Paint.Style.FILL
        }

        val whitePaint = Paint(textFlags).apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.SANS_SERIF
        }

        val textBold = Paint(textFlags).apply {
            color = Color.parseColor("#171817")
            textSize = 12f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        }

        val textGrey = Paint(textFlags).apply {
            color = Color.parseColor("#5C625E")
            textSize = 10f
            typeface = Typeface.SANS_SERIF
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#22252A")
            strokeWidth = 1.2f
            style = Paint.Style.STROKE
        }

        val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            isDither = true
        }

        // --- Top Dark Charcoal Header with Wave Curve ---
        val headerHeight = 105f
        val topRect = RectF(0f, 0f, width, headerHeight)
        canvas.drawRect(topRect, darkPaint)

        val wavePath = Path().apply {
            moveTo(0f, headerHeight)
            cubicTo(width * 0.35f, headerHeight + 35f, width * 0.70f, headerHeight - 20f, width, headerHeight + 20f)
            lineTo(width, headerHeight)
            lineTo(0f, headerHeight)
            close()
        }
        val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = silverGrey
            style = Paint.Style.FILL
        }
        canvas.drawPath(wavePath, wavePaint)

        // Top Left: Logo & Title
        val logo = data.logoBitmap
        if (logo != null) {
            val logoDst = RectF(20f, 20f, 65f, 65f)
            drawCircularLogo(canvas, logo, logoDst, bitmapPaint)
            whitePaint.textSize = 16f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "LOREM WORKSPACE", 75f, 44f, whitePaint)
        } else if (data.isSample) {
            val logoDst = RectF(20f, 20f, 65f, 65f)
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#383C42")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(logoDst, 8f, 8f, bgPaint)
            val logoTextPaint = Paint(textFlags).apply {
                color = goldYellow
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("LOGO", logoDst.centerX(), logoDst.centerY() + 3f, logoTextPaint)
            whitePaint.textSize = 16f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "LOREM WORKSPACE", 75f, 44f, whitePaint)
        } else {
            whitePaint.textSize = 16f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "LOREM WORKSPACE", 20f, 44f, whitePaint)
        }

        // Top Right Contact Info
        whitePaint.textAlign = Paint.Align.RIGHT
        whitePaint.textSize = 9f
        whitePaint.typeface = Typeface.DEFAULT

        var wsHeaderY = 26f
        if (data.workspace != null) {
            if (!data.workspace.phone.isNullOrEmpty()) { canvas.drawText("Phone: ${data.workspace.phone}", width - 20f, wsHeaderY, whitePaint); wsHeaderY += 13f }
            if (!data.workspace.email.isNullOrEmpty()) { canvas.drawText("Email: ${data.workspace.email}", width - 20f, wsHeaderY, whitePaint); wsHeaderY += 13f }
            if (!data.workspace.address.isNullOrEmpty()) { canvas.drawText("Address: ${data.workspace.address}", width - 20f, wsHeaderY, whitePaint); wsHeaderY += 13f }
            if (!data.workspace.taxNumber.isNullOrEmpty()) { canvas.drawText("Tax/NTN: ${data.workspace.taxNumber}", width - 20f, wsHeaderY, whitePaint); wsHeaderY += 13f }
        }
        whitePaint.textAlign = Paint.Align.LEFT

        var currentY = headerHeight + 45f

        // --- INVOICE Title & Details ---
        val titlePaint = Paint(textFlags).apply {
            color = Color.parseColor("#171817")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("INVOICE", margin, currentY, titlePaint)

        currentY += 28f

        // Left: Client info
        textGrey.textSize = 9.5f
        canvas.drawText("TO:", margin, currentY, textGrey)
        textBold.textSize = 13f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 16f, textBold)
        var clientY = currentY + 32f
        if (data.client != null) {
            textGrey.textSize = 9.5f
            if (!data.client.contactPerson.isNullOrEmpty()) { canvas.drawText("Attn: ${data.client.contactPerson}", margin, clientY, textGrey); clientY += 13f }
            if (!data.client.address.isNullOrEmpty()) { canvas.drawText(data.client.address, margin, clientY, textGrey); clientY += 13f }
            if (!data.client.phone.isNullOrEmpty()) { canvas.drawText("Phone: ${data.client.phone}", margin, clientY, textGrey); clientY += 13f }
            if (!data.client.email.isNullOrEmpty()) { canvas.drawText("Email: ${data.client.email}", margin, clientY, textGrey); clientY += 13f }
        }

        // Right: Invoice Meta
        val rightX = width - margin
        textGrey.textAlign = Paint.Align.RIGHT
        textBold.textAlign = Paint.Align.RIGHT

        textBold.textSize = 12f
        canvas.drawText("Invoice #: ${data.invoice.invoiceNum}", rightX, currentY + 16f, textBold)

        textGrey.textSize = 9.5f
        canvas.drawText("Date: ${data.invoice.issueDate.format(dateFormatter)}", rightX, currentY + 30f, textGrey)
        canvas.drawText("Due: ${data.invoice.dueDate.format(dateFormatter)}", rightX, currentY + 44f, textGrey)

        textBold.textSize = 11f
        canvas.drawText("Status: ${data.invoice.status.uppercase()}", rightX, currentY + 58f, textBold)
        if (data.invoice.paidDate != null && data.invoice.status == "Paid") {
            canvas.drawText("Paid: ${data.invoice.paidDate.format(dateFormatter)}", rightX, currentY + 72f, textGrey)
        }

        textGrey.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        currentY = Math.max(clientY + 15f, currentY + 80f)

        // --- Table Headers (Gold Item Desc block + Dark Charcoal Price/Qty/Total block) ---
        val headerBarH = 28f
        val splitX = width * 0.48f

        val goldHeaderRect = RectF(margin, currentY, splitX, currentY + headerBarH)
        canvas.drawRect(goldHeaderRect, goldPaint)

        val darkHeaderRect = RectF(splitX, currentY, width - margin, currentY + headerBarH)
        canvas.drawRect(darkHeaderRect, darkPaint)

        textBold.textSize = 10f
        textBold.color = Color.parseColor("#171817")
        canvas.drawText("ITEM DESCRIPTION", margin + 12f, currentY + 18f, textBold)

        whitePaint.textSize = 10f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col1 = splitX + 20f               // Price
        val col2 = width - margin - 120f      // Qty
        val col3 = width - margin - 12f       // Total

        canvas.drawText("PRICE", col1, currentY + 18f, whitePaint)
        canvas.drawText("QTY", col2, currentY + 18f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col3, currentY + 18f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        currentY += headerBarH

        // --- Table Items ---
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightRowBg
            style = Paint.Style.FILL
        }

        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEachIndexed { index, item ->
            val rowHeight = 28f
            if (index % 2 == 1) {
                val rowRect = RectF(margin, currentY, width - margin, currentY + rowHeight)
                canvas.drawRect(rowRect, rowPaint)
            }

            textBold.textSize = 12f
            textBold.color = Color.parseColor("#171817")
            canvas.drawText(item.itemName, margin + 12f, currentY + 18f, textBold)

            textGrey.textSize = 11f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col1, currentY + 18f, textGrey)
            canvas.drawText("${item.itemQuantity.toInt()}", col2, currentY + 18f, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col3, currentY + 18f, textBold)
            textBold.textAlign = Paint.Align.LEFT

            currentY += rowHeight
        }

        currentY += 20f

        // --- Totals Block ---
        val labelX = width - margin - 170f

        textGrey.textSize = 11f
        canvas.drawText("Sub Total", labelX, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", col3, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 18f

        if (data.invoice.taxPercentage > 0) {
            val taxAmt = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax (${data.invoice.taxPercentage.toInt()}%)", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmt)}", col3, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        if (data.invoice.discountValue > 0) {
            val discAmt = if (data.invoice.discountType == "Percent") subtotal * (data.invoice.discountValue / 100) else data.invoice.discountValue
            canvas.drawText("Discount", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${data.currencySymbol} ${String.format("%.2f", discAmt)}", col3, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 18f
        }

        currentY += 10f

        // Gold GRAND TOTAL Bar
        val grandTotalRect = RectF(width - margin - 230f, currentY, width - margin, currentY + 30f)
        canvas.drawRect(grandTotalRect, goldPaint)

        textBold.textSize = 12f
        canvas.drawText("GRAND TOTAL", width - margin - 218f, currentY + 20f, textBold)

        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", col3 - 10f, currentY + 20f, textBold)
        textBold.textAlign = Paint.Align.LEFT

        // --- Bottom Section: Payment Info, Terms & Signature ---
        val footerY = height - margin - 100f
        textBold.textSize = 11f
        textGrey.textSize = 10f

        canvas.drawText("PAYMENT METHOD", margin, footerY, textBold)
        val pmText = if (!data.paymentInfo.isNullOrEmpty()) data.paymentInfo else "Cash, Bank Transfer, Card"
        canvas.drawText(pmText, margin, footerY + 16f, textGrey)

        val termsY = footerY + 38f
        canvas.drawText("NOTES", margin, termsY, textBold)
        val noteText = if (!data.invoice.endNote.isNullOrEmpty()) data.invoice.endNote else "Thank you for your business!"
        canvas.drawText(noteText, margin, termsY + 16f, textGrey)

        // Signature
        val sig = data.signatureBitmap
        if (sig != null) {
            val sigDst = RectF(width - margin - 120f, footerY - 15f, width - margin, footerY + 35f)
            canvas.drawBitmap(sig, null, sigDst, bitmapPaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 50f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        } else {
            canvas.drawLine(width - margin - 130f, footerY + 35f, width - margin, footerY + 35f, linePaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, footerY + 50f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        }
    }
}
