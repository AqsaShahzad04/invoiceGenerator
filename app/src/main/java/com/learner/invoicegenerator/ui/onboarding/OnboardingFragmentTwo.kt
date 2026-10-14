package com.learner.invoicegenerator.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentOnboardingTwoBinding

class OnboardingFragmentTwo : Fragment(R.layout.fragment_onboarding_two) {
    private var _binding: FragmentOnboardingTwoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingTwoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sessionManager = SessionManager.getInstance(requireContext())
        if (sessionManager.isOnboardingCompleted()) {
            val destination = if (sessionManager.isLoggedIn()) R.id.homeScreenFragment else R.id.loginScreenFragment
            findNavController().navigate(
                destination,
                null,
                androidx.navigation.navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
            return
        }

        binding.backButtontwo.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.continueButton.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragmentTwo_to_onboardingFragmentThree)
        }
        binding.skipbtn.setOnClickListener {
            sessionManager.setOnboardingCompleted()
            findNavController().navigate(
                R.id.action_onboardingFragmentTwo_to_logInFragment,
                null,
                androidx.navigation.navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

