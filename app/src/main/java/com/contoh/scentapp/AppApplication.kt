package com.contoh.scentapp

import android.app.Application
import com.contoh.scentapp.data.local.AppDatabase
import com.contoh.scentapp.data.repository.CartRepository
import com.contoh.scentapp.data.repository.SearchHistoryRepository
import com.google.firebase.FirebaseApp

class AppApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var searchHistoryRepository: SearchHistoryRepository
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        database = AppDatabase.getDatabase(this)
        
        // Inisialisasi Repositori yang butuh DAO
        CartRepository.getInstance(database.cartDao())
        searchHistoryRepository = SearchHistoryRepository(database.searchHistoryDao())
    }
}
