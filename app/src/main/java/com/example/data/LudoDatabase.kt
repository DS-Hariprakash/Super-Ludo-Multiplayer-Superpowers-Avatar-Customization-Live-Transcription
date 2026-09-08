package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LeaderboardEntry::class], version = 1, exportSchema = false)
abstract class LudoDatabase : RoomDatabase() {
  abstract fun leaderboardDao(): LeaderboardDao

  companion object {
    @Volatile
    private var INSTANCE: LudoDatabase? = null

    fun getDatabase(context: Context): LudoDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          LudoDatabase::class.java,
          "ludo_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
