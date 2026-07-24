package com.learner.invoicegenerator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.Dao.Userdao
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.local.entity.Client


@Database(entities = [User::class,Client::class], version = 3)
abstract class InvoiceDatabase: RoomDatabase() {
    abstract fun userDao(): Userdao
    abstract fun clientDao(): Clientdao
}
object DatabaseProvider{
    @Volatile
    private var INSTANCE: InvoiceDatabase? = null

    fun getDatabase(context: Context): InvoiceDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                InvoiceDatabase::class.java,
                "app_database"
            )
                .fallbackToDestructiveMigration(false)
                .build()
            INSTANCE = instance
            instance
        }

    }

}

