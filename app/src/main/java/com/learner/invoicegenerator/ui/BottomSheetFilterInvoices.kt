package com.learner.invoicegenerator.ui.invoice.filter

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.BottomSheetFilterInvoicesBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DateFilterResult(
    val fromDate: LocalDate,
    val toDate: LocalDate,
    val label: String
)

enum class QuickRange(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    LAST_3_MONTHS("Last 3 months"),
    THIS_YEAR("This year"),
    ALL_TIME("All time")
}

class BottomSheetFilterInvoices(
    private val onApply: (DateFilterResult) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterInvoicesBinding? = null
    private val binding get() = _binding!!

    private var selectedQuickRange: QuickRange? = QuickRange.THIS_MONTH
    private var customFromDate: LocalDate? = null
    private var customToDate: LocalDate? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    private lateinit var chipMap: Map<QuickRange, TextView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterInvoicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipMap = mapOf(
            QuickRange.TODAY to binding.btnToday,
            QuickRange.YESTERDAY to binding.btnYesterday,
            QuickRange.THIS_WEEK to binding.btnThisWeek,
            QuickRange.THIS_MONTH to binding.btnThisMonth,
            QuickRange.LAST_MONTH to binding.btnLastMonth,
            QuickRange.LAST_3_MONTHS to binding.btnLast3Months,
            QuickRange.THIS_YEAR to binding.btnThisYear,
            QuickRange.ALL_TIME to binding.btnAllTime
        )

        setupChipClickListeners()
        setupDateInputs()
        applyChipStyles()

        binding.closeBtnDateFilter.setOnClickListener { dismiss() }
        binding.applyBtn.setOnClickListener { handleApplyClick() }

    }

    private fun setupChipClickListeners() {
        chipMap.forEach { (range, chip) ->
            chip.setOnClickListener {
                selectedQuickRange = range
                customFromDate = null
                customToDate = null
                binding.fromDateInput.text.clear()
                binding.toDateInput.text.clear()
                applyChipStyles()
            }
        }
    }

    private fun applyChipStyles() {
        chipMap.forEach { (range, chip) ->
            if (range == selectedQuickRange) {
                chip.setBackgroundResource(R.drawable.green_btn_bg)
                chip.backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.primary_green)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.bg_cream))
            } else {
                chip.setBackgroundResource(R.drawable.bg_input_field)
                chip.backgroundTintList = null
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.btn_text_dark))
            }
        }
    }

    private fun setupDateInputs() {
        binding.fromDateInput.setOnClickListener {
            showDatePicker { picked ->
                customFromDate = picked
                selectedQuickRange = null
                applyChipStyles()
                binding.fromDateInput.setText(picked.format(dateFormatter))
            }
        }

        binding.toDateInput.setOnClickListener {
            showDatePicker { picked ->
                customToDate = picked
                selectedQuickRange = null
                applyChipStyles()
                binding.toDateInput.setText(picked.format(dateFormatter))
            }
        }
    }

    private fun showDatePicker(onDatePicked: (LocalDate) -> Unit) {
        val today = LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth -> onDatePicked(LocalDate.of(year, month + 1, dayOfMonth)) },
            today.year,
            today.monthValue - 1,
            today.dayOfMonth
        ).show()
    }

    private fun handleApplyClick() {
        val result = when {
            selectedQuickRange != null -> resolveQuickRange(selectedQuickRange!!)
            customFromDate != null && customToDate != null -> DateFilterResult(
                fromDate = customFromDate!!,
                toDate = customToDate!!,
                label = "Custom range"
            )
            else -> return
        }

        onApply(result)
        dismiss()
    }

    // Maps each quick-select option to its concrete from/to date range relative to today.
    private fun resolveQuickRange(range: QuickRange): DateFilterResult {
        val today = LocalDate.now()
        return when (range) {
            QuickRange.TODAY -> DateFilterResult(today, today, range.label)

            QuickRange.YESTERDAY -> {
                val yesterday = today.minusDays(1)
                DateFilterResult(yesterday, yesterday, range.label)
            }

            QuickRange.THIS_WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                val endOfWeek = startOfWeek.plusDays(6)
                DateFilterResult(startOfWeek, endOfWeek, range.label)
            }

            QuickRange.THIS_MONTH -> {
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth())
                DateFilterResult(startOfMonth, endOfMonth, range.label)
            }

            QuickRange.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1)
                val end = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                DateFilterResult(start, end, range.label)
            }

            QuickRange.LAST_3_MONTHS -> {
                val start = today.minusMonths(3).withDayOfMonth(1)
                DateFilterResult(start, today, range.label)
            }

            QuickRange.THIS_YEAR -> {
                val startOfYear = today.withDayOfYear(1)
                val endOfYear = today.withDayOfYear(today.lengthOfYear())
                DateFilterResult(startOfYear, endOfYear, range.label)
            }

            QuickRange.ALL_TIME -> DateFilterResult(LocalDate.of(2000, 1, 1), LocalDate.of(2099, 12, 31), range.label)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "BottomSheetFilterInvoices"
    }
}