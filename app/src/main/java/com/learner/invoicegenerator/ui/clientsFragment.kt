package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.FragmentClientsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.clients.adapter.ClientAdapter
import com.learner.invoicegenerator.ui.clients.adapter.ClientRowStats
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.AvatarUtils
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class clientsFragment : Fragment(R.layout.fragment_clients) {
    private var _binding: FragmentClientsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClientViewModel by activityViewModels()
    private val invoiceViewModel: InvoiceViewModel by activityViewModels()

    private var fullClientList: List<Client> = emptyList()
    private var currentStatsMap: Map<Int, ClientRowStats> = emptyMap()
    private var currentCurrencySymbol: String = "$"
    private lateinit var clientAdapter: ClientAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager.getInstance(requireContext())
        val activeWorkspaceId = sessionManager.getActiveWorkspaceId()

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
                    clientAdapter.updateData(fullClientList, currentStatsMap, currentCurrencySymbol)
                } else {
                    val filteredList = fullClientList.filter {
                        it.businessName.contains(query, ignoreCase = true)
                    }
                    clientAdapter.updateData(filteredList, currentStatsMap, currentCurrencySymbol)
                }
            }
        })

        binding.clientsList.layoutManager = LinearLayoutManager(requireContext())
        binding.clientsList.adapter = clientAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(
                    viewModel.allClients,
                    invoiceViewModel.getInvoicesByWorkspaceId(activeWorkspaceId),
                    sessionManager.currencyCode
                ) { clientsList, invoicesList, currencyCode ->
                    val currencySymbol = CurrencyData.currencies.find { it.code == currencyCode }?.symbol ?: "$"

                    val statsMap = clientsList.associate { client ->
                        val clientInvoices = invoicesList.filter { it.clientId == client.id }
                        client.id to ClientRowStats(
                            totalBilled = clientInvoices.sumOf { it.totalAmount },
                            invoiceCount = clientInvoices.size
                        )
                    }

                    Triple(clientsList, statsMap, currencySymbol)
                }.collect { (clientsList, statsMap, currencySymbol) ->
                    fullClientList = clientsList
                    currentStatsMap = statsMap
                    currentCurrencySymbol = currencySymbol

                    clientAdapter.updateData(clientsList, statsMap, currencySymbol)

                    if (clientsList.isEmpty()) {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.clientsList.visibility = View.GONE
                    } else {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.clientsList.visibility = View.VISIBLE
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}