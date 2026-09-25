package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.BottomSheetDeleteInvoiceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceState
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import kotlinx.coroutines.launch

class BottomSheetDeleteInvoice: BottomSheetDialogFragment() {

    private var _binding: BottomSheetDeleteInvoiceBinding?=null
    val binding get()=_binding!!
    val invoiceViewModel: InvoiceViewModel by  activityViewModels()
    companion object{
        private const val INVOICE_NUM="currentInvoiceId"

        fun newInstance(invoiceNum:String): BottomSheetDeleteInvoice{
            val fragment= BottomSheetDeleteInvoice()
            val args= Bundle()
            args.putString(INVOICE_NUM,invoiceNum)
            fragment.arguments=args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetDeleteInvoiceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cancelBtn.setOnClickListener {
            dismiss()
        }
        val invNum=arguments?.getString(INVOICE_NUM)?:""
        binding.invNum.text=invNum
        binding.btnDelete.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                val invoice=invoiceViewModel.getInvoiceByInvoiceNum(invNum)
                if(invoice!=null){
                    invoiceViewModel.deleteInvoice(invoice)
                }

            }


        }
        invoiceViewModel.addInvoiceState.observe(viewLifecycleOwner){state->
            when(state){
                is InvoiceState.Success->{
                    dismiss()
                    requireParentFragment().findNavController().navigate(R.id.action_final_invoice_To_Home_fragment)
                    invoiceViewModel.resetState()
                    Toast.makeText(context,"Invoice deleted successfully", Toast.LENGTH_SHORT).show()
                }
                is InvoiceState.Error->{
                    Toast.makeText(context,state.msg,Toast.LENGTH_SHORT).show()
                    invoiceViewModel.resetState()
                }
                else->{}
            }

        }

    }
}