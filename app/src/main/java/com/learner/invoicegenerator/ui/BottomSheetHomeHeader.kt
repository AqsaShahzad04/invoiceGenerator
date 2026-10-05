package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetHomeHeaderBinding

class BottomSheetHomeHeader(
    private val onHeaderStyleChanged: ((String) -> Unit)? = null
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetHomeHeaderBinding? = null
    val binding get() = _binding!!

    private var selectedStyle = "Compact"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetHomeHeaderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sessionManager = SessionManager.getInstance(requireContext())
        selectedStyle = sessionManager.getHomeHeaderStyle()
        updateSelectionUI()

        binding.optionCompact.setOnClickListener {
            selectedStyle = "Compact"
            updateSelectionUI()
        }

        binding.optionEditorial.setOnClickListener {
            selectedStyle = "Editorial"
            updateSelectionUI()
        }

        binding.doneBtn.setOnClickListener {
            sessionManager.setHomeHeaderStyle(selectedStyle)
            onHeaderStyleChanged?.invoke(selectedStyle)
            dismiss()
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }
    }

    private fun updateSelectionUI() {
        if (selectedStyle == "Editorial") {
            binding.ivCompactCheck.visibility = View.GONE
            binding.ivEditorialCheck.visibility = View.VISIBLE
        } else {
            binding.ivCompactCheck.visibility = View.VISIBLE
            binding.ivEditorialCheck.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}