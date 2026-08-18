package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.repository.ItemRepository
import com.learner.invoicegenerator.databinding.FragmentItemsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModelFactory
import com.learner.invoicegenerator.ui.items.ItemAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ItemsFragment : Fragment(R.layout.fragment_items) {

    private var _binding: FragmentItemsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ItemViewModel
    private lateinit var adapter: ItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager(requireContext())
        val viewModel: ItemViewModel by activityViewModels()
        viewModel.setWorkspaceId(sessionManager.getActiveWorkspaceId())

        setupRecyclerView()
        setupListeners()
        observeItems()
    }



    private fun setupRecyclerView() {
        adapter = ItemAdapter(
            onEditClick = { item ->
                val action = ItemsFragmentDirections.actionItemsFragmentToAddEditItemsFragment(item.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { item ->
                viewModel.deleteItem(item)
            }
        )
        binding.clientsList.layoutManager = LinearLayoutManager(requireContext())
        binding.clientsList.adapter = adapter
    }

    private fun setupListeners() {
        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Header Add Button
        binding.addclientBtn2.setOnClickListener {
            val action = ItemsFragmentDirections.actionItemsFragmentToAddEditItemsFragment(-1)
            findNavController().navigate(action)
        }
        
        // Empty State Add Button
        binding.addClientbtn.setOnClickListener {
            val action = ItemsFragmentDirections.actionItemsFragmentToAddEditItemsFragment(-1)
            findNavController().navigate(action)
        }
        
        binding.scanbtn.setOnClickListener {
            // Scanner implementation later
            Toast.makeText(requireContext(), "Scanner coming soon", Toast.LENGTH_SHORT).show()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString().trim())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeItems() {
        lifecycleScope.launch {
            viewModel.allItems.collectLatest { items ->
                val query = binding.etSearch.text.toString().trim()
                
                when {
                    items.isEmpty() && query.isEmpty() -> {
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        binding.clientsList.visibility = View.GONE
                        binding.tvNoSearchResults.visibility = View.GONE
                    }
                    items.isEmpty() && query.isNotEmpty() -> {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.clientsList.visibility = View.GONE
                        binding.tvNoSearchResults.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.clientsList.visibility = View.VISIBLE
                        binding.tvNoSearchResults.visibility = View.GONE
                        adapter.submitList(items)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}