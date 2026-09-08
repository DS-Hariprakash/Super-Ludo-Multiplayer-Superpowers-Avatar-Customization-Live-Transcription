package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaderboardDao {
  @Query("SELECT * FROM leaderboard ORDER BY winRatio DESC, wins DESC, matchesPlayed DESC")
  fun getAllOrdered(): Flow<List<LeaderboardEntry>>

  @Query("SELECT * FROM leaderboard WHERE id = :id LIMIT 1")
  suspend fun getEntryById(id: String): LeaderboardEntry?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdate(entry: LeaderboardEntry)

  @Insert(onConflict = OnConflictStrategy.IGNORE)
  suspend fun insertAll(entries: List<LeaderboardEntry>)

  @Query("SELECT COUNT(*) FROM leaderboard")
  suspend fun getCount(): Int

  @Query("DELETE FROM leaderboard")
  suspend fun clearAll()
}
