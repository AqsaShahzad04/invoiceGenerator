package com.learner.invoicegenerator.ui

import android.content.Context
import android.content.Intent
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
import com.learner.invoicegenerator.databinding.FragmentFinalInvoiceBinding
import com.learner.invoicegenerator.ui.adaptor.ItemsFinalInvoiceAdapter
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.launch
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

class FragmentFinalInvoice: Fragment(R.layout.fragment_final_invoice) {

    private var _binding: FragmentFinalInvoiceBinding?=null
    val binding get()=_binding!!
    val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()
    var subTotal=0.0
    var tax=0.0
    var discount=0.0
    var currentDiscountRate:Double?=null
    var currentDiscountFlat:Double?=null
    var total=0.0
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    var adapter: ItemsFinalInvoiceAdapter?=null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentFinalInvoiceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()
        val currencyCode = sessionManager.getCurrencyCode()
        binding.btnShare.isEnabled=false
        val currencySymbol = CurrencyData.currencies.find { it.code == currencyCode }?.symbol ?: "$"
        binding.finalInvoiceItemsRV.layoutManager = LinearLayoutManager(context)


        viewLifecycleOwner.lifecycleScope.launch {
            val activeWorkspace = workspaceViewModel.getWorkspaceById(activeWorkspaceId)
            if (activeWorkspace != null) {
                binding.myWorkspaceName.text = activeWorkspace.name
                binding.tvNtn.text = activeWorkspace.taxNumber
            }
        }



                    val itemsList=invoiceViewModel.selectedItems.value
                    adapter = ItemsFinalInvoiceAdapter(itemsList, currencySymbol)
                    binding.finalInvoiceItemsRV.adapter = adapter
                    subTotal = 0.0
                    itemsList.forEach { item ->
                        subTotal += item.unitPrice*item.itemQuantity
                    }
                    binding.subtotalValue.text = String.format("%.2f", subTotal)


                   val draft=invoiceViewModel.invoiceDraft.value
                    if (draft != null) {
                        binding.tvInvoiceNumber.text = draft.invoiceNum
                        binding.tvCurrentDate.text = draft.issueDate.format(formatter)
                        binding.tvDueDate.text = draft.dueDate.format(formatter)
                        binding.endNote.text = draft.endNote
                        if (draft.taxPercentage != 0.0) {
                            calculateTaxAndDisplay(draft.taxPercentage)
                        } else {
                            calculateTaxAndDisplay(draft.taxPercentage)
                        }
                        if (draft.discountType == "Flat") {
                            calculateDiscountAndDisplay(null, draft.discountValue)
                        } else if (draft.discountType == "Percent") {
                            calculateDiscountAndDisplay(draft.discountValue, null)
                        } else {
                            calculateDiscountAndDisplay(null, null)
                        }
                        lateinit var file:File
                        viewLifecycleOwner.lifecycleScope.launch {
                            binding.tvClientBusinessName.text =
                                clientViewModel.getClientById(draft.clientId,activeWorkspaceId)?.businessName

                            binding.invoiceParentCard.post {
                                file = createPdf(binding.invoiceParentCard, requireContext())
                                val finalInvoice = draft.copy(pdfPath = file.absolutePath)
                                invoiceViewModel.insertInvoice(finalInvoice)
                                val invoiceId=observeState()
                                invoiceViewModel.selectedItems.value.let{itemsList->
                                    itemsList.forEach { item->
                                        item.invoiceId=invoiceId
                                        invoiceViewModel.insertInvoiceItemLine(item)
                                    }
                                    Toast.makeText(context,"invoice and items are added  successfully", Toast.LENGTH_SHORT).show()
                                }
                                binding.btnShare.isEnabled=true
                                binding.btnShare.setOnClickListener {
                                    sharePdf(requireContext(),file)
                                }
                            }

                        }


        }



    }


    private fun observeState():Int{
        var invoiceId=0
        invoiceViewModel.addInvoiceState.observe(viewLifecycleOwner){state->
            when(state){
                is InvoiceState.Success->{
                    invoiceId=state.id
                    invoiceViewModel.resetState()

                }
                is InvoiceState.Error->{
                    Toast.makeText(requireContext(), state.msg, Toast.LENGTH_SHORT).show()
                }
                else->{}
            }
        }
        return invoiceId
    }
        private fun calculateDiscountAndDisplay(discountRate: Double?, discountInFlat: Double?) {
            currentDiscountRate = discountRate
            currentDiscountFlat = discountInFlat

            discount = when {
                discountInFlat != null -> discountInFlat
                discountRate != null -> subTotal * (discountRate / 100)
                else -> 0.0
            }
            binding.discountLabel.text =
                if (discountInFlat != null) "Discount" else "Discount($discountRate%)"

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

    private fun createPdf(view: View, context: Context): File {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(view.width, view.height, 1).create()
        val page = pdf.startPage(pageInfo)
        view.draw(page.canvas)
        pdf.finishPage(page)

        val invoicesDir = File(context.filesDir, "invoices")
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs()
        }

        val file = File(invoicesDir, "invoice_${System.currentTimeMillis()}.pdf")
        pdf.writeTo(FileOutputStream(file))
        pdf.close()

        return file
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "com.learner.invoicegenerator.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Invoice"))
    }

}