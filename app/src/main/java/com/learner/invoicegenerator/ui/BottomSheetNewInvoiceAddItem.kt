package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.learner.invoicegenerator.databinding.BottomSheetNewInvoiceAddItemBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemState
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState

class BottomSheetNewInvoiceAddItem: BottomSheetDialogFragment() {
    private var _binding: BottomSheetNewInvoiceAddItemBinding? = null
    val binding get() = _binding!!
    val itemViewModel: ItemViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetNewInvoiceAddItemBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        val sessionManager = SessionManager.getInstance(requireContext())
        val workspaceId = sessionManager.getActiveWorkspaceId()

        itemViewModel.itemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItemState.Success->{
                    itemViewModel.resetState()
                    dismiss()
                }
                is ItemState.Error->{
                    itemViewModel.resetState()
                    binding.itemNameInput.error = state.message
                }
                else->Unit
            }
        }
        binding.additemBtn.setOnClickListener {
            val itemName = binding.itemNameInput.text.toString().trim()
            val price = binding.priceInput.text.toString().toDouble()
            val selectedChipId = binding.unitChipGroup.checkedChipId
            val selectedChip = binding.unitChipGroup.findViewById<Chip>(selectedChipId)
            val selectedUnit = selectedChip.text.toString()
            if (itemName.isNullOrEmpty()) {
                binding.itemNameInput.error = "item name is required"
                return@setOnClickListener
            }
            if (selectedChip == null) {
                // show a unit-required error here
                return@setOnClickListener
            }
            val item = Item(
                itemName = itemName,
                barcode = null,
                price = price,
                unit = selectedUnit,
                category = "Grocery",
                workspaceId = workspaceId
            )
            itemViewModel.addItems(item)


        }
    }
}