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
import com.learner.invoicegenerator.databinding.BottomSheetTaxRateBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetTaxRate(rate: Double) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetTaxRateBinding? = null
    val binding get() = _binding!!

    var selectedRate = rate
    private var isUpdatingUI = false

    private val commonRates = listOf(0, 5, 10, 13, 17, 18, 25)

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetTaxRateBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun updateRate(newRate: Double) {
        selectedRate = newRate.coerceIn(0.0, 100.0)
        val textToSet = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
        binding.taxRateValue.text = textToSet
        if (binding.taxRateInput.text.toString() != textToSet) {
            isUpdatingUI = true
            binding.taxRateInput.setText(textToSet)
            isUpdatingUI = false
        }
        updateCheckedChip()
    }

    fun updateCheckedChip() {
        val wasUpdating = isUpdatingUI
        isUpdatingUI = true
        for (i in 0 until binding.commonRatesChipGroup.childCount) {
            val chip = binding.commonRatesChipGroup.getChildAt(i) as Chip
            val shouldCheck = (chip.tag as Int).toDouble() == selectedRate
            if (chip.isChecked != shouldCheck) {
                chip.isChecked = shouldCheck
            }
        }
        isUpdatingUI = wasUpdating
    }

    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.resetState()
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        val initialText = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
        binding.taxRateValue.text = initialText
        if (binding.taxRateInput.text.toString() != initialText) {
            isUpdatingUI = true
            binding.taxRateInput.setText(initialText)
            isUpdatingUI = false
        }

        val styledContext = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_TaxRate_chip)
        commonRates.forEach { rate ->
            val chip = Chip(styledContext)
            chip.text = "$rate%"
            chip.isCheckable = true
            chip.tag = rate
            chip.isChecked = rate.toDouble() == selectedRate

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !isUpdatingUI) {
                    selectedRate = rate.toDouble()
                    val textToSet = rate.toString()
                    binding.taxRateValue.text = textToSet
                    if (binding.taxRateInput.text.toString() != textToSet) {
                        isUpdatingUI = true
                        binding.taxRateInput.setText(textToSet)
                        isUpdatingUI = false
                    }
                }
            }
            binding.commonRatesChipGroup.addView(chip)
        }

        binding.decreaseTaxRateBtn.setOnClickListener {
            updateRate(selectedRate - 1)
        }
        binding.increaseTaxRateBtn.setOnClickListener {
            updateRate(selectedRate + 1)
        }

        binding.taxRateInput.addTextChangedListener {
            if (isUpdatingUI) return@addTextChangedListener
            val value = it.toString().toDoubleOrNull()
            if (value != null) {
                selectedRate = value.coerceIn(0.0, 100.0)
                binding.taxRateValue.text = if (selectedRate == selectedRate.toInt().toDouble()) selectedRate.toInt().toString() else selectedRate.toString()
                updateCheckedChip()
            }
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

        settingsViewModel.state.observe(viewLifecycleOwner) { state ->
            if (!isSubmitting) return@observe
            when (state) {
                is com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState.Success -> {
                    isSubmitting = false
                    dismiss()
                    settingsViewModel.resetState()
                }
                is com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState.Error -> {
                    isSubmitting = false
                    android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_SHORT).show()
                    settingsViewModel.resetState()
                }
                else -> {}
            }
        }

        binding.doneBtn.setOnClickListener {
            isSubmitting = true
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateTaxRate(activeWorkspaceId, selectedRate)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}