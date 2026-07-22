package com.learner.invoicegenerator.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentOnboardingTwoBinding

class OnboardingFragmentTwo: Fragment(R.layout.fragment_onboarding_two) {
    private var _binding: FragmentOnboardingTwoBinding? = null
    private val binding get()=_binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentOnboardingTwoBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButtontwo.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragmentTwo_to_onboardingFragmentThree)
        }
        binding.skipbtn.setOnClickListener{
            findNavController().navigate(R.id.action_onboardingFragmentTwo_to_logInFragment)
        }
    }
}

