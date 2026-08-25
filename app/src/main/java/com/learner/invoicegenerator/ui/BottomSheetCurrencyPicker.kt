package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.BottomSheetCurrencyPickerBinding
import com.learner.invoicegenerator.ui.adaptor.CurrencyAdaptor
import com.learner.invoicegenerator.ui.model.Currency
import com.learner.invoicegenerator.utils.CurrencyData

class BottomSheetCurrencyPicker: BottomSheetDialogFragment() {
    private var _binding: BottomSheetCurrencyPickerBinding?=null
    val binding get()=_binding!!


    val currencies= CurrencyData.currencies



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        _binding= BottomSheetCurrencyPickerBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view:View,savedInstanceState: Bundle?){
        super.onViewCreated(view,savedInstanceState)
        val sessionManager= SessionManager.getInstance(requireContext())
        binding.closebtn.setOnClickListener{
            dismiss()
        }
        var currentCurrency=sessionManager.getCurrencyCode()?:"USD"
        var adapter= CurrencyAdaptor(currencies,currentCurrency){currency->
            sessionManager.setCurrency(currency.code)
        }


        binding.currencyRv.layoutManager= LinearLayoutManager(context)
        binding.currencyRv.adapter=adapter

    }




    override fun onDestroyView(){
        super.onDestroyView()
        _binding=null
    }

}