package com.contoh.scentapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.contoh.scentapp.data.local.dao.CartDao
import com.contoh.scentapp.data.local.dao.SearchHistoryDao
import com.contoh.scentapp.data.local.entity.CartItemEntity
import com.contoh.scentapp.data.local.entity.SearchHistoryEntity

@Database(
    entities = [CartItemEntity::class, SearchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun cartDao(): CartDao
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scentapp_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
