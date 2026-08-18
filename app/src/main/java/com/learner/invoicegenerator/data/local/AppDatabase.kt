package com.learner.invoicegenerator.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.learner.invoicegenerator.data.local.Dao.Clientdao
import com.learner.invoicegenerator.data.local.Dao.Userdao
import com.learner.invoicegenerator.data.local.Dao.ItemDao
import com.learner.invoicegenerator.data.local.Dao.WorkspaceDao
import com.learner.invoicegenerator.data.local.entity.User
import com.learner.invoicegenerator.data.local.entity.Workspace
import com.learner.invoicegenerator.data.local.entity.Client
import com.learner.invoicegenerator.data.local.entity.Item


@Database(entities = [User::class,Client::class,Item::class,Workspace::class], version = 6)
abstract class InvoiceDatabase: RoomDatabase() {
    abstract fun userDao(): Userdao
    abstract fun clientDao(): Clientdao
    abstract fun itemDao(): ItemDao

    abstract fun workspaceDao(): WorkspaceDao
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

