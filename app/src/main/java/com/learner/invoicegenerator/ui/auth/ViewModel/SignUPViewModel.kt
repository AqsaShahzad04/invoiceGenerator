package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.repository.UserRepository
import kotlinx.coroutines.launch

class SignUPViewModel(private val repository: UserRepository) : ViewModel()  {
    private val _signUpState = MutableLiveData<SignUpState>()
    val signUpState: LiveData<SignUpState> get() = _signUpState

    fun signUp(user: User) {
        _signUpState.value = SignUpState.Loading
        viewModelScope.launch {
            try {
                val existingUser = repository.getUserByEmail(user.email)
                if (existingUser != null) {
                    _signUpState.value = SignUpState.Error("An account with this email already exists")
                    return@launch
                }
                repository.insertUser(user)
                _signUpState.value = SignUpState.Success(user)
            } catch (e: Exception) {
                _signUpState.value = SignUpState.Error(e.message ?: "Sign Up failed")
            }
        }
    }

}