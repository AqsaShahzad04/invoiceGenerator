package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.chip.Chip
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.Item
import com.learner.invoicegenerator.data.repository.ItemRepository
import com.learner.invoicegenerator.databinding.FragmentAddEditItemsBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemState
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModelFactory
import kotlinx.coroutines.launch

class AddEditItemsFragment : Fragment(R.layout.fragment_add_edit_items) {

    private var _binding: FragmentAddEditItemsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ItemViewModel
    private val args: AddEditItemsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditItemsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupUI()
        observeState()
    }

    private fun setupViewModel() {
        val database = DatabaseProvider.getDatabase(requireContext())
        val repository = ItemRepository(database.itemDao())
        val factory = ItemViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ItemViewModel::class.java]
    }

    private fun setupUI() {
        val itemId = args.itemId

        if (itemId != -1) {
            binding.ItemsHeading.text = "Edit item"
            binding.createItemBtn.text = "Update item"
            loadItem(itemId)
        }

        binding.backbtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.createItemBtn.setOnClickListener {
            saveItem()
        }
    }

    private fun loadItem(itemId: Int) {
        lifecycleScope.launch {
            val item = DatabaseProvider.getDatabase(requireContext()).itemDao().getItemById(itemId)
            item?.let {
                binding.itemNameInput.setText(it.itemName)
                binding.barcodeinputField.setText(it.barcode)
                binding.priceInput.setText(if (it.price == 0.0) "0" else it.price.toString())
                
                // Select Unit Chip
                for (i in 0 until binding.unitChipGroup.childCount) {
                    val chip = binding.unitChipGroup.getChildAt(i) as Chip
                    if (chip.text == it.unit) {
                        chip.isChecked = true
                        break
                    }
                }

                // Select Category Chip
                for (i in 0 until binding.categoryChipGroup.childCount) {
                    val chip = binding.categoryChipGroup.getChildAt(i) as Chip
                    if (chip.text == it.category) {
                        chip.isChecked = true
                        break
                    }
                }
            }
        }
    }

    private fun saveItem() {
        val name = binding.itemNameInput.text.toString().trim()
        val barcode = binding.barcodeinputField.text.toString().trim()
        val priceStr = binding.priceInput.text.toString().trim()
        val price = if (priceStr.isEmpty()) 0.0 else priceStr.toDoubleOrNull() ?: 0.0
        
        // Default to first chip if none selected (should not happen with singleSelection and checked by default)
        val selectedUnitId = binding.unitChipGroup.checkedChipId
        val unit = if (selectedUnitId != View.NO_ID) {
            binding.unitChipGroup.findViewById<Chip>(selectedUnitId).text.toString()
        } else {
            "piece" // fallback
        }
        
        val selectedCategoryId = binding.categoryChipGroup.checkedChipId
        val category = if (selectedCategoryId != View.NO_ID) {
            binding.categoryChipGroup.findViewById<Chip>(selectedCategoryId).text.toString()
        } else {
            "Grocery" // fallback
        }

        if (name.isEmpty()) {
            binding.itemNameInput.error = "Item name is required"
            return
        }

        val sessionManager = SessionManager(requireContext())
        val item = Item(
            id = if (args.itemId == -1) 0 else args.itemId,
            itemName = name,
            barcode = barcode,
            price = price,
            unit = unit,
            category = category,
            workspaceId = sessionManager.getActiveWorkspaceId()
        )

        if (args.itemId == -1) {
            viewModel.addItems(item)
        } else {
            viewModel.updateItem(item)
        }
    }

    private fun observeState() {
        viewModel.itemState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ItemState.Success -> {
                    Toast.makeText(requireContext(), "Item saved", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is ItemState.Error -> {
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