package com.learner.invoicegenerator.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentClientDetailBinding
import com.learner.invoicegenerator.ui.clients.viewmodel.ClientViewModel
import com.learner.invoicegenerator.utils.CurrencyData
import kotlinx.coroutines.launch

class ClientDetailFragment : Fragment(R.layout.fragment_client_detail) {

    private val args: ClientDetailFragmentArgs by navArgs()
    private var _binding: FragmentClientDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ClientViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val clientId = args.clientId
        val avatarLetter = args.clientProfileLetter
        val avatarColor = args.profileBackgroundColor
        val sessionManager= SessionManager.getInstance(requireContext())
        viewLifecycleOwner.lifecycleScope.launch{
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                sessionManager.currencyCode.collect(){currencyCode->
                    val currency= CurrencyData.currencies.find {
                        it.code==currencyCode
                    }
                    val currencySymbol=currency?.symbol
                    binding.totalBilledValuecurrency.setText(currencySymbol)
                    binding.outstandingValuecurrency.setText(currencySymbol)
                }
            }
        }



        lifecycleScope.launch {
            val client = viewModel.getClientById(clientId)
            if (client != null) {
                binding.businessName.text = client.businessName
                binding.profileCircle.text = avatarLetter
                binding.profileCircle.background.setTint(Color.parseColor(avatarColor))
                
                if (client.email.isNullOrBlank()) {
                    binding.clientMail.text = "Add email Address"
                    binding.clientMail.setTextColor(Color.parseColor("#0C861A"))
                    binding.emailAddBtn.visibility = View.VISIBLE
                } else {
                    binding.clientMail.text = client.email
                    binding.clientMail.setTextColor(Color.parseColor("#171817"))
                    binding.emailAddBtn.visibility = View.GONE
                }
                
                if (client.phone.isNullOrBlank()) {
                    binding.clientNum.text = "Add Phone Number"
                    binding.clientNum.setTextColor(Color.parseColor("#0C861A"))
                    binding.phoneAddBtn.visibility = View.VISIBLE
                } else {
                    binding.clientNum.text = client.phone
                    binding.clientNum.setTextColor(Color.parseColor("#171817"))
                    binding.phoneAddBtn.visibility = View.GONE
                }

                if (client.address.isNullOrBlank()) {
                    binding.clientAddress.text = "Add billing Address"
                    binding.clientAddress.setTextColor(Color.parseColor("#0C861A"))
                    binding.addressAddBtn.visibility = View.VISIBLE
                } else {
                    binding.clientAddress.text = client.address
                    binding.clientAddress.setTextColor(Color.parseColor("#171817"))
                    binding.addressAddBtn.visibility = View.GONE
                }
            } else {
                binding.businessName.text = "Client not found"
            }
        }

        binding.backbtnbg.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.trashbutton.setOnClickListener {
            lifecycleScope.launch {
                val client = viewModel.getClientById(clientId)
                if (client != null) {
                    viewModel.deleteClient(client)
                    findNavController().popBackStack()
                }
            }
        }

        binding.editbtn.setOnClickListener {
            val action = ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = clientId)
            findNavController().navigate(action)
        }

        binding.emailAddBtn.setOnClickListener {
            val action = ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = clientId)
            findNavController().navigate(action)
        }

        binding.phoneAddBtn.setOnClickListener {
            val action = ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = clientId)
            findNavController().navigate(action)
        }

        binding.addressAddBtn.setOnClickListener {
            val action = ClientDetailFragmentDirections.actionClientDetailFragmentToAddEditClientFragment(clientId = clientId)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}