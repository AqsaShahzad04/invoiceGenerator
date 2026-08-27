package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceSelectClientBinding
import com.learner.invoicegenerator.ui.adaptor.InvoiceAdapter
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class NewInvoiceSelectClientFragment: Fragment(R.layout.fragment_newinvoice_select_client) {
    private var _binding: FragmentNewinvoiceSelectClientBinding?=null
    val binding get()=_binding!!

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
         viewLifecycleOwner.lifecycleScope.launch{
            viewModel.allClients.collect { clientsList->
             var adapter = InvoiceAdapter(clientsList)
                binding.clientInInvoiceRV.layoutManager= LinearLayoutManager(context)
                binding.clientInInvoiceRV.adapter=adapter
            }
        }

    }
}