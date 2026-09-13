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
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.databinding.BottomSheetNumbringResetBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetNumberingReset(reset: NumberingReset): BottomSheetDialogFragment() {

    private var _binding: BottomSheetNumbringResetBinding? = null
    val binding  get() =_binding!!
    val defaultNumReset=reset
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetNumbringResetBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager=SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        var selectedNumReset=defaultNumReset
        initReset(defaultNumReset)
        binding.yearlyRadioBtn.isClickable = false
        binding.monthlyRadioBtn.isClickable = false
        binding.neverRadioBtn.isClickable = false
        binding.closebtn.setOnClickListener {
            dismiss()
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


        binding.doneBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateNumberingReset(activeWorkspaceId, selectedNumReset)
            }

        }
        binding.yearlySection.setOnClickListener {
            selectedNumReset= NumberingReset.YEARLY
            UpdateUI(binding.yearlyRadioBtn)

        }


        binding.monthlySection.setOnClickListener {
            selectedNumReset= NumberingReset.MONTHLY
            UpdateUI(binding.monthlyRadioBtn)
        }

        binding.neverSection.setOnClickListener {
            selectedNumReset= NumberingReset.NEVER
            UpdateUI(binding.neverRadioBtn)
        }





    }

    private fun UpdateUI(radioButton: RadioButton){
        listOf(
            binding.yearlyRadioBtn,
            binding.monthlyRadioBtn,
            binding.neverRadioBtn
        ).forEach { it.isChecked=false }
        radioButton.isChecked=true
    }
    private fun initReset(reset: NumberingReset){
        when (reset) {
            NumberingReset.YEARLY -> UpdateUI(binding.yearlyRadioBtn)
            NumberingReset.MONTHLY -> UpdateUI(binding.monthlyRadioBtn)
            NumberingReset.NEVER -> UpdateUI(binding.neverRadioBtn)
        }

    }
}