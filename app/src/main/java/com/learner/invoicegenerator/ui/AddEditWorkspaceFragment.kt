package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.databinding.FragmentAddEditWorkspaceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import kotlinx.coroutines.launch

class AddEditWorkspaceFragment : Fragment(R.layout.fragment_add_edit_workspace) {

    private var _binding: FragmentAddEditWorkspaceBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: WorkspaceViewModel by activityViewModels()
    private val args: AddEditWorkspaceFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }

    private fun setupUI() {
        val workspaceId = args.workspaceId

        if (workspaceId != -1) {
            binding.title.text = "Edit workspace"
            binding.createBtn.text = "Update workspace"
            loadWorkspace(workspaceId)
        }

        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.createBtn.setOnClickListener {
            saveWorkspace()
        }
    }

    private fun loadWorkspace(workspaceId: Int) {
        lifecycleScope.launch {
            val sessionManager = SessionManager(requireContext())
            val userId = sessionManager.getUserId()
            viewModel.getWorkspacesByUserId(userId).collect { list ->
                val workspace = list.find { it.id == workspaceId }
                workspace?.let {
                    binding.workspaceNameInput.setText(it.name)
                    binding.emailInput.setText(it.email)
                    binding.phoneInput.setText(it.phone)
                    binding.taxInput.setText(it.taxNumber)
                    binding.addressInput.setText(it.address)
                }
            }
        }
    }

    private fun saveWorkspace() {
        val name = binding.workspaceNameInput.text.toString().trim()
        val email = binding.emailInput.text.toString().trim()
        val phone = binding.phoneInput.text.toString().trim()
        val tax = binding.taxInput.text.toString().trim()
        val address = binding.addressInput.text.toString().trim()

        if (name.isEmpty()) {
            binding.workspaceNameInput.error = "Workspace name is required"
            return
        }

        val sessionManager = SessionManager(requireContext())
        val userId = sessionManager.getUserId()

        val workspace = Workspace(
            id = if (args.workspaceId == -1) 0 else args.workspaceId,
            name = name,
            ownerUserId = userId,
            email = email,
            phone = phone,
            taxNumber = tax,
            address = address,
            isDefault = args.workspaceId == -1
        )

        if (args.workspaceId == -1) {
            viewModel.addWorkspace(workspace)
        } else {
            viewModel.updateWorkspace(workspace)
        }
    }

    private fun observeState() {
        viewModel.workspaceState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkspaceState.Loading -> {
                    binding.createBtn.isEnabled = false
                }
                is WorkspaceState.Success -> {
                    Toast.makeText(requireContext(), "Workspace saved", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is WorkspaceState.Error -> {
                    binding.createBtn.isEnabled = true
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