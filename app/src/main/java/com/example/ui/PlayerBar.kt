package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Player

@Composable
fun PlayerBar(
  players: List<Player>,
  currentPlayerIndex: Int,
  modifier: Modifier = Modifier
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    players.forEachIndexed { index, player ->
      val isCurrentTurn = index == currentPlayerIndex
      PlayerCard(
        player = player,
        isCurrentTurn = isCurrentTurn,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun PlayerCard(
  player: Player,
  isCurrentTurn: Boolean,
  modifier: Modifier = Modifier
) {
  val pColor = player.quadrantColor.primary
  val borderColor by animateColorAsState(
    targetValue = if (isCurrentTurn) pColor else Color(0xFF334155),
    animationSpec = tween(300),
    label = "player_border"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF1E293B),
    shadowElevation = if (isCurrentTurn) 6.dp else 1.dp,
    modifier = modifier
      .clip(RoundedCornerShape(14.dp))
      .border(
        width = if (isCurrentTurn) 2.dp else 1.dp,
        color = borderColor,
        shape = RoundedCornerShape(14.dp)
      )
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .background(
          if (isCurrentTurn) {
            Brush.verticalGradient(
              listOf(pColor.copy(alpha = 0.22f), Color(0xFF0F172A))
            )
          } else {
            Brush.verticalGradient(
              listOf(Color(0xFF1E293B), Color(0xFF0F172A))
            )
          }
        )
        .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
      // Avatar with status ring and accessories
      val cfg = player.avatarConfig
      val auraCol = cfg.auraColor
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(auraCol.copy(alpha = 0.25f))
          .border(1.5.dp, auraCol, CircleShape)
      ) {
        val displayEmoji = if (player.isFrozenForTurn) "❄️" else cfg.baseEmoji.ifEmpty { player.avatar }
        Text(
          text = displayEmoji,
          fontSize = 16.sp
        )
        if (cfg.hat != null && !player.isFrozenForTurn) {
          Text(
            text = cfg.hat.emoji,
            fontSize = 11.sp,
            modifier = Modifier
              .align(Alignment.TopCenter)
              .offset(y = (-2).dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Player Name
      Text(
        text = player.name,
        fontWeight = if (isCurrentTurn) FontWeight.Bold else FontWeight.Medium,
        fontSize = 11.sp,
        color = if (isCurrentTurn) Color.White else Color(0xFF94A3B8),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(2.dp))

      // Finished tokens / Progress
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        val totalTokens = player.tokens.size
        val finishedCount = player.finishedCount

        Text(
          text = "$finishedCount/$totalTokens",
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (finishedCount == totalTokens) Color(0xFFFFD700) else Color(0xFF64748B)
        )
        if (finishedCount > 0) {
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "🏆", fontSize = 9.sp)
        }
      }

      // Tag for Bot or Human
      Text(
        text = if (player.isAi) "BOT" else "YOU",
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = if (player.isAi) Color(0xFF64748B) else pColor,
        letterSpacing = 0.8.sp
      )
    }
  }
}
