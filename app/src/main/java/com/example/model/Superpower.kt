package com.example.model

import androidx.compose.ui.graphics.Color

/**
 * Superpower types that can appear randomly on board tiles or be triggered by players.
 */
enum class SuperpowerType(
  val title: String,
  val symbol: String,
  val description: String,
  val badgeColor: Color
) {
  THUNDER_DASH(
    title = "Thunder Dash",
    symbol = "⚡",
    description = "Instantly surge forward +3 extra steps!",
    badgeColor = Color(0xFFFFD600)
  ),
  TITAN_SHIELD(
    title = "Titan Shield",
    symbol = "🛡️",
    description = "Energy shield protects token from being captured!",
    badgeColor = Color(0xFF00E5FF)
  ),
  WARP_PORTAL(
    title = "Warp Portal",
    symbol = "🌀",
    description = "Teleports token directly to the next Safe Zone!",
    badgeColor = Color(0xFFD500F9)
  ),
  SONIC_BLAST(
    title = "Sonic Blast",
    symbol = "💥",
    description = "Knocks back nearby enemy tokens by 2 spaces!",
    badgeColor = Color(0xFFFF3D00)
  ),
  CRYO_FREEZE(
    title = "Cryo Freeze",
    symbol = "❄️",
    description = "Freezes the next opponent for 1 turn!",
    badgeColor = Color(0xFF40C4FF)
  ),
  BONUS_ROLL(
    title = "Mega Die",
    symbol = "🎲",
    description = "Grants an immediate bonus roll!",
    badgeColor = Color(0xFF00E676)
  )
}

/**
 * An active superpower rune spawned on a board tile.
 */
data class SuperpowerRune(
  val tileIndex: Int,
  val type: SuperpowerType,
  val spawnTurn: Int = 0
)

/**
 * Positional match colors assigned to the 4 board quadrants for each match.
 */
data class MatchQuadrantColor(
  val name: String,
  val primary: Color,
  val secondary: Color,
  val glow: Color
)

/**
 * A curated theme palette for the match.
 */
data class MatchColorPalette(
  val themeName: String,
  val quadrants: List<MatchQuadrantColor>
)

object MatchColorPalettes {
  val NeonCyber = MatchColorPalette(
    themeName = "Cyber Neon",
    quadrants = listOf(
      MatchQuadrantColor("Neon Cyan", Color(0xFF00E5FF), Color(0xFF0091EA), Color(0x6600E5FF)),
      MatchQuadrantColor("Electric Pink", Color(0xFFFF1744), Color(0xFFC51162), Color(0x66FF1744)),
      MatchQuadrantColor("Solar Amber", Color(0xFFFFAB00), Color(0xFFFF6D00), Color(0x66FFAB00)),
      MatchQuadrantColor("Cyber Lime", Color(0xFF00E676), Color(0xFF00C853), Color(0x6600E676))
    )
  )

  val CosmicElemental = MatchColorPalette(
    themeName = "Cosmic Elements",
    quadrants = listOf(
      MatchQuadrantColor("Astral Purple", Color(0xFFD500F9), Color(0xFFAA00FF), Color(0x66D500F9)),
      MatchQuadrantColor("Solar Flare", Color(0xFFFF3D00), Color(0xFFDD2C00), Color(0x66FF3D00)),
      MatchQuadrantColor("Quantum Teal", Color(0xFF1DE9B6), Color(0xFF00BFA5), Color(0x661DE9B6)),
      MatchQuadrantColor("Deep Blue", Color(0xFF2979FF), Color(0xFF0059B2), Color(0x662979FF))
    )
  )

  val SynthwaveArcade = MatchColorPalette(
    themeName = "Synthwave Arcade",
    quadrants = listOf(
      MatchQuadrantColor("Sunset Coral", Color(0xFFFF5252), Color(0xFFD32F2F), Color(0x66FF5252)),
      MatchQuadrantColor("Mint Turbo", Color(0xFF69F0AE), Color(0xFF00E676), Color(0x6669F0AE)),
      MatchQuadrantColor("Gold Star", Color(0xFFFFD700), Color(0xFFFF9100), Color(0x66FFD700)),
      MatchQuadrantColor("Royal Indigo", Color(0xFF7C4DFF), Color(0xFF651FFF), Color(0x667C4DFF))
    )
  )

  val EmeraldRoyale = MatchColorPalette(
    themeName = "Emerald Royale",
    quadrants = listOf(
      MatchQuadrantColor("Ruby Red", Color(0xFFF43F5E), Color(0xFFBE123C), Color(0x66F43F5E)),
      MatchQuadrantColor("Sapphire Blue", Color(0xFF38BDF8), Color(0xFF0284C7), Color(0x6638BDF8)),
      MatchQuadrantColor("Emerald Jade", Color(0xFF10B981), Color(0xFF047857), Color(0x6610B981)),
      MatchQuadrantColor("Amber Topaz", Color(0xFFFBBF24), Color(0xFFD97706), Color(0x66FBBF24))
    )
  )

  val AllPalettes = listOf(NeonCyber, CosmicElemental, SynthwaveArcade, EmeraldRoyale)

  fun getRandomShuffledPalette(): MatchColorPalette {
    val basePalette = AllPalettes.random()
    return basePalette.copy(quadrants = basePalette.quadrants.shuffled())
  }
}
