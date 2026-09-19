package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.Chip
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetSendReminderAfterDaysBinding

import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetSendRemindersAfterDays(currentDays: Int) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSendReminderAfterDaysBinding? = null
    val binding get() = _binding!!
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()


    val currentDays=currentDays
    private var selectedDays = currentDays

    private val presetDays = listOf(1, 3, 5, 7, 14)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetSendReminderAfterDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        selectedDays = currentDays
        binding.reminderDaysValue.text = selectedDays.toString()

        setupChips()
        highlightMatchingChip()

        binding.decreaseReminderDaysBtn.setOnClickListener {
            if (selectedDays > 1) {
                selectedDays--
                binding.reminderDaysValue.text = selectedDays.toString()
                highlightMatchingChip()
            }
        }

        binding.increaseReminderDaysBtn.setOnClickListener {
            selectedDays++
            binding.reminderDaysValue.text = selectedDays.toString()
            highlightMatchingChip()
        }

        binding.doneBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateSendReminderAfterDueDays(activeWorkspaceId,selectedDays)
            }

            dismiss()
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }
    }

    private fun setupChips() {
        binding.reminderPresetsChipGroup.removeAllViews()

        val styledContext = ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_TaxRate_chip)
        presetDays.forEach { days ->
            val chip = Chip(styledContext).apply {
                text = if (days == 1) "1 day" else "$days days"
                isCheckable = true
                tag = days
                setOnClickListener {
                    selectedDays = days
                    binding.reminderDaysValue.text = selectedDays.toString()
                    highlightMatchingChip()
                }
            }
            binding.reminderPresetsChipGroup.addView(chip)
        }
    }

    private fun highlightMatchingChip() {
        for (i in 0 until binding.reminderPresetsChipGroup.childCount) {
            val chip = binding.reminderPresetsChipGroup.getChildAt(i) as Chip
            chip.isChecked = (chip.tag as Int) == selectedDays
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}