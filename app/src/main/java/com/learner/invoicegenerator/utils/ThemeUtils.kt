package com.learner.invoicegenerator.utils

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import com.learner.invoicegenerator.R

object ThemeUtils {

    fun getColor(context: Context, colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    fun getPainterColor(context: Context, colorRes: Int): Int {
        return ContextCompat.getColor(context, colorRes)
    }

    fun getPrimaryColor(context: Context): Int {
        return getColor(context, R.color.primary_green)
    }

    fun getTextColorPrimary(context: Context): Int {
        return getColor(context, R.color.text_primary)
    }

    fun getTextColorSecondary(context: Context): Int {
        return getColor(context, R.color.text_secondary)
    }

    fun getTextColorGrey(context: Context): Int {
        return getColor(context, R.color.text_grey)
    }

    fun getBackgroundColor(context: Context): Int {
        return getColor(context, R.color.background_app)
    }

    fun getSurfaceColor(context: Context): Int {
        return getColor(context, R.color.surface_card)
    }

    fun getButtonAccentColor(context: Context): Int {
        return getColor(context, R.color.button_accent)
    }

    fun getButtonOnAccentColor(context: Context): Int {
        return getColor(context, R.color.on_primary_text)
    }
}
