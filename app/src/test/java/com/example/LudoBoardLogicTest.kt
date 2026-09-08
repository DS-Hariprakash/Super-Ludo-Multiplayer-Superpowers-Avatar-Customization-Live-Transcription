package com.example

import com.example.game.LudoBoardLogic
import com.example.model.Token
import com.example.model.TokenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LudoBoardLogicTest {

  @Test
  fun testTrackCoordinatesCount() {
    assertEquals(36, LudoBoardLogic.TrackCoords.size)
    assertEquals(4, LudoBoardLogic.StartTrackIndices.size)
    assertEquals(8, LudoBoardLogic.SafeTrackIndices.size)
  }

  @Test
  fun testStartIndicesSymmetry() {
    // Each start index should be spaced by 9 tiles
    val starts = LudoBoardLogic.StartTrackIndices
    assertEquals(6, starts[0])
    assertEquals(15, starts[1])
    assertEquals(24, starts[2])
    assertEquals(33, starts[3])

    for (i in 0 until 3) {
      assertEquals(9, starts[i + 1] - starts[i])
    }
  }

  @Test
  fun testTokenInYardRequiresSixToMove() {
    val token = Token(
      id = "p0_t0",
      playerIndex = 0,
      tokenIndex = 0,
      state = TokenState.IN_YARD
    )

    assertFalse(LudoBoardLogic.canMoveToken(token, 1))
    assertFalse(LudoBoardLogic.canMoveToken(token, 5))
    assertTrue(LudoBoardLogic.canMoveToken(token, 6))
  }

  @Test
  fun testTokenMoveFromYardEntersStartTrack() {
    val token = Token(
      id = "p0_t0",
      playerIndex = 0,
      tokenIndex = 0,
      state = TokenState.IN_YARD
    )

    val updated = LudoBoardLogic.applyMove(token, 6)
    assertEquals(TokenState.ON_TRACK, updated.state)
    assertEquals(6, updated.trackPos)
    assertEquals(0, updated.stepsTaken)
  }

  @Test
  fun testTokenMovementAlongTrack() {
    val token = Token(
      id = "p0_t0",
      playerIndex = 0,
      tokenIndex = 0,
      state = TokenState.ON_TRACK,
      trackPos = 6,
      stepsTaken = 0
    )

    val moved = LudoBoardLogic.applyMove(token, 4)
    assertEquals(TokenState.ON_TRACK, moved.state)
    assertEquals(10, moved.trackPos)
    assertEquals(4, moved.stepsTaken)
  }

  @Test
  fun testSafeHavens() {
    // Start tiles must be safe
    assertTrue(LudoBoardLogic.SafeTrackIndices.contains(6))
    assertTrue(LudoBoardLogic.SafeTrackIndices.contains(15))
    assertTrue(LudoBoardLogic.SafeTrackIndices.contains(24))
    assertTrue(LudoBoardLogic.SafeTrackIndices.contains(33))
  }
}
