package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentQuickSearchBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class QuickSearchFragment : Fragment(R.layout.fragment_quick_search) {

    private var _binding: FragmentQuickSearchBinding? = null
    val binding get() = _binding!!

    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()
    val itemViewModel: ItemViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuickSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etSearchAnything.addTextChangedListener { text ->
            val query = text.toString()
            invoiceViewModel.setSearchQuery(query)
            clientViewModel.setSearchQuery(query)
            itemViewModel.setSearchQuery(query)
        }

        // Live Counts Observers
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    invoiceViewModel.getInvoicesByWorkspaceId(activeWorkspaceId).collect { invoices ->
                        val totalCount = invoices.size
                        val unpaidCount = invoices.count { it.status != "Paid" && it.dueDate.isBefore(LocalDate.now()) }
                        binding.tvAllInvoicesSubtitle.text = "$totalCount total"
                        binding.tvUnpaidInvoicesSubtitle.text = "$unpaidCount to collect"
                    }
                }
                launch {
                    clientViewModel.allClients.collect { clients ->
                        binding.tvAllClientsSubtitle.text = "${clients.size} active"
                    }
                }
                launch {
                    itemViewModel.allItems.collect { items ->
                        binding.tvAllItemsSubtitle.text = "${items.size} in catalogue"
                    }
                }
            }
        }

        // Shortcut Row Click Listeners
        binding.rowAllInvoices.setOnClickListener {
            findNavController().navigate(R.id.action_quickSearchFragment_to_invoicesFragment)
        }

        binding.rowAllClients.setOnClickListener {
            findNavController().navigate(R.id.action_quickSearchFragment_to_clientsFragment)
        }

        binding.rowAllItems.setOnClickListener {
            findNavController().navigate(R.id.action_quickSearchFragment_to_itemsFragment)
        }

        binding.rowUnpaidInvoices.setOnClickListener {
            findNavController().navigate(R.id.action_quickSearchFragment_to_invoicesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}