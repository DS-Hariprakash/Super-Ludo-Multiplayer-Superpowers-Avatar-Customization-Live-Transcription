package com.example.game

import com.example.model.Player
import com.example.model.SuperpowerRune
import com.example.model.SuperpowerType
import com.example.model.Token
import com.example.model.TokenState

data class BoardCoord(val row: Int, val col: Int)

object LudoBoardLogic {
  const val GRID_SIZE = 11
  const val TRACK_COUNT = 36
  const val HOME_CORRIDOR_LENGTH = 3
  const val TOTAL_STEPS_TO_HOME = TRACK_COUNT - 1 + HOME_CORRIDOR_LENGTH + 1 // 35 track steps + 3 corridor + 1 finish = 39

  // Perimeter track coordinates (0..35) in clockwise sequence
  val TrackCoords: List<BoardCoord> = listOf(
    BoardCoord(3, 4),  // 0
    BoardCoord(2, 4),  // 1 (Safe Star)
    BoardCoord(1, 4),  // 2
    BoardCoord(0, 4),  // 3
    BoardCoord(0, 5),  // 4 (Top cap - P0 home entrance)
    BoardCoord(0, 6),  // 5
    BoardCoord(1, 6),  // 6 (P0 Start Tile - Safe)
    BoardCoord(2, 6),  // 7
    BoardCoord(3, 6),  // 8
    BoardCoord(4, 7),  // 9
    BoardCoord(4, 8),  // 10 (Safe Star)
    BoardCoord(4, 9),  // 11
    BoardCoord(4, 10), // 12
    BoardCoord(5, 10), // 13 (Right cap - P1 home entrance)
    BoardCoord(6, 10), // 14
    BoardCoord(6, 9),  // 15 (P1 Start Tile - Safe)
    BoardCoord(6, 8),  // 16
    BoardCoord(6, 7),  // 17
    BoardCoord(7, 6),  // 18
    BoardCoord(8, 6),  // 19 (Safe Star)
    BoardCoord(9, 6),  // 20
    BoardCoord(10, 6), // 21
    BoardCoord(10, 5), // 22 (Bottom cap - P2 home entrance)
    BoardCoord(10, 4), // 23
    BoardCoord(9, 4),  // 24 (P2 Start Tile - Safe)
    BoardCoord(8, 4),  // 25
    BoardCoord(7, 4),  // 26
    BoardCoord(6, 3),  // 27
    BoardCoord(6, 2),  // 28 (Safe Star)
    BoardCoord(6, 1),  // 29
    BoardCoord(6, 0),  // 30
    BoardCoord(5, 0),  // 31 (Left cap - P3 home entrance)
    BoardCoord(4, 0),  // 32
    BoardCoord(4, 1),  // 33 (P3 Start Tile - Safe)
    BoardCoord(4, 2),  // 34
    BoardCoord(4, 3)   // 35
  )

  // Start track indices for players 0..3
  val StartTrackIndices = listOf(6, 15, 24, 33)

  // Safe tiles (cannot be captured here)
  val SafeTrackIndices = setOf(1, 6, 10, 15, 19, 24, 28, 33)

  // Home corridor entrance tiles on perimeter
  val HomeEntranceIndices = listOf(4, 13, 22, 31)

  // Home corridor coordinates for each player (length 3 heading towards center)
  val HomeCorridors: List<List<BoardCoord>> = listOf(
    // P0: Top player heading down to center
    listOf(BoardCoord(1, 5), BoardCoord(2, 5), BoardCoord(3, 5)),
    // P1: Right player heading left to center
    listOf(BoardCoord(5, 9), BoardCoord(5, 8), BoardCoord(5, 7)),
    // P2: Bottom player heading up to center
    listOf(BoardCoord(9, 5), BoardCoord(8, 5), BoardCoord(7, 5)),
    // P3: Left player heading right to center
    listOf(BoardCoord(5, 1), BoardCoord(5, 2), BoardCoord(5, 3))
  )

  // Center Home coordinates
  val CenterHomeCoords = listOf(
    BoardCoord(4, 5), // P0 Home
    BoardCoord(5, 6), // P1 Home
    BoardCoord(6, 5), // P2 Home
    BoardCoord(5, 4)  // P3 Home
  )

  // Yard coordinates for pawns (up to 4 tokens per player)
  val YardCoords: List<List<BoardCoord>> = listOf(
    // Quadrant 0 (Top-Left)
    listOf(BoardCoord(1, 1), BoardCoord(1, 2), BoardCoord(2, 1), BoardCoord(2, 2)),
    // Quadrant 1 (Top-Right)
    listOf(BoardCoord(1, 8), BoardCoord(1, 9), BoardCoord(2, 8), BoardCoord(2, 9)),
    // Quadrant 2 (Bottom-Right)
    listOf(BoardCoord(8, 8), BoardCoord(8, 9), BoardCoord(9, 8), BoardCoord(9, 9)),
    // Quadrant 3 (Bottom-Left)
    listOf(BoardCoord(8, 1), BoardCoord(8, 2), BoardCoord(9, 1), BoardCoord(9, 2))
  )

  /**
   * Determine the BoardCoord for a token based on its state and positions.
   */
  fun getTokenCoord(token: Token): BoardCoord {
    return when (token.state) {
      TokenState.IN_YARD -> {
        val yardList = YardCoords[token.playerIndex]
        yardList[token.tokenIndex % yardList.size]
      }
      TokenState.ON_TRACK -> {
        TrackCoords[token.trackPos % TrackCoords.size]
      }
      TokenState.IN_HOME_STRETCH -> {
        val corridor = HomeCorridors[token.playerIndex]
        corridor[token.homeStretchPos.coerceIn(0, corridor.lastIndex)]
      }
      TokenState.FINISHED -> {
        CenterHomeCoords[token.playerIndex]
      }
    }
  }

  /**
   * Check if a token can make a legal move with the rolled dice value.
   */
  fun canMoveToken(token: Token, diceRoll: Int): Boolean {
    if (token.isHome || token.isFrozen) return false

    if (token.state == TokenState.IN_YARD) {
      // Must roll a 6 to leave yard
      return diceRoll == 6
    }

    val totalSteps = token.stepsTaken + diceRoll
    return totalSteps <= TOTAL_STEPS_TO_HOME
  }

  /**
   * Calculate the preview destination and intermediate steps for a token move.
   */
  fun calculatePath(token: Token, diceRoll: Int): List<BoardCoord> {
    if (!canMoveToken(token, diceRoll)) return emptyList()

    val path = mutableListOf<BoardCoord>()
    val playerIdx = token.playerIndex
    val startTrack = StartTrackIndices[playerIdx]

    if (token.state == TokenState.IN_YARD) {
      // Step directly onto start track tile
      path.add(TrackCoords[startTrack])
      return path
    }

    var currentSteps = token.stepsTaken
    var currentTrack = token.trackPos
    var currentHomeStretch = token.homeStretchPos

    for (step in 1..diceRoll) {
      currentSteps++
      if (currentSteps < 36) {
        // Still on perimeter track
        currentTrack = (currentTrack + 1) % TRACK_COUNT
        path.add(TrackCoords[currentTrack])
      } else {
        // In home stretch or entering it
        val homeStep = currentSteps - 36
        if (homeStep < HOME_CORRIDOR_LENGTH) {
          path.add(HomeCorridors[playerIdx][homeStep])
        } else {
          path.add(CenterHomeCoords[playerIdx])
        }
      }
    }

    return path
  }

  /**
   * Applies the move to a token and returns updated token state.
   */
  fun applyMove(token: Token, diceRoll: Int): Token {
    val playerIdx = token.playerIndex
    val startTrack = StartTrackIndices[playerIdx]

    if (token.state == TokenState.IN_YARD) {
      return token.copy(
        state = TokenState.ON_TRACK,
        trackPos = startTrack,
        stepsTaken = 0
      )
    }

    val newSteps = token.stepsTaken + diceRoll
    return if (newSteps < 35) {
      val newTrack = (token.trackPos + diceRoll) % TRACK_COUNT
      token.copy(
        trackPos = newTrack,
        stepsTaken = newSteps
      )
    } else if (newSteps < 35 + HOME_CORRIDOR_LENGTH) {
      val homePos = newSteps - 35
      token.copy(
        state = TokenState.IN_HOME_STRETCH,
        homeStretchPos = homePos,
        stepsTaken = newSteps
      )
    } else {
      token.copy(
        state = TokenState.FINISHED,
        stepsTaken = TOTAL_STEPS_TO_HOME
      )
    }
  }

  /**
   * Find if this landing captures an opposing token.
   * Safe tiles and Titan Shields prevent capture.
   */
  fun findCapturableToken(
    activeToken: Token,
    allPlayers: List<Player>
  ): Token? {
    if (activeToken.state != TokenState.ON_TRACK) return null
    if (activeToken.trackPos in SafeTrackIndices) return null

    for (player in allPlayers) {
      if (player.index == activeToken.playerIndex) continue
      for (enemy in player.tokens) {
        if (enemy.state == TokenState.ON_TRACK && enemy.trackPos == activeToken.trackPos) {
          return enemy
        }
      }
    }
    return null
  }

  /**
   * Find next safe haven tile index ahead on track.
   */
  fun getNextSafeHaven(fromTrack: Int): Int {
    val sortedSafe = SafeTrackIndices.sorted()
    for (safe in sortedSafe) {
      if (safe > fromTrack) return safe
    }
    return sortedSafe.first()
  }
}
