package com.learner.invoicegenerator.ui.template

object TemplateFactory {
    fun getTemplate(templateId: Int): InvoiceTemplate {
        return when (templateId) {
            1 -> ClassicTemplateRenderer()
            2 -> ModernTemplateRenderer()
            3 -> MinimalTemplateRenderer()
            4 -> BoldTemplateRenderer()
            else -> ClassicTemplateRenderer()
        }
    }
}
