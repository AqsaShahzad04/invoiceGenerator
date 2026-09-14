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
        binding.lateFeeDisplayValue.text = selectedRate.toString()
        binding.customRateInput.setText(selectedRate.toString())
        updateCheckedChip()
    }

    fun updateCheckedChip() {
        for (i in 0 until binding.latefeeRatesChipGroup.childCount) {
            val chip = binding.latefeeRatesChipGroup.getChildAt(i) as Chip
            chip.isChecked = (chip.tag as Double) == selectedRate
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

        binding.lateFeeDisplayValue.text = selectedRate.toString()
        binding.customRateInput.setText(selectedRate.toString())

        val styledContext = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_TaxRate_chip)
        commonRates.forEach { rate ->
            val chip = Chip(styledContext)
            chip.text = "$rate%"
            chip.isCheckable = true
            chip.tag = rate
            chip.isChecked = rate == selectedRate

            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedRate = rate
                    binding.lateFeeDisplayValue.text = rate.toString()
                    binding.customRateInput.setText(rate.toString())
                }
            }
            binding.latefeeRatesChipGroup.addView(chip)
        }


        binding.customRateInput.addTextChangedListener {
            val value = it.toString().toDoubleOrNull()
            if (value != null) {
                selectedRate = value.coerceIn(0.0, 100.0)
                binding.lateFeeDisplayValue.text = selectedRate.toString()
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