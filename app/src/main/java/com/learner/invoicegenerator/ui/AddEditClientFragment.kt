package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.FragmentAddEditClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory

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
             }
        }
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
                address = address
            )
            if (businessName.isEmpty()) {
                binding.BusinessName.error = "Business name is required"
                return@setOnClickListener
            }
            viewModel.addClient(client)
        }
    }

    }



