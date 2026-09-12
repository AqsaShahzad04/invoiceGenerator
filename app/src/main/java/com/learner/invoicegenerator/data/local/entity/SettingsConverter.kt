package com.learner.invoicegenerator.data.local.entity

import androidx.room.TypeConverter

class SettingsConverter {

    @TypeConverter
    fun fromNumberingReset(value: NumberingReset): String = value.name

    @TypeConverter
    fun toNumberingReset(value: String): NumberingReset = NumberingReset.valueOf(value)

    @TypeConverter
    fun fromPaymentDueDateOffset(value: PaymentDueDateOffset): String = value.name

    @TypeConverter
    fun toPaymentDueDateOffset(value: String): PaymentDueDateOffset = PaymentDueDateOffset.valueOf(value)

    @TypeConverter
    fun fromPaymentMethodsList(value: List<PaymentMethods>): String =
        value.joinToString(",") { it.name }

    @TypeConverter
    fun toPaymentMethodsList(value: String): List<PaymentMethods> =
        if (value.isBlank()) emptyList()
        else value.split(",").map { PaymentMethods.valueOf(it) }
}