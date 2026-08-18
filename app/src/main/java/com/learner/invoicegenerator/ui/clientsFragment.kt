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
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.repository.ClientRepository
import com.learner.invoicegenerator.databinding.FragmentClientsBinding
import com.learner.invoicegenerator.ui.clients.adapter.ClientAdapter
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModelFactory
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.launch
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.activityViewModels

class clientsFragment: Fragment(R.layout.fragment_clients)  {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!
    var fullClientList : List<Client> =emptyList()


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

        val sessionManager = SessionManager(requireContext())

        val viewModel :ClientViewModel by activityViewModels()
        viewModel.setWorkspaceId(sessionManager.getActiveWorkspaceId())

        // Step 1: Adapter banao (abhi khaali list ke sath)
        clientAdapter = ClientAdapter(emptyList()) { client ->
            val letter = AvatarUtils.getLetter(client.businessName)
            val color = AvatarUtils.getColor(client.businessName)

            val action = clientsFragmentDirections.actionClientFragmentToClientdetailsFragment(
                clientId = client.id,
                clientProfileLetter = letter,
                profileBackgroundColor = color
            )
            findNavController().navigate(action)
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()

                if (query.isEmpty()) {
                    clientAdapter.updateList(fullClientList)
                } else {
                    val filteredList = fullClientList.filter {
                        it.businessName.contains(query, ignoreCase = true)
                    }
                    clientAdapter.updateList(filteredList)
                }
            }
        })

        // Step 2: LayoutManager set karo (vertical list)
        binding.clientsList.layoutManager = LinearLayoutManager(requireContext())

        // Step 3: Adapter ko RecyclerView se jodo
        binding.clientsList.adapter = clientAdapter

        // Step 4: Room database se data collect karo aur adapter ko do
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.allClients.collect { clientsList ->
                fullClientList=clientsList

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