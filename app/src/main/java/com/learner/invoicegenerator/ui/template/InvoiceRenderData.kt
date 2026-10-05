package com.learner.invoicegenerator.ui.template

import android.graphics.Bitmap
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.data.local.entity.Workspace

data class InvoiceRenderData(
    val invoice: Invoice,
    val items: List<InvoiceItemLine>,
    val workspace: Workspace?,
    val client: Client?,
    val logoBitmap: Bitmap? = null,
    val signatureBitmap: Bitmap? = null,
    val currencySymbol: String = "$",
    val paymentInfo: String? = null,
    val isSample: Boolean = false
)
