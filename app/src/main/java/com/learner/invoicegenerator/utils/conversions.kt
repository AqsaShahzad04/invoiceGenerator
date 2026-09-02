package com.learner.invoicegenerator.util

import android.content.Context


object conversions{
    fun Int.dpToPx(context: Context): Int =
        (this * context.resources.displayMetrics.density).toInt()
}
