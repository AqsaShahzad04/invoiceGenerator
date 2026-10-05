package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.databinding.FragmentInvoicesBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceAdapter
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.invoice.filter.BottomSheetFilterInvoices
import com.learner.invoicegenerator.util.conversions
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class InvoicesFragment: Fragment(R.layout.fragment_invoices) {
    private var _binding: FragmentInvoicesBinding? = null
    val binding get() = _binding!!
    var selectedState = invoicesState.All
    lateinit var adapter: InvoiceAdapter
    val today = LocalDate.now()
    var allInvoices: List<Invoice> = emptyList()
    var paidInvoices: List<Invoice> = emptyList()
    var unpaidInvoices: List<Invoice> = emptyList()
    var pendingInvoices: List<Invoice> = emptyList()

    var activeFromDate: LocalDate = LocalDate.now().withDayOfMonth(1)
    var activeToDate: LocalDate = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth())

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val invoiceViewModel: InvoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInvoicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val currencyCode = sessionManager.getCurrencyCode()
        val currencySymbol = CurrencyData.currencies.find { it.code == currencyCode }?.symbol ?: "$"

        drawUI(selectedState, currencySymbol)

        binding.filterAll.setOnClickListener {
            updateUI(binding.filterAll)
            selectedState = invoicesState.All
            drawUI(selectedState, currencySymbol)
        }
        binding.filterPaid.setOnClickListener {
            updateUI(binding.filterPaid)
            selectedState = invoicesState.Paid
            drawUI(selectedState, currencySymbol)
        }
        binding.filterPending.setOnClickListener {
            updateUI(binding.filterPending)
            selectedState = invoicesState.Pending
            drawUI(selectedState, currencySymbol)
        }
        binding.filterUnpaid.setOnClickListener {
            updateUI(binding.filterUnpaid)
            selectedState = invoicesState.Unpaid
            drawUI(selectedState, currencySymbol)
        }

        binding.newInvoiceBtn.setOnClickListener {
            findNavController().navigate(R.id.action_invoices_fragment_to_create_invoice_fragment)
        }
        binding.searchInvoices.addTextChangedListener { text ->
            invoiceViewModel.setSearchQuery(text.toString())
        }

        binding.thisMonthChip.setOnClickListener {
            BottomSheetFilterInvoices { result ->
                binding.thisMonthChip.text = result.label
                invoiceViewModel.setDateRange(result.fromDate, result.toDate)
            }.show(childFragmentManager, BottomSheetFilterInvoices.TAG)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    invoiceViewModel.dateRange.collect { range ->
                        activeFromDate = range.first
                        activeToDate = range.second
                        drawUI(selectedState, currencySymbol)
                    }
                }

                launch {
                    invoiceViewModel.allInvoices.collect { invoices ->
                        allInvoices = invoices
                        updateFilteredLists()
                        drawUI(selectedState, currencySymbol)
                    }
                }
            }
        }
    }

    private fun updateFilteredLists() {
        paidInvoices = allInvoices.filter { it.status == "Paid" }
        unpaidInvoices = allInvoices.filter { it.status != "Paid" && it.dueDate.isBefore(today) }
        pendingInvoices = allInvoices.filter { it.status != "Paid" && !it.dueDate.isBefore(today) }
    }

    private fun drawUI(selectedState: invoicesState, currencySymbol: String) {
        val invoicesToRender = when (selectedState) {
            invoicesState.All -> allInvoices
            invoicesState.Paid -> paidInvoices
            invoicesState.Unpaid -> unpaidInvoices
            invoicesState.Pending -> pendingInvoices
        }
        renderInvoices(invoicesToRender, currencySymbol)
    }

    private fun renderInvoices(invoices: List<Invoice>, currencySymbol: String) {
        binding.filterAll.text = "All ${allInvoices.size}"
        binding.filterPaid.text = "Paid ${paidInvoices.size}"
        binding.filterUnpaid.text = "Unpaid ${unpaidInvoices.size}"
        binding.filterPending.text = "Pending ${pendingInvoices.size}"

        val formattedFrom = activeFromDate.format(dateFormatter)
        val formattedTo = activeToDate.format(dateFormatter)
        binding.invoiceSummaryText.text = "${invoices.size} invoices ($formattedFrom - $formattedTo)"

        if (invoices.isNotEmpty()) {
            adapter = InvoiceAdapter(invoices) { selectedInvoice ->
                openInvoiceDetails(selectedInvoice)
            }
            val total = invoices.sumOf { it.totalAmount }
            binding.emptyStateLayout.visibility = View.GONE
            binding.invoicesRv.visibility = View.VISIBLE
            binding.invoiceSummaryTotal.text = "$currencySymbol ${conversions.formatAmount(total)}"
            binding.invoicesRv.adapter = adapter
            binding.invoicesRv.layoutManager = LinearLayoutManager(requireContext())
        } else {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.newInvoiceBtn2.setOnClickListener {
                findNavController().navigate(R.id.action_invoices_fragment_to_create_invoice_fragment)
            }
            binding.invoicesRv.visibility = View.GONE
            binding.invoiceSummaryTotal.text = "$currencySymbol 0"
        }
    }

    private fun updateUI(view: TextView) {
        listOf(
            binding.filterPaid,
            binding.filterPending,
            binding.filterUnpaid,
            binding.filterAll
        ).forEach { element ->
            element.background = null
            element.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_grey))
        }
        view.setBackgroundResource(R.drawable.bg_segment_active)
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
    }

    private fun openInvoiceDetails(invoice: Invoice) {
        invoiceViewModel.updateInvoiceDraft(invoice)
        invoiceViewModel.setSelectedTemplate(invoice.templateId)
        viewLifecycleOwner.lifecycleScope.launch {
            val items = invoiceViewModel.getItemsbyInvoiceId(invoice.id).first()
            invoiceViewModel.updateSelectedItems(items.toMutableList())
            findNavController().navigate(R.id.action_invoices_fragment_to_fragmentFinalInvoice)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!requireActivity().isChangingConfigurations) {
            invoiceViewModel.resetDateRangeToDefault()
            invoiceViewModel.setSearchQuery("")
        }
        _binding = null
    }
}
