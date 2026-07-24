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
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.repository.UserRepository
import com.learner.invoicegenerator.databinding.FragmentSignupScreenBinding
import com.learner.invoicegenerator.ui.auth.ViewModel.SignUPViewModel
import com.learner.invoicegenerator.ui.auth.ViewModel.SignUpState
import com.learner.invoicegenerator.ui.auth.ViewModel.SignUpViewModelFactory

class SignupScreenFragment: Fragment(R.layout.fragment_signup_screen) {
    private var _binding: FragmentSignupScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding= FragmentSignupScreenBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dao= DatabaseProvider.getDatabase(requireContext()).userDao()
        val repository= UserRepository(dao)
        val factory= SignUpViewModelFactory(repository)
        val viewModel= ViewModelProvider(this,factory)[SignUPViewModel::class.java]


        viewModel.signUpState.observe(viewLifecycleOwner){state->
            when(state){
                is SignUpState.Loading->{
                    binding.createAccountbtn.isEnabled=false
                    binding.signupProgressBar.visibility=View.VISIBLE
                }
                is SignUpState.Success->{
                    binding.createAccountbtn.isEnabled = true
                    binding.signupProgressBar.visibility = View.GONE
                    findNavController().navigate(R.id.action_signUpScreenFragment_to_homeScreenFragment)
                }
                is SignUpState.Error->{
                    binding.createAccountbtn.isEnabled = true
                    binding.signupProgressBar.visibility = View.GONE
                    binding.email.error=state.message
                }
            }
        }


        binding.createAccountbtn.setOnClickListener {
            val name = binding.fullName.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()
            val confirmPassword = binding.confirmPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                binding.fullName.error = "All fields are required"
                binding.email.error = "All fields are required"
                binding.editTextTextPassword.error = "All fields are required"
                binding.confirmPassword.error = "All fields are required"
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                binding.confirmPassword.error = "Password does not match"
                return@setOnClickListener
            }
            val emailPattern = android.util.Patterns.EMAIL_ADDRESS
            if (!emailPattern.matcher(email).matches()) {
                binding.email.error = "Enter a valid email address"
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.editTextTextPassword.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }


            val user= User(name = name, email = email, password = password)
            viewModel.signUp(user)


        }

        binding.backbtn.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_onboardingFragmentThree)
        }
        binding.signin.setOnClickListener {
            findNavController().navigate(R.id.action_signUpScreenFragment_to_loginScreenFragment)
        }
    }

   override fun onDestroyView(){
        super.onDestroyView()
        _binding=null
    }
}

