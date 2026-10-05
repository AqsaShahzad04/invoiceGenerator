package com.learner.invoicegenerator.ui.auth.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.learner.invoicegenerator.data.repository.UserRepository
import com.learner.invoicegenerator.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginViewModel(val repository: UserRepository): ViewModel() {
    private val _loginState= MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState
    fun loginUser(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val user = repository.getUserByEmail(email)
                val isValid = withContext(Dispatchers.Default) {
                    user != null && PasswordHasher.verify(password, user.password)
                }
                if (isValid) {
                    _loginState.value = LoginState.Success(user!!)
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