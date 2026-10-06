package com.learner.invoicegenerator.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.utils.ThemeUtils
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceReviewBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.template.InvoiceRenderData
import kotlinx.coroutines.launch
import java.time.LocalDate

class NewInvoicesReviewFragment : Fragment(R.layout.fragment_newinvoice_review) {

    private var _binding: FragmentNewinvoiceReviewBinding? = null
    val binding get() = _binding!!

    val invoiceViewModel: InvoiceViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewinvoiceReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTemplateClickListeners()
        loadAndRenderCanvases()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                invoiceViewModel.selectedTemplate.collect { selected ->
                    updateTemplateSelectionUI(selected)
                }
            }
        }
    }

    private fun loadAndRenderCanvases() {
        val sampleWorkspace = com.learner.invoicegenerator.data.local.entity.Workspace(
            id = 1,
            name = "BUSINESS NAME HERE",
            ownerUserId = 1,
            isDefault = false,
            email = "contact@businessname.com",
            phone = "+1 (555) 123-4567",
            taxNumber = "NTN-1234567",
            address = "123 Business St, City, Country",
            logoUri = null
        )

        val sampleClient = com.learner.invoicegenerator.data.local.entity.Client(
            id = 1,
            businessName = "CLIENT NAME HERE",
            contactPerson = "John Doe",
            email = "client@example.com",
            phone = "+1 (555) 987-6543",
            address = "456 Client Ave, Suite 100, City",
            workspaceId = 1
        )

        val sampleInvoice = Invoice(
            invoiceNum = "INV-00001",
            workspaceId = 1,
            clientId = 1,
            clientBusinessName = "CLIENT NAME HERE",
            status = "Pending",
            issueDate = LocalDate.now(),
            dueDate = LocalDate.now().plusDays(14),
            paidDate = null,
            currencyCode = "$",
            taxPercentage = 10.0,
            discountType = "Percent",
            discountValue = 5.0,
            signaturePath = null,
            endNote = "Thank you for your business!",
            pdfPath = null,
            totalAmount = 1282.50
        )

        val sampleItems = listOf(
            InvoiceItemLine(1, 0, 1, "Web Design & Development", 750.0, 1.0, "hrs"),
            InvoiceItemLine(2, 0, 2, "Branding & Graphic Design", 600.0, 1.0, "pcs")
        )

        val sampleRenderData = InvoiceRenderData(
            invoice = sampleInvoice,
            items = sampleItems,
            workspace = sampleWorkspace,
            client = sampleClient,
            logoBitmap = null,
            signatureBitmap = null,
            currencySymbol = "$",
            paymentInfo = "Bank Transfer, Credit Card, Cash",
            isSample = true
        )

        binding.template1Canvas.templateId = 1
        binding.template1Canvas.renderData = sampleRenderData

        binding.template2Canvas.templateId = 2
        binding.template2Canvas.renderData = sampleRenderData

        binding.template3Canvas.templateId = 3
        binding.template3Canvas.renderData = sampleRenderData

        binding.template4Canvas.templateId = 4
        binding.template4Canvas.renderData = sampleRenderData
    }

    private fun setupTemplateClickListeners() {
        binding.template1Container.setOnClickListener {
            invoiceViewModel.setSelectedTemplate(1)
        }
        binding.template2Container.setOnClickListener {
            invoiceViewModel.setSelectedTemplate(2)
        }
        binding.template3Container.setOnClickListener {
            invoiceViewModel.setSelectedTemplate(3)
        }
        binding.template4Container.setOnClickListener {
            invoiceViewModel.setSelectedTemplate(4)
        }
    }

    private fun updateTemplateSelectionUI(selectedTemplate: Int) {
        val containers = listOf(
            binding.template1Container,
            binding.template2Container,
            binding.template3Container,
            binding.template4Container
        )

        val checkIcons = listOf(
            binding.ivTemplate1Check,
            binding.ivTemplate2Check,
            binding.ivTemplate3Check,
            binding.ivTemplate4Check
        )

        val textBadges = listOf(
            binding.tvTemplate1Badge,
            binding.tvTemplate2Badge,
            binding.tvTemplate3Badge,
            binding.tvTemplate4Badge
        )

        val activeColor = ContextCompat.getColor(requireContext(), R.color.btn_bg_dark)
        val inactiveColor = ThemeUtils.getTextColorGrey(requireContext())

        containers.forEachIndexed { index, container ->
            val isSelected = (index + 1) == selectedTemplate
            container.setBackgroundResource(
                if (isSelected) R.drawable.bg_template_selected
                else R.drawable.bg_template_unselected
            )
            checkIcons[index].visibility = if (isSelected) View.VISIBLE else View.GONE
            textBadges[index].setTextColor(if (isSelected) activeColor else inactiveColor)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}