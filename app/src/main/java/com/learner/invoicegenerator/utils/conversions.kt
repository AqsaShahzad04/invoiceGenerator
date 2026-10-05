package com.learner.invoicegenerator.util

import android.content.Context
import kotlin.math.floor

object conversions {
    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()

    fun calculatePercentageChange(thisMonth: Double, previousMonth: Double): Double {
        if (previousMonth == 0.0) {
            return if (thisMonth > 0.0) 100.0 else 0.0
        }
        return ((thisMonth - previousMonth) / previousMonth) * 100.0
    }

    fun formatAmount(value: Double): String {
        return if (value == floor(value)) {
            // No decimal part at all (e.g. 500.0, 1200.0)
            value.toLong().toString()
        } else {
            // Has decimals — show up to 2 digits, but trim trailing zeros
            val formatted = String.format(java.util.Locale.US, "%.2f", value)
            formatted.trimEnd('0').trimEnd('.')
        }
    }
}
