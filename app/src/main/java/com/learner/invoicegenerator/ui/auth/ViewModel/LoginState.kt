package com.learner.invoicegenerator.ui.auth.ViewModel

import com.learner.invoicegenerator.data.local.entity.User

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()

}