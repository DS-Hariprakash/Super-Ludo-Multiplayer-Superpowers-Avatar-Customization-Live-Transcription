package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GamePhase
import com.example.game.GameState
import com.example.model.Player
import kotlinx.coroutines.launch

/**
 * Dedicated, authentic Ludo Dice Rolling Arena.
 * Features a spacious felt-styled rolling pit, prominent 3D dice, active player HUD,
 * and tactile roll triggers across the entire tray.
 */
@Composable
fun LudoDiceArena(
  state: GameState,
  onRollDice: () -> Unit,
  modifier: Modifier = Modifier
) {
  val currentPlayer = state.currentPlayer ?: return
  val pColor = currentPlayer.quadrantColor.primary
  val canRoll = state.phase == GamePhase.WAITING_TO_ROLL && !state.isDiceRolling
  val isAiTurn = currentPlayer.isAi

  // Glowing pulse when human turn to roll
  val infiniteTransition = rememberInfiniteTransition(label = "arena_glow")
  val arenaBorderAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "arena_border_alpha"
  )

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color(0xFF0F172A),
    shadowElevation = if (canRoll && !isAiTurn) 10.dp else 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .border(
        width = if (canRoll && !isAiTurn) 2.dp else 1.2.dp,
        color = if (canRoll && !isAiTurn) pColor.copy(alpha = arenaBorderAlpha) else Color(0xFF334155),
        shape = RoundedCornerShape(22.dp)
      )
      .clickable(
        enabled = canRoll && !isAiTurn,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = pColor),
        onClick = onRollDice
      )
      .testTag("ludo_dice_arena")
  ) {
    // Authentic felt rolling tray gradient
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0xFF1E293B),
              Color(0xFF0D1525)
            ),
            radius = 600f
          )
        )
        .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Left: Active Player Profile & Turn Status
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Player Avatar Badge with Aura
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(pColor.copy(alpha = 0.25f))
              .border(2.dp, pColor, CircleShape)
          ) {
            Text(
              text = currentPlayer.avatarConfig?.baseEmoji ?: if (currentPlayer.isAi) "🤖" else "👤",
              fontSize = 22.sp
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = currentPlayer.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              if (currentPlayer.isAi) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF334155))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text("BOT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8))
                }
              }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Contextual Turn Prompt
            Text(
              text = when {
                state.extraTurnGranted -> "🎉 BONUS ROLL!"
                state.consecutiveSixes > 0 -> "⚡ Streak: ${state.consecutiveSixes} Sixes!"
                state.phase == GamePhase.WAITING_TO_ROLL && isAiTurn -> "🤖 Rolling dice..."
                state.phase == GamePhase.WAITING_TO_ROLL -> "👉 Tap arena to roll!"
                state.phase == GamePhase.SELECTING_TOKEN && !isAiTurn -> "✨ Move glowing pawn"
                state.phase == GamePhase.SELECTING_TOKEN && isAiTurn -> "🤖 AI is moving..."
                state.phase == GamePhase.ANIMATING_MOVE -> "⚡ Moving pawn..."
                else -> "Round ${state.turnCount}"
              },
              fontSize = 11.sp,
              fontWeight = if (canRoll && !isAiTurn) FontWeight.Bold else FontWeight.Medium,
              color = if (canRoll && !isAiTurn) Color(0xFFFFD700) else Color(0xFF94A3B8)
            )
          }
        }

        // Center / Right: Spacious Tactile 3D Dice Pit
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Large 3D Dice
          InteractiveDice(
            value = state.diceValue,
            isRolling = state.isDiceRolling,
            canRoll = canRoll,
            isAiTurn = isAiTurn,
            accentColor = pColor,
            onRoll = onRollDice,
            diceSize = 68.dp
          )

          // Tactile Roll Action Pill when human turn
          if (canRoll && !isAiTurn) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                  Brush.linearGradient(
                    listOf(pColor, pColor.copy(alpha = 0.8f))
                  )
                )
                .clickable(onClick = onRollDice)
                .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "ROLL",
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Black,
                  letterSpacing = 1.sp,
                  color = Color.Black
                )
                Text(
                  text = "🎲 TAP",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Black.copy(alpha = 0.7f)
                )
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Authentic 3D Porcelain White Ludo Die with realistic indented colored pips
 * and ultra-fast physics tumbling.
 */
@Composable
fun InteractiveDice(
  value: Int,
  isRolling: Boolean,
  canRoll: Boolean,
  isAiTurn: Boolean,
  accentColor: Color,
  onRoll: () -> Unit,
  modifier: Modifier = Modifier,
  diceSize: Dp = 72.dp
) {
  val rotX = remember { Animatable(0f) }
  val rotY = remember { Animatable(0f) }
  val scale = remember { Animatable(1f) }

  // Quick pulsing scale when ready to roll
  val infiniteTransition = rememberInfiniteTransition(label = "dice_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(450, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  LaunchedEffect(isRolling) {
    if (isRolling) {
      launch {
        rotX.animateTo(
          targetValue = rotX.value + 720f,
          animationSpec = tween(durationMillis = 180, easing = LinearEasing)
        )
      }
      launch {
        rotY.animateTo(
          targetValue = rotY.value + 720f,
          animationSpec = tween(durationMillis = 180, easing = LinearEasing)
        )
      }
      launch {
        scale.animateTo(0.92f, tween(60))
        scale.animateTo(1.10f, tween(70))
        scale.animateTo(1.0f, tween(50))
      }
    } else {
      rotX.snapTo(0f)
      rotY.snapTo(0f)
    }
  }

  val effectiveScale = if (canRoll && !isRolling && !isAiTurn) pulseScale * scale.value else scale.value

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(diceSize + 12.dp)
      .graphicsLayer {
        scaleX = effectiveScale
        scaleY = effectiveScale
        rotationX = rotX.value
        rotationY = rotY.value
        cameraDistance = 14f * density
      }
      .drawBehind {
        // Soft radial aura matching player color
        if (canRoll) {
          drawCircle(
            brush = Brush.radialGradient(
              colors = listOf(accentColor.copy(alpha = 0.5f), Color.Transparent),
              center = center,
              radius = size.width * 0.65f
            )
          )
        }
      }
  ) {
    // 3D-styled physical ivory die cube
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      shadowElevation = if (canRoll) 10.dp else 4.dp,
      tonalElevation = 6.dp,
      modifier = Modifier
        .size(diceSize)
        .clip(RoundedCornerShape(16.dp))
        .border(
          width = 1.5.dp,
          brush = Brush.linearGradient(
            listOf(
              Color(0xFFFFFFFF),
              if (canRoll) accentColor else Color(0xFFCBD5E1)
            )
          ),
          shape = RoundedCornerShape(16.dp)
        )
        .clickable(
          enabled = canRoll && !isRolling && !isAiTurn,
          interactionSource = remember { MutableInteractionSource() },
          indication = ripple(bounded = true, color = accentColor),
          onClick = onRoll
        )
        .testTag("interactive_dice")
    ) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.linearGradient(
              colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF8FAFC),
                Color(0xFFE2E8F0)
              ),
              start = Offset(0f, 0f),
              end = Offset(120f, 120f)
            )
          )
          .padding(8.dp)
      ) {
        DicePips(
          pipCount = value.coerceIn(1, 6),
          pipColor = when (value) {
            1 -> Color(0xFFDC2626) // Classic Crimson Ace
            6 -> Color(0xFFD97706) // Golden Six
            else -> Color(0xFF0F172A) // Obsidian Navy
          }
        )
      }
    }
  }
}

/**
 * Compact Center Board Dice rendered directly in the central triangle of LudoBoardView.
 */
@Composable
fun CenterBoardDice(
  value: Int,
  isRolling: Boolean,
  canRoll: Boolean,
  isAiTurn: Boolean,
  accentColor: Color,
  onRoll: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .clickable(
        enabled = canRoll && !isRolling && !isAiTurn,
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = false, color = accentColor),
        onClick = onRoll
      )
  ) {
    InteractiveDice(
      value = value,
      isRolling = isRolling,
      canRoll = canRoll,
      isAiTurn = isAiTurn,
      accentColor = accentColor,
      onRoll = onRoll,
      diceSize = 48.dp
    )
  }
}

/**
 * Realistic standard dice pips layout (1..6).
 */
@Composable
private fun DicePips(
  pipCount: Int,
  pipColor: Color
) {
  val pipSize = 9.dp
  Box(modifier = Modifier.fillMaxSize()) {
    when (pipCount) {
      1 -> {
        // Large center ace pip
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(pipSize + 4.dp)
            .clip(CircleShape)
            .background(pipColor)
            .shadow(2.dp, CircleShape)
        )
      }
      2 -> {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
      }
      3 -> {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
      }
      4 -> {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
      }
      5 -> {
        Box(
          modifier = Modifier
            .align(Alignment.TopStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.Center)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .size(pipSize)
            .clip(CircleShape)
            .background(pipColor)
        )
      }
      6 -> {
        Row(
          modifier = Modifier.fillMaxSize(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
          ) {
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
          }
          Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
          ) {
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
            Box(modifier = Modifier.size(pipSize).clip(CircleShape).background(pipColor))
          }
        }
      }
    }
  }
}
