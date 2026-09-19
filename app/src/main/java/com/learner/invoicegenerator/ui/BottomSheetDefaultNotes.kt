package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetDefaultNotesBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import kotlinx.coroutines.launch

class BottomSheetDefaultNotes(currentNote: String) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetDefaultNotesBinding? = null
    val binding get() = _binding!!
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()
    val currentNote=currentNote


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetDefaultNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun UpdateselectedNote(view:View?,prefix:String){
        listOf(
            binding.quickFill1Btn,
            binding.quickFill2Btn,
            binding.quickFill3Btn,
            binding.quickFill4Btn
        ).forEach { it.setBackgroundResource(R.drawable.bg_input_field) }
        if(view!=null){
            view.setBackgroundResource(R.drawable.bg_row_selected)
            binding.defaultNotesInput.setText(prefix)
        }
        else{
            binding.defaultNotesInput.setText(prefix)
        }


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val activeWorkspaceId=sessionManager.getActiveWorkspaceId()

        initNote()

       binding.quickFill4Btn.setOnClickListener {
           UpdateselectedNote(binding.quickFill4Btn,binding.quickFill4Btn.text.toString())
       }
        binding.quickFill3Btn.setOnClickListener {
            UpdateselectedNote(binding.quickFill3Btn,binding.quickFill3Btn.text.toString())
        }
        binding.quickFill2Btn.setOnClickListener {
            UpdateselectedNote(binding.quickFill2Btn,binding.quickFill2Btn.text.toString())
        }
        binding.quickFill1Btn.setOnClickListener {
            UpdateselectedNote(binding.quickFill1Btn,binding.quickFill1Btn.text.toString())
        }

        binding.doneBtn.setOnClickListener {
            val note = binding.defaultNotesInput.text.toString()
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateDefaultNotes(activeWorkspaceId,note)
            }

            dismiss()
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }
    }


    private fun initNote(){
        when(currentNote){
            binding.quickFill1Btn.text.toString()->UpdateselectedNote(binding.quickFill1Btn,binding.quickFill1Btn.text.toString())
            binding.quickFill2Btn.text.toString()->UpdateselectedNote(binding.quickFill2Btn,binding.quickFill2Btn.text.toString())
            binding.quickFill3Btn.text.toString()->UpdateselectedNote(binding.quickFill3Btn,binding.quickFill3Btn.text.toString())
            binding.quickFill4Btn.text.toString()->UpdateselectedNote(binding.quickFill4Btn,binding.quickFill4Btn.text.toString())
            else->UpdateselectedNote(null,currentNote)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}