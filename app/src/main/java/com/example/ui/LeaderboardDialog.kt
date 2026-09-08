package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.LeaderboardEntry

@Composable
fun LeaderboardDialog(
  entries: List<LeaderboardEntry>,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 20.dp,
      modifier = Modifier
        .fillMaxWidth()
        .height(580.dp)
        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), RoundedCornerShape(24.dp))
        .padding(2.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Header
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("🏆", fontSize = 24.sp)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SUPER LUDO LEADERBOARD",
            fontSize = 17.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFFFFD700),
            letterSpacing = 0.5.sp
          )
        }

        Text(
          text = "Ranked by Win Ratio, Total Wins & Matches Played",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Top 3 Podium
        if (entries.isNotEmpty()) {
          TopPodiumRow(entries = entries.take(3))
          Spacer(modifier = Modifier.height(12.dp))
        }

        // Leaderboard List
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          itemsIndexed(entries) { index, entry ->
            LeaderboardCard(rank = index + 1, entry = entry)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
          onClick = onDismiss,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("close_leaderboard_button")
        ) {
          Text("Close", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun TopPodiumRow(entries: List<LeaderboardEntry>) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier.fillMaxWidth()
  ) {
    // 2nd Place
    if (entries.size > 1) {
      PodiumPillar(
        entry = entries[1],
        rank = 2,
        medal = "🥈",
        accentColor = Color(0xFFE2E8F0),
        modifier = Modifier.weight(1f)
      )
    }

    // 1st Place
    if (entries.isNotEmpty()) {
      PodiumPillar(
        entry = entries[0],
        rank = 1,
        medal = "🥇",
        accentColor = Color(0xFFFFD700),
        modifier = Modifier.weight(1.15f)
      )
    }

    // 3rd Place
    if (entries.size > 2) {
      PodiumPillar(
        entry = entries[2],
        rank = 3,
        medal = "🥉",
        accentColor = Color(0xFFCD7F32),
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun PodiumPillar(
  entry: LeaderboardEntry,
  rank: Int,
  medal: String,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = Color(0xFF1E293B),
    modifier = modifier
      .border(1.5.dp, accentColor.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .background(
          Brush.verticalGradient(
            listOf(accentColor.copy(alpha = 0.2f), Color(0xFF0F172A))
          )
        )
        .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
      Text(medal, fontSize = 18.sp)
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = entry.avatar,
        fontSize = 20.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = entry.playerName,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        maxLines = 1
      )
      Text(
        text = "${(entry.winRatio * 100).toInt()}% WR",
        fontSize = 10.sp,
        fontWeight = FontWeight.Black,
        color = accentColor
      )
      Text(
        text = "${entry.wins}W / ${entry.losses}L",
        fontSize = 9.sp,
        color = Color(0xFF94A3B8)
      )
    }
  }
}

@Composable
private fun LeaderboardCard(
  rank: Int,
  entry: LeaderboardEntry
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFF1E293B),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .background(Color(0xFF1E293B))
        .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
      // Rank Badge
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(26.dp)
          .clip(CircleShape)
          .background(
            when (rank) {
              1 -> Color(0xFFFFD700)
              2 -> Color(0xFFE2E8F0)
              3 -> Color(0xFFCD7F32)
              else -> Color(0xFF334155)
            }
          )
      ) {
        Text(
          text = "#$rank",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (rank <= 3) Color.Black else Color.White
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Avatar
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(Color(0xFF0F172A))
          .border(1.dp, Color(0xFF00E5FF), CircleShape)
      ) {
        Text(entry.avatar, fontSize = 16.sp)
      }

      Spacer(modifier = Modifier.width(10.dp))

      // Name and Achievements
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = entry.playerName,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color.White
        )

        // Achievement badges
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier = Modifier.padding(top = 2.dp)
        ) {
          entry.achievementList.take(3).forEach { achievement ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF0F172A))
                .border(0.5.dp, Color(0xFF64748B), RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              Text(
                text = achievement,
                fontSize = 8.sp,
                color = Color(0xFF38BDF8),
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      // Stats Column
      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${(entry.winRatio * 100).toInt()}% WR",
          fontWeight = FontWeight.Black,
          fontSize = 12.sp,
          color = Color(0xFF00E676)
        )
        Text(
          text = "${entry.wins}W / ${entry.matchesPlayed}M",
          fontSize = 10.sp,
          color = Color(0xFFCBD5E1)
        )
        Text(
          text = "💥 ${entry.totalKnockouts}  ⚡ ${entry.totalSuperpowersUsed}",
          fontSize = 9.sp,
          color = Color(0xFF94A3B8)
        )
      }
    }
  }
}
