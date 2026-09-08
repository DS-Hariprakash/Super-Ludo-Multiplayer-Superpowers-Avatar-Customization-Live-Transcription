package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.GameEventNotification

@Composable
fun SuperpowerBanner(
  notification: GameEventNotification?,
  modifier: Modifier = Modifier
) {
  AnimatedVisibility(
    visible = notification != null,
    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
    modifier = modifier
  ) {
    if (notification != null) {
      val accentColor = notification.type?.badgeColor ?: Color(0xFF38BDF8)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .border(
            width = 1.5.dp,
            brush = Brush.horizontalGradient(
              listOf(accentColor, accentColor.copy(alpha = 0.3f))
            ),
            shape = RoundedCornerShape(16.dp)
          )
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .background(
              Brush.horizontalGradient(
                listOf(
                  accentColor.copy(alpha = 0.2f),
                  Color(0xFF0F172A)
                )
              )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(accentColor.copy(alpha = 0.25f))
              .border(1.dp, accentColor, RoundedCornerShape(10.dp))
          ) {
            Text(
              text = notification.type?.symbol ?: "✨",
              fontSize = 18.sp
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = notification.title,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color.White
            )
            Text(
              text = notification.subtitle,
              fontSize = 12.sp,
              color = Color(0xFFCBD5E1),
              lineHeight = 15.sp
            )
          }
        }
      }
    }
  }
}
