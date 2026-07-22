package com.learner.invoicegenerator.data.repository

import com.learner.invoicegenerator.data.local.Dao.Userdao
import com.learner.invoicegenerator.data.local.entity.User

class UserRepository(private val userDao: Userdao) {

    suspend fun insertUser(user: User) {
      userDao.insertUser(user)
    }
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
    }
