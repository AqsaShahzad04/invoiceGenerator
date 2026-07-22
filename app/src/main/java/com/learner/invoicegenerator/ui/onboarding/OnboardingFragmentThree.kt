package com.learner.invoicegenerator.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.databinding.FragmentOnboardingThreeBinding

class OnboardingFragmentThree: Fragment(R.layout.fragment_onboarding_three) {
    private var _binding: FragmentOnboardingThreeBinding? = null
    private val binding get()=_binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentOnboardingThreeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
      binding.startButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragmentThree_to_login_screen_fragment)
        }
        binding.skipbtn.setOnClickListener{
            findNavController().navigate(R.id.action_onboardingFragmentThree_to_login_screen_fragment)
        }
    }
}
