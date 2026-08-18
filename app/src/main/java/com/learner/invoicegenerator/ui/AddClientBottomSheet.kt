package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.BottomSheetAddClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel

class AddClientBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddClientBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetAddClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager(requireContext())

        binding.closeBtn.setOnClickListener { dismiss() }

        viewModel.addClientState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ClientState.Success -> {
                    dismiss()
                }
                else -> {}
            }
        }

        binding.addClientBtn.setOnClickListener {
            val businessName = binding.clientNameInput.text.toString().trim()
            val phone = binding.clientPhoneInput.text.toString().trim()
            if (businessName.isNotEmpty()) {
                val client = Client(
                    businessName = businessName,
                    contactPerson = "",
                    email = "",
                    address = "",
                    phone = phone,
                    workspaceId = sessionManager.getActiveWorkspaceId()
                )
                viewModel.addClient(client)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}