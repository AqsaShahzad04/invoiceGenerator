package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class SettingsFragment: Fragment(R.layout.fragment_settings)  {
    private var _binding: FragmentSettingsBinding? = null
    val binding get()=_binding!!

    val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
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
        binding.signatureTogglebtn.isClickable=false

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
                if(settings?.defaultTax==false){
                    binding.defaultTaxSubtitle.text="No tax on new invoice"
                }
                else{
                    val taxRate=settings?.taxRate.toString()
                    binding.defaultTaxSubtitle.text="Applied at $taxRate%"
                }
                binding.selectedInvoicePrefix.text = settings?.invoicePrefix
                binding.numResettime.text = settings?.numberingReset.toString()
                binding.paymentNetRate.text=settings?.paymentDueDateOffset.toString()
                binding.taxPercentage.text=settings?.taxRate.toString()
                binding.defaultTaxToggleBtn.isChecked=settings?.defaultTax?:false
                binding.discountLineTogglebtn.isChecked=settings?.discountLine?:false
                binding.signatureTogglebtn.isChecked=settings?.signatureBlock?:false
                binding.taxPercentage.text = "${settings?.taxRate?.toInt()?:25}%"
                binding.LateFeeValue.text=settings?.lateFee?.toString()?:"off"
                binding.paymentMethodsSubtitle.text=paymentMethods.joinToString(".")
                binding.NumOfPaymentMethods.text=paymentMethods.size.toString()+"active"
                binding.defaultNotesValue.text=settings?.defaultNotes?:"Thank you"
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
            val signEnabled=currentSettings?.signatureBlock?.not()?:false
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateSignatureBlock(activeWorkspaceId,signEnabled)
            }

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
        binding.lateFeeSection.setOnClickListener {
            BottomSheetLateFee(currentSettings?.lateFee?:0.0).show(childFragmentManager,"lateFeeRateBottomSheet")
        }




        viewLifecycleOwner.lifecycleScope.launch{
            val workspace=workspaceViewModel.getWorkspaceById(activeWorkspaceId)
            workspace?.let{
                binding.workspaceSubtitle.setText(workspace.name)
                binding.icChivWorkspace.setOnClickListener {
                    findNavController().navigate(R.id.action_settingsFragment_to_manageWorkspaceFragment)
                }
            }
            workspaceViewModel.getWorkspacesByUserId(userId).map{it.size}
                .collect{count->
                    binding.workspaceNum.setText(count.toString())
                }



        }




    }

}