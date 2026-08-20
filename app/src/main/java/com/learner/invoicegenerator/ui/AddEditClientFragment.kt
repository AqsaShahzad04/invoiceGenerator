package com.learner.invoicegenerator.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.databinding.FragmentAddEditClientBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientState
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.AvatarUtils
import kotlinx.coroutines.launch

class AddEditClientFragment : Fragment(R.layout.fragment_add_edit_client) {

    private var _binding: FragmentAddEditClientBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ClientViewModel by activityViewModels()
    private val args: AddEditClientFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditClientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.resetState()

        val clientId = args.clientId ?: -1
        val sessionManager = SessionManager.getInstance(requireContext())

        if (clientId == -1) {
            binding.addClientbtn.text = "Add Client"
            binding.addClientbtn.setOnClickListener {
                val businessName = binding.BusinessName.text.toString().trim()
                val contactPerson = binding.name.text.toString().trim()
                val email = binding.email.text.toString().trim()
                val phone = binding.phoneNum.text.toString().trim()
                val address = binding.address.text.toString().trim()

                if (businessName.isEmpty()) {
                    binding.BusinessName.error = "Business name is required"
                    return@setOnClickListener
                }

                val client = Client(
                    businessName = businessName,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    address = address,
                    workspaceId = sessionManager.getActiveWorkspaceId()
                )
                viewModel.addClient(client)
            }
        } else {
            binding.addClientbtn.text = "Edit Client"
            binding.newClient.text = "Edit Client"
            
            lifecycleScope.launch {
                val client = viewModel.getClientById(clientId)
                client?.let {
                    binding.BusinessName.setText(it.businessName)
                    binding.name.setText(it.contactPerson)
                    binding.email.setText(it.email)
                    binding.address.setText(it.address)
                    binding.phoneNum.setText(it.phone)
                }
            }

            binding.addClientbtn.setOnClickListener {
                val businessName = binding.BusinessName.text.toString().trim()
                val contactPerson = binding.name.text.toString().trim()
                val email = binding.email.text.toString().trim()
                val phone = binding.phoneNum.text.toString().trim()
                val address = binding.address.text.toString().trim()

                if (businessName.isEmpty()) {
                    binding.BusinessName.error = "Business name is required"
                    return@setOnClickListener
                }

                val client = Client(
                    id = clientId,
                    businessName = businessName,
                    contactPerson = contactPerson,
                    email = email,
                    phone = phone,
                    address = address,
                    workspaceId = sessionManager.getActiveWorkspaceId()
                )
                viewModel.updateClient(client)
            }
        }

        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.BusinessName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val name = s.toString().trim()
                if (name.isNotBlank()) {
                    binding.previewAvatar.visibility = View.VISIBLE
                    binding.previewAvatar.text = AvatarUtils.getLetter(name)
                    binding.previewAvatar.background.setTint(Color.parseColor(AvatarUtils.getColor(name)))
                } else {
                    binding.previewAvatar.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewModel.addClientState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ClientState.Success -> {
                    findNavController().navigateUp()
                }
                is ClientState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}