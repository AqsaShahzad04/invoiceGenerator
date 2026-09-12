package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.State
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
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
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceSelectClientBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceSelectClientAdapter
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.launch

class NewInvoiceSelectClientFragment: Fragment(R.layout.fragment_newinvoice_select_client) {
    private var _binding: FragmentNewinvoiceSelectClientBinding?=null
    val binding get()=_binding!!
    private var adapter: InvoiceSelectClientAdapter? = null
    private val viewModel: ClientViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewinvoiceSelectClientBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        binding.clientInInvoiceRV.layoutManager = LinearLayoutManager(context)


        fun selectClient(newClient: Client) {
            viewModel.selectClient(newClient)
        }
        binding.searchField.addTextChangedListener{text->
            viewModel.setSearchQuery(text.toString())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED){
                viewModel.allClients.collect { clientsList ->
                    adapter = InvoiceSelectClientAdapter(
                        clientsList,
                        ::selectClient,
                        viewModel.selectedClient.value?.id
                    )
                    binding.clientInInvoiceRV.adapter = adapter
                    binding.noResultsTextView.visibility= if(clientsList.isEmpty()) View.VISIBLE else View.GONE
                }
            }

        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedClient.collect { client ->
                adapter?.setSelectedClient(client?.id)
            }
        }





        binding.newInvoiceAddClientBtn.setOnClickListener {
            BottomSheetNewInvoiceAddNewClient().show(parentFragmentManager,"Add client on the go")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        _binding = null
    }
}