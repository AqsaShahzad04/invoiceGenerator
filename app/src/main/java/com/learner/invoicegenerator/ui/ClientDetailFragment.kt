package com.learner.invoicegenerator.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.FragmentClientDetailBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory
import kotlinx.coroutines.launch

class ClientDetailFragment: Fragment(R.layout.fragment_client_detail) {


    private val args: ClientDetailFragmentArgs by navArgs()

    private var _binding: FragmentClientDetailBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val database = DatabaseProvider.getDatabase(requireContext())
        val dao = database.clientDao()
        val repository = ClientRepository(dao)
        val viewModelFactory = ClientViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ClientViewModel::class.java)
        val clientId = args.clientId
        val avatarLetter = args.clientProfileLetter
        val avatarColor = args.profileBackgroundColor
        val businessName = binding.businessName
        val clientEmail = binding.clientMail
        val clientNum = binding.clientNum
        val clientAddress = binding.clientAddress
        val avatar = binding.profileCircle
        val backbtn = binding.backbtnbg
        lifecycleScope.launch {
            val client = viewModel.getClientById(clientId)
            if (client != null) {
                businessName.text = client?.businessName
                avatar.text = avatarLetter
                avatar.background.setTint(Color.parseColor(avatarColor))
                if (client.email.isNullOrBlank()) {
                    clientEmail.text = "Add email Address"
                    clientEmail.setTextColor(Color.parseColor("#0C861A"))
                    binding.emailAddBtn.visibility = View.VISIBLE
                } else {
                    clientEmail.text = client?.email
                    clientEmail.setTextColor(Color.parseColor("#171817"))
                    binding.emailAddBtn.visibility = View.GONE
                }
                if (client.phone.isNullOrBlank()) {
                    clientNum.text = "Add Phone Number"
                    clientNum.setTextColor(Color.parseColor("#0C861A"))
                    binding.phoneAddBtn.visibility = View.VISIBLE
                } else {
                    clientNum.text = client?.phone
                    clientNum.setTextColor(Color.parseColor("#171817"))
                    binding.phoneAddBtn.visibility = View.GONE
                }


                if (client.address.isNullOrBlank()) {
                    clientAddress.text = "Add billing Address"
                    clientAddress.setTextColor(Color.parseColor("#0C861A"))
                    binding.addressAddBtn.visibility = View.VISIBLE
                } else {
                    clientAddress.text = client?.address
                    clientAddress.setTextColor(Color.parseColor("#171817"))
                    binding.addressAddBtn.visibility = View.GONE
                }

            } else {
                businessName.text = "Client not found"
            }

        }
        backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_clientDetailFragment_to_clientFragment)
        }
        binding.trashbutton.setOnClickListener {
            lifecycleScope.launch {
                val client = viewModel.getClientById(clientId)
                if (client != null) {
                    viewModel.deleteClient(client)
                    findNavController().navigate(R.id.action_clientDetailFragment_to_clientFragment)
                }
            }
        }
        binding.editbtn.setOnClickListener {
            val action = ClientDetailFragmentDirections
                .actionClientDetailFragmentToAddEditClientFragment(clientId = args.clientId)
            findNavController().navigate(action)
        }
        binding.emailAddBtn.setOnClickListener {
           val action= ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = args.clientId)
            findNavController().navigate(action)
        }
        binding.phoneAddBtn.setOnClickListener {
           val action=ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = args.clientId)
            findNavController().navigate(action)
        }
        binding.addressAddBtn.setOnClickListener {
            val action=ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = args.clientId)
            findNavController().navigate(action)
        }
    }
}


