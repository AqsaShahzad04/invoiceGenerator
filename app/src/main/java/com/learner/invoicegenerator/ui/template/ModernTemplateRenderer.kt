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
        val tealPrimary = Color.parseColor("#005C5A")
        val tealSecondary = Color.parseColor("#007A78")
        val darkHeader = Color.parseColor("#171817")

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        val tealPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tealSecondary
            style = Paint.Style.FILL
        }

        val textDark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        val textBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textGrey = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5C625E")
            textSize = 9f
            typeface = Typeface.DEFAULT
        }

        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5E5E5")
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        // --- Top Header Banners ---
        val headerHeight = 90f

        // Top dark left shape
        val leftPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(width * 0.45f, 0f)
            lineTo(width * 0.38f, headerHeight - 20f)
            lineTo(0f, headerHeight - 20f)
            close()
        }
        val darkPaintFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkHeader
            style = Paint.Style.FILL
        }
        canvas.drawPath(leftPath, darkPaintFill)

        // Top teal right banner
        val rightPath = Path().apply {
            moveTo(width * 0.40f, 0f)
            lineTo(width, 0f)
            lineTo(width, headerHeight)
            lineTo(width * 0.32f, headerHeight)
            close()
        }
        canvas.drawPath(rightPath, tealPaint)

        // Logo & Workspace Name in Header
        val logo = data.logoBitmap
        if (logo != null) {
            val logoDst = RectF(20f, 15f, 55f, 50f)
            canvas.drawBitmap(logo, null, logoDst, null)
            whitePaint.textSize = 12f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", 65f, 35f, whitePaint)
        } else {
            whitePaint.textSize = 14f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "MY WORKSPACE", 20f, 35f, whitePaint)
        }

        // Top Right: "INVOICE" title & Meta
        whitePaint.textAlign = Paint.Align.RIGHT
        whitePaint.textSize = 22f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("INVOICE", width - 20f, 32f, whitePaint)

        whitePaint.textSize = 8f
        whitePaint.typeface = Typeface.DEFAULT
        canvas.drawText("Invoice No:  ${data.invoice.invoiceNum}", width - 20f, 48f, whitePaint)
        canvas.drawText("Invoice Date:  ${data.invoice.issueDate.format(dateFormatter)}", width - 20f, 58f, whitePaint)
        canvas.drawText("Due Date:  ${data.invoice.dueDate.format(dateFormatter)}", width - 20f, 68f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        var currentY = headerHeight + 25f

        // --- Bill To & Details Section ---
        textBold.color = tealSecondary
        textBold.textSize = 10f
        canvas.drawText("INVOICE TO:", margin, currentY, textBold)
        textBold.color = Color.parseColor("#171817")

        textBold.textSize = 12f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 16f, textBold)

        if (data.client != null) {
            if (!data.client.phone.isNullOrEmpty()) canvas.drawText("Phone: ${data.client.phone}", margin, currentY + 30f, textGrey)
            if (!data.client.email.isNullOrEmpty()) canvas.drawText("Email: ${data.client.email}", margin, currentY + 42f, textGrey)
            if (!data.client.address.isNullOrEmpty()) canvas.drawText("Address: ${data.client.address}", margin, currentY + 54f, textGrey)
        }

        // Right side: Payment Status badge
        textGrey.textAlign = Paint.Align.RIGHT
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("STATUS:", width - margin, currentY, textGrey)
        textBold.textSize = 12f
        canvas.drawText(data.invoice.status.uppercase(), width - margin, currentY + 16f, textBold)

        textGrey.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        currentY += 75f

        // --- Table Headers (Teal Banner) ---
        val tableHeaderRect = RectF(margin, currentY, width - margin, currentY + 22f)
        canvas.drawRect(tableHeaderRect, tealPaint)

        whitePaint.textSize = 9f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col0 = margin + 10f
        val col1 = margin + 40f
        val col2 = width - margin - 180f
        val col3 = width - margin - 100f
        val col4 = width - margin - 10f

        canvas.drawText("NO.", col0, currentY + 14f, whitePaint)
        canvas.drawText("ITEM DESCRIPTION", col1, currentY + 14f, whitePaint)
        canvas.drawText("PRICE", col2, currentY + 14f, whitePaint)
        canvas.drawText("QTY.", col3, currentY + 14f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, currentY + 14f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        currentY += 22f

        // Items Rows
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEachIndexed { index, item ->
            val rowY = currentY + 16f
            val itemNoStr = String.format("%02d", index + 1)

            textGrey.textSize = 9f
            canvas.drawText(itemNoStr, col0, rowY, textGrey)

            textBold.textSize = 10f
            canvas.drawText(item.itemName, col1, rowY, textBold)

            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col2, rowY, textGrey)
            canvas.drawText("${item.itemQuantity.toInt()}", col3, rowY, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, rowY, textBold)
            textBold.textAlign = Paint.Align.LEFT

            currentY += 28f
            canvas.drawLine(margin, currentY, width - margin, currentY, gridPaint)
        }

        currentY += 15f

        // --- Totals Box ---
        val boxWidth = 180f
        val boxLeft = width - margin - boxWidth

        val taxAmount = if (data.invoice.taxPercentage > 0) subtotal * (data.invoice.taxPercentage / 100) else 0.0
        val discAmount = if (data.invoice.discountValue > 0) {
            if (data.invoice.discountType == "Percent") subtotal * (data.invoice.discountValue / 100) else data.invoice.discountValue
        } else 0.0

        // Subtotal row
        textGrey.textSize = 9f
        canvas.drawText("Subtotal:", boxLeft + 10f, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", width - margin - 10f, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 16f

        // Discount row
        if (discAmount > 0) {
            canvas.drawText("Discount:", boxLeft + 10f, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("- ${data.currencySymbol} ${String.format("%.2f", discAmount)}", width - margin - 10f, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        // Tax row
        if (taxAmount > 0) {
            canvas.drawText("Tax (${data.invoice.taxPercentage.toInt()}%):", boxLeft + 10f, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmount)}", width - margin - 10f, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        // Total Box Fill
        val totalBoxRect = RectF(boxLeft, currentY, width - margin, currentY + 24f)
        canvas.drawRect(totalBoxRect, tealPaint)

        whitePaint.textSize = 10f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Total:", boxLeft + 10f, currentY + 16f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", width - margin - 10f, currentY + 16f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        // --- Bottom Banner & Signature ---
        val bottomY = height - 50f

        val bottomPath = Path().apply {
            moveTo(0f, height - 35f)
            lineTo(width * 0.60f, height - 35f)
            lineTo(width * 0.55f, height)
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
            val sigDst = RectF(width - margin - 100f, bottomY - 35f, width - margin, bottomY + 5f)
            canvas.drawBitmap(sig, null, sigDst, null)
            canvas.drawLine(width - margin - 110f, bottomY + 6f, width - margin, bottomY + 6f, gridPaint)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Signature", width - margin, bottomY + 18f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        }
    }
}
