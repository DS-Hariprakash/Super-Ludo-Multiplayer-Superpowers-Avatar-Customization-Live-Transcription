package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leaderboard")
data class LeaderboardEntry(
  @PrimaryKey val id: String,
  val playerName: String,
  val avatar: String,
  val wins: Int = 0,
  val losses: Int = 0,
  val matchesPlayed: Int = 0,
  val winRatio: Float = 0f,
  val totalKnockouts: Int = 0,
  val totalSuperpowersUsed: Int = 0,
  val achievements: String = "", // Comma-separated list of achievements
  val lastPlayedTimestamp: Long = System.currentTimeMillis()
) {
  val achievementList: List<String>
    get() = if (achievements.isBlank()) emptyList() else achievements.split("|").filter { it.isNotBlank() }

  companion object {
    val InitialSeed = listOf(
      LeaderboardEntry(
        id = "champ_1",
        playerName = "Nova Striker",
        avatar = "⚡",
        wins = 12,
        losses = 3,
        matchesPlayed = 15,
        winRatio = 0.80f,
        totalKnockouts = 26,
        totalSuperpowersUsed = 34,
        achievements = "First Knockout|Superpower Streak|Unstoppable|Grand Champion"
      ),
      LeaderboardEntry(
        id = "champ_2",
        playerName = "Aegis Shield",
        avatar = "🛡️",
        wins = 9,
        losses = 4,
        matchesPlayed = 13,
        winRatio = 0.69f,
        totalKnockouts = 18,
        totalSuperpowersUsed = 29,
        achievements = "First Knockout|Shield Master|Superpower Streak"
      ),
      LeaderboardEntry(
        id = "champ_3",
        playerName = "Warp Runner",
        avatar = "🌀",
        wins = 8,
        losses = 5,
        matchesPlayed = 13,
        winRatio = 0.61f,
        totalKnockouts = 15,
        totalSuperpowersUsed = 22,
        achievements = "First Knockout|Teleport Ace"
      ),
      LeaderboardEntry(
        id = "champ_4",
        playerName = "Shadow Rex",
        avatar = "🐉",
        wins = 6,
        losses = 6,
        matchesPlayed = 12,
        winRatio = 0.50f,
        totalKnockouts = 14,
        totalSuperpowersUsed = 19,
        achievements = "First Knockout"
      )
    )
  }
}
