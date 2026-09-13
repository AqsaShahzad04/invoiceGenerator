package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.learner.invoicegenerator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetSelectInvoicePrefixBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceState
import kotlinx.coroutines.launch

class BottomSheetSelectInvoicePrefix(defaultPrefix:String): BottomSheetDialogFragment() {
    private var _binding: BottomSheetSelectInvoicePrefixBinding?=null
    val binding get()=_binding!!
    var selectedPrefix=defaultPrefix
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetSelectInvoicePrefixBinding.inflate(inflater,container,false)
        return binding.root
    }

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    fun UpdateselectedPrefix(view:View?,prefix:String){
        listOf(
            binding.Suggestion1,
            binding.Suggestion2,
            binding.Suggestion3,
            binding.Suggestion4
        ).forEach { it.setBackgroundResource(R.drawable.bg_input_field) }
        if(view!=null){
            view.setBackgroundResource(R.drawable.bg_row_selected)
            binding.invoicePrefixDisplay.text = prefix
        }
        else{
            binding.invoicePrefixDisplay.text = prefix
        }


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
           init()
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        
        binding.customPrefixInput.addTextChangedListener{
            UpdateselectedPrefix(null,it.toString())

        }
        

        settingsViewModel.state.observe(viewLifecycleOwner) { state ->
            when(state){
               is WorkspaceSettingsState.Success->{
                   dismiss()
                   Toast.makeText(context,"prefix updated", Toast.LENGTH_SHORT).show()
                   settingsViewModel.resetState()
               }
                is WorkspaceSettingsState.Error->{
                    Toast.makeText(context,state.message,Toast.LENGTH_SHORT).show()
                }
                else->{}
          }

        }

        binding.doneBtn.setOnClickListener {
            val finalPrefix=binding.invoicePrefixDisplay.text.toString()
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateInvoicePrefix(activeWorkspaceId,finalPrefix)

            }
        }
         binding.Suggestion1.setOnClickListener {
             selectedPrefix=binding.sug1Prefix.text.toString()
             UpdateselectedPrefix(binding.Suggestion1,"INV-2026-")
         }

        binding.Suggestion2.setOnClickListener {
            selectedPrefix=binding.sug2Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion2,"2026-")
        }
        binding.Suggestion3.setOnClickListener {
            selectedPrefix=binding.sug3Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion3,"AISH")
        }
        binding.Suggestion4.setOnClickListener {
            selectedPrefix=binding.sug4Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion4,"#")
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

    }

    private fun init() {
        binding.invoicePrefixDisplay.text=selectedPrefix
        binding.customPrefixInput.setText(selectedPrefix)
        when (selectedPrefix) {
            "INV-2026-" -> {
                binding.Suggestion1.setBackgroundResource(R.drawable.bg_row_selected)
            }
            "2026-" -> {
                binding.Suggestion2.setBackgroundResource(R.drawable.bg_row_selected)
            }
            "AISH" -> {
                binding.Suggestion3.setBackgroundResource(R.drawable.bg_row_selected)
            }
            "#" -> {
                binding.Suggestion4.setBackgroundResource(R.drawable.bg_row_selected)
            }
        }
    }
}