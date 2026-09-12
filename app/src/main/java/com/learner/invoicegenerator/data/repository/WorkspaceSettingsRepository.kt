package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.dao.WorkspaceSettingsDao
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.PaymentMethods
import kotlinx.coroutines.flow.Flow

class WorkspaceSettingsRepository(
    private val dao: WorkspaceSettingsDao
) {

    // ---------- Read ----------

    fun getSettingsByWorkspaceId(workspaceId: Int): Flow<WorkspaceSettings?> =
        dao.getSettingsByWorkspaceId(workspaceId)

    // ---------- Insert ----------

    suspend fun insertDefaultSettings(settings: WorkspaceSettings) =
        dao.insertDefaultSettings(settings)

    // ---------- Update: Invoicing ----------

    suspend fun updateInvoicePrefix(workspaceId: Int, value: String) =
        dao.updateInvoicePrefix(workspaceId, value)

    suspend fun updateNumberingReset(workspaceId: Int, value: NumberingReset) =
        dao.updateNumberingReset(workspaceId, value)

    suspend fun updatePaymentDueDateOffset(workspaceId: Int, value: PaymentDueDateOffset) =
        dao.updatePaymentDueDateOffset(workspaceId, value)

    // ---------- Update: Tax & Fees ----------

    suspend fun updateDefaultTax(workspaceId: Int, value: Boolean) =
        dao.updateDefaultTax(workspaceId, value)

    suspend fun updateTaxRate(workspaceId: Int, value: Double) =
        dao.updateTaxRate(workspaceId, value)

    suspend fun updateLateFee(workspaceId: Int, value: Double) =
        dao.updateLateFee(workspaceId, value)

    suspend fun updatePaymentMethods(workspaceId: Int, value: List<PaymentMethods>) =
        dao.updatePaymentMethods(workspaceId, value)

    // ---------- Update: Document ----------

    suspend fun updateDiscountLine(workspaceId: Int, value: Boolean) =
        dao.updateDiscountLine(workspaceId, value)

    suspend fun updateSignatureBlock(workspaceId: Int, value: Boolean) =
        dao.updateSignatureBlock(workspaceId, value)

    suspend fun updateDefaultNotes(workspaceId: Int, value: String?) =
        dao.updateDefaultNotes(workspaceId, value)

    // ---------- Update: Reminders ----------

    suspend fun updateAutoReminders(workspaceId: Int, value: Boolean) =
        dao.updateAutoReminders(workspaceId, value)

    suspend fun updateSendReminderAfterDueDays(workspaceId: Int, value: Int) =
        dao.updateSendReminderAfterDueDays(workspaceId, value)

    // ---------- Update: Preferences ----------

    suspend fun updateNotifications(workspaceId: Int, value: Boolean) =
        dao.updateNotifications(workspaceId, value)
}