package com.learner.invoicegenerator.ui

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.getSystemService
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter
import android.content.ContentValues
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import java.io.FileInputStream
import java.io.IOException

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

    private var originalPaymentStatus: String = ""
    private var currentPaymentStatus: String = ""
    private var currentInvoiceId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentFinalInvoiceBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()
        val currencyCode = sessionManager.getCurrencyCode()
        setButtonState(binding.btnShare, false)
        setButtonState(binding.btnDownloadPdf, false)
        val currencySymbol = CurrencyData.currencies.find { it.code == currencyCode }?.symbol ?: "$"
        binding.finalInvoiceItemsRV.layoutManager = LinearLayoutManager(context)

        binding.btnBack.setOnClickListener {
            commitPaymentStatusIfChanged()
            findNavController().navigateUp()
        }



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
            val signaturePath=draft.signaturePath
            if(!signaturePath.isNullOrEmpty()){
                val bitmap= BitmapFactory.decodeFile(signaturePath)
                if(bitmap!=null){
                    binding.signatureBox.setImageBitmap(bitmap)
                    binding.signatureBox.visibility=View.VISIBLE
                }
                else {
                    binding.signatureBox.visibility = View.GONE
                }
            }
            else{
                binding.signatureBox.visibility=View.GONE
            }

            originalPaymentStatus = draft.status
            currentPaymentStatus = draft.status
            updatePaymentStatusUI(currentPaymentStatus)

            binding.btnMarkPaid.setOnClickListener {
                currentPaymentStatus = if (currentPaymentStatus == "Paid") "Unpaid" else "Paid"
                updatePaymentStatusUI(currentPaymentStatus)
            }
            binding.btnDelete.setOnClickListener {
                val bottomSheet= BottomSheetDeleteInvoice.newInstance(draft.invoiceNum)
                bottomSheet.show(parentFragmentManager,"deleteInvoiceBottomSheet")
            }


            lateinit var file:File
            viewLifecycleOwner.lifecycleScope.launch {
                binding.tvClientBusinessName.text =
                    clientViewModel.getClientById(draft.clientId,activeWorkspaceId)?.businessName
                val workspaceUri=workspaceViewModel.getWorkspaceById(activeWorkspaceId)?.logoUri
                displayLogo(workspaceUri)
                binding.invoiceParentCard.post {
                    file = createPdf(binding.invoiceParentCard, requireContext())
                    val finalInvoice = draft.copy(pdfPath = file.absolutePath)
                    invoiceViewModel.insertInvoice(finalInvoice)
                    val invoiceId=observeState()
                    currentInvoiceId=finalInvoice.invoiceNum
                    invoiceViewModel.selectedItems.value.let{itemsList->
                        itemsList.forEach { item->
                            item.invoiceId=invoiceId
                            invoiceViewModel.insertInvoiceItemLine(item)
                        }
                    }

                    setButtonState(binding.btnShare, true)
                    binding.btnShare.setOnClickListener {
                        sharePdf(requireContext(),file)
                    }

                    setButtonState(binding.btnDownloadPdf, true)
                    binding.btnDownloadPdf.setOnClickListener {
                        setButtonState(binding.btnDownloadPdf, false)
                        viewLifecycleOwner.lifecycleScope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                saveInvoicePdfToDownloads(requireContext(), file, file.name)
                            }
                            setButtonState(binding.btnDownloadPdf, true)

                            if (uri != null) {
                                Toast.makeText(context, "Invoice downloaded to Downloads folder", Toast.LENGTH_SHORT).show()
                                openPdf(requireContext(), uri)
                            } else {
                                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

            }


        }



    }

    override fun onDestroyView() {
        commitPaymentStatusIfChanged()
        super.onDestroyView()
        _binding = null
    }

    private fun commitPaymentStatusIfChanged() {
        if (currentPaymentStatus != originalPaymentStatus && currentInvoiceId != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val invoice = invoiceViewModel.getInvoiceByInvoiceNum(currentInvoiceId ?: "")
                invoice?.let {
                    invoiceViewModel.updateInvoice(it.copy(status = currentPaymentStatus))
                }
            }
            originalPaymentStatus = currentPaymentStatus
        }
    }

    private fun updatePaymentStatusUI(paymentStatus:String){
        if(paymentStatus=="Paid"){
            binding.paymentCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.primary_green))
            binding.tvStatus.text="Paid"
            binding.paymentStatus.text="Unmark"
            binding.icPaymentStatus.setBackgroundResource(R.drawable.ic_unpaid)
            binding.btnMarkPaid.backgroundTintList=
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.bg_cream))
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.primary_green))
            binding.paymentStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.btn_bg_dark))
        }
        else{
            binding.paymentCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.carrot_red_shade))
            binding.tvStatus.text="Unpaid"
            binding.paymentStatus.text="Mark paid"
            binding.icPaymentStatus.setBackgroundResource(R.drawable.ic_check)
            binding.btnMarkPaid.backgroundTintList=
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.signal_green))
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.carrot_red_shade))
            binding.paymentStatus.setTextColor(ContextCompat.getColor(requireContext(),R.color.bg_cream))
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

    private fun setButtonState(button: View, enabled: Boolean) {
        button.isEnabled = enabled
        button.alpha = if (enabled) 1.0f else 0.5f
    }
    private fun displayLogo(uriString: String?) {
        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)
            binding.ivWorkspaceLogo.setImageURI(uri)
            binding.ivWorkspaceLogo.visibility = View.VISIBLE
            binding.placeholderIconLogo.visibility = View.GONE

        } else {
            binding.ivWorkspaceLogo.visibility = View.GONE
            binding.placeholderIconLogo.visibility = View.VISIBLE
        }
    }

    private fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveInvoicePdfToDownloads(
        context: Context,
        sourceFile: File,
        displayName: String = "Invoice_${System.currentTimeMillis()}.pdf"
    ): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver

                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, displayName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val itemUri = resolver.insert(collection, contentValues) ?: return null

                resolver.openOutputStream(itemUri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: return null

                contentValues.clear()
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(itemUri, contentValues, null, null)

                itemUri
            } else {
                // API < 29 fallback — needs WRITE_EXTERNAL_STORAGE permission granted beforehand
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val destFile = File(downloadsDir, displayName)
                FileOutputStream(destFile).use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                FileProvider.getUriForFile(
                    context,
                    "com.learner.invoicegenerator.fileprovider",
                    destFile
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}