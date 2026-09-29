package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetLateFeeBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetLateFee(rate: Double) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetLateFeeBinding? = null
    val binding get() = _binding!!

    var selectedRate = rate
    private var isUpdatingUI = false

    private val commonRates = listOf(0.0, 1.0, 1.5,2.0, 3.0, 5.0)

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetLateFeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateRate(newRate: Double) {
        selectedRate = newRate.coerceIn(0.0, 100.0)
        val textToSet = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
        binding.lateFeeDisplayValue.text = textToSet
        if (binding.customRateInput.text.toString() != textToSet) {
            isUpdatingUI = true
            binding.customRateInput.setText(textToSet)
            isUpdatingUI = false
        }
        updateCheckedChip()
    }

    fun updateCheckedChip() {
        val wasUpdating = isUpdatingUI
        isUpdatingUI = true
        for (i in 0 until binding.latefeeRatesChipGroup.childCount) {
            val chip = binding.latefeeRatesChipGroup.getChildAt(i) as Chip
            val shouldCheck = (chip.tag as Double) == selectedRate
            if (chip.isChecked != shouldCheck) {
                chip.isChecked = shouldCheck
            }
        }
        isUpdatingUI = wasUpdating
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        val initialText = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
        binding.lateFeeDisplayValue.text = initialText
        if (binding.customRateInput.text.toString() != initialText) {
            isUpdatingUI = true
            binding.customRateInput.setText(initialText)
            isUpdatingUI = false
        }

        val styledContext = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_TaxRate_chip)
        commonRates.forEach { rate ->
            val chip = Chip(styledContext)
            chip.text = "$rate%"
            chip.isCheckable = true
            chip.tag = rate
            chip.isChecked = rate == selectedRate

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isUpdatingUI) {
                    selectedRate = rate
                    val textToSet = if (rate == rate.toInt().toDouble()) rate.toInt().toString() else rate.toString()
                    binding.lateFeeDisplayValue.text = textToSet
                    if (binding.customRateInput.text.toString() != textToSet) {
                        isUpdatingUI = true
                        binding.customRateInput.setText(textToSet)
                        isUpdatingUI = false
                    }
                }
            }
            binding.latefeeRatesChipGroup.addView(chip)
        }


        binding.customRateInput.addTextChangedListener {
            if (isUpdatingUI) return@addTextChangedListener
            val value = it.toString().toDoubleOrNull()
            if (value != null) {
                selectedRate = value.coerceIn(0.0, 100.0)
                binding.lateFeeDisplayValue.text = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
                updateCheckedChip()
            }
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

        binding.doneBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateLateFee(activeWorkspaceId, selectedRate)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}