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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AvatarAccessory
import com.example.model.AvatarCatalog
import com.example.model.AvatarConfig
import com.example.model.Player

@Composable
fun AvatarCustomizerDialog(
  players: List<Player>,
  onDismiss: () -> Unit,
  onSaveAvatar: (playerIndex: Int, config: AvatarConfig) -> Unit
) {
  var selectedPlayerIdx by remember { mutableIntStateOf(0) }
  val targetPlayer = players.getOrElse(selectedPlayerIdx) { players.first() }

  var currentConfig by remember(selectedPlayerIdx) {
    mutableStateOf(targetPlayer.avatarConfig)
  }

  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Model", "Hats", "Glasses", "Aura")

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
          text = "✨ Avatar Studio",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Text(
          text = "Customize character, accessories & aura for the board",
          fontSize = 11.sp,
          color = Color(0xFF94A3B8)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Target Player Selector Chips
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          players.forEachIndexed { idx, p ->
            FilterChip(
              selected = selectedPlayerIdx == idx,
              onClick = { selectedPlayerIdx = idx },
              label = { Text(p.name, fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = p.quadrantColor.primary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Pawn Preview Card
        PawnAvatarPreview(
          config = currentConfig,
          playerColor = targetPlayer.quadrantColor.primary
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Row
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFF1E293B),
          contentColor = Color(0xFF00E5FF),
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = Color(0xFF00E5FF)
            )
          },
          modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  title,
                  fontSize = 11.sp,
                  fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                  color = if (selectedTab == index) Color.White else Color(0xFF94A3B8)
                )
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Content
        when (selectedTab) {
          0 -> {
            // Models Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              AvatarCatalog.PreMadeModels.chunked(4).forEach { row ->
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  row.forEach { model ->
                    val isSelected = currentConfig.modelId == model.id
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
                        .border(
                          1.5.dp,
                          if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                          RoundedCornerShape(12.dp)
                        )
                        .clickable {
                          currentConfig = currentConfig.copy(
                            modelId = model.id,
                            modelName = model.name,
                            baseEmoji = model.emoji,
                            auraColorHex = model.defaultAuraColor
                          )
                        }
                        .padding(vertical = 10.dp)
                    ) {
                      Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = model.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = model.name.split(" ").first(),
                          fontSize = 9.sp,
                          color = Color(0xFFCBD5E1)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
          1 -> {
            // Hats List
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              AvatarCatalog.AvailableHats.take(4).forEach { hat ->
                AccessoryChip(
                  accessory = hat,
                  isSelected = currentConfig.hat?.id == hat.id,
                  onSelect = {
                    currentConfig = currentConfig.copy(hat = if (hat.id == "none") null else hat)
                  },
                  modifier = Modifier.weight(1f)
                )
              }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              AvatarCatalog.AvailableHats.drop(4).forEach { hat ->
                AccessoryChip(
                  accessory = hat,
                  isSelected = currentConfig.hat?.id == hat.id,
                  onSelect = {
                    currentConfig = currentConfig.copy(hat = if (hat.id == "none") null else hat)
                  },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
          2 -> {
            // Glasses List
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              AvatarCatalog.AvailableGlasses.forEach { glasses ->
                AccessoryChip(
                  accessory = glasses,
                  isSelected = currentConfig.glasses?.id == glasses.id,
                  onSelect = {
                    currentConfig = currentConfig.copy(glasses = if (glasses.id == "none") null else glasses)
                  },
                  modifier = Modifier.weight(1f)
                )
              }
            }
          }
          3 -> {
            // Aura Colors
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              AvatarCatalog.AuraPalettes.chunked(3).forEach { row ->
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  row.forEach { (name, hex) ->
                    val isSelected = currentConfig.auraColorHex == hex
                    Box(
                      contentAlignment = Alignment.Center,
                      modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(hex))
                        .border(
                          2.dp,
                          if (isSelected) Color.White else Color.Transparent,
                          RoundedCornerShape(10.dp)
                        )
                        .clickable {
                          currentConfig = currentConfig.copy(auraColorHex = hex)
                        }
                    ) {
                      Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                      )
                    }
                  }
                }
              }
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
              onSaveAvatar(selectedPlayerIdx, currentConfig)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E5FF)),
            modifier = Modifier
              .weight(1f)
              .testTag("save_avatar_button")
          ) {
            Text("Save Avatar", fontWeight = FontWeight.Bold, color = Color.Black)
          }
        }
      }
    }
  }
}

@Composable
private fun AccessoryChip(
  accessory: AvatarAccessory,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B))
      .border(
        1.5.dp,
        if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
        RoundedCornerShape(12.dp)
      )
      .clickable(onClick = onSelect)
      .padding(vertical = 10.dp, horizontal = 4.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = accessory.emoji.ifEmpty { "🚫" },
        fontSize = 20.sp
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = accessory.name,
        fontSize = 9.sp,
        color = Color(0xFFCBD5E1),
        maxLines = 1
      )
    }
  }
}

@Composable
private fun PawnAvatarPreview(
  config: AvatarConfig,
  playerColor: Color
) {
  Surface(
    shape = RoundedCornerShape(18.dp),
    color = Color(0xFF1E293B),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.5.dp, config.auraColor.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .background(
          Brush.verticalGradient(
            listOf(
              config.auraColor.copy(alpha = 0.2f),
              Color(0xFF0F172A)
            )
          )
        )
        .padding(16.dp)
    ) {
      // 3D Pawn preview with Aura, Hat & Glasses
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
      ) {
        // Outer Aura Ring
        Box(
          modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .border(3.dp, config.auraColor, CircleShape)
            .background(config.auraColor.copy(alpha = 0.15f))
        )

        // Pawn Sphere
        Surface(
          shape = CircleShape,
          color = playerColor,
          shadowElevation = 8.dp,
          modifier = Modifier
            .size(52.dp)
            .border(2.dp, Color.White, CircleShape)
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
              .fillMaxWidth()
              .background(
                Brush.radialGradient(
                  listOf(Color.White.copy(alpha = 0.6f), playerColor)
                )
              )
          ) {
            // Base character emoji
            Text(
              text = config.baseEmoji,
              fontSize = 24.sp
            )

            // Glasses overlay
            if (config.glasses != null && config.glasses.emoji.isNotEmpty()) {
              Text(
                text = config.glasses.emoji,
                fontSize = 16.sp,
                modifier = Modifier.offset(y = 2.dp)
              )
            }
          }
        }

        // Hat accessory overlay sitting on head
        if (config.hat != null && config.hat.emoji.isNotEmpty()) {
          Text(
            text = config.hat.emoji,
            fontSize = 20.sp,
            modifier = Modifier
              .align(Alignment.TopCenter)
              .offset(y = (-4).dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "${config.modelName} ${config.hat?.emoji ?: ""} ${config.glasses?.emoji ?: ""}".trim(),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.White
      )
      Text(
        text = "Board Pawn Preview",
        fontSize = 10.sp,
        color = Color(0xFF94A3B8)
      )
    }
  }
}
