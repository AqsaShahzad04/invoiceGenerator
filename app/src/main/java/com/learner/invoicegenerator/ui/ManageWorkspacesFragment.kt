package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentManageWorkspacesBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.items.ManageWorkspaceAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ManageWorkspacesFragment : Fragment(R.layout.fragment_manage_workspaces) {

    private var _binding: FragmentManageWorkspacesBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: WorkspaceViewModel by activityViewModels()
    private lateinit var adapter: ManageWorkspaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageWorkspacesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val sessionManager = SessionManager.getInstance(requireContext())
        val userId = sessionManager.getUserId()
        val activeId = sessionManager.getActiveWorkspaceId()

        adapter = ManageWorkspaceAdapter(activeId, 
            onWorkspaceClick = { workspace ->
                sessionManager.setActiveWorkspace(workspace.id)
                adapter.setActiveId(workspace.id)
            },
            onEditClick = { workspace ->
                val action = ManageWorkspacesFragmentDirections.actionManageWorkspacesFragmentToAddEditWorkspaceFragment(workspace.id)
                findNavController().navigate(action)
            }
        )

        binding.workspaceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.workspaceRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getWorkspacesByUserId(userId).collectLatest { list ->
                adapter.submitList(list)
            }
        }

        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnAddWorkspace.setOnClickListener {
            val action = ManageWorkspacesFragmentDirections.actionManageWorkspacesFragmentToAddEditWorkspaceFragment(-1)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}