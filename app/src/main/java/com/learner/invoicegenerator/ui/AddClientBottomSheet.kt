package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.BottomSheetAddClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory

class AddClientBottomSheet: BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddClientBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View? {
        _binding = BottomSheetAddClientBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao= DatabaseProvider.getDatabase(requireContext()).clientDao()
        val repository= ClientRepository(dao)
        val viewModelFactory= ClientViewModelFactory(repository)
        val viewModel= ViewModelProvider(this, viewModelFactory).get(ClientViewModel::class.java)


        binding.closeBtn.setOnClickListener {
            dismiss()
        }

        viewModel.addClientState.observe(viewLifecycleOwner){state->
            when(state){
                is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Success->{
                    binding.addClientBtn.isEnabled=false
                    binding.editclientProgressBar.visibility=View.GONE
                    dismiss()
                }
                is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Error->{
                    binding.editclientProgressBar.visibility=View.GONE
                    binding.addClientBtn.isEnabled=true
                }
                is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Loading->{
                    binding.editclientProgressBar.visibility=View.VISIBLE
                    binding.addClientBtn.isEnabled=false
                }
                is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Idle->{
                    binding.editclientProgressBar.visibility = View.GONE
                }
            }
        }

        val sessionManager = SessionManager(requireContext())

        binding.addClientBtn.setOnClickListener {
            val businessName = binding.clientNameInput.text.toString().trim()
            val phone= binding.clientPhoneInput.text.toString().trim()
            if(businessName.isNotEmpty()){
                val client= Client(
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
    


}


