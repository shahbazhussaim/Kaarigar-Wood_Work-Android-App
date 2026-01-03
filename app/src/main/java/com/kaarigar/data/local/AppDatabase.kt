package com.kaarigar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kaarigar.data.local.dao.ProductDao
import com.kaarigar.data.local.dao.RequestDao
import com.kaarigar.data.local.dao.UserDao
import com.kaarigar.data.local.entity.ProductEntity
import com.kaarigar.data.local.entity.RequestEntity
import com.kaarigar.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, ProductEntity::class, RequestEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun requestDao(): RequestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kaarigar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
