package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.LiveAudioState

@Composable
fun LiveTranscriptionBar(
  audioState: LiveAudioState,
  onToggleMic: () -> Unit,
  onQuickVoiceCommand: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }
  val isLive = audioState.isListening || audioState.isStreaming
  val accentColor by animateColorAsState(
    targetValue = if (isLive) Color(0xFFFF1744) else Color(0xFF00E5FF),
    animationSpec = tween(300),
    label = "audio_accent"
  )

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF0F172A),
    shadowElevation = 4.dp,
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
  ) {
    Column(
      modifier = Modifier
        .background(
          Brush.verticalGradient(
            listOf(
              accentColor.copy(alpha = 0.10f),
              Color(0xFF0F172A)
            )
          )
        )
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      // Main Single-Line Ticker Row
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Blinking LIVE Dot
          PulsingLiveDot(isLive = isLive)

          Spacer(modifier = Modifier.width(6.dp))

          // Equalizer Audio Waveform Bars
          AudioEqualizerBars(
            levels = audioState.audioWaveLevels,
            barColor = accentColor
          )

          Spacer(modifier = Modifier.width(8.dp))

          // Live transcript line or default prompt
          Text(
            text = if (audioState.activeTranscript.isNotBlank()) audioState.activeTranscript
                   else if (audioState.aiCommentary.isNotBlank()) audioState.aiCommentary
                   else if (isLive) "Listening to speech..."
                   else "Tap mic to stream audio",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (audioState.activeTranscript.isNotBlank()) Color.White else Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Expand / Collapse shortcuts toggle
          IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier.size(30.dp)
          ) {
            Icon(
              imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
              contentDescription = if (isExpanded) "Collapse Audio Panel" else "Expand Audio Panel",
              tint = Color(0xFF94A3B8),
              modifier = Modifier.size(18.dp)
            )
          }

          // Mic Button
          IconButton(
            onClick = onToggleMic,
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(accentColor.copy(alpha = 0.2f))
              .border(1.dp, accentColor, CircleShape)
              .testTag("toggle_mic_button")
          ) {
            Icon(
              imageVector = if (audioState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
              contentDescription = "Toggle Microphone",
              tint = if (audioState.isListening) Color(0xFFFF5252) else Color.White,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      // Expanded Content: AI commentary & Quick voice speech chips
      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 6.dp)) {
          if (audioState.aiCommentary.isNotBlank()) {
            Text(
              text = "🎙️ AI: ${audioState.aiCommentary}",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFFFD700),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
          }

          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
          ) {
            val voiceShortcuts = listOf(
              "Roll a six! 🎲",
              "Shields up! 🛡️",
              "Thunder dash! ⚡",
              "Knockout! 💥",
              "Victory sprint! 🏆"
            )
            voiceShortcuts.forEach { phrase ->
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(0xFF1E293B))
                  .border(0.8.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                  .clickable { onQuickVoiceCommand(phrase.replace(Regex("[^a-zA-Z0-9 ]"), "").trim()) }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = phrase,
                  fontSize = 10.sp,
                  color = Color(0xFFCBD5E1),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PulsingLiveDot(isLive: Boolean) {
  val transition = rememberInfiniteTransition(label = "live_pulse")
  val alpha by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot_alpha"
  )

  Box(
    modifier = Modifier
      .size(8.dp)
      .clip(CircleShape)
      .background(if (isLive) Color(0xFFFF1744).copy(alpha = alpha) else Color(0xFF64748B))
  )
}

@Composable
private fun AudioEqualizerBars(
  levels: List<Float>,
  barColor: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    modifier = Modifier.height(14.dp)
  ) {
    levels.forEach { level ->
      val animatedHeight by animateFloatAsState(
        targetValue = (level * 14f).coerceIn(3f, 14f),
        animationSpec = tween(120),
        label = "bar_h"
      )
      Box(
        modifier = Modifier
          .width(2.5.dp)
          .height(animatedHeight.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(barColor)
      )
    }
  }
}
