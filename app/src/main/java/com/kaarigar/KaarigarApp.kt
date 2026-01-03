package com.kaarigar

import android.app.Application
import com.kaarigar.data.local.AppDatabase

class KaarigarApp : Application() {
    
    // Manual DI - Singleton Instance of Database
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        // Initialize other libs if needed
        com.google.firebase.firestore.FirebaseFirestore.setLoggingEnabled(true)
    }
}
