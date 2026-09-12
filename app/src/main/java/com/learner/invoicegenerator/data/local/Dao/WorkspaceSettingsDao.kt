package com.learner.invoicegenerator.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.PaymentMethods

@Dao
interface WorkspaceSettingsDao {

    // ---------- Read ----------

    @Query("SELECT * FROM Settings WHERE workspaceId = :workspaceId")
    fun getSettingsByWorkspaceId(workspaceId: Int): Flow<WorkspaceSettings?>

    // ---------- Insert ----------

    @Insert
    suspend fun insertDefaultSettings(settings: WorkspaceSettings)

    // ---------- Update: Invoicing ----------

    @Query("UPDATE Settings SET invoicePrefix = :value WHERE workspaceId = :workspaceId")
    suspend fun updateInvoicePrefix(workspaceId: Int, value: String)

    @Query("UPDATE Settings SET numberingReset = :value WHERE workspaceId = :workspaceId")
    suspend fun updateNumberingReset(workspaceId: Int, value: NumberingReset)

    @Query("UPDATE Settings SET paymentDueDateOffset = :value WHERE workspaceId = :workspaceId")
    suspend fun updatePaymentDueDateOffset(workspaceId: Int, value: PaymentDueDateOffset)

    // ---------- Update: Tax & Fees ----------

    @Query("UPDATE Settings SET defaultTax = :value WHERE workspaceId = :workspaceId")
    suspend fun updateDefaultTax(workspaceId: Int, value: Boolean)

    @Query("UPDATE Settings SET taxRate = :value WHERE workspaceId = :workspaceId")
    suspend fun updateTaxRate(workspaceId: Int, value: Double)

    @Query("UPDATE Settings SET lateFee = :value WHERE workspaceId = :workspaceId")
    suspend fun updateLateFee(workspaceId: Int, value: Double)

    @Query("UPDATE Settings SET paymentMethods = :value WHERE workspaceId = :workspaceId")
    suspend fun updatePaymentMethods(workspaceId: Int, value: List<PaymentMethods>)

    // ---------- Update: Document ----------

    @Query("UPDATE Settings SET discountLine = :value WHERE workspaceId = :workspaceId")
    suspend fun updateDiscountLine(workspaceId: Int, value: Boolean)

    @Query("UPDATE Settings SET signatureBlock = :value WHERE workspaceId = :workspaceId")
    suspend fun updateSignatureBlock(workspaceId: Int, value: Boolean)

    @Query("UPDATE Settings SET defaultNotes = :value WHERE workspaceId = :workspaceId")
    suspend fun updateDefaultNotes(workspaceId: Int, value: String?)

    // ---------- Update: Reminders ----------

    @Query("UPDATE Settings SET autoReminders = :value WHERE workspaceId = :workspaceId")
    suspend fun updateAutoReminders(workspaceId: Int, value: Boolean)

    @Query("UPDATE Settings SET sendReminderAfterDueDays = :value WHERE workspaceId = :workspaceId")
    suspend fun updateSendReminderAfterDueDays(workspaceId: Int, value: Int)

    // ---------- Update: Preferences ----------

    @Query("UPDATE Settings SET notifications = :value WHERE workspaceId = :workspaceId")
    suspend fun updateNotifications(workspaceId: Int, value: Boolean)
}