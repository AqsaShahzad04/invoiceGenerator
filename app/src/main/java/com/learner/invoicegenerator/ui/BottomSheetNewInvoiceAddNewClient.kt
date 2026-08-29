package com.learner.invoicegenerator.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.BottomSheetNewInvoiceAddNewClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel

class BottomSheetNewInvoiceAddNewClient: BottomSheetDialogFragment() {

    private var _binding: BottomSheetNewInvoiceAddNewClientBinding?=null
    val binding get()=_binding!!

    val clientViewModel : ClientViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetNewInvoiceAddNewClientBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()
        binding.addClientBtn.setOnClickListener {
            val businessName=binding.businessNameInput.text.toString().trim()
            val clientName=binding.clientNameInput.text.toString().trim()
            val phoneNum=binding.clientPhoneInput.text.toString().trim()
            val address=binding.clientAddressInput.text.toString().trim()
            if (businessName.isNullOrEmpty()){
                binding.businessNameInput.error="Business name is required!!"
                return@setOnClickListener
            }
            val client= Client(
                businessName = businessName,
                contactPerson = clientName,
                email="",
                phone = phoneNum,
                address = address,
                workspaceId = activeWorkspaceId
            )
            clientViewModel.addClient(client)

        }
    }
}