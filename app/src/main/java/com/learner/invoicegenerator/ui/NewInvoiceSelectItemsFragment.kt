package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceSelectItemBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import kotlinx.coroutines.launch

class NewInvoiceSelectItemsFragment: Fragment(R.layout.fragment_newinvoice_select_item) {
    private var _binding: FragmentNewinvoiceSelectItemBinding?=null
    val binding get()=_binding!!
    val itemViewModel: ItemViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewinvoiceSelectItemBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch{
            itemViewModel.allItems.collect {itemsList->
                itemsList.forEach { item->
                    val styledContext= ContextThemeWrapper(requireContext(),
                        R.style.ThemeOverlay_Catalogue_Chip)
                    val chip= Chip(styledContext)
                    chip.text=item.itemName
                    chip.isCheckable=true
                    chip.tag=item.id

                    chip.setOnCheckedChangeListener { _, isChecked ->
                        if(isChecked){
                            val itemLine= InvoiceItemLine(
                                invoiceId = 0,
                                itemId = item.id,
                                itemName = item.itemName,
                                unitPrice = item.price,
                                itemQuantity = 1.0,
                                itemUnit = item.unit
                            )
                        invoiceViewModel.addToSelectedItems(itemLine)
                        }
                        else{
                            invoiceViewModel.removeFromSelectedItems(item.id)
                        }
                    }

                    binding.catalogueChipGroup.addView(chip)
                }
            }
        }


    }
}