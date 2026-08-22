package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.learner.invoicegenerator.databinding.BottomSheetCurrencyPickerBinding
import com.learner.invoicegenerator.ui.adaptor.CurrencyAdaptor
import com.learner.invoicegenerator.ui.model.Currency

class BottomSheetCurrencyPicker: BottomSheetDialogFragment() {
    private var _binding: BottomSheetCurrencyPickerBinding?=null
    val binding get()=_binding!!


        val currencies = listOf(
            Currency("PKR", "Pakistani Rupee · Rs", false),
            Currency("USD", "US Dollar · $", true),
            Currency("EUR", "Euro · €", false),
            Currency("GBP", "Pound Sterling · £", false),
            Currency("INR", "Indian Rupee · ₹", false),
            Currency("AED", "UAE Dirham · AED", false),
            Currency("SAR", "Saudi Riyal · SAR", false),
            Currency("QAR", "Qatari Riyal · QAR", false),
            Currency("BDT", "Bangladeshi Taka · ৳", false),
            Currency("LKR", "Sri Lankan Rupee · Rs", false),
            Currency("NGN", "Nigerian Naira · ₦", false),
            Currency("KES", "Kenyan Shilling · KSh", false),
            Currency("ZAR", "South African Rand · R", false),
            Currency("EGP", "Egyptian Pound · E£", false),
            Currency("TRY", "Turkish Lira · ₺", false),
            Currency("CAD", "Canadian Dollar · C$", false),
            Currency("AUD", "Australian Dollar · A$", false),
            Currency("CHF", "Swiss Franc · CHF", false),
            Currency("SGD", "Singapore Dollar · S$", false),
            Currency("MYR", "Malaysian Ringgit · RM", false),
            Currency("IDR", "Indonesian Rupiah · Rp", false),
            Currency("PHP", "Philippine Peso · ₱", false),
            Currency("THB", "Thai Baht · ฿", false),
            Currency("JPY", "Japanese Yen · ¥", false),
            Currency("CNY", "Chinese Yuan · ¥", false),
            Currency("BRL", "Brazilian Real · R$", false),
            Currency("MXN", "Mexican Peso · Mex$", false)
    )




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
        binding.closebtn.setOnClickListener{
            dismiss()
        }
        var adapter= CurrencyAdaptor(currencies)
        binding.currencyRv.layoutManager= LinearLayoutManager(context)
        binding.currencyRv.adapter=adapter

    }

    override fun onDestroyView(){
        super.onDestroyView()
        _binding=null
    }

}