package com.example

import com.example.data.LeaderboardEntry
import com.example.model.AvatarCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AvatarAndLeaderboardTest {

  @Test
  fun testAvatarCatalogPreMadeModels() {
    val models = AvatarCatalog.PreMadeModels
    assertTrue(models.size >= 8)

    models.forEach { model ->
      assertTrue(model.id.isNotBlank())
      assertTrue(model.name.isNotBlank())
      assertTrue(model.emoji.isNotBlank())
    }
  }

  @Test
  fun testAvatarCatalogAccessories() {
    val hats = AvatarCatalog.AvailableHats
    val glasses = AvatarCatalog.AvailableGlasses
    val auras = AvatarCatalog.AuraPalettes

    assertTrue(hats.any { it.id == "crown" })
    assertTrue(glasses.any { it.id == "shades" })
    assertTrue(auras.isNotEmpty())
  }

  @Test
  fun testDefaultAvatarConfigsForFourPlayers() {
    for (i in 0 until 4) {
      val config = AvatarCatalog.defaultConfigForPlayer(i)
      assertNotNull(config)
      assertTrue(config.baseEmoji.isNotBlank())
      assertNotNull(config.auraColor)
    }
  }

  @Test
  fun testLeaderboardEntryCalculations() {
    val entry = LeaderboardEntry(
      id = "p1",
      playerName = "Nova Champion",
      avatar = "⚡",
      wins = 12,
      losses = 3,
      matchesPlayed = 15,
      winRatio = 12f / 15f,
      totalKnockouts = 18,
      totalSuperpowersUsed = 25,
      achievements = "First Knockout|Superpower Streak|Unstoppable|Victory Royale"
    )

    assertEquals(0.8f, entry.winRatio, 0.001f)
    assertEquals(4, entry.achievementList.size)
    assertTrue(entry.achievementList.contains("First Knockout"))
    assertTrue(entry.achievementList.contains("Superpower Streak"))
    assertTrue(entry.achievementList.contains("Unstoppable"))
    assertTrue(entry.achievementList.contains("Victory Royale"))
  }
}
