package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.core.widget.addTextChangedListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import androidx.camera.core.CameraX
import com.learner.invoicegenerator.R
import android.graphics.Color
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceSelectItemBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceItemsAdpater
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.util.conversions.dpToPx
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.launch

class NewInvoiceSelectItemsFragment: Fragment(R.layout.fragment_newinvoice_select_item) {
    private var _binding: FragmentNewinvoiceSelectItemBinding?=null
    val binding get()=_binding!!
    val itemViewModel: ItemViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()

    lateinit var adapter: InvoiceItemsAdpater
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewinvoiceSelectItemBinding.inflate(inflater,container,false)
        return binding.root
    }
    fun observeScannedItem(){
        viewLifecycleOwner.lifecycleScope.launch {
            itemViewModel.scannedLookUpState.collect { state->
                when(state){
                    is ItemViewModel.ScannedLookUpState.Found -> {

                        itemViewModel.resetLookUpState()   // consume kar liya, wapas Idle
                    }
                    is ItemViewModel.ScannedLookUpState.NotFound -> {
                        Toast.makeText(requireContext(), "Product not found. Please enter details manually.", Toast.LENGTH_SHORT).show()
                        itemViewModel.resetLookUpState()
                    }
                    ItemViewModel.ScannedLookUpState.Idle -> {  }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.itemsInInvoiceRV.layoutManager= LinearLayoutManager(context)

            val sessionManager= SessionManager.getInstance(requireContext())
            val currencyCode=sessionManager.getCurrencyCode()
            val currencyobj= CurrencyData.currencies.find{
                it.code==currencyCode
            }
         binding.scanBarcodeBtn.setOnClickListener {
             val barcodeSheet= BottomSheetScanBarcode()
             barcodeSheet.arguments= Bundle().apply{
                 putString("mode","invoice")
             }
             barcodeSheet.show(parentFragmentManager,"barcode sheet")
         }

        binding.searchField.addTextChangedListener{text->
            itemViewModel.setSearchQuery(text.toString())
        }
            val currency=currencyobj?.symbol
            viewLifecycleOwner.lifecycleScope.launch{
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                    invoiceViewModel.selectedItems.collect { itemsList->
                        adapter= InvoiceItemsAdpater(itemsList,currency?:"USD",
                            onIncrement={id->invoiceViewModel.incrementQuantity(id)},
                            ondecrement={id->invoiceViewModel.decrementQuantity(id)}
                        )
                        binding.itemsInInvoiceRV.adapter=adapter
                        if(itemsList.isNotEmpty()){
                            var subtotal=0
                            itemsList.forEach { item->
                                subtotal+= (item.unitPrice*item.itemQuantity).toInt()
                            }
                            binding.subtotalAmount.text=subtotal.toString()
                            binding.subtotalSection.visibility=View.VISIBLE

                        }
                        else{
                            binding.subtotalSection.visibility=View.GONE
                            binding.subtotalSection.visibility=View.GONE

                        }
                    }
                }
            }

        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                itemViewModel.allItems.collect {itemsList->
                    binding.catalogueChipGroup.removeAllViews()
                    val styledContext= ContextThemeWrapper(requireContext(),
                        R.style.ThemeOverlay_Catalogue_Chip)
                    itemsList.forEach { item->
                        val chip= Chip(styledContext)
                        chip.text=item.itemName
                        chip.isCheckable=true
                        chip.tag=item.id

                        val alreadySelected=invoiceViewModel.selectedItems.value.any{it.itemId==item.id}
                        val isNewlyAdded=item.id==itemViewModel.lastAddedItemId.value

                        if(isNewlyAdded && !alreadySelected){
                            val itemLine = InvoiceItemLine(
                                invoiceId = 0,
                                itemId = item.id,
                                itemName = item.itemName,
                                unitPrice = item.price,
                                itemQuantity = 1.0,
                                itemUnit = item.unit
                            )
                            invoiceViewModel.addToSelectedItems(itemLine)
                        }
                        chip.isChecked=alreadySelected||isNewlyAdded


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
                    itemViewModel.clearLastAddedItemId()
                    val newItemChip = TextView(styledContext).apply {
                        text = "New Item"
                        setTextColor(Color.parseColor("#0C861A"))
                        textSize = 11f
                        typeface = ResourcesCompat.getFont(context, R.font.inter_semibold)

                        background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.bg_dashed_chip
                        )

                        val icon = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_plus
                        )?.mutate()?.apply {
                            setTint(Color.parseColor("#5C625E"))
                            setBounds(
                                0,
                                0,
                                11.dpToPx(context),
                                11.dpToPx(context)
                            )
                        }

                        setCompoundDrawablesRelative(icon, null, null, null)
                        compoundDrawablePadding = 5.dpToPx(context)

                        minHeight = 32.dpToPx(context)

                        setPadding(
                            11.dpToPx(context),
                            0,
                            7.dpToPx(context),
                            0
                        )

                        gravity = Gravity.CENTER
                        translationY = 6.dpToPx(context).toFloat()

                        isClickable = true
                        isFocusable = true

                        setOnClickListener {
                            BottomSheetNewInvoiceAddItem().show(parentFragmentManager,"newInvoiceAddItems")
                        }
                    }
                    binding.catalogueChipGroup.addView(newItemChip)
                    binding.noResultsTextView.visibility=if(itemsList.isEmpty()) View.VISIBLE else View.GONE
                }
            }



            }



        }


    }
