package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.PaymentMethods
import com.learner.invoicegenerator.databinding.BottomSheetPaymentMethodsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class BottomSheetPaymentMethods(paymentMethods: MutableList<PaymentMethods>): BottomSheetDialogFragment() {

    private var _binding: BottomSheetPaymentMethodsBinding?=null
    val binding get()=_binding!!
    val selectedMethods=paymentMethods
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetPaymentMethodsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager=SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        initMethods()
        listOf(
            binding.bankRadioBtn,
            binding.cardRadioBtn,
            binding.cashRadioBtn,
            binding.ustdRadioBtn,
            binding.chequeRadioBtn,
            binding.easyPaisaRadioBtn,
            binding.jazzcashRadioBtn
        ).forEach { it.isClickable=false }

        binding.bankTransferSection.setOnClickListener {
           updateUI(binding.bankRadioBtn, PaymentMethods.BANKTransfer)

        }
        binding.cardSection.setOnClickListener {
            updateUI(binding.cardRadioBtn, PaymentMethods.Card)
        }
        binding.cashTransferSection.setOnClickListener {
            updateUI(binding.cashRadioBtn, PaymentMethods.Cash)
        }
        binding.ustdSection.setOnClickListener {
            updateUI(binding.ustdRadioBtn, PaymentMethods.USTD)
        }
        binding.chequeSection.setOnClickListener {
            updateUI(binding.chequeRadioBtn, PaymentMethods.Cheque)
        }
        binding.easyPaisaTransferSection.setOnClickListener {
            updateUI(binding.easyPaisaRadioBtn, PaymentMethods.EasyPaisa)
        }
        binding.jazzcashSection.setOnClickListener {
            updateUI(binding.jazzcashRadioBtn, PaymentMethods.Jazzcash)
        }

        settingsViewModel.state.observe(viewLifecycleOwner){state->
            when(state){
                is WorkspaceSettingsState.Success->{
                    dismiss()
                    binding.doneBtn.isEnabled=false
                    Toast.makeText(context,"Reset updated",Toast.LENGTH_SHORT).show()
                    settingsViewModel.resetState()
                }
                is WorkspaceSettingsState.Error->{
                    binding.doneBtn.isEnabled=true
                    Toast.makeText(context,"Error updating reset,Try again",Toast.LENGTH_SHORT).show()
                    settingsViewModel.resetState()
                }
                else->{}

            }
        }

        binding.doneBtn.setOnClickListener{
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updatePaymentMethods(activeWorkspaceId,selectedMethods)
            }
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

    }

    private fun updateUI(radioButton: RadioButton,method: PaymentMethods){
        radioButton.isChecked=!radioButton.isChecked
        if(!selectedMethods.contains(method)){
            selectedMethods.add(method)
        }
        else{
            selectedMethods.remove(method)
        }

    }
    private fun initMethods(){
        selectedMethods.forEach { method->
            when(method){
                PaymentMethods.BANKTransfer->binding.bankRadioBtn.isChecked=true
                PaymentMethods.Cash->binding.cashRadioBtn.isChecked=true
                PaymentMethods.Jazzcash->binding.jazzcashRadioBtn.isChecked=true
                PaymentMethods.EasyPaisa->binding.easyPaisaRadioBtn.isChecked=true
                PaymentMethods.Cheque->binding.chequeRadioBtn.isChecked=true
                PaymentMethods.USTD->binding.ustdRadioBtn.isChecked=true
                PaymentMethods.Card->binding.cardRadioBtn.isChecked=true
            }
        }
    }
}