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
        binding.taxRateValue.text = selectedRate.toInt().toString()
        binding.taxRateInput.setText(selectedRate.toInt().toString())
        updateCheckedChip()
    }

    fun updateCheckedChip() {
        for (i in 0 until binding.commonRatesChipGroup.childCount) {
            val chip = binding.commonRatesChipGroup.getChildAt(i) as Chip
            chip.isChecked = (chip.tag as Int).toDouble() == selectedRate
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        binding.taxRateValue.text = selectedRate.toInt().toString()
        binding.taxRateInput.setText(selectedRate.toInt().toString())

        val styledContext = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_TaxRate_chip)
        commonRates.forEach { rate ->
            val chip = Chip(styledContext)
            chip.text = "$rate%"
            chip.isCheckable = true
            chip.tag = rate
            chip.isChecked = rate.toDouble() == selectedRate

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedRate = rate.toDouble()
                    binding.taxRateValue.text = rate.toString()
                    binding.taxRateInput.setText(rate.toString())
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
            val value = it.toString().toDoubleOrNull()
            if (value != null) {
                selectedRate = value.coerceIn(0.0, 100.0)
                binding.taxRateValue.text = selectedRate.toInt().toString()
                updateCheckedChip()
            }
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

        binding.doneBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateTaxRate(activeWorkspaceId, selectedRate)
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}