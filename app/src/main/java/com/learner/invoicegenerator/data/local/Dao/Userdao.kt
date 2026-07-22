package com.learner.invoicegenerator.data.local.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.learner.invoicegenerator.data.local.entity.User

@Dao
interface Userdao {
    @Insert
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?


}