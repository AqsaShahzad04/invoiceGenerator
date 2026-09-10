package com.learner.invoicegenerator.ui

import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.os.Bundle
import android.Manifest
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.learner.invoicegenerator.ApiCalling.RetrofitInstance
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.data.local.entity.InvoiceItemLine
import com.learner.invoicegenerator.databinding.BottomSheetScanBarcodeBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.InvoiceViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.ItemViewModel
import kotlinx.coroutines.launch


class BottomSheetScanBarcode: BottomSheetDialogFragment() {
    private var _binding: BottomSheetScanBarcodeBinding?=null
    val binding get()=_binding!!
    private val scanner = BarcodeScanning.getClient()

    val invoiceViewModel: InvoiceViewModel by activityViewModels ()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= BottomSheetScanBarcodeBinding.inflate(inflater,container,false)
        return binding.root
    }
    private lateinit var scanAnimator: ObjectAnimator
    var barcodeAlreadyFound=false

    @OptIn(ExperimentalGetImage::class)
    fun startCamera(){
        val cameraProviderFuture= ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(requireContext())){imageProxy->
               if(barcodeAlreadyFound){
                   imageProxy.close()
                   return@setAnalyzer
               }
                val mediaImage=imageProxy.image
                if(mediaImage!=null){
                    val inputImage=InputImage.fromMediaImage(mediaImage,imageProxy.imageInfo.rotationDegrees)
                    scanner.process(inputImage).addOnSuccessListener {barcodes->
                        for(barcode in barcodes){
                            val rawValue=barcode.rawValue
                            if (rawValue != null && !barcodeAlreadyFound) {
                               barcodeAlreadyFound=true
                                requireActivity().runOnUiThread {
                                    binding.barcodeNumInput.setText(rawValue)
                                    scanAnimator.cancel()
                                }
                            }
                        }

                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
                }
                else{
                    imageProxy.close()
                }
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(this, cameraSelector, preview,imageAnalysis)
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private val permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()){granted->
        if(granted){
            startCamera()
        }
        else{
            Toast.makeText(requireContext(),"Camera permission is required to scan barcode", Toast.LENGTH_SHORT).show()
        }

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemViewModel: ItemViewModel by activityViewModels()
        val sessionManager= SessionManager.getInstance(requireContext())
        val workspaceId=sessionManager.getActiveWorkspaceId()
        binding.closebtn.setOnClickListener {
            dismiss()
        }
        val mode=arguments?.getString("mode")
        if(mode=="invoice"){
           binding.scanBarcodeHeading.setText("Scan to bill")
            binding.subtitle.setText("Scan each item to add it to the invoice. scan item\n again adds quantity")
            binding.useCodeBtn.setText("Add")
            binding.additemUsingBarcodeBtn.visibility=View.VISIBLE

            binding.useCodeBtn.setOnClickListener {
                val code=binding.barcodeNumInput.text.toString()
                if(code==null){
                    binding.barcodeNumInput.error="scan the barcode or enter the code mmanually here"
                }
                else{
                    viewLifecycleOwner.lifecycleScope.launch{
                      val item=itemViewModel.searchItemBycode(code,workspaceId)
                        if(item!=null){
                           val itemExistInInvoice= invoiceViewModel.selectedItems.value.find{it.itemId==item.id}
                            if(itemExistInInvoice!=null){
                                val currentList= invoiceViewModel.selectedItems.value.toMutableList()
                               val index=currentList.indexOf(itemExistInInvoice)
                                currentList[index]= InvoiceItemLine(
                                    invoiceId = 0,
                                    itemId = item.id,
                                    itemName = item.itemName,
                                    unitPrice = item.price,
                                    itemQuantity =itemExistInInvoice.itemQuantity+1,
                                    itemUnit = itemExistInInvoice.itemUnit
                                )
                                invoiceViewModel.updateSelectedItems(currentList)
                            }
                            else{
                                val item= InvoiceItemLine(
                                    invoiceId = 0,
                                    itemId = item.id,
                                    itemName = item.itemName,
                                    unitPrice = item.price,
                                    itemQuantity =1.0,
                                    itemUnit = item.unit
                                )
                                invoiceViewModel.addToSelectedItems(item)
                            }
                        }
                        else{
                            binding.BarcodeErrorText.setText("Not in catalogue. $code")
                          binding.warningBox.visibility=View.VISIBLE
                            binding.closeWarningBtn.setOnClickListener {
                                binding.warningBox.visibility=View.GONE
                            }
                            binding.addProductBtn.setOnClickListener {
                                findNavController().navigate()
                            }

                        }
                    }

                }
            }

        }

        binding.useCodeBtn.setOnClickListener {
            val code=binding.barcodeNumInput.text.toString()
            if(code==null){
                binding.barcodeNumInput.error="scan the barcode or enter the code mmanually here"
            }
            else{
                itemViewModel.fetchdetailsFromApi(code,workspaceId)
                dismiss()
            }

        }



        if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
            startCamera()
        }
        else{
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val distanceInPx = 176 * resources.displayMetrics.density
         scanAnimator= ObjectAnimator.ofFloat(binding.scanLine,"translationY",0f,distanceInPx)
        scanAnimator.duration=1500
        scanAnimator.repeatMode= ObjectAnimator.REVERSE
        scanAnimator.repeatCount= ObjectAnimator.INFINITE
        scanAnimator.start()

    }
    override fun onDestroyView(){
        super.onDestroyView()
        scanAnimator.cancel()
    }
}


