package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentNewInvoiceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.launch

class NewInvoiceFragment: Fragment(R.layout.fragment_new_invoice) {

    private var _binding: FragmentNewInvoiceBinding?=null
    val binding get()=_binding!!
    val clientViewModel: ClientViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels ()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewInvoiceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clientViewModel.resetselectedClients()
        invoiceViewModel.resetSelectedItems()

        val navHostFragment=childFragmentManager.findFragmentById(R.id.newInvoiceNavHost) as NavHostFragment
        val invoiceNavController=navHostFragment.findNavController()

        val stepOrder=listOf(R.id.selectClientFragment,
            R.id.selectItemsFragment,
            R.id.addDetailsFragment,
            R.id.reviewInvoiceFragment)

        var currentIndex=0
        val indicatorViews=listOf(binding.clientIdicator,binding.itemsIndicator,binding.detailsIndicator,binding.reviewIdicator)
        invoiceNavController.addOnDestinationChangedListener { _, destination, _ ->
             currentIndex = stepOrder.indexOf(destination.id)
            binding.iconBackInvoice.setOnClickListener {
                if(currentIndex==0){
                    findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)
                }
                else{
                    invoiceNavController.popBackStack()
                }
            }


            val nextStepActions=listOf(
                R.id.action_select_client_fragment_to_select_items_fragment,
                R.id.action_select_items_fragment_to_add_details_fragment,
                R.id.action_add_details_fragment_to_review_fragment
            )
            binding.continueBtn.setOnClickListener {
                if(currentIndex==stepOrder.lastIndex){
                    findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)
                }
                else{
                    invoiceNavController.navigate(nextStepActions[currentIndex])
                }
            }

            if(currentIndex==0){
                viewLifecycleOwner.lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        clientViewModel.selectedClient.collect { client ->
                            binding.continueBtn.isEnabled = client != null
                            if(binding.continueBtn.isEnabled){
                                binding.continueBtn.background.setTint(
                                    ContextCompat.getColor(requireContext(), R.color.btn_bg_dark)
                                )
                                binding.continueBtn.setTextColor(
                                    ContextCompat.getColor(requireContext(),R.color.bg_cream)
                                )
                            }
                            else{
                                binding.continueBtn.background.setTint(
                                    ContextCompat.getColor(requireContext(), R.color.greyish_white)
                                )
                                binding.continueBtn.setTextColor(
                                    ContextCompat.getColor(requireContext(),R.color.grey)
                                )
                            }


                        }
                    }
                }
            }
            else if(currentIndex==1){
                viewLifecycleOwner.lifecycleScope.launch {
                    viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                        invoiceViewModel.selectedItems.collect {itemsList->
                            if(itemsList.isNotEmpty()){
                                binding.continueBtn.isEnabled=true
                                binding.continueBtn.backgroundTintList=
                                    context?.let { ContextCompat.getColorStateList(it,R.color.btn_bg_dark) }
                                binding.continueBtn.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
                            }
                            else{
                                binding.continueBtn.isEnabled=false
                                binding.continueBtn.backgroundTintList=
                                    context?.let { ContextCompat.getColorStateList(it,R.color.greyish_white) }
                                binding.continueBtn.setTextColor(android.graphics.Color.parseColor("#9CA3A0"))
                            }

                        }
                    }
                }
            }
            else{
                binding.continueBtn.isEnabled=false
                binding.continueBtn.backgroundTintList=
                    context?.let { ContextCompat.getColorStateList(it,R.color.greyish_white) }
                binding.continueBtn.setTextColor(android.graphics.Color.parseColor("#9CA3A0"))
            }


            indicatorViews.forEachIndexed { index, indicatorView ->
                if (index <= currentIndex) {
                    indicatorView.setBackgroundResource(R.drawable.bg_invoice_step_indicators_selected)
                } else {
                    indicatorView.setBackgroundResource(R.drawable.bg_invoice_step_indicators_unselected)
                }
            }
        }

        binding.cancelBtnInvoice.setOnClickListener {
            findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)

        }



    }
}