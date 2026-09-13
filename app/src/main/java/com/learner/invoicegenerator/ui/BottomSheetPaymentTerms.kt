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
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.databinding.BottomSheetPaymentTermsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetPaymentTerms(paymentDueDateOffset: PaymentDueDateOffset): BottomSheetDialogFragment() {
    private var _binding: BottomSheetPaymentTermsBinding?=null
    val binding get()=_binding!!
    val settingViewModel: WorkspaceSettingsViewModel by activityViewModels ()
    var offset=paymentDueDateOffset

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetPaymentTermsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initOffset(offset)
        disableRadioButtonDefaultBehaviour()
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        binding.closebtn.setOnClickListener {
            dismiss()
        }
        binding.dueSection.setOnClickListener {
            offset= PaymentDueDateOffset.NET0
            UpdateUI(binding.dueRadioBtn,"0")
        }
        binding.net7Section.setOnClickListener {
            offset= PaymentDueDateOffset.NET7
            UpdateUI(binding.net7RadioBtn,"7")
        }
        binding.net14Section.setOnClickListener {
            offset= PaymentDueDateOffset.NET14
            UpdateUI(binding.net14RadioBtn,"14")
        }
        binding.net30Section.setOnClickListener {
            offset= PaymentDueDateOffset.NET30
            UpdateUI(binding.net30RadioBtn,"30")
        }
        binding.net45Section.setOnClickListener {
            offset= PaymentDueDateOffset.NET45
            UpdateUI(binding.net45RadioBtn,"45")
        }
        binding.net60Section.setOnClickListener {
            offset= PaymentDueDateOffset.NET60
            UpdateUI(binding.net60RadioBtn,"60")
        }

        settingViewModel.state.observe(viewLifecycleOwner){state->
            when(state){
                is WorkspaceSettingsState.Success->{
                    dismiss()
                    settingViewModel.resetState()
                }
                is WorkspaceSettingsState.Error->{
                    Toast.makeText(context,"update failed , Try again",Toast.LENGTH_SHORT).show()
                    settingViewModel.resetState()
                }
                else->{}
            }

        }

        binding.doneBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                settingViewModel.updatePaymentDueDateOffset(activeWorkspaceId,offset)
            }
        }

    }
    private fun disableRadioButtonDefaultBehaviour(){
        binding.dueRadioBtn.isClickable=false
        binding.net7RadioBtn.isClickable=false
        binding.net14RadioBtn.isClickable=false
        binding.net30RadioBtn.isClickable=false
        binding.net45RadioBtn.isClickable=false
        binding.net60RadioBtn.isClickable=false
    }

    private fun  UpdateUI(radio: RadioButton,days:String){
        binding.dueDateNumDisplay.text=days
        listOf(
            binding.dueRadioBtn,
            binding.net7RadioBtn,
            binding.net14RadioBtn,
            binding.net30RadioBtn,
            binding.net45RadioBtn,
            binding.net60RadioBtn
        ).forEach { it.isChecked=false }
        radio.isChecked=true
    }
    private fun initOffset(offset: PaymentDueDateOffset){
        binding.dueDateNumDisplay.text=offset.toString()
        when(offset){
            PaymentDueDateOffset.NET0->UpdateUI(binding.dueRadioBtn,"0")
            PaymentDueDateOffset.NET14->UpdateUI(binding.net14RadioBtn,"14")
            PaymentDueDateOffset.NET7->UpdateUI(binding.net7RadioBtn,"7")
            PaymentDueDateOffset.NET30->UpdateUI(binding.net30RadioBtn,"30")
            PaymentDueDateOffset.NET45->UpdateUI(binding.net45RadioBtn,"45")
            PaymentDueDateOffset.NET60->UpdateUI(binding.net60RadioBtn,"60")
        }
    }
}