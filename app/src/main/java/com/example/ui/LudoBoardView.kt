package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BoardCoord
import com.example.game.LudoBoardLogic
import com.example.model.Player
import com.example.model.SuperpowerRune
import com.example.model.Token
import com.example.model.TokenState

@Composable
fun LudoBoardView(
  players: List<Player>,
  superpowerRunes: List<SuperpowerRune>,
  highlightedTokenIds: Set<String>,
  onTokenClicked: (Token) -> Unit,
  diceValue: Int = 1,
  isDiceRolling: Boolean = false,
  canRollDice: Boolean = false,
  isAiTurn: Boolean = false,
  activePlayerColor: Color = Color(0xFFFFD700),
  onDiceRoll: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  BoxWithConstraints(
    modifier = modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(24.dp))
      .background(Color(0xFF0F172A))
      .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
      .padding(4.dp)
  ) {
    val boardSize = maxWidth
    val cellSize = boardSize / LudoBoardLogic.GRID_SIZE.toFloat()

    // 1. Board Background and Grid Tracks Canvas
    BoardGridCanvas(
      players = players,
      cellSize = cellSize,
      superpowerRunes = superpowerRunes,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Interactive Superpower Runes Overlay
    superpowerRunes.forEach { rune ->
      val coord = LudoBoardLogic.TrackCoords.getOrNull(rune.tileIndex)
      if (coord != null) {
        SuperpowerRuneView(
          rune = rune,
          cellSize = cellSize,
          coord = coord
        )
      }
    }

    // 3. Center Board Interactive Dice Overlay
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(cellSize * 3f)
        .offset(x = cellSize * 4f, y = cellSize * 4f)
    ) {
      CenterBoardDice(
        value = diceValue,
        isRolling = isDiceRolling,
        canRoll = canRollDice,
        isAiTurn = isAiTurn,
        accentColor = activePlayerColor,
        onRoll = onDiceRoll
      )
    }

    // 4. Tokens Layer
    allTokensFrom(players).forEach { token ->
      val coord = LudoBoardLogic.getTokenCoord(token)
      val isHighlighted = highlightedTokenIds.contains(token.id)
      val player = players.getOrNull(token.playerIndex)

      TokenPawnView(
        token = token,
        player = player,
        isHighlighted = isHighlighted,
        cellSize = cellSize,
        coord = coord,
        onClick = { onTokenClicked(token) }
      )
    }
  }
}

private fun allTokensFrom(players: List<Player>): List<Token> {
  return players.flatMap { it.tokens }
}

/**
 * Draws the complete high-polish 11x11 board canvas:
 * - 4 Corner Base Yards with match colors
 * - 4 Arms containing perimeter tracks and home runways
 * - Center 3x3 Home Victory Zone
 */
@Composable
private fun BoardGridCanvas(
  players: List<Player>,
  cellSize: Dp,
  superpowerRunes: List<SuperpowerRune>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val cellPx = cellSize.toPx()

    // A. Draw Corner Bases (4x4 cells each)
    val cornerBases = listOf(
      // Top-Left (Quadrant 0)
      Triple(0, 0, players.getOrNull(0)?.quadrantColor?.primary ?: Color(0xFF00E5FF)),
      // Top-Right (Quadrant 1)
      Triple(0, 7, players.getOrNull(1)?.quadrantColor?.primary ?: Color(0xFFFF1744)),
      // Bottom-Right (Quadrant 2)
      Triple(7, 7, players.getOrNull(2)?.quadrantColor?.primary ?: Color(0xFFFFAB00)),
      // Bottom-Left (Quadrant 3)
      Triple(7, 0, players.getOrNull(3)?.quadrantColor?.primary ?: Color(0xFF00E676))
    )

    cornerBases.forEachIndexed { qIdx, (startRow, startCol, color) ->
      val x = startCol * cellPx
      val y = startRow * cellPx
      val baseSize = 4 * cellPx

      // Outer base rounded square
      drawRoundRect(
        brush = Brush.linearGradient(
          colors = listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0.12f)),
          start = Offset(x, y),
          end = Offset(x + baseSize, y + baseSize)
        ),
        topLeft = Offset(x + 4f, y + 4f),
        size = Size(baseSize - 8f, baseSize - 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx())
      )
      drawRoundRect(
        color = color.copy(alpha = 0.7f),
        topLeft = Offset(x + 4f, y + 4f),
        size = Size(baseSize - 8f, baseSize - 8f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx(), 18.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
      )

      // Inner Yard Circle Pad
      drawCircle(
        color = color.copy(alpha = 0.18f),
        radius = baseSize * 0.34f,
        center = Offset(x + baseSize / 2, y + baseSize / 2)
      )
      drawCircle(
        color = color.copy(alpha = 0.45f),
        radius = baseSize * 0.34f,
        center = Offset(x + baseSize / 2, y + baseSize / 2),
        style = Stroke(width = 1.5.dp.toPx())
      )
    }

    // B. Draw Perimeter Track Tiles (36 cells)
    LudoBoardLogic.TrackCoords.forEachIndexed { index, coord ->
      val x = coord.col * cellPx
      val y = coord.row * cellPx

      val isSafe = LudoBoardLogic.SafeTrackIndices.contains(index)
      val startPlayerIdx = LudoBoardLogic.StartTrackIndices.indexOf(index)

      val tileColor = if (startPlayerIdx != -1) {
        players.getOrNull(startPlayerIdx)?.quadrantColor?.primary?.copy(alpha = 0.45f)
          ?: Color(0xFF1E293B)
      } else if (isSafe) {
        Color(0xFF334155)
      } else {
        Color(0xFF1E293B)
      }

      drawRoundRect(
        color = tileColor,
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(cellPx - 4f, cellPx - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
      )
      drawRoundRect(
        color = if (isSafe) Color(0xFFFFD700).copy(alpha = 0.6f) else Color(0xFF334155),
        topLeft = Offset(x + 2f, y + 2f),
        size = Size(cellPx - 4f, cellPx - 4f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = if (isSafe) 1.5.dp.toPx() else 0.8.dp.toPx())
      )

      // Star icon mark on safe tiles
      if (isSafe && startPlayerIdx == -1) {
        drawCircle(
          color = Color(0xFFFFD700).copy(alpha = 0.85f),
          radius = cellPx * 0.18f,
          center = Offset(x + cellPx / 2f, y + cellPx / 2f)
        )
      }
    }

    // C. Draw Home Corridors (3 tiles each for each player)
    players.forEachIndexed { pIdx, player ->
      val corridor = LudoBoardLogic.HomeCorridors.getOrNull(pIdx) ?: return@forEachIndexed
      val pColor = player.quadrantColor.primary
      corridor.forEach { coord ->
        val x = coord.col * cellPx
        val y = coord.row * cellPx
        drawRoundRect(
          color = pColor.copy(alpha = 0.55f),
          topLeft = Offset(x + 2f, y + 2f),
          size = Size(cellPx - 4f, cellPx - 4f),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )
        drawRoundRect(
          color = pColor,
          topLeft = Offset(x + 2f, y + 2f),
          size = Size(cellPx - 4f, cellPx - 4f),
          cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx(), 6.dp.toPx()),
          style = Stroke(width = 1.2.dp.toPx())
        )
      }
    }

    // D. Center Home 3x3 Area (rows 4..6, cols 4..6)
    val centerLeft = 4 * cellPx
    val centerTop = 4 * cellPx
    val centerDim = 3 * cellPx
    val centerMidX = centerLeft + centerDim / 2f
    val centerMidY = centerTop + centerDim / 2f

    // 4 Triangular sectors pointing to center
    players.forEachIndexed { pIdx, player ->
      val color = player.quadrantColor.primary
      val path = Path().apply {
        moveTo(centerMidX, centerMidY)
        when (pIdx) {
          0 -> { // Top sector
            lineTo(centerLeft, centerTop)
            lineTo(centerLeft + centerDim, centerTop)
          }
          1 -> { // Right sector
            lineTo(centerLeft + centerDim, centerTop)
            lineTo(centerLeft + centerDim, centerTop + centerDim)
          }
          2 -> { // Bottom sector
            lineTo(centerLeft + centerDim, centerTop + centerDim)
            lineTo(centerLeft, centerTop + centerDim)
          }
          3 -> { // Left sector
            lineTo(centerLeft, centerTop + centerDim)
            lineTo(centerLeft, centerTop)
          }
        }
        close()
      }
      drawPath(path, color = color.copy(alpha = 0.4f))
      drawPath(path, color = color, style = Stroke(width = 1.2.dp.toPx()))
    }

    // Golden center finish medallion
    drawCircle(
      brush = Brush.radialGradient(
        colors = listOf(Color(0xFFFFD700), Color(0xFFB8860B)),
        center = Offset(centerMidX, centerMidY),
        radius = cellPx * 0.65f
      ),
      radius = cellPx * 0.65f,
      center = Offset(centerMidX, centerMidY)
    )
    drawCircle(
      color = Color.White.copy(alpha = 0.8f),
      radius = cellPx * 0.65f,
      center = Offset(centerMidX, centerMidY),
      style = Stroke(width = 2.dp.toPx())
    )
  }
}

/**
 * Superpower Rune View with pulsing energy aura and icon.
 */
@Composable
private fun SuperpowerRuneView(
  rune: SuperpowerRune,
  cellSize: Dp,
  coord: BoardCoord
) {
  val infiniteTransition = rememberInfiniteTransition(label = "rune_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.9f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "rune_scale"
  )

  val left = cellSize * coord.col
  val top = cellSize * coord.row

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .offset(x = left, y = top)
      .size(cellSize)
  ) {
    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier
        .size(cellSize * 0.78f)
        .graphicsLayer {
          scaleX = pulseScale
          scaleY = pulseScale
        }
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            listOf(
              rune.type.badgeColor.copy(alpha = 0.8f),
              Color(0xFF0F172A)
            )
          )
        )
        .border(1.5.dp, rune.type.badgeColor, CircleShape)
    ) {
      Text(
        text = rune.type.symbol,
        fontSize = (cellSize.value * 0.42f).sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

/**
 * 3D-styled Token Pawn with interactive animations, player custom avatar, accessories, shield, and highlight rings.
 */
@Composable
private fun TokenPawnView(
  token: Token,
  player: Player?,
  isHighlighted: Boolean,
  cellSize: Dp,
  coord: BoardCoord,
  onClick: () -> Unit
) {
  val playerColor = player?.quadrantColor?.primary ?: Color.White
  val avatarConfig = player?.avatarConfig
  val auraColor = avatarConfig?.auraColor ?: playerColor

  val infiniteTransition = rememberInfiniteTransition(label = "token_pulse")
  val pulseRing by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.35f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "ring_scale"
  )

  val left = cellSize * coord.col
  val top = cellSize * coord.row

  Box(
    contentAlignment = Alignment.Center,
    modifier = Modifier
      .offset(x = left, y = top)
      .size(cellSize)
      .clickable(
        enabled = isHighlighted,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, radius = cellSize * 0.6f),
        onClick = onClick
      )
      .testTag("token_${token.id}")
  ) {
    // Custom Player Aura Ring
    Box(
      modifier = Modifier
        .size(cellSize * 0.85f)
        .clip(CircleShape)
        .border(1.5.dp, auraColor.copy(alpha = 0.5f), CircleShape)
    )

    // Pulsing highlight ring when movable
    if (isHighlighted) {
      Box(
        modifier = Modifier
          .size(cellSize * 0.95f)
          .graphicsLayer {
            scaleX = pulseRing
            scaleY = pulseRing
          }
          .clip(CircleShape)
          .border(2.5.dp, auraColor, CircleShape)
      )
    }

    // Titan Shield Aura (Energy forcefield)
    if (token.hasShield) {
      Box(
        modifier = Modifier
          .size(cellSize * 0.92f)
          .clip(CircleShape)
          .border(2.dp, Color(0xFF00E5FF), CircleShape)
          .drawBehind {
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(Color(0x6600E5FF), Color.Transparent),
                center = center,
                radius = size.width * 0.5f
              )
            )
          }
      )
    }

    // Pawn Body Sphere
    Surface(
      shape = CircleShape,
      color = playerColor,
      shadowElevation = if (isHighlighted) 8.dp else 3.dp,
      modifier = Modifier
        .size(cellSize * 0.66f)
        .border(1.5.dp, Color.White.copy(alpha = 0.9f), CircleShape)
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.radialGradient(
              colors = listOf(Color.White.copy(alpha = 0.55f), playerColor),
              center = Offset(12f, 12f),
              radius = 24f
            )
          )
      ) {
        if (token.isHome) {
          Text(
            text = "🏆",
            fontSize = (cellSize.value * 0.32f).sp,
            fontWeight = FontWeight.Black
          )
        } else {
          // Dynamic Player Base Avatar Emoji
          val baseGlyph = avatarConfig?.baseEmoji ?: player?.avatar ?: "${token.tokenIndex + 1}"
          Text(
            text = baseGlyph,
            fontSize = (cellSize.value * 0.34f).sp
          )

          // Glasses accessory
          if (avatarConfig?.glasses != null && avatarConfig.glasses.emoji.isNotEmpty()) {
            Text(
              text = avatarConfig.glasses.emoji,
              fontSize = (cellSize.value * 0.22f).sp,
              modifier = Modifier.offset(y = 1.dp)
            )
          }
        }
      }
    }

    // Hat accessory sitting proudly on top of the pawn!
    if (!token.isHome && avatarConfig?.hat != null && avatarConfig.hat.emoji.isNotEmpty()) {
      Text(
        text = avatarConfig.hat.emoji,
        fontSize = (cellSize.value * 0.28f).sp,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .offset(y = (-3).dp)
      )
    }
  }
}
