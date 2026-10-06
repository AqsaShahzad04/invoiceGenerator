package com.learner.invoicegenerator.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.learner.invoicegenerator.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.databinding.FragmentSettingsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.zip.Inflater
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.PaymentMethods
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.databinding.BottomSheetPaymentMethodsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import android.widget.Toast
import android.widget.LinearLayout
import android.widget.TextView
import android.app.AlertDialog
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import java.util.Locale

class SettingsFragment: Fragment(R.layout.fragment_settings)  {
    private var _binding: FragmentSettingsBinding? = null
    val binding get()=_binding!!

    val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()
    val itemViewModel: ItemViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        SavedInstanceState: Bundle?
    ): View? {
        _binding=FragmentSettingsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        val userId=sessionManager.getUserId()
         var currentSettings: WorkspaceSettings?=null

        binding.defaultTaxToggleBtn.isClickable=false
        binding.discountLineTogglebtn.isClickable=false

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                sessionManager.currencyCode.collect(){currencyCode->
                    val currency= CurrencyData.currencies.find {
                        it.code==currencyCode
                    }
                    val currencySymbol=currency?.symbol
                    binding.currentCurrency.setText(currencySymbol)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch{
            settingsViewModel.getSettingsByWorkspaceId(activeWorkspaceId).collect { settings->
                val paymentMethods: List<PaymentMethods> = settings?.paymentMethods?:listOf(
                    PaymentMethods.BANKTransfer,
                    PaymentMethods.Cash)
                val taxRateInt = (settings?.taxRate ?: 25.0).toInt()
                if (settings?.defaultTax == false) {
                    binding.defaultTaxSubtitle.text = "No tax on new invoice"
                } else {
                    binding.defaultTaxSubtitle.text = "Applied at $taxRateInt%"
                }
                binding.selectedInvoicePrefix.text = settings?.invoicePrefix ?: "INV-2026-"
                binding.numResettime.text = (settings?.numberingReset ?: NumberingReset.YEARLY).toString()
                binding.paymentNetRate.text = (settings?.paymentDueDateOffset ?: PaymentDueDateOffset.NET14).toString()
                binding.taxPercentage.text = "$taxRateInt%"
                binding.defaultTaxToggleBtn.isChecked = settings?.defaultTax ?: false
                binding.discountLineTogglebtn.isChecked = settings?.discountLine ?: false
                binding.paymentMethodsSubtitle.text = paymentMethods.joinToString(". ")
                binding.NumOfPaymentMethods.text = "${paymentMethods.size} active"
                binding.defaultNotesValue.text = settings?.defaultNotes ?: "Thank you"
                binding.sendAfterdaysNum.text = settings?.sendReminderAfterDueDays?.let { "$it days" } ?: "3 days"
                currentSettings=settings


            }
        }

        binding.currencySection.setOnClickListener {
            BottomSheetCurrencyPicker().show(parentFragmentManager,"currencyPickerBottomSheet")
        }
        binding.invoicePrefixSection.setOnClickListener {
            BottomSheetSelectInvoicePrefix(currentSettings?.invoicePrefix?:"INV-2026-").show(childFragmentManager,"invoicePrefixSElectionBottomSheet")
        }
        binding.numberingResetSection.setOnClickListener {
            BottomSheetNumberingReset(currentSettings?.numberingReset?: NumberingReset.YEARLY).show(childFragmentManager,"numberingResetBottomSheet")
        }
        binding.paymentTermsSection.setOnClickListener {
            BottomSheetPaymentTerms(currentSettings?.paymentDueDateOffset?: PaymentDueDateOffset.NET14).show(childFragmentManager,"paymentOffsetDueDateBottomSheet")
        }


        binding.taxRateSection.setOnClickListener {
            BottomSheetTaxRate(currentSettings?.taxRate?:25.0).show(childFragmentManager,"taxRateBottomSheet")
        }
        binding.defaultTaxSection.setOnClickListener {
            val taxEnabled: Boolean = currentSettings?.defaultTax?.not()?:false
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateDefaultTax(activeWorkspaceId,taxEnabled)
            }
        }
        binding.discountLineSection.setOnClickListener {
            val discountEnabled=currentSettings?.discountLine?.not()?:false
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateDiscountLine(activeWorkspaceId,discountEnabled)
            }

        }
        binding.signSection.setOnClickListener {
            BottomSheetAddSignature().show(parentFragmentManager,"AddSignatureBottomSheet")

        }
        binding.notesSection.setOnClickListener {
            BottomSheetDefaultNotes(currentSettings?.defaultNotes?:"Thank you").show(childFragmentManager,"defaultNotesBottomSheet")
        }

        binding.sendAfterSection.setOnClickListener {
            BottomSheetSendRemindersAfterDays(currentSettings?.sendReminderAfterDueDays?:3).show(childFragmentManager,"sendReminderAfterDaysBottomSheet")
        }

        binding.PaymentMethodsSection.setOnClickListener {
            val methods:MutableList<PaymentMethods> = currentSettings?.paymentMethods?:mutableListOf(
                PaymentMethods.BANKTransfer, PaymentMethods.Cash)
            BottomSheetPaymentMethods(methods).show(childFragmentManager,"paymentMethodsBottomSheet")
        }


        binding.autoReminderTogglebtn.isChecked = sessionManager.isAutoRemindersEnabled()
        binding.autoReminderTogglebtn.setOnCheckedChangeListener { _, isChecked ->
            sessionManager.setAutoRemindersEnabled(isChecked)
        }
        binding.autoRemindersSection.setOnClickListener {
            binding.autoReminderTogglebtn.toggle()
        }

        binding.darkmodeToggleBtn.isChecked = sessionManager.isDarkModeEnabled()

        binding.darkmodeToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            if (sessionManager.isDarkModeEnabled() != isChecked) {
                sessionManager.setDarkModeEnabled(isChecked)
                val mode = if (isChecked) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode)
            }
        }

        binding.darkmodeSection.setOnClickListener {
            binding.darkmodeToggleBtn.toggle()
        }

        binding.currentHomeHeader.text = sessionManager.getHomeHeaderStyle()
        binding.homeHeaderSection.setOnClickListener {
            BottomSheetHomeHeader { newStyle ->
                binding.currentHomeHeader.text = newStyle
            }.show(childFragmentManager, "homeHeaderBottomSheet")
        }




        viewLifecycleOwner.lifecycleScope.launch{
            val workspace=workspaceViewModel.getWorkspaceById(activeWorkspaceId)
            workspace?.let{
                binding.workspaceSubtitle.setText(workspace.name)
                binding.workspaceSection.setOnClickListener {
                    findNavController().navigate(R.id.action_settingsFragment_to_manageWorkspaceFragment)
                }
            }
            workspaceViewModel.getWorkspacesByUserId(userId).map{it.size}
                .collect{count->
                    binding.workspaceNum.setText(count.toString())
                }



        }

        binding.clearCacheSection.setOnClickListener {
            clearCache(it.context)
        }

        binding.ResetAllDataSection.setOnClickListener {
            resetAllData(it.context)
        }

        // Search functionality
        setupSearch()

    }

    private fun setupSearch() {
        binding.etSearchSettings.addTextChangedListener { text ->
            val query = text?.toString()?.lowercase(Locale.getDefault())?.trim() ?: ""
            filterSettingsSections(query)
        }
    }

    private fun filterSettingsSections(query: String) {
        val sections = listOf(
            binding.settingsSection1,
            binding.settingsSection2,
            binding.settingsSection3,
            binding.settingsSection4,
            binding.settingsSection5,
            binding.settingsSection6,
            binding.settingsSection7,
            binding.settingsSection8
        )

        val cleanQuery = query.trim().lowercase(Locale.getDefault())

        sections.forEach { section ->
            var anyRowVisibleInSection = false

            for (i in 0 until section.childCount) {
                val child = section.getChildAt(i)
                if (child is LinearLayout && child.id != View.NO_ID) {
                    if (cleanQuery.isEmpty()) {
                        child.visibility = View.VISIBLE
                        anyRowVisibleInSection = true
                    } else {
                        val isMatch = matchesQuery(child, cleanQuery)
                        child.visibility = if (isMatch) View.VISIBLE else View.GONE
                        if (isMatch) {
                            anyRowVisibleInSection = true
                        }
                    }
                } else if (child !is TextView) {
                    // Divider line between rows
                    child.visibility = if (cleanQuery.isEmpty()) View.VISIBLE else View.GONE
                }
            }

            section.visibility = if (cleanQuery.isEmpty() || anyRowVisibleInSection) View.VISIBLE else View.GONE
        }
    }

    private fun matchesQuery(rowView: View, query: String): Boolean {
        val allText = getAllTextViewTexts(rowView).lowercase(Locale.getDefault())
        if (allText.contains(query)) return true

        if (rowView.id == R.id.ResetAllDataSection && ("delete".contains(query) || "workspace".contains(query) || "reset".contains(query) || "clear".contains(query))) {
            return true
        }
        return false
    }

    private fun getAllTextViewTexts(view: View): String {
        val builder = StringBuilder()
        if (view is TextView) {
            builder.append(" ").append(view.text)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                builder.append(" ").append(getAllTextViewTexts(view.getChildAt(i)))
            }
        }
        return builder.toString()
    }

    private fun clearCache(context: Context) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    context.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
                    context.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
                }
                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Error clearing cache", e)
                Toast.makeText(context, "Failed to clear cache", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resetAllData(context: Context) {
        val sessionManager = SessionManager.getInstance(context)
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        // Show confirmation dialog
        AlertDialog.Builder(context)
            .setTitle(R.string.delete_all_data_confirm_title)
            .setMessage(R.string.delete_all_data_confirm_message)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                deleteWorkspaceData(activeWorkspaceId, context)
            }
            .setNegativeButton(android.R.string.no, null)
            .show()
    }

    private fun deleteWorkspaceData(workspaceId: Int, context: Context) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Delete all data using viewmodels
                invoiceViewModel.deleteAllInvoices(workspaceId)
                itemViewModel.deleteAllItems(workspaceId)
                clientViewModel.deleteAllClients(workspaceId)

                // Wait a bit for the operations to complete
                kotlinx.coroutines.delay(500)

                Toast.makeText(context, R.string.delete_all_data_success, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("SettingsFragment", "Error deleting workspace data", e)
                Toast.makeText(context, "Failed to delete data", Toast.LENGTH_SHORT).show()
            }
        }
    }

}