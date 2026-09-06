package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentNewinvoiceAddDetailsBinding

class NewInvoiceAddDetailsFragment: Fragment(R.layout.fragment_newinvoice_add_details) {

    private var _binding: FragmentNewinvoiceAddDetailsBinding?=null
    val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentNewinvoiceAddDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}