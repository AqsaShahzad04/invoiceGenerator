package com.learner.invoicegenerator.ui.auth.ViewModel

import com.learner.invoicegenerator.data.local.entity.User

sealed class SignUpState {
    object Loading : SignUpState()
    data class Success(val user: User) : SignUpState()
    data class Error(val message: String) : SignUpState()
}