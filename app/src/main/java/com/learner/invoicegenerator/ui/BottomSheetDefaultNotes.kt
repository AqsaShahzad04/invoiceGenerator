package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetDefaultNotesBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetDefaultNotes(currentNote: String) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDefaultNotesBinding? = null
    val binding get() = _binding!!
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val currentNote=currentNote
    private val quickFillOptions by lazy {
        listOf(
            binding.quickFill1Btn,
            binding.quickFill2Btn,
            binding.quickFill3Btn,
            binding.quickFill4Btn
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetDefaultNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        binding.defaultNotesInput.setText(currentNote ?: "")
        highlightMatchingQuickFill(currentNote)

        quickFillOptions.forEach { option ->
            option.setOnClickListener {
                binding.defaultNotesInput.setText(option.text)
                highlightSelected(option)
            }
        }

        binding.doneBtn.setOnClickListener {
            val note = binding.defaultNotesInput.text.toString()
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateDefaultNotes(activeWorkspaceId,note)
            }

            dismiss()
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }
    }

    private fun highlightMatchingQuickFill(note: String?) {
        val match = quickFillOptions.firstOrNull { it.text.toString() == note }
        match?.let { highlightSelected(it) }
    }

    private fun highlightSelected(selected: TextView) {
        quickFillOptions.forEach { option ->
            option.setBackgroundResource(
                if (option == selected) R.drawable.bg_row_selected
                else R.drawable.bg_input_field
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}