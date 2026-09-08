package com.example.game

import com.example.model.MatchColorPalette
import com.example.model.Player
import com.example.model.SuperpowerRune
import com.example.model.SuperpowerType
import com.example.model.Token

enum class GamePhase {
  WAITING_TO_ROLL,
  SELECTING_TOKEN,
  ANIMATING_MOVE,
  GAME_OVER
}

data class GameEventNotification(
  val id: Long = System.currentTimeMillis(),
  val title: String,
  val subtitle: String,
  val type: SuperpowerType? = null
)

data class GameState(
  val players: List<Player> = emptyList(),
  val currentPlayerIndex: Int = 0,
  val phase: GamePhase = GamePhase.WAITING_TO_ROLL,
  val diceValue: Int = 1,
  val isDiceRolling: Boolean = false,
  val consecutiveSixes: Int = 0,
  val extraTurnGranted: Boolean = false,
  val superpowerRunes: List<SuperpowerRune> = emptyList(),
  val activeNotification: GameEventNotification? = null,
  val matchPalette: MatchColorPalette = com.example.model.MatchColorPalettes.NeonCyber,
  val winner: Player? = null,
  val turnCount: Int = 1,
  val isAiThinking: Boolean = false,
  val highlightedTokenIds: Set<String> = emptySet(),
  val previewPath: List<BoardCoord> = emptyList(),
  val tokensPerPlayer: Int = 2,
  val fastSpeed: Boolean = true,
  val matchLog: List<String> = emptyList()
) {
  val currentPlayer: Player? get() = players.getOrNull(currentPlayerIndex)
  val isCurrentPlayerAi: Boolean get() = currentPlayer?.isAi == true
}
