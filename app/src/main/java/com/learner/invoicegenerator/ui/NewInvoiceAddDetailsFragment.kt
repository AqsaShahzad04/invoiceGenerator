package com.learner.invoicegenerator.ui

import android.app.DatePickerDialog
import android.content.res.ColorStateList
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
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.PaymentDueDateOffset
import com.learner.invoicegenerator.data.local.entity.WorkspaceSettings
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceAddDetailsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

class NewInvoiceAddDetailsFragment : Fragment(R.layout.fragment_newinvoice_add_details) {

    private var _binding: FragmentNewinvoiceAddDetailsBinding? = null
    val binding get() = _binding!!

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val invoiceViewModel: InvoiceViewModel by activityViewModels()

    // ---- calculation state ----
    private var subtotal = 0.0
    private var tax = 0.0
    private var discount = 0.0
    private var total = 0.0

    private var issuedDate: LocalDate? =null
    private var dueDate: LocalDate?=null

    // remembered rates, so values can be re-applied when subtotal changes
    private var currentTaxRate = 0.0
    private var currentDiscountRate: Double? = null
    private var currentDiscountFlat: Double? = null

    private var currentSettings: WorkspaceSettings? = null
    private var updatingEditTextManually = false

    private var addSignature:Boolean=false

    private var note:String?=null

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

        binding.issueDateInput.text = LocalDate.now().format(formatter)
        issuedDate=LocalDate.now()

        binding.issueDateSection.setOnClickListener { showCalenderDialog(binding.issueDateInput) }
        binding.dueDateSection.setOnClickListener { showCalenderDialog(binding.dueDateInput) }

        val taxRateViews = listOf(binding.noTaxChip, binding.selectedTaxChip)
        taxRateViews.forEach { chip -> chip.isCheckable = false }

        val discountView = listOf(
            binding.discountInFlat,
            binding.discountInPercentage,
            binding.noneDiscount
        )

        // ---- items: recalculate subtotal from scratch every time ----
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                invoiceViewModel.selectedItems.collect { items ->
                    subtotal = 0.0
                    items.forEach { item ->
                        subtotal += item.unitPrice * item.itemQuantity
                    }
                    binding.subtotalValue.text = String.format("%.2f", subtotal)

                    // subtotal changed, so tax and discount must be recalculated
                    calculateTaxAndDisplay(currentTaxRate)
                    calculateDiscountAndDisplay(currentDiscountRate, currentDiscountFlat)
                }
            }
        }

        // ---- workspace settings: due date + tax rate in one place ----
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.getSettingsByWorkspaceId(activeWorkspaceId).collect { settings ->
                    currentSettings = settings
                     note=settings?.defaultNotes
                    binding.notesInput.setText(note)
                    val paymentTermsDays = settings?.paymentDueDateOffset ?: PaymentDueDateOffset.NET14
                    val daysOffset = convertOffsetIntoLong(paymentTermsDays)
                    binding.dueDateInput.text = LocalDate.now().plusDays(daysOffset).format(formatter)
                    dueDate= LocalDate.now().plusDays(daysOffset)
                    val taxRate = settings?.taxRate ?: 17.0
                    binding.selectedTaxChip.text = "$taxRate%"

                    if (taxRate != 0.0) {
                        calculateTaxAndDisplay(taxRate)
                        updateTaxChipsUI(binding.selectedTaxChip, taxRateViews)
                    } else {
                        calculateTaxAndDisplay(0.0)
                        updateTaxChipsUI(binding.noTaxChip, taxRateViews)
                    }
                }
            }
        }

        // ---- tax chips ----
        binding.changeTaxChip.setOnClickListener {
            BottomSheetTaxRate(currentSettings?.taxRate ?: 17.0)
                .show(childFragmentManager, "taxRateBottomSheet")
        }
        binding.noTaxChip.setOnClickListener {
            calculateTaxAndDisplay(0.0)
            updateTaxChipsUI(binding.noTaxChip, taxRateViews)
        }
        binding.selectedTaxChip.setOnClickListener {
            calculateTaxAndDisplay(currentSettings?.taxRate ?: 17.0)
            updateTaxChipsUI(binding.selectedTaxChip, taxRateViews)
        }

        // ---- discount mode ----
        binding.discountInPercentage.setOnClickListener {
            binding.customTaxInPercent.visibility = View.VISIBLE
            binding.customTaxInFlat.visibility = View.GONE
            calculateDiscountAndDisplay(
                binding.discountAmountInput.text.toString().toDoubleOrNull(),
                null
            )
            updateDiscountNamesUI(binding.discountInPercentage, discountView)
        }
        binding.discountInFlat.setOnClickListener {
            binding.customTaxInPercent.visibility = View.GONE
            binding.customTaxInFlat.visibility = View.VISIBLE
            calculateDiscountAndDisplay(
                null,
                binding.discountAmountFlat.text.toString().toDoubleOrNull()
            )
            updateDiscountNamesUI(binding.discountInFlat, discountView)
        }
        binding.noneDiscount.setOnClickListener {
            binding.customTaxInPercent.visibility = View.GONE
            binding.customTaxInFlat.visibility = View.GONE
            highlightSelectedChip(null)
            calculateDiscountAndDisplay(null, null)
            updateDiscountNamesUI(binding.noneDiscount, discountView)
        }

        // ---- discount preset chips ----
        binding.Chip1.setOnClickListener {
            setDiscountInputText("5")
            highlightSelectedChip(binding.Chip1)
            calculateDiscountAndDisplay(5.0, null)
        }
        binding.chip2.setOnClickListener {
            setDiscountInputText("10")
            highlightSelectedChip(binding.chip2)
            calculateDiscountAndDisplay(10.0, null)
        }
        binding.chip3.setOnClickListener {
            setDiscountInputText("15")
            highlightSelectedChip(binding.chip3)
            calculateDiscountAndDisplay(15.0, null)
        }
        binding.chip4.setOnClickListener {
            setDiscountInputText("20")
            highlightSelectedChip(binding.chip4)
            calculateDiscountAndDisplay(20.0, null)
        }

        // ---- typed discount ----
        binding.discountAmountInput.addTextChangedListener {
            if (updatingEditTextManually) return@addTextChangedListener

            val typedText = it.toString()
            val typedValue = typedText.toDoubleOrNull()

            highlightSelectedChip(
                when (typedText) {
                    "5" -> binding.Chip1
                    "10" -> binding.chip2
                    "15" -> binding.chip3
                    "20" -> binding.chip4
                    else -> null
                }
            )
            calculateDiscountAndDisplay(typedValue, null)
        }

        binding.discountAmountFlat.addTextChangedListener {
            val flat = it.toString().toDoubleOrNull()
            calculateDiscountAndDisplay(null, flat)
        }

        addSignature =  binding.addSignToggleBtn.isChecked
    }

    private fun setDiscountInputText(value: String) {
        updatingEditTextManually = true
        binding.discountAmountInput.setText(value)
        binding.discountAmountInput.setSelection(value.length)
        updatingEditTextManually = false
    }

    private fun highlightSelectedChip(selectedChip: Chip?) {
        listOf(
            binding.Chip1,
            binding.chip2,
            binding.chip3,
            binding.chip4
        ).forEach { chip ->
            chip.isCheckable = false
            chip.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.greyish_white))
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
            chip.chipStrokeWidth = 0F
        }
        selectedChip?.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green_alpha))
        selectedChip?.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        selectedChip?.chipStrokeWidth = 1F
        selectedChip?.chipStrokeColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.primary_green))
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

        if (discountInFlat != null) {
            discount = discountInFlat
            binding.discountLabel.text = "Discount"
        } else if (discountRate != null) {
            discount = subtotal * (discountRate / 100)
            binding.discountLabel.text = "Discount($discountRate%)"
        } else {
            discount = 0.0
        }

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
        allViews.forEach { item ->
            item.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(requireContext(), R.color.greyish_white)
            )
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        }
        view.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(requireContext(), R.color.btn_bg_dark)
        )
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_cream))
    }

    private fun updateTaxChipsUI(view: Chip, allViews: List<Chip>) {
        allViews.forEach { item ->
            item.chipBackgroundColor =
                ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.greyish_white))
            item.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        }
        view.chipBackgroundColor =
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.btn_bg_dark))
        view.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_cream))
    }

    private fun showCalenderDialog(calenderView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay)
                calenderView.text = selectedDate.format(formatter)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
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