package com.learner.invoicegenerator.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.databinding.FragmentFinalInvoiceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.template.InvoicePdfGenerator
import com.learner.invoicegenerator.ui.template.InvoiceRenderData
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FragmentFinalInvoice : Fragment(R.layout.fragment_final_invoice) {

    private var _binding: FragmentFinalInvoiceBinding? = null
    val binding get() = _binding!!

    val workspaceViewModel: WorkspaceViewModel by activityViewModels()
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinalInvoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        setButtonState(binding.btnShare, false)
        setButtonState(binding.btnDownloadPdf, false)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        val draft = invoiceViewModel.invoiceDraft.value
        val itemsList = invoiceViewModel.selectedItems.value
        val chosenTemplateId = if (draft != null && draft.id > 0) {
            draft.templateId
        } else {
            invoiceViewModel.selectedTemplate.value
        }
        var file: File? = null

        val invoiceCurrencyCode = draft?.currencyCode ?: sessionManager.getCurrencyCode() ?: "USD"
        val currencySymbol = CurrencyData.currencies.find { it.code == invoiceCurrencyCode }?.symbol
            ?: (if (invoiceCurrencyCode.length <= 3 && !invoiceCurrencyCode.contains("-")) invoiceCurrencyCode else "$")

        if (draft != null) {
            updatePaymentStatusUI(draft.status)

            // Compute accurate grand total (subtotal - discount + tax) up-front
            val subtotal = itemsList.sumOf { it.unitPrice * it.itemQuantity }
            val taxAmount = if (draft.taxPercentage > 0) subtotal * (draft.taxPercentage / 100) else 0.0
            val discAmount = if (draft.discountValue > 0) {
                if (draft.discountType == "Percent") subtotal * (draft.discountValue / 100) else draft.discountValue
            } else 0.0
            val calculatedGrandTotal = (subtotal - discAmount) + taxAmount
            val updatedDraft = draft.copy(totalAmount = calculatedGrandTotal, templateId = chosenTemplateId)

            // Render on canvas immediately to eliminate blank screen loading delay
            val initialRenderData = InvoiceRenderData(
                invoice = updatedDraft,
                items = itemsList,
                workspace = null,
                client = null,
                logoBitmap = null,
                signatureBitmap = null,
                currencySymbol = currencySymbol,
                paymentInfo = null
            )
            binding.invoiceCanvasView.templateId = chosenTemplateId
            binding.invoiceCanvasView.renderData = initialRenderData

            binding.btnMarkPaid.setOnClickListener {
                val currentDraft = invoiceViewModel.invoiceDraft.value ?: return@setOnClickListener
                val newStatus = if (currentDraft.status == "Paid") "Pending" else "Paid"
                val updatedStatusDraft = currentDraft.copy(
                    status = newStatus,
                    paidDate = if (newStatus == "Paid") java.time.LocalDate.now() else null
                )
                invoiceViewModel.updateInvoiceDraft(updatedStatusDraft)
                updatePaymentStatusUI(newStatus)

                val currentData = binding.invoiceCanvasView.renderData
                if (currentData != null) {
                    val updatedData = currentData.copy(invoice = updatedStatusDraft)
                    binding.invoiceCanvasView.renderData = updatedData

                    viewLifecycleOwner.lifecycleScope.launch {
                        val updatedPdf = InvoicePdfGenerator.generatePdf(requireContext(), updatedData, chosenTemplateId)
                        file = updatedPdf
                        val updatedInvoiceWithPdf = updatedStatusDraft.copy(pdfPath = updatedPdf.absolutePath)
                        if (updatedInvoiceWithPdf.id > 0) {
                            invoiceViewModel.updateInvoice(updatedInvoiceWithPdf)
                        }
                    }
                }
            }

            binding.btnDelete.setOnClickListener {
                val bottomSheet = BottomSheetDeleteInvoice.newInstance(draft.invoiceNum)
                bottomSheet.show(parentFragmentManager, "deleteInvoiceBottomSheet")
            }

            // Fetch workspace, client, settings, logo, and signature concurrently on IO thread
            viewLifecycleOwner.lifecycleScope.launch {
                val activeWorkspaceDeferred = async(Dispatchers.IO) { workspaceViewModel.getWorkspaceById(activeWorkspaceId) }
                val clientDeferred = async(Dispatchers.IO) { clientViewModel.getClientById(draft.clientId, activeWorkspaceId) }
                val settingsDeferred = async(Dispatchers.IO) { settingsViewModel.getSettingsByWorkspaceId(activeWorkspaceId).first() }

                val activeWorkspace = activeWorkspaceDeferred.await()
                val client = clientDeferred.await()
                val settings = settingsDeferred.await()
                val paymentMethodsStr = settings?.paymentMethods?.joinToString(", ") { it.name }

                val logoDeferred = async(Dispatchers.IO) {
                    val uriStr = activeWorkspace?.logoUri
                    if (!uriStr.isNullOrEmpty()) {
                        try {
                            val uri = Uri.parse(uriStr)
                            requireContext().contentResolver.openInputStream(uri)?.use { stream ->
                                val options = BitmapFactory.Options().apply {
                                    inPreferredConfig = Bitmap.Config.ARGB_8888
                                    inScaled = false
                                }
                                BitmapFactory.decodeStream(stream, null, options)
                            }
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                val sigDeferred = async(Dispatchers.IO) {
                    if (!draft.signaturePath.isNullOrEmpty()) {
                        try {
                            val options = BitmapFactory.Options().apply {
                                inPreferredConfig = Bitmap.Config.ARGB_8888
                                inScaled = false
                            }
                            BitmapFactory.decodeFile(draft.signaturePath, options)
                        } catch (e: Exception) {
                            null
                        }
                    } else null
                }

                val logoBitmap = logoDeferred.await()
                val sigBitmap = sigDeferred.await()

                val renderData = InvoiceRenderData(
                    invoice = updatedDraft,
                    items = itemsList,
                    workspace = activeWorkspace,
                    client = client,
                    logoBitmap = logoBitmap,
                    signatureBitmap = sigBitmap,
                    currencySymbol = currencySymbol,
                    paymentInfo = paymentMethodsStr
                )

                // Update canvas with complete information
                binding.invoiceCanvasView.templateId = chosenTemplateId
                binding.invoiceCanvasView.renderData = renderData

                // Check if PDF file already exists on disk
                val existingPdfPath = draft.pdfPath
                val existingFile = if (!existingPdfPath.isNullOrEmpty()) File(existingPdfPath) else null

                if (existingFile != null && existingFile.exists()) {
                    file = existingFile
                } else {
                    // Generate fresh high-res PDF file if it doesn't exist on disk yet
                    val pdfFile = InvoicePdfGenerator.generatePdf(requireContext(), renderData, chosenTemplateId)
                    file = pdfFile

                    val finalInvoice = updatedDraft.copy(pdfPath = pdfFile.absolutePath)
                    if (draft.id > 0) {
                        invoiceViewModel.updateInvoice(finalInvoice)
                    } else {
                        val invoiceId = invoiceViewModel.insertInvoiceWithItems(finalInvoice, itemsList)
                        val savedInvoice = finalInvoice.copy(id = invoiceId.toInt())
                        invoiceViewModel.updateInvoiceDraft(savedInvoice)
                    }
                }

                // Enable Share & Download Buttons
                setButtonState(binding.btnShare, true)
                binding.btnShare.setOnClickListener {
                    file?.let { sharePdf(requireContext(), it) }
                }

                setButtonState(binding.btnDownloadPdf, true)
                binding.btnDownloadPdf.setOnClickListener {
                    val currentFile = file ?: return@setOnClickListener
                    setButtonState(binding.btnDownloadPdf, false)
                    viewLifecycleOwner.lifecycleScope.launch {
                        val uri = withContext(Dispatchers.IO) {
                            saveInvoicePdfToDownloads(requireContext(), currentFile, currentFile.name)
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

    private fun setButtonState(button: View, enabled: Boolean) {
        button.isEnabled = enabled
        button.alpha = if (enabled) 1.0f else 0.5f
    }

    private fun updatePaymentStatusUI(status: String) {
        if (status == "Paid") {
            binding.paymentStatusText.text = "Unmark"
            binding.icPaymentStatus.setImageResource(R.drawable.ic_unpaid)
            binding.btnMarkPaid.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.bg_cream))
            binding.paymentStatusText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.btn_bg_dark)
            )
        } else {
            binding.paymentStatusText.text = "Mark paid"
            binding.icPaymentStatus.setImageResource(R.drawable.ic_check)
            binding.btnMarkPaid.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.signal_green))
            binding.paymentStatusText.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.bg_cream)
            )
        }
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

    private fun saveInvoicePdfToDownloads(
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
