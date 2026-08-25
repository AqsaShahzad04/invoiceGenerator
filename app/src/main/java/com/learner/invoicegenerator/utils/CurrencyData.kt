package com.learner.invoicegenerator.utils

import com.learner.invoicegenerator.ui.model.Currency

 object CurrencyData {

    val currencies = listOf(
        Currency("PKR", "Pakistani Rupee · Rs", "Rs"),
        Currency("USD", "US Dollar · $", "$"),
        Currency("EUR", "Euro · €", "€"),
        Currency("GBP", "Pound Sterling · £", "£"),
        Currency("INR", "Indian Rupee · ₹", "₹"),
        Currency("AED", "UAE Dirham · AED", "AED"),
        Currency("SAR", "Saudi Riyal · SAR", "SAR"),
        Currency("QAR", "Qatari Riyal · QAR", "QAR"),
        Currency("BDT", "Bangladeshi Taka · ৳", "৳"),
        Currency("LKR", "Sri Lankan Rupee · Rs", "Rs"),
        Currency("NGN", "Nigerian Naira · ₦", "₦"),
        Currency("KES", "Kenyan Shilling · KSh", "KSh"),
        Currency("ZAR", "South African Rand · R", "R"),
        Currency("EGP", "Egyptian Pound · E£", "E£"),
        Currency("TRY", "Turkish Lira · ₺", "₺"),
        Currency("CAD", "Canadian Dollar · C$", "C$"),
        Currency("AUD", "Australian Dollar · A$", "A$"),
        Currency("CHF", "Swiss Franc · CHF", "CHF"),
        Currency("SGD", "Singapore Dollar · S$", "S$"),
        Currency("MYR", "Malaysian Ringgit · RM", "RM"),
        Currency("IDR", "Indonesian Rupiah · Rp", "Rp"),
        Currency("PHP", "Philippine Peso · ₱", "₱"),
        Currency("THB", "Thai Baht · ฿", "฿"),
        Currency("JPY", "Japanese Yen · ¥", "¥"),
        Currency("CNY", "Chinese Yuan · ¥", "¥"),
        Currency("BRL", "Brazilian Real · R$", "R$"),
        Currency("MXN", "Mexican Peso · Mex$", "Mex$")
    )
}