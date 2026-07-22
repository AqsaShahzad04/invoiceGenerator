package com.learner.invoicegenerator

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class InvoiceApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}