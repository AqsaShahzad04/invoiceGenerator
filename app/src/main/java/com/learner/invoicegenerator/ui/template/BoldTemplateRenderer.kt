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

        val darkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = darkCharcoal
            style = Paint.Style.FILL
        }

        val goldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = goldYellow
            style = Paint.Style.FILL
        }

        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 10f
            typeface = Typeface.DEFAULT
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

        // --- Top Dark Charcoal Header with Wave Curve ---
        val headerHeight = 95f
        val topRect = RectF(0f, 0f, width, headerHeight)
        canvas.drawRect(topRect, darkPaint)

        // Wave curve shape below top header
        val wavePath = Path().apply {
            moveTo(0f, headerHeight)
            cubicTo(width * 0.35f, headerHeight + 30f, width * 0.70f, headerHeight - 20f, width, headerHeight + 15f)
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
            val logoDst = RectF(20f, 20f, 55f, 55f)
            canvas.drawBitmap(logo, null, logoDst, null)
            whitePaint.textSize = 14f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "LOREM", 65f, 42f, whitePaint)
        } else {
            whitePaint.textSize = 16f
            whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(data.workspace?.name?.uppercase() ?: "LOREM", 20f, 42f, whitePaint)
        }

        // Top Right Contact Info
        whitePaint.textAlign = Paint.Align.RIGHT
        whitePaint.textSize = 8f
        whitePaint.typeface = Typeface.DEFAULT

        if (data.workspace != null) {
            if (!data.workspace.phone.isNullOrEmpty()) canvas.drawText("Phone: ${data.workspace.phone}", width - 20f, 30f, whitePaint)
            if (!data.workspace.email.isNullOrEmpty()) canvas.drawText("Email: ${data.workspace.email}", width - 20f, 42f, whitePaint)
            if (!data.workspace.address.isNullOrEmpty()) canvas.drawText("Area: ${data.workspace.address}", width - 20f, 54f, whitePaint)
        }
        whitePaint.textAlign = Paint.Align.LEFT

        var currentY = headerHeight + 45f

        // --- Large INVOICE Title & Client Details ---
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#171817")
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("INVOICE", margin, currentY, titlePaint)

        currentY += 25f

        // Left: Client info
        textGrey.textSize = 9f
        canvas.drawText("To:", margin, currentY, textGrey)
        textBold.textSize = 11f
        canvas.drawText(data.invoice.clientBusinessName, margin, currentY + 14f, textBold)
        if (data.client != null) {
            if (!data.client.address.isNullOrEmpty()) canvas.drawText(data.client.address, margin, currentY + 26f, textGrey)
            if (!data.client.phone.isNullOrEmpty()) canvas.drawText(data.client.phone, margin, currentY + 38f, textGrey)
        }

        // Right: Invoice Meta
        val rightX = width - margin
        textGrey.textAlign = Paint.Align.RIGHT
        textBold.textAlign = Paint.Align.RIGHT

        canvas.drawText("Invoice No:  ${data.invoice.invoiceNum}", rightX, currentY + 14f, textBold)
        canvas.drawText("Date:  ${data.invoice.issueDate.format(dateFormatter)}", rightX, currentY + 26f, textGrey)

        textGrey.textAlign = Paint.Align.LEFT
        textBold.textAlign = Paint.Align.LEFT

        currentY += 60f

        // --- Table Headers (Gold Item Desc block + Dark Charcoal Price/Qty/Total block) ---
        val headerHeightBar = 24f
        val splitX = width * 0.50f

        // Gold section for ITEM DESCRIPTION
        val goldHeaderRect = RectF(margin, currentY, splitX, currentY + headerHeightBar)
        canvas.drawRect(goldHeaderRect, goldPaint)

        // Dark section for PRICE, QTY, TOTAL
        val darkHeaderRect = RectF(splitX, currentY, width - margin, currentY + headerHeightBar)
        canvas.drawRect(darkHeaderRect, darkPaint)

        textBold.textSize = 9f
        textBold.color = Color.parseColor("#171817")
        canvas.drawText("ITEM DESCRIPTION", margin + 10f, currentY + 16f, textBold)

        whitePaint.textSize = 9f
        whitePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val col1 = splitX + 20f               // Price
        val col2 = width - margin - 110f      // Qty
        val col3 = width - margin - 10f       // Total

        canvas.drawText("PRICE", col1, currentY + 16f, whitePaint)
        canvas.drawText("QTY", col2, currentY + 16f, whitePaint)

        whitePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL", col3, currentY + 16f, whitePaint)
        whitePaint.textAlign = Paint.Align.LEFT

        currentY += headerHeightBar

        // --- Table Items (Alternating light rows) ---
        val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightRowBg
            style = Paint.Style.FILL
        }

        val subtotal = data.items.sumOf { it.unitPrice * it.itemQuantity }

        data.items.forEachIndexed { index, item ->
            val rowHeight = 24f
            if (index % 2 == 1) {
                val rowRect = RectF(margin, currentY, width - margin, currentY + rowHeight)
                canvas.drawRect(rowRect, rowPaint)
            }

            textBold.textSize = 10f
            canvas.drawText(item.itemName, margin + 10f, currentY + 16f, textBold)

            textGrey.textSize = 9f
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", item.unitPrice)}", col1, currentY + 16f, textGrey)
            canvas.drawText("${item.itemQuantity.toInt()}", col2, currentY + 16f, textGrey)

            textBold.textAlign = Paint.Align.RIGHT
            val itemTotal = item.unitPrice * item.itemQuantity
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", itemTotal)}", col3, currentY + 16f, textBold)
            textBold.textAlign = Paint.Align.LEFT

            currentY += rowHeight
        }

        currentY += 15f

        // --- Totals Block ---
        val labelX = width - margin - 150f

        textGrey.textSize = 9f
        canvas.drawText("Sub Total", labelX, currentY, textGrey)
        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", subtotal)}", col3, currentY, textBold)
        textBold.textAlign = Paint.Align.LEFT
        currentY += 16f

        if (data.invoice.taxPercentage > 0) {
            val taxAmt = subtotal * (data.invoice.taxPercentage / 100)
            canvas.drawText("Tax Vat ${data.invoice.taxPercentage.toInt()}%", labelX, currentY, textGrey)
            textBold.textAlign = Paint.Align.RIGHT
            canvas.drawText("${data.currencySymbol} ${String.format("%.2f", taxAmt)}", col3, currentY, textBold)
            textBold.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        currentY += 10f

        // Gold GRAND TOTAL Bar
        val grandTotalRect = RectF(width - margin - 220f, currentY, width - margin, currentY + 28f)
        canvas.drawRect(grandTotalRect, goldPaint)

        textBold.textSize = 11f
        canvas.drawText("GRAND TOTAL", width - margin - 210f, currentY + 18f, textBold)

        textBold.textAlign = Paint.Align.RIGHT
        canvas.drawText("${data.currencySymbol} ${String.format("%.2f", data.invoice.totalAmount)}", col3 - 10f, currentY + 18f, textBold)
        textBold.textAlign = Paint.Align.LEFT

        // --- Footer & Signature ---
        val footerY = height - margin - 40f

        textBold.textSize = 10f
        canvas.drawText("Thank you for your business!", margin, footerY, textBold)

        val sig = data.signatureBitmap
        if (sig != null) {
            val sigDst = RectF(width - margin - 100f, footerY - 25f, width - margin, footerY + 15f)
            canvas.drawBitmap(sig, null, sigDst, null)
            textGrey.textAlign = Paint.Align.RIGHT
            canvas.drawText("Authorized Manager", width - margin, footerY + 26f, textGrey)
            textGrey.textAlign = Paint.Align.LEFT
        }
    }
}
