package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetSelectInvoicePrefixBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class BottomSheetDefaultNotes(note:String): BottomSheetDialogFragment() {
    private var _binding: BottomSheetSelectInvoicePrefixBinding?=null
    val binding get()=_binding!!
    var selectedNote=note
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetSelectInvoicePrefixBinding.inflate(inflater,container,false)
        return binding.root
    }

    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()



}