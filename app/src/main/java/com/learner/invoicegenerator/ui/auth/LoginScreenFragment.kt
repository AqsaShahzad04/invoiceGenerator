package com.learner.invoicegenerator.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.learner.invoicegenerator.R
import com.learner.invoicegenerator.data.local.DatabaseProvider
import com.learner.invoicegenerator.data.repository.UserRepository
import com.learner.invoicegenerator.databinding.FragmentLoginScreenBinding
import com.learner.invoicegenerator.data.local.SessionManager
import com.learner.invoicegenerator.ui.auth.ViewModel.LoginState
import com.learner.invoicegenerator.ui.auth.ViewModel.LoginViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.LoginViewModelFactory

class LoginScreenFragment : Fragment(R.layout.fragment_login_screen) {
    private var _binding: FragmentLoginScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao= DatabaseProvider.getDatabase(requireContext()).userDao()
        val repository= UserRepository(dao)
        val factory= LoginViewModelFactory(repository)
        val viewModel= ViewModelProvider(this, factory)[LoginViewModel::class.java]
        val sessionManager = SessionManager(requireContext())


       viewModel.loginState.observe(viewLifecycleOwner){state->
           when(state){
              is LoginState.Loading->{
                 binding.button2.isEnabled=false
                 binding.loginProgressBar.visibility=View.VISIBLE
              }
              is LoginState.Success->{
                  binding.button2.isEnabled=true
                  binding.loginProgressBar.visibility=View.GONE
                  sessionManager.createLoginSession(state.user.id)
                  findNavController().navigate(R.id.action_loginScreenFragment_to_homeScreenFragment)
              }
               is LoginState.Error->{
                   binding.button2.isEnabled=true
                   binding.loginProgressBar.visibility=View.GONE
                   binding.editTextTextEmailAddress.error=state.message
               }
           }
       }


        binding.button2.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

             if(email.isEmpty()||password.isEmpty()) {
                 binding.editTextTextEmailAddress.error = "All fields are required"
                 binding.editTextTextPassword.error = "All fields are required"
             }
            else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editTextTextEmailAddress.error = "Enter a valid email address"
                return@setOnClickListener
            }
            viewModel.loginUser(email, password)
        }


        binding.backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_onboardingFragmentThree)
        }
        binding.createone.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_signUpScreenFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding=null
    }
}