package com.learner.invoicegenerator.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.databinding.FragmentOnboardingOneBinding

class OnboardingFragmentOne : Fragment(R.layout.fragment_onboarding_one) {
    private var _binding: FragmentOnboardingOneBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOnboardingOneBinding.inflate(inflater, container, false)
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
                navOptions {
                    popUpTo(R.id.nav_graph) { inclusive = true }
                }
            )
            return
        }

        binding.button.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingFragmentOne_to_onboardingFragmentTwo)
        }
        binding.skipbtn.setOnClickListener {
            sessionManager.setOnboardingCompleted()
            findNavController().navigate(
                R.id.action_onboardingFragmentOne_to_logInFragment,
                null,
                navOptions {
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