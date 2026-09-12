package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.learner.invoicegenerator.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.databinding.BottomSheetSelectInvoicePrefixBinding

class BottomSheetSelectInvoicePrefix(prefix:String): BottomSheetDialogFragment() {
    private var _binding: BottomSheetSelectInvoicePrefixBinding?=null
    val binding get()=_binding!!
    var selectedPrefix=prefix
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetSelectInvoicePrefixBinding.inflate(inflater,container,false)
        return binding.root
    }

    fun UpdateselectedPrefix(view:View){
        listOf(
            binding.Suggestion1,
            binding.Suggestion2,
            binding.Suggestion3,
            binding.Suggestion4
        ).forEach { it.isSelected=false }
        view.setBackgroundResource(R.drawable.bg_row_selected)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.customPrefixInput.addTextChangedListener{
            binding.invoicePrefixDisplay.text = it.toString()
        }
         binding.Suggestion1.setOnClickListener {
             selectedPrefix=binding.sug1Prefix.text.toString()
             UpdateselectedPrefix(binding.Suggestion1)
         }

        binding.Suggestion2.setOnClickListener {
            selectedPrefix=binding.sug2Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion2)
        }
        binding.Suggestion3.setOnClickListener {
            selectedPrefix=binding.sug3Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion3)
        }
        binding.Suggestion4.setOnClickListener {
            selectedPrefix=binding.sug4Prefix.text.toString()
            UpdateselectedPrefix(binding.Suggestion4)
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

    }
}