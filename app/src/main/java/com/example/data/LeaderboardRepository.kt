package com.example.data

import com.example.model.Player
import kotlinx.coroutines.flow.Flow

class LeaderboardRepository(private val dao: LeaderboardDao) {

  val topPlayers: Flow<List<LeaderboardEntry>> = dao.getAllOrdered()

  suspend fun ensureSeeded() {
    if (dao.getCount() == 0) {
      dao.insertAll(LeaderboardEntry.InitialSeed)
    }
  }

  suspend fun recordMatchOutcome(winnerIndex: Int, players: List<Player>) {
    players.forEach { player ->
      val playerId = "player_${player.name.lowercase().replace(" ", "_")}"
      val isWinner = player.index == winnerIndex
      val existing = dao.getEntryById(playerId)

      val newWins = (existing?.wins ?: 0) + (if (isWinner) 1 else 0)
      val newLosses = (existing?.losses ?: 0) + (if (!isWinner) 1 else 0)
      val totalMatches = newWins + newLosses
      val newWinRatio = if (totalMatches > 0) newWins.toFloat() / totalMatches else 0f
      val newKnockouts = (existing?.totalKnockouts ?: 0) + player.knockoutCount
      val newSuperpowers = (existing?.totalSuperpowersUsed ?: 0) + player.superpowersUsedCount

      val achievementsSet = existing?.achievementList?.toMutableSet() ?: mutableSetOf()

      // Evaluate Key Achievements
      if (newKnockouts > 0) {
        achievementsSet.add("First Knockout")
      }
      if (player.knockoutCount >= 2 || newKnockouts >= 8) {
        achievementsSet.add("Unstoppable")
      }
      if (player.superpowersUsedCount >= 2 || newSuperpowers >= 6) {
        achievementsSet.add("Superpower Streak")
      }
      if (isWinner) {
        achievementsSet.add("Victory Royale")
      }
      if (newWins >= 5) {
        achievementsSet.add("Grand Master")
      }

      val updatedEntry = LeaderboardEntry(
        id = playerId,
        playerName = player.name,
        avatar = player.avatarConfig.baseEmoji.ifEmpty { player.avatar },
        wins = newWins,
        losses = newLosses,
        matchesPlayed = totalMatches,
        winRatio = newWinRatio,
        totalKnockouts = newKnockouts,
        totalSuperpowersUsed = newSuperpowers,
        achievements = achievementsSet.joinToString("|"),
        lastPlayedTimestamp = System.currentTimeMillis()
      )

      dao.insertOrUpdate(updatedEntry)
    }
  }
}
