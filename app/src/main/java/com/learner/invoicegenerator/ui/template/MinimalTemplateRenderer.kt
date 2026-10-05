package com.learner.invoicegenerator.ui.template

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.format.DateTimeFormatter

class MinimalTemplateRenderer : InvoiceTemplate {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy")

    override fun draw(canvas: Canvas, width: Float, height: Float, data: InvoiceRenderData) {
        canvas.drawColor(Color.WHITE)

        val margin = 36f
        val orangeColor = Color.parseColor("#E05A10")
        val darkColor = Color.parseColor("#171817")
        val greyColor = Color.parseColor("#5C625E")

        val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orangeColor
            style = Paint.Style.FILL
        }

        val textDark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkColor
            textSize = 10f
            typeface = Typeface.DEFAULT
        }

        val textBold = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkColor
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val textGrey = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = greyColor
            textSize = 9f
            typeface = Typeface.DEFAULT
        }

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val lightLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E5E5E5")
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
        }

        // --- Top Right Orange Accent Block ---
        val accentPath = Path().apply {
            moveTo(width - 40f, 0f)
            lineTo(width, 0f)
            lineTo(width, 100f)
            lineTo(width - 40f, 50f)
            close()
        }
        canvas.drawPath(accentPath, orangePaint)

        // --- Bottom Left Orange Vertical Bar Accent ---
        val leftAccentPath = Path().apply {
            moveTo(0f, height - 120f)
            lineTo(12f, height - 120f)
            lineTo(12f, height)
            lineTo(0f, height)
            close()
        }
        canvas.drawPath(leftAccentPath, orangePaint)

        var currentY = margin + 15f

        // Top Left: Workspace Logo / Cursive Title
        val logo = data.logoBitmap
        if (logo != null) {
            val logoDst = RectF(margin, currentY, margin + 45f, currentY + 45f)
            canvas.drawBitmap(logo, null, logoDst, null)
            val nameX = margin + 55f
            textBold.textSize = 15f
            canvas.drawText(data.workspace?.name ?: "Workspace", nameX, currentY + 22f, textBold)
        } else {
            textBold.textSize = 18f
            textBold.color = orangeColor
            canvas.drawText(data.workspace?.name ?: "Workspace", margin, currentY + 22f, textBold)
            textBold.color = darkColor
        }

        // Top Right: "INVOICE" title & Date
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = orangeColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("INVOICE", width - margin - 20f, currentY + 25f, titlePaint)

        textGrey.textAlign = Paint.Align.RIGHT
        textGrey.textSize = 9f
        canvas.drawText(data.invoice.issueDate.format(dateFormatter), width - margin - 20f, currentY + 42f, textGrey)
        textGrey.textAlign = Paint.Align.LEFT

        currentY += 85f

        // --- Details Row ---
        textBold.textSize = 10f
        canvas.drawText("NO / ISN ${data.invoice.invoiceNum}", margin, currentY, textBold)

        // Client info right aligned
        textGrey.textAlign = Paint.Align.RIGHT
        textBold.textAlign = Paint.Align.RIGHT

        canvas.drawText("TO.", width - margin, currentY - 14f, textGrey)
        textBold.textSize = 11f
        canvas.drawText(data.invoice.clientBusinessName, width - margin, currentY, textBold)

        if (data.client != null) {
            if (!data.client.address.isNullOrEmpty()) canvas.drawText(data.client.address, width - margin, currentY + 14f, textGrey)
            if (!data.client.phone.isNullOrEmpty()) canvas.drawText(data.client.phone, width - margin, currentY + 26f, textGrey)
        }

        textGrey.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        currentY += 55f

        // --- Table Headers ---
        canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)

        val col1 = margin + 10f                  // Qty
        val col2 = margin + 60f                  // Description
        val col3 = width - margin - 110f          // Price
        val col4 = width - margin - 10f           // Total

        textBold.textSize = 9f
        canvas.drawText("QTY", col1, currentY + 14f, textBold)
        canvas.drawText("DESCRIPTION", col2, currentY + 14f, textBold)
        canvas.drawText("PRICE", col3, currentY + 14f, textBold)

        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col4, currentY + 14f, textBold)
        textBold.textAlign = Paint.Align.LEFT

        currentY += 22f
        canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)

        currentY += 18f

        // Table Rows
        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEach { item ->
            textGrey.textSize = 10f
            canvas.drawText("${item.itemQuantity.toInt()}", col1, currentY, textGrey)

            textBold.textSize = 10f
            canvas.drawText(item.itemName, col2, currentY, textBold)

            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col3, currentY, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col4, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT

            currentY += 24f
            canvas.drawLine(margin, currentY, width - margin, currentY, lightLinePaint)
            currentY += 16f
        }

        currentY += 10f

        // --- Totals Section ---
        val rightX = width - margin
        val labelX = width - margin - 140f

        textGrey.textSize = 9f
        canvas.drawText("Sub Total", labelX, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", rightX, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 16f

        if (data.invoice.taxPercentage > 0) {
            val taxAmt = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax ${data.invoice.taxPercentage.toInt()}%", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmt)}", rightX, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        currentY += 10f

        // GRAND TOTAL Orange Box
        val boxWidth = 190f
        val boxLeft = width - margin - boxWidth
        val grandTotalRect = RectF(boxLeft, currentY, rightX, currentY + 28f)
        canvas.drawRect(grandTotalRect, orangePaint)

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("GRAND TOTAL", boxLeft + 12f, currentY + 18f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", rightX - 12f, currentY + 18f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        // --- Footer Notes ---
        val footerY = height - margin - 40f
        textGrey.textSize = 8f
        canvas.drawText("Terms & Condition", margin + 10f, footerY, textBold)
        if (!data.invoice.endNote.isNullOrEmpty()) {
            canvas.drawText(data.invoice.endNote, margin + 10f, footerY + 12f, textGrey)
        }
    }
}
