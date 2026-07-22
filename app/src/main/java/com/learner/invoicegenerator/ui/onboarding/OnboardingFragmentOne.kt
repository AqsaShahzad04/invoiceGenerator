package com.learner.invoicegenerator.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.ActivityMainBinding
import com.learner.invoicegenerator.databinding.FragmentOnboardingOneBinding
import java.util.zip.Inflater

class OnboardingFragmentOne: Fragment(R.layout.fragment_onboarding_one){
    private var _binding: FragmentOnboardingOneBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentOnboardingOneBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       binding.button.setOnClickListener {
           findNavController().navigate(R.id.action_onboardingFragmentOne_to_onboardingFragmentTwo)
       }
        binding.skipbtn.setOnClickListener{
            findNavController().navigate(R.id.action_onboardingFragmentOne_to_logInFragment)
        }
    }
}




