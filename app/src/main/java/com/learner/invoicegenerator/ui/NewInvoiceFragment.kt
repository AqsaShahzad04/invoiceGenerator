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
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentNewInvoiceBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.launch

class NewInvoiceFragment: Fragment(R.layout.fragment_new_invoice) {

    private var _binding: FragmentNewInvoiceBinding?=null
    val binding get()=_binding!!
    val clientViewModel: ClientViewModel by activityViewModels()
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

        binding.iconBackInvoice.setOnClickListener {
            findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)
        }
        binding.cancelBtnInvoice.setOnClickListener {
            findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)

        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                clientViewModel.selectedClient.collect { client ->
                    binding.continueBtn.isEnabled = client != null
                    binding.continueBtn.setBackgroundColor(
                        ContextCompat.getColor(requireContext(), R.color.btn_bg_dark)
                    )
                    binding.continueBtn.setTextColor(
                        ContextCompat.getColor(requireContext(),R.color.bg_cream)
                    )

                }
            }
        }
        binding.continueBtn.setOnClickListener {
            findNavController().navigate(R.id.action_invoice_screen_to_home_fragment)
        }

    }
}