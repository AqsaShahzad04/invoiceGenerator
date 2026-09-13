package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.PaymentMethods
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.data.repository.WorkspaceSettingsRepository
import kotlinx.coroutines.flow.Flow


class WorkspaceSettingsViewModel(
    private val repository: WorkspaceSettingsRepository
) : ViewModel() {

    private val _state = MutableLiveData<WorkspaceSettingsState>(WorkspaceSettingsState.Idle)
    val state: LiveData<WorkspaceSettingsState> = _state

    fun resetState() {
        _state.value = WorkspaceSettingsState.Idle
    }

    // ---------- Read ----------

    fun getSettingsByWorkspaceId(workspaceId: Int): Flow<WorkspaceSettings?> =
        repository.getSettingsByWorkspaceId(workspaceId)

    // ---------- Insert ----------

    suspend fun insertDefaultSettings(workspaceId:Int) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.insertDefaultSettings(workspaceId)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to create settings",)
        }
    }

    // ---------- Update: Invoicing ----------

    suspend fun updateInvoicePrefix(workspaceId: Int, value: String) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateInvoicePrefix(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update invoice prefix",)
        }
    }

    suspend fun updateNumberingReset(workspaceId: Int, value: NumberingReset) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateNumberingReset(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update numbering reset",)
        }
    }

    suspend fun updatePaymentDueDateOffset(workspaceId: Int, value: PaymentDueDateOffset) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updatePaymentDueDateOffset(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update payment due date offset",)
        }
    }

    // ---------- Update: Tax & Fees ----------

    suspend fun updateDefaultTax(workspaceId: Int, value: Boolean) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateDefaultTax(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update default tax",)
        }
    }

    suspend fun updateTaxRate(workspaceId: Int, value: Double) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateTaxRate(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update tax rate",)
        }
    }

    suspend fun updateLateFee(workspaceId: Int, value: Double) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateLateFee(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update late fee",)
        }
    }

    suspend fun updatePaymentMethods(workspaceId: Int, value: List<PaymentMethods>) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updatePaymentMethods(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update payment methods",)
        }
    }

    // ---------- Update: Document ----------

    suspend fun updateDiscountLine(workspaceId: Int, value: Boolean) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateDiscountLine(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update discount line",)
        }
    }

    suspend fun updateSignatureBlock(workspaceId: Int, value: Boolean) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateSignatureBlock(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update signature block",)
        }
    }

    suspend fun updateDefaultNotes(workspaceId: Int, value: String?) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateDefaultNotes(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update default notes",)
        }
    }

    // ---------- Update: Reminders ----------

    suspend fun updateAutoReminders(workspaceId: Int, value: Boolean) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateAutoReminders(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update auto reminders",)
        }
    }

    suspend fun updateSendReminderAfterDueDays(workspaceId: Int, value: Int) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateSendReminderAfterDueDays(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update reminder days",)
        }
    }

    // ---------- Update: Preferences ----------

    suspend fun updateNotifications(workspaceId: Int, value: Boolean) {
        _state.value = WorkspaceSettingsState.Loading
        try {
            repository.updateNotifications(workspaceId, value)
            _state.value = WorkspaceSettingsState.Success
        } catch (e: Exception) {
            _state.value = WorkspaceSettingsState.Error(e.message ?: "Failed to update notifications",)
        }
    }
}