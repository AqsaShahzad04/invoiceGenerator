package com.learner.invoicegenerator.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetAddSignatureBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsState
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceSettingsViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.WorkspaceViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class BottomSheetAddSignature: BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddSignatureBinding?=null
    val binding get()=_binding!!
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels ()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetAddSignatureBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        val workspaceId=sessionManager.getActiveWorkspaceId()

        binding.closebtn.setOnClickListener {
            dismiss()
        }
        val signPad=binding.signaturePad
        signPad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                binding.signatureLine.visibility=View.GONE
                binding.signaturePlaceholder.visibility=View.GONE
            }

            override fun onSigned() {

            }

            override fun onClear() {
                binding.signatureLine.visibility=View.VISIBLE
                binding.signaturePlaceholder.visibility=View.VISIBLE
            }
        })

        binding.clearSignatureBtn.setOnClickListener {
           signPad.clear()
        }
        settingsViewModel.state.observe(viewLifecycleOwner){state->
            when(state){
                is WorkspaceSettingsState.Success->{
                    dismiss()
                    settingsViewModel.resetState()
                }
                is WorkspaceSettingsState.Error->{
                    Toast.makeText(context,"update failed , Try again",Toast.LENGTH_SHORT).show()
                    settingsViewModel.resetState()
                }
                else->{}
            }

        }

        binding.saveSignatureBtn.setOnClickListener {
            val bitmap=signPad.signatureBitmap
            val file= File(requireContext().filesDir,System.currentTimeMillis().toString()+".png")
            FileOutputStream(file).use{out->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val bitmapPath=file.absolutePath
            viewLifecycleOwner.lifecycleScope.launch{
                settingsViewModel.updateSignatureBlock(workspaceId,bitmapPath)
            }
        }
    }
}


