package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.MatchColorPalette
import com.example.model.MatchColorPalettes
import com.example.model.Player
import com.example.model.SuperpowerType

@Composable
fun NewMatchDialog(
  currentPalette: MatchColorPalette,
  onDismiss: () -> Unit,
  onStartMatch: (playerCount: Int, tokensPerPlayer: Int, humanCount: Int, palette: MatchColorPalette) -> Unit
) {
  var selectedPlayerCount by remember { mutableIntStateOf(4) }
  var selectedTokensCount by remember { mutableIntStateOf(2) }
  var selectedHumanCount by remember { mutableIntStateOf(2) }
  var selectedPalette by remember { mutableStateOf(currentPalette) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
        .padding(2.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🎮 Match Setup",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Customize mini Ludo multiplayer settings",
          fontSize = 12.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Players Count (2, 3, 4)
        Text(
          text = "Number of Players",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = Color(0xFFCBD5E1),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
        ) {
          listOf(2, 3, 4).forEach { count ->
            FilterChip(
              selected = selectedPlayerCount == count,
              onClick = {
                selectedPlayerCount = count
                if (selectedHumanCount > count) selectedHumanCount = count
              },
              label = { Text("$count Players") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF0284C7),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Human Players (Pass & Play)
        Text(
          text = "Human Players (Pass & Play)",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = Color(0xFFCBD5E1),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
        ) {
          (1..selectedPlayerCount).forEach { humans ->
            FilterChip(
              selected = selectedHumanCount == humans,
              onClick = { selectedHumanCount = humans },
              label = { Text(if (humans == 1) "1 (vs AI)" else "$humans") },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF7C3AED),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tokens per player (2 for quick mini-game, 4 for classic)
        Text(
          text = "Tokens per Player",
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = Color(0xFFCBD5E1),
          modifier = Modifier.fillMaxWidth()
        )
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
        ) {
          listOf(2 to "2 (Fast Mini Game)", 4 to "4 (Standard)").forEach { (count, label) ->
            FilterChip(
              selected = selectedTokensCount == count,
              onClick = { selectedTokensCount = count },
              label = { Text(label, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF10B981),
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Match Color Palette
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Match Colors: ${selectedPalette.themeName}",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color(0xFFCBD5E1)
          )
          Text(
            text = "🎲 Randomize",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF38BDF8),
            modifier = Modifier
              .clickable {
                selectedPalette = MatchColorPalettes.getRandomShuffledPalette()
              }
              .padding(4.dp)
          )
        }

        // Palette color swatches
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
        ) {
          selectedPalette.quadrants.forEach { quad ->
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .weight(1f)
                .height(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(quad.primary)
            ) {
              Text(
                text = quad.name.take(4),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
          ) {
            Text("Cancel", color = Color(0xFF94A3B8))
          }
          Button(
            onClick = {
              onStartMatch(selectedPlayerCount, selectedTokensCount, selectedHumanCount, selectedPalette)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            modifier = Modifier
              .weight(1f)
              .testTag("start_match_button")
          ) {
            Text("Start Match", fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun SuperpowerGuideDialog(
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 16.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(2.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
        .padding(2.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "⚡ Superpowers & Rules",
          fontSize = 19.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Collect mystery runes on the track to trigger superpowers!",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Superpower cards
        SuperpowerType.entries.forEach { power ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF1E293B))
              .border(1.dp, power.badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .padding(10.dp)
          ) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(power.badgeColor.copy(alpha = 0.25f))
                .border(1.dp, power.badgeColor, CircleShape)
            ) {
              Text(text = power.symbol, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = power.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = power.badgeColor
              )
              Text(
                text = power.description,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Mini Ludo Quick Rules",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color.White,
          modifier = Modifier.fillMaxWidth()
        )
        Text(
          text = "• Roll a 6 to summon a token from your yard\n" +
            "• Rolling a 6 grants a bonus turn!\n" +
            "• Capturing an opponent sends them home and grants a bonus turn!\n" +
            "• ⭐ Star tiles and Start tiles are Safe Havens\n" +
            "• Bring all your pawns to the center victory podium to win!",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8),
          lineHeight = 16.sp,
          modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Got It!", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
fun VictoryDialog(
  winner: Player,
  turnCount: Int,
  themeName: String,
  onPlayAgain: () -> Unit
) {
  Dialog(onDismissRequest = {}) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 24.dp,
      modifier = Modifier
        .fillMaxWidth()
        .border(2.5.dp, Color(0xFFFFD700), RoundedCornerShape(28.dp))
        .padding(2.dp)
    ) {
      Column(
        modifier = Modifier
          .background(
            Brush.verticalGradient(
              listOf(
                Color(0x33FFD700),
                Color(0xFF0F172A)
              )
            )
          )
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🏆",
          fontSize = 52.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "MATCH VICTORY!",
          fontSize = 22.sp,
          fontWeight = FontWeight.Black,
          color = Color(0xFFFFD700),
          letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "${winner.name} Wins the Match!",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Match Stats Card
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = Color(0xFF1E293B),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Total Turns", fontSize = 11.sp, color = Color(0xFF94A3B8))
              Text("$turnCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("Theme", fontSize = 11.sp, color = Color(0xFF94A3B8))
              Text(themeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = winner.quadrantColor.primary)
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = onPlayAgain,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("play_again_button")
        ) {
          Text("Play Again", fontWeight = FontWeight.Bold, color = Color.Black)
        }
      }
    }
  }
}
