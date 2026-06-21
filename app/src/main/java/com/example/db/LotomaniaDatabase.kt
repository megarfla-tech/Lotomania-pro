package com.example.db

import android.content.Context
import androidx.room.*

@Database(entities = [LotomaniaDraw::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class LotomaniaDatabase : RoomDatabase() {
    abstract fun dao(): LotomaniaDao

    companion object {
        @Volatile
        private var INSTANCE: LotomaniaDatabase? = null

        fun getDatabase(context: Context): LotomaniaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotomaniaDatabase::class.java,
                    "lotomania_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
