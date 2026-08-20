package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetSwitchWorkspaceBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import com.learner.invoicegenerator.ui.items.WorkspaceAdapter
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BottomSheetSwitchWorkspace : BottomSheetDialogFragment() {

    private var _binding: BottomSheetSwitchWorkspaceBinding? = null
    private val binding get() = _binding!!

    private val viewModel: WorkspaceViewModel by activityViewModels()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            navigationBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetSwitchWorkspaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager.getInstance(requireContext())
        val activeId = sessionManager.getActiveWorkspaceId()
        val userId = sessionManager.getUserId()








        binding.closeBtn.setOnClickListener { dismiss() }
        
        binding.btnNewWorkspace.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.addEditWorkspaceFragment)
        }

        binding.btnManage.setOnClickListener {
            dismiss()
            findNavController().navigate(R.id.manageWorkspacesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}