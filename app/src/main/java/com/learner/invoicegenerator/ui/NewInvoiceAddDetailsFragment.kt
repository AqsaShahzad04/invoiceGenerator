package com.learner.invoicegenerator.ui

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Invoice
import com.learner.invoicegenerator.data.local.entity.NumberingReset
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceAddDetailsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class NewInvoiceAddDetailsFragment : Fragment(R.layout.fragment_newinvoice_add_details) {

    private var _binding: FragmentNewinvoiceAddDetailsBinding? = null
    val binding get() = _binding!!

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()
    val clientViewModel: ClientViewModel by activityViewModels()

    private var subtotal = 0.0
    private var tax = 0.0
    private var discount = 0.0
    private var total = 0.0

    private var currentTaxRate = 0.0
    private var currentDiscountRate: Double? = null
    private var currentDiscountFlat: Double? = null

    private var currentSettings: WorkspaceSettings? = null
    private var updatingEditTextManually = false

    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewinvoiceAddDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        // ---------- STEP 1: draft sirf EK BAAR banao ----------
        if (invoiceViewModel.invoiceDraft.value == null) {
            viewLifecycleOwner.lifecycleScope.launch {
                val settings = settingsViewModel.getSettingsByWorkspaceId(activeWorkspaceId).first()
                currentSettings = settings

                val clientId = clientViewModel.selectedClient.value?.id
                if (clientId == null) {
                    Toast.makeText(requireContext(), "Select client first", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val resetPeriod = settings?.numberingReset ?: NumberingReset.MONTHLY
                val number = invoiceViewModel.getInvoiceNum(activeWorkspaceId, resetPeriod)
                val prefix = settings?.invoicePrefix ?: "INV-"
                val fullInvoiceNum = prefix + String.format("%04d", number)

                val daysOffset = convertOffsetIntoLong(settings?.paymentDueDateOffset ?: PaymentDueDateOffset.NET14)

                val draft = Invoice(
                    invoiceNum = fullInvoiceNum,
                    workspaceId = activeWorkspaceId,
                    clientId = clientId,
                    status = "Unpaid",
                    issueDate = LocalDate.now(),
                    dueDate = LocalDate.now().plusDays(daysOffset),
                    currencyCode = sessionManager.getCurrencyCode() ?: "$",
                    taxPercentage = if(settings?.defaultTax==true) settings?.taxRate?:10.0 else 0.0,
                    discountType = "None",
                    discountValue = 0.0,
                    signaturePath = settings?.signatureBlock,
                    endNote = settings?.defaultNotes ?: "Thank you",
                    pdfPath = null
                )
                invoiceViewModel.updateInvoiceDraft(draft)
            }
        }

        // ---------- STEP 2: draft change ho to UI update karo ----------
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                invoiceViewModel.invoiceDraft.collect { draft ->
                    if (draft != null) renderDraft(draft)
                }
            }
        }

        // ---------- STEP 2.5 (NEW — Bug 1 fix): live settings collector, sirf tax sync ke liye ----------
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.getSettingsByWorkspaceId(activeWorkspaceId).collect { settings ->
                    currentSettings = settings
                    val newTaxRate = settings?.taxRate ?: 17.0

                    val draft = invoiceViewModel.invoiceDraft.value
                    // sirf tab update karo jab "% tax" already selected ho (No Tax nahi), warna
                    // "No Tax" wala intentional choice overwrite ho jayega
                    if (draft != null  && draft.taxPercentage != newTaxRate) {
                        updateDraft { it.copy(taxPercentage = newTaxRate) }
                    }
                }
            }
        }

        // ---------- STEP 3: items se subtotal ----------
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                invoiceViewModel.selectedItems.collect { items ->
                    subtotal = 0.0
                    items.forEach { item -> subtotal += item.unitPrice * item.itemQuantity }
                    binding.subtotalValue.text = String.format("%.2f", subtotal)
                    calculateTaxAndDisplay(currentTaxRate)
                    calculateDiscountAndDisplay(currentDiscountRate, currentDiscountFlat)
                }
            }
        }

        // ---------- STEP 4: user actions -> draft update ----------

        binding.issueDateSection.setOnClickListener {
            showCalenderDialog { selectedDate ->
                updateDraft { it.copy(issueDate = selectedDate) }
            }
        }
        binding.dueDateSection.setOnClickListener {
            showCalenderDialog { selectedDate ->
                updateDraft { it.copy(dueDate = selectedDate) }
            }
        }

        val taxRateViews = listOf(binding.noTaxChip, binding.selectedTaxChip)
        taxRateViews.forEach { it.isCheckable = false }

        binding.changeTaxChip.setOnClickListener {
            BottomSheetTaxRate(currentSettings?.taxRate ?: 17.0)
                .show(childFragmentManager, "taxRateBottomSheet")
        }
        binding.noTaxChip.setOnClickListener {
            updateDraft { it.copy(taxPercentage = 0.0) }
        }
        binding.selectedTaxChip.setOnClickListener {
            updateDraft { it.copy(taxPercentage = currentSettings?.taxRate ?: 17.0) }
        }

        val discountView = listOf(binding.discountInFlat, binding.discountInPercentage, binding.noneDiscount)

        binding.discountInPercentage.setOnClickListener {
            binding.customTaxInPercent.visibility = View.VISIBLE
            binding.customTaxInFlat.visibility = View.GONE
            updateDiscountNamesUI(binding.discountInPercentage, discountView)
            updateDraft { it.copy(discountType = "Percent") }
        }
        binding.discountInFlat.setOnClickListener {
            binding.customTaxInPercent.visibility = View.GONE
            binding.customTaxInFlat.visibility = View.VISIBLE
            updateDiscountNamesUI(binding.discountInFlat, discountView)
            updateDraft { it.copy(discountType = "Flat") }
        }
        binding.noneDiscount.setOnClickListener {
            binding.customTaxInPercent.visibility = View.GONE
            binding.customTaxInFlat.visibility = View.GONE
            highlightSelectedChip(null)
            updateDiscountNamesUI(binding.noneDiscount, discountView)
            updateDraft { it.copy(discountType = "None", discountValue = 0.0) }
        }

        binding.Chip1.setOnClickListener { setDiscountInputText("5"); highlightSelectedChip(binding.Chip1)
            updateDraft { it.copy(discountType = "Percent", discountValue = 5.0) } }
        binding.chip2.setOnClickListener { setDiscountInputText("10"); highlightSelectedChip(binding.chip2)
            updateDraft { it.copy(discountType = "Percent", discountValue = 10.0) } }
        binding.chip3.setOnClickListener { setDiscountInputText("15"); highlightSelectedChip(binding.chip3)
            updateDraft { it.copy(discountType = "Percent", discountValue = 15.0) } }
        binding.chip4.setOnClickListener { setDiscountInputText("20"); highlightSelectedChip(binding.chip4)
            updateDraft { it.copy(discountType = "Percent", discountValue = 20.0) } }

        binding.discountAmountInput.addTextChangedListener {
            if (updatingEditTextManually) return@addTextChangedListener
            val typedValue = it.toString().toDoubleOrNull()
            highlightSelectedChip(
                when (it.toString()) {
                    "5" -> binding.Chip1; "10" -> binding.chip2
                    "15" -> binding.chip3; "20" -> binding.chip4
                    else -> null
                }
            )
            updateDraft { d -> d.copy(discountType = "Percent", discountValue = typedValue ?: 0.0) }
        }
        // Bug 2 fix: guard added so setText() from renderDraft doesn't re-trigger this
        binding.discountAmountFlat.addTextChangedListener {
            if (updatingEditTextManually) return@addTextChangedListener
            val flat = it.toString().toDoubleOrNull()
            updateDraft { d -> d.copy(discountType = "Flat", discountValue = flat ?: 0.0) }
        }

        binding.notesInput.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.post {
                    binding.root.findViewById<NestedScrollView>(R.id.nestedView)
                        .smoothScrollTo(0, view.bottom)
                }
            }
        }
        binding.notesInput.addTextChangedListener {
            updateDraft { d -> d.copy(endNote = it.toString()) }
        }

        // Bug 3 fix: empty string instead of null when no signature block is set,
        // so intent ("wants signature") isn't lost when signatureBlock is null in settings
        binding.addSignToggleBtn.setOnCheckedChangeListener { _, isChecked ->
            updateDraft { d ->
                d.copy(signaturePath = if (isChecked) (currentSettings?.signatureBlock ?: "") else null)
            }
        }
    }

    private fun updateDraft(transform: (Invoice) -> Invoice) {
        invoiceViewModel.invoiceDraft.value?.let { current ->
            invoiceViewModel.updateInvoiceDraft(transform(current))

        }
    }

    private fun renderDraft(draft: Invoice) {
        binding.issueDateInput.text = draft.issueDate.format(formatter)
        binding.dueDateInput.text = draft.dueDate.format(formatter)
        binding.selectedTaxChip.text = "${draft.taxPercentage}%"

        if (binding.notesInput.text.toString() != draft.endNote) {
            binding.notesInput.setText(draft.endNote)
        }
        if (binding.addSignToggleBtn.isChecked != (draft.signaturePath != null)) {
            if(draft.signaturePath==null){
                Toast.makeText(context,"Add your sign from settings first",Toast.LENGTH_SHORT).show()
                binding.addSignToggleBtn.isChecked=false
            }
            else{
                binding.addSignToggleBtn.isChecked = draft.signaturePath != null
            }

        }

        val taxRateViews = listOf(binding.noTaxChip, binding.selectedTaxChip)
        if (draft.taxPercentage != 0.0) {
            calculateTaxAndDisplay(draft.taxPercentage)
            updateTaxChipsUI(binding.selectedTaxChip, taxRateViews)
        } else {
            calculateTaxAndDisplay(0.0)
            updateTaxChipsUI(binding.noTaxChip, taxRateViews)
            binding.selectedTaxChip.text=(currentSettings?.taxRate?:10.0).toString()
        }

        // Bug 2 fix: fully restore discount UI state from draft, not just the numbers
        val discountView = listOf(binding.discountInFlat, binding.discountInPercentage, binding.noneDiscount)
        when (draft.discountType) {
            "Percent" -> {
                binding.customTaxInPercent.visibility = View.VISIBLE
                binding.customTaxInFlat.visibility = View.GONE
                updateDiscountNamesUI(binding.discountInPercentage, discountView)
                if (binding.discountAmountInput.text.toString() != formatDiscountValue(draft.discountValue)) {
                    setDiscountInputText(formatDiscountValue(draft.discountValue))
                }
                highlightSelectedChip(
                    when (draft.discountValue) {
                        5.0 -> binding.Chip1; 10.0 -> binding.chip2
                        15.0 -> binding.chip3; 20.0 -> binding.chip4
                        else -> null
                    }
                )
                calculateDiscountAndDisplay(draft.discountValue, null)
            }
            "Flat" -> {
                binding.customTaxInPercent.visibility = View.GONE
                binding.customTaxInFlat.visibility = View.VISIBLE
                updateDiscountNamesUI(binding.discountInFlat, discountView)
                if (binding.discountAmountFlat.text.toString() != formatDiscountValue(draft.discountValue)) {
                    updatingEditTextManually = true
                    binding.discountAmountFlat.setText(formatDiscountValue(draft.discountValue))
                    updatingEditTextManually = false
                }
                calculateDiscountAndDisplay(null, draft.discountValue)
            }
            else -> {
                binding.customTaxInPercent.visibility = View.GONE
                binding.customTaxInFlat.visibility = View.GONE
                updateDiscountNamesUI(binding.noneDiscount, discountView)
                highlightSelectedChip(null)
                calculateDiscountAndDisplay(null, null)
            }
        }
    }

    private fun formatDiscountValue(value: Double): String {
        return if (value == value.toInt().toDouble()) value.toInt().toString() else value.toString()
    }

    private fun setDiscountInputText(value: String) {
        updatingEditTextManually = true
        binding.discountAmountInput.setText(value)
        binding.discountAmountInput.setSelection(value.length)
        updatingEditTextManually = false
    }

    private fun highlightSelectedChip(selectedChip: Chip?) {
        listOf(binding.Chip1, binding.chip2, binding.chip3, binding.chip4).forEach { chip ->
            chip.isCheckable = false
            chip.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.greyish_white))
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
            chip.chipStrokeWidth = 0F
        }
        selectedChip?.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green_alpha))
        selectedChip?.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        selectedChip?.chipStrokeWidth = 1F
        selectedChip?.chipStrokeColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green))
    }

    private fun calculateTaxAndDisplay(taxRate: Double) {
        currentTaxRate = taxRate
        tax = subtotal * (taxRate / 100)
        binding.taxLabel.text = "tax($taxRate%)"
        binding.taxValue.text = String.format("%.2f", tax)
        updateTotal()
    }

    private fun calculateDiscountAndDisplay(discountRate: Double?, discountInFlat: Double?) {
        currentDiscountRate = discountRate
        currentDiscountFlat = discountInFlat

        discount = when {
            discountInFlat != null -> discountInFlat
            discountRate != null -> subtotal * (discountRate / 100)
            else -> 0.0
        }
        binding.discountLabel.text = if (discountInFlat != null) "Discount" else "Discount($discountRate%)"

        val show = discount > 0.0
        binding.discountLabel.visibility = if (show) View.VISIBLE else View.GONE
        binding.discountValue.visibility = if (show) View.VISIBLE else View.GONE
        binding.discountNum.visibility = if (show) View.VISIBLE else View.GONE
        binding.discountValue.text = String.format("%.2f", discount)
        binding.discountNum.text = String.format("%.2f", discount)

        updateTotal()
    }

    private fun updateTotal() {
        total = (subtotal - discount) + tax
        binding.totalValue.text = String.format("%.2f", total)
    }

    private fun updateDiscountNamesUI(view: TextView, allViews: List<TextView>) {
        allViews.forEach {
            it.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.greyish_white))
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        }
        view.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_cream))
    }

    private fun updateTaxChipsUI(view: Chip, allViews: List<Chip>) {
        allViews.forEach {
            it.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.greyish_white))
            it.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        }
        view.chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_cream))
    }

    private fun showCalenderDialog(onDateSelected: (LocalDate) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                onDateSelected(LocalDate.of(year, month + 1, day))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun convertOffsetIntoLong(paymentOffset: PaymentDueDateOffset): Long {
        return when (paymentOffset) {
            PaymentDueDateOffset.NET0 -> 0
            PaymentDueDateOffset.NET7 -> 7
            PaymentDueDateOffset.NET14 -> 14
            PaymentDueDateOffset.NET30 -> 30
            PaymentDueDateOffset.NET45 -> 45
            PaymentDueDateOffset.NET60 -> 60
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}