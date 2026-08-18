package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.graphics.Color
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.FragmentAddEditClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher

class AddEditClientFragment: Fragment(R.layout.fragment_add_edit_client)  {

    private var _binding: FragmentAddEditClientBinding? = null
    private val binding get()=_binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        _binding= FragmentAddEditClientBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database= DatabaseProvider.getDatabase(requireContext())
        val dao=database.clientDao()
        val repository= ClientRepository(dao)
        val viewModelFactory= ClientViewModelFactory(repository)
        val viewModel= ViewModelProvider(this, viewModelFactory).get(ClientViewModel::class.java)

        val sessionManager = SessionManager(requireContext())
        val args: AddEditClientFragmentArgs by navArgs()
        val clientId=args.clientId ?: -1


        if(clientId==-1){
            binding.addClientbtn.text="Add Client"
            binding.addClientbtn.setOnClickListener {
                val businessName=binding.BusinessName.text.toString().trim()
                val contactPerson=binding.name.text.toString().trim()
                val email=binding.email.text.toString().trim()
                val phone=binding.phoneNum.text.toString().trim()
                val address=binding.address.text.toString().trim()

                val client= Client(
                    businessName = businessName,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    address = address,
                    workspaceId = sessionManager.getActiveWorkspaceId()
                )
                if (businessName.isEmpty()) {
                    binding.BusinessName.error = "Business name is required"
                    return@setOnClickListener
                }
                viewModel.addClient(client)
            }

        }
        else{
            binding.addClientbtn.text="Edit Client"
            binding.newClient.text="Edit Client"
            var currentWorkspaceId: Int? = sessionManager.getActiveWorkspaceId()
            lifecycleScope.launch {
                val client=viewModel.getClientById(clientId)
                if(client!=null){
                    currentWorkspaceId = client.workspaceId
                    binding.BusinessName.setText(client.businessName)
                    if(client.contactPerson!=null){
                        binding.name.setText(client.contactPerson)
                    }
                    if(client.email!=null){
                        binding.email.setText(client.email)
                    }
                    if(client.address!=null){
                        binding.address.setText(client.address)
                    }
                    if(client.phone!=null){
                        binding.phoneNum.setText(client.phone)
                    }
                }
            }
            binding.addClientbtn.setOnClickListener {
                val businessName=binding.BusinessName.text.toString().trim()
                val contactPerson=binding.name.text.toString().trim()
                val email=binding.email.text.toString().trim()
                val phone=binding.phoneNum.text.toString().trim()
                val address=binding.address.text.toString().trim()
                val client= Client(
                    id=clientId,
                    businessName = businessName,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    address = address,
                    workspaceId = currentWorkspaceId ?: 1 // Preserve workspaceId
                )
                if (businessName.isEmpty()) {
                    binding.BusinessName.error = "Business name is required"
                    return@setOnClickListener
                }
                viewModel.updateClient(client)
            }
        }
        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.updateState.collect { state ->
                when (state) {
                    is ClientState.Success -> {
                        Toast.makeText(requireContext(), "Client updated", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                    is ClientState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        binding.BusinessName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s.toString().trim()

                if (name.isNotBlank()) {
                    binding.previewAvatar.visibility = View.VISIBLE
                    binding.previewAvatar.text = AvatarUtils.getLetter(name)
                    binding.previewAvatar.background.setTint(
                        Color.parseColor(AvatarUtils.getColor(name))
                    )
                } else {
                    binding.previewAvatar.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

         viewModel.addClientState.observe(viewLifecycleOwner){state->
             when(state){
                 is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Success->{
                     binding.addClientbtn.isEnabled=false
                     binding.editclientProgressBar.visibility=View.GONE
                    findNavController().navigateUp()
                 }
                 is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Error->{
                     binding.editclientProgressBar.visibility=View.GONE
                     binding.addClientbtn.isEnabled=true
                 }
                 is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Loading->{
                     binding.editclientProgressBar.visibility=View.VISIBLE
                     binding.addClientbtn.isEnabled=false
                 }
                 is com.learner.invoicegenerator.ui.clients.viewmodel.ClientState.Idle->{
                     binding.editclientProgressBar.visibility = View.GONE
                 }
             }
        }

    }
    }



