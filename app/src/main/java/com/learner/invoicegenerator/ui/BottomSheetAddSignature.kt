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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class BottomSheetAddSignature(
    private val existingSignaturePath: String? = null
) : BottomSheetDialogFragment() {
    private var _binding: BottomSheetAddSignatureBinding? = null
    val binding get() = _binding!!
    val settingsViewModel: WorkspaceSettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomSheetAddSignatureBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var isSubmitting = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        settingsViewModel.resetState()

        val sessionManager = SessionManager.getInstance(requireContext())
        val workspaceId = sessionManager.getActiveWorkspaceId()

        val signPad = binding.signaturePad

        val hasSignature = !existingSignaturePath.isNullOrEmpty()
        if (hasSignature) {
            binding.signatureHeading.text = "Update signature"
            binding.removeSignatureBtn.visibility = View.VISIBLE
            val file = File(existingSignaturePath)
            if (file.exists()) {
                try {
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    if (bitmap != null) {
                        signPad.signatureBitmap = bitmap
                        binding.signatureLine.visibility = View.GONE
                        binding.signaturePlaceholder.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            binding.signatureHeading.text = "Add signature"
            binding.removeSignatureBtn.visibility = View.GONE
        }

        binding.closebtn.setOnClickListener {
            dismiss()
        }

        signPad.setOnSignedListener(object : SignaturePad.OnSignedListener {
            override fun onStartSigning() {
                binding.signatureLine.visibility = View.GONE
                binding.signaturePlaceholder.visibility = View.GONE
            }

            override fun onSigned() {

            }

            override fun onClear() {
                binding.signatureLine.visibility = View.VISIBLE
                binding.signaturePlaceholder.visibility = View.VISIBLE
            }
        })

        binding.clearSignatureBtn.setOnClickListener {
            signPad.clear()
        }

        binding.removeSignatureBtn.setOnClickListener {
            isSubmitting = true
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateSignatureBlock(workspaceId, "")
            }
        }

        settingsViewModel.state.observe(viewLifecycleOwner) { state ->
            if (!isSubmitting) return@observe
            when (state) {
                is WorkspaceSettingsState.Success -> {
                    isSubmitting = false
                    dismiss()
                    settingsViewModel.resetState()
                }
                is WorkspaceSettingsState.Error -> {
                    isSubmitting = false
                    Toast.makeText(context, "Update failed, try again", Toast.LENGTH_SHORT).show()
                    settingsViewModel.resetState()
                }
                else -> {}
            }
        }

        binding.saveSignatureBtn.setOnClickListener {
            if (signPad.isEmpty) {
                Toast.makeText(context, "Please add a signature before saving", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val bitmap = signPad.signatureBitmap
            val file = File(requireContext().filesDir, System.currentTimeMillis().toString() + ".png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val bitmapPath = file.absolutePath
            isSubmitting = true
            viewLifecycleOwner.lifecycleScope.launch {
                settingsViewModel.updateSignatureBlock(workspaceId, bitmapPath)
            }
        }
    }
}


