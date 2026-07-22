package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.repository.UserRepository
import kotlinx.coroutines.launch


class LoginViewModel(val repository: UserRepository): ViewModel() {
    private val _loginState= MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState
    fun loginUser(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                if (user != null && user.password == password) {
                    _loginState.value = LoginState.Success(user)
                } else {
                    _loginState.value = LoginState.Error("Invalid email or password")
                }
                }
            catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")

            }

        }

    }
}