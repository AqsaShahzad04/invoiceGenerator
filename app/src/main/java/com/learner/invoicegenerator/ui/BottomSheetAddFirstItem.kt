package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.databinding.BottomSheetAddFirstItemBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemState
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel

class BottomSheetAddFirstItem: BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddFirstItemBinding? = null
    val binding get() = _binding!!
    private val viewModel: ItemViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=BottomSheetAddFirstItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view:View,SavedInstanceState:Bundle?){
        super.onViewCreated(view,SavedInstanceState)

        val session= SessionManager.getInstance(requireContext())
        val workspaceId=session.getActiveWorkspaceId()

        viewModel.itemState.observe(viewLifecycleOwner){state->
            when(state){
                is ItemState.Loading->{
                    binding.addItemBtn.isEnabled=false
                }
                is ItemState.Success->{
                    binding.addItemBtn.isEnabled=false
                    Toast.makeText(requireContext(),"Item added",Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is ItemState.Error->{
                    binding.addItemBtn.isEnabled=true
                    Toast.makeText(requireContext(),state.message,Toast.LENGTH_SHORT).show()
                }
                else->{}
            }

        }

        binding.addItemBtn.setOnClickListener {
           val itemName=binding.itemNameInput.text.toString()
            val price=binding.priceInput.text.toString()
            if(itemName.isEmpty()){
                binding.itemNameInput.error="Item name is required"
                return@setOnClickListener
            }
            if(price.isEmpty()){
                binding.priceInput.error="Price is required"
                return@setOnClickListener
            }
            val item= Item(
                itemName = itemName,
                barcode = null,
                price = price.toDoubleOrNull()?:0.0,
                unit = "piece",
                category = "Grocery",
                workspaceId = workspaceId
            )

            viewModel.addItems(item)

        }
        binding.closebtn.setOnClickListener {
            dismiss()
        }
    }

}