package com.learner.invoicegenerator.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentAddEditClientBinding

class AddEditClientFragment: Fragment(R.layout.fragment_add_edit_client)  {

    private var _binding: FragmentAddEditClientBinding? = null
    private val binding get()=_binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        _binding= FragmentAddEditClientBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
    val businessName=binding.BusinessName


}