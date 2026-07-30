package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.FragmentClientsBinding
import com.learner.invoicegenerator.ui.clients.adapter.ClientAdapter
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory
import kotlinx.coroutines.launch

class clientsFragment: Fragment(R.layout.fragment_clients)  {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!

    private lateinit var clientAdapter: ClientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentClientsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val database = DatabaseProvider.getDatabase(requireContext())
        val dao = database.clientDao()
        val repository = ClientRepository(dao)
        val viewModelFactory = ClientViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ClientViewModel::class.java)

        // Step 1: Adapter banao (abhi khaali list ke sath)
        clientAdapter = ClientAdapter(emptyList()) { client ->
           findNavController().navigate(R.id.action_clientFragment_to_ClientdetailsFragment)
        }

        // Step 2: LayoutManager set karo (vertical list)
        binding.clientsList.layoutManager = LinearLayoutManager(requireContext())

        // Step 3: Adapter ko RecyclerView se jodo
        binding.clientsList.adapter = clientAdapter

        // Step 4: Room database se data collect karo aur adapter ko do
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allClients.collect { clientsList ->
                clientAdapter.updateList(clientsList)

                if (clientsList.isEmpty()) {
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.clientsList.visibility = View.GONE
                } else {
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.clientsList.visibility = View.VISIBLE
                }
            }
        }

        binding.addClientbtn.setOnClickListener {
            findNavController().navigate(R.id.action_clientFragment_to_addEditClientFragment)
        }
        binding.addclientBtn2.setOnClickListener {
            findNavController().navigate(R.id.action_clientFragment_to_addEditClientFragment)
        }
    }
}