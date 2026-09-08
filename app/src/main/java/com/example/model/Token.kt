package com.example.model

/**
 * Lifecycle state of a Ludo token pawn.
 */
enum class TokenState {
  IN_YARD,
  ON_TRACK,
  IN_HOME_STRETCH,
  FINISHED
}

data class Token(
  val id: String,
  val playerIndex: Int,
  val tokenIndex: Int,
  val state: TokenState = TokenState.IN_YARD,
  val trackPos: Int = -1, // 0..35 on perimeter track
  val homeStretchPos: Int = -1, // 0..2 home corridor
  val stepsTaken: Int = 0, // 0 to 38
  val hasShield: Boolean = false,
  val isFrozen: Boolean = false
) {
  val isHome: Boolean get() = state == TokenState.FINISHED
  val isInYard: Boolean get() = state == TokenState.IN_YARD
}
