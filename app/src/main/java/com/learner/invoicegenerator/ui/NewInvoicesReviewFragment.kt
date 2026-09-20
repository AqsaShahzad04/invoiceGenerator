package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceReviewBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceItemsReviewAdapter
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

class NewInvoicesReviewFragment: Fragment(R.layout.fragment_newinvoice_review) {

    private var _binding : FragmentNewinvoiceReviewBinding?=null
    val binding get()=_binding!!
    val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    var subTotal=0.0
    var tax=0.0
    var discount=0.0
    var currentDiscountRate:Double?=null
    var currentDiscountFlat:Double?=null
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    var itemsCounter=0
    var total=0.0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewinvoiceReviewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        binding.itemDetailsRow.layoutManager= LinearLayoutManager(context)
        viewLifecycleOwner.lifecycleScope.launch{
            val myCurrentWorkspaceName=workspaceViewModel.getWorkspaceById(activeWorkspaceId)?.name
            binding.myWorkspaceName.text=myCurrentWorkspaceName?:"My space"
        }
        binding.clientsusinessName.text=clientViewModel.selectedClient.value?.businessName?:"Client"
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                invoiceViewModel.selectedItems.collect { itemsList->
                    val adapter= InvoiceItemsReviewAdapter(itemsList)
                     binding.itemDetailsRow.adapter=adapter
                    itemsList.forEach { item->
                        subTotal+=item.unitPrice
                        itemsCounter+=(1*item.itemQuantity.toInt())

                    }
                    binding.subtotalValue.text= String.format("%.2f", subTotal)
                    binding.numOfItemsHeading.text=itemsCounter.toString()+"ITEMS"
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                invoiceViewModel.invoiceDraft.collect {draft->
                   if(draft!=null){
                       if(draft.taxPercentage!=0.0){
                           calculateTaxAndDisplay(draft.taxPercentage)
                       }
                       else{
                           calculateTaxAndDisplay(draft.taxPercentage)
                       }
                       if(draft.discountType=="Flat"){
                           calculateDiscountAndDisplay(null,draft.discountValue)
                       }
                       else if(draft.discountType=="Percent"){
                           calculateDiscountAndDisplay(draft.discountValue,null)
                       }
                       else{
                           calculateDiscountAndDisplay(null,null)
                       }
                       binding.dateRange.text="${draft.issueDate.format(formatter)} -> ${draft.dueDate.format(formatter)}"

                   }
                }
            }

        }




    }
    private fun calculateDiscountAndDisplay(discountRate: Double?, discountInFlat: Double?) {
        currentDiscountRate = discountRate
        currentDiscountFlat = discountInFlat

        discount = when {
            discountInFlat != null -> discountInFlat
            discountRate != null -> subTotal * (discountRate / 100)
            else -> 0.0
        }
        binding.discountLabel.text = if (discountInFlat != null) "Discount" else "Discount($discountRate%)"

        val show = discount > 0.0
        binding.discountLabel.visibility = if (show) View.VISIBLE else View.GONE
        binding.discountValue.visibility = if (show) View.VISIBLE else View.GONE
        binding.discountValue.text = String.format("%.2f", discount)
        updateTotal()
    }
    private fun calculateTaxAndDisplay(taxRate: Double) {
        tax = taxRate
        tax = subTotal * (taxRate / 100)
        binding.taxLabel.text = "tax($taxRate%)"
        binding.taxValue.text = String.format("%.2f", tax)
        updateTotal()
    }
    private fun updateTotal() {
        total = (subTotal - discount) + tax
        binding.totalValue.text = String.format("%.2f", total)
    }
}