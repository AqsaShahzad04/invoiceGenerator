package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.databinding.BottomSheetSetupWorkspaceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel

class BottomSheetSetupWorkspace : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSetupWorkspaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkspaceViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSetupWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetState()
        val sessionManager = SessionManager.getInstance(requireContext())

        binding.closeBtn.setOnClickListener { dismiss() }

        binding.saveBtn.setOnClickListener {
            val name = binding.workspaceNameInput.text.toString().trim()
            if (name.isEmpty()) {
                binding.workspaceNameInput.error = "Workspace name is required"
                return@setOnClickListener
            }

            val userId = sessionManager.getUserId()
            val workspace = Workspace(
                name = name,
                ownerUserId = userId,
                isDefault = true
            )
            viewModel.addWorkspace(workspace)

        }

        viewModel.workspaceState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkspaceState.Loading -> {
                    binding.saveBtn.isEnabled = false
                }
                is WorkspaceState.Success -> {
                    Toast.makeText(requireContext(), "Workspace created!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                is WorkspaceState.Error -> {
                    binding.saveBtn.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}