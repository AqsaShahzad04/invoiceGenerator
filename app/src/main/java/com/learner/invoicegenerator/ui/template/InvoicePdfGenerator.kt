package com.learner.invoicegenerator.ui.template

import android.content.Context
import android.print.PdfPrintHelper
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object InvoicePdfGenerator {

    suspend fun generatePdf(
        context: Context,
        data: InvoiceRenderData,
        templateId: Int
    ): File = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            var webView: WebView? = null
            try {
                val invoicesDir = File(context.filesDir, "invoices")
                if (!invoicesDir.exists()) {
                    invoicesDir.mkdirs()
                }

                val safeInvoiceNum = data.invoice.invoiceNum.replace("#", "").replace("/", "_")
                val fileName = "Invoice_${safeInvoiceNum}_${System.currentTimeMillis()}.pdf"
                val outputFile = File(invoicesDir, fileName)

                val htmlContent = InvoiceHtmlTemplateFactory.generateHtml(data, templateId)

                webView = WebView(context.applicationContext)
                webView.settings.javaScriptEnabled = false
                webView.settings.loadWithOverviewMode = true
                webView.settings.useWideViewPort = true

                continuation.invokeOnCancellation {
                    try {
                        webView?.stopLoading()
                        webView?.destroy()
                    } catch (_: Exception) {}
                }

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        try {
                            val activeWebView = webView ?: return
                            val printAdapter = activeWebView.createPrintDocumentAdapter("Invoice")
                            PdfPrintHelper.print(printAdapter, outputFile) { success, throwable ->
                                try {
                                    activeWebView.destroy()
                                } catch (_: Exception) {}

                                if (success) {
                                    if (continuation.isActive) {
                                        continuation.resume(outputFile)
                                    }
                                } else {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(
                                            throwable ?: RuntimeException("PDF generation failed")
                                        )
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            try { webView?.destroy() } catch (_: Exception) {}
                            if (continuation.isActive) {
                                continuation.resumeWithException(e)
                            }
                        }
                    }
                }

                webView.loadDataWithBaseURL(
                    null,
                    htmlContent,
                    "text/html",
                    "UTF-8",
                    null
                )
            } catch (e: Exception) {
                try { webView?.destroy() } catch (_: Exception) {}
                if (continuation.isActive) {
                    continuation.resumeWithException(e)
                }
            }
        }
    }
}
