package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GamePhase
import com.example.game.LudoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LudoGameScreen(
  viewModel: LudoViewModel = viewModel()
) {
  val state by viewModel.gameState.collectAsState()
  val liveAudioState by viewModel.liveAudioState.collectAsState()
  val topLeaderboard by viewModel.topLeaderboard.collectAsState()

  var showNewMatchDialog by remember { mutableStateOf(false) }
  var showRulesDialog by remember { mutableStateOf(false) }
  var showLeaderboardDialog by remember { mutableStateOf(false) }
  var showAvatarCustomizerDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              contentAlignment = Alignment.Center,
              modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                  Brush.linearGradient(
                    listOf(Color(0xFF00E5FF), Color(0xFFD500F9))
                  )
                )
            ) {
              Text("🎲", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "SUPER LUDO",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = Color.White
              )
              Text(
                text = "🎨 ${state.matchPalette.themeName} Match",
                fontSize = 10.sp,
                color = state.currentPlayer?.quadrantColor?.primary ?: Color(0xFF38BDF8)
              )
            }
          }
        },
        actions = {
          // Dynamic Leaderboard Button
          IconButton(
            onClick = { showLeaderboardDialog = true },
            modifier = Modifier.testTag("leaderboard_button")
          ) {
            Icon(
              imageVector = Icons.Default.Leaderboard,
              contentDescription = "Leaderboard",
              tint = Color(0xFFFFD700)
            )
          }

          // Avatar Studio Customizer Button
          IconButton(
            onClick = { showAvatarCustomizerDialog = true },
            modifier = Modifier.testTag("avatar_studio_button")
          ) {
            Icon(
              imageVector = Icons.Default.Face,
              contentDescription = "Avatar Studio",
              tint = Color(0xFF00E5FF)
            )
          }

          // Shuffle Match Colors Button
          IconButton(
            onClick = { viewModel.shuffleMatchColors() },
            modifier = Modifier.testTag("shuffle_colors_button")
          ) {
            Icon(
              imageVector = Icons.Default.ColorLens,
              contentDescription = "Shuffle Match Colors",
              tint = Color(0xFF38BDF8)
            )
          }

          // Speed toggle
          IconButton(
            onClick = { viewModel.toggleSpeed() },
            modifier = Modifier.testTag("speed_toggle_button")
          ) {
            Icon(
              imageVector = if (state.fastSpeed) Icons.Default.FastForward else Icons.Default.Speed,
              contentDescription = "Game Speed",
              tint = if (state.fastSpeed) Color(0xFFFFD700) else Color(0xFF94A3B8)
            )
          }

          // Rules & Superpowers guide
          IconButton(
            onClick = { showRulesDialog = true },
            modifier = Modifier.testTag("rules_button")
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = "Superpower Rules",
              tint = Color(0xFFE2E8F0)
            )
          }

          // New Match Dialog
          IconButton(
            onClick = { showNewMatchDialog = true },
            modifier = Modifier.testTag("new_match_button")
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "New Match",
              tint = Color(0xFFE2E8F0)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = Color(0xFF0F172A)
        )
      )
    },
    containerColor = Color(0xFF0A0F1D)
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .widthIn(max = 540.dp)
          .padding(horizontal = 12.dp, vertical = 6.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Floating Event Notification Banner
        SuperpowerBanner(
          notification = state.activeNotification,
          modifier = Modifier.fillMaxWidth()
        )

        // Player Tray
        PlayerBar(
          players = state.players,
          currentPlayerIndex = state.currentPlayerIndex,
          modifier = Modifier.fillMaxWidth()
        )

        // The Mini Ludo Board (11x11 Grid Canvas with Center Dice)
        LudoBoardView(
          players = state.players,
          superpowerRunes = state.superpowerRunes,
          highlightedTokenIds = state.highlightedTokenIds,
          onTokenClicked = { token -> viewModel.onTokenTapped(token) },
          diceValue = state.diceValue,
          isDiceRolling = state.isDiceRolling,
          canRollDice = state.phase == GamePhase.WAITING_TO_ROLL && !state.isDiceRolling,
          isAiTurn = state.isCurrentPlayerAi,
          activePlayerColor = state.currentPlayer?.quadrantColor?.primary ?: Color(0xFFFFD700),
          onDiceRoll = { viewModel.onRollDiceClicked() },
          modifier = Modifier.fillMaxWidth()
        )

        // Dedicated Ludo Dice Rolling Arena (Felt tray, 3D dice, active turn HUD)
        LudoDiceArena(
          state = state,
          onRollDice = { viewModel.onRollDiceClicked() },
          modifier = Modifier.fillMaxWidth()
        )

        // Real-Time Audio Feed & Live Transcription Bar
        LiveTranscriptionBar(
          audioState = liveAudioState,
          onToggleMic = { viewModel.toggleMic() },
          onQuickVoiceCommand = { phrase -> viewModel.broadcastVoiceCommand(phrase) },
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
      }
    }
  }

  // Dialogs
  if (showLeaderboardDialog) {
    LeaderboardDialog(
      entries = topLeaderboard,
      onDismiss = { showLeaderboardDialog = false }
    )
  }

  if (showAvatarCustomizerDialog) {
    AvatarCustomizerDialog(
      players = state.players,
      onDismiss = { showAvatarCustomizerDialog = false },
      onSaveAvatar = { playerIdx, config ->
        viewModel.updatePlayerAvatar(playerIdx, config)
      }
    )
  }

  if (showNewMatchDialog) {
    NewMatchDialog(
      currentPalette = state.matchPalette,
      onDismiss = { showNewMatchDialog = false },
      onStartMatch = { players, tokens, humans, palette ->
        viewModel.startNewMatch(
          playerCount = players,
          tokensPerPlayer = tokens,
          humanCount = humans,
          customPalette = palette
        )
        showNewMatchDialog = false
      }
    )
  }

  if (showRulesDialog) {
    SuperpowerGuideDialog(
      onDismiss = { showRulesDialog = false }
    )
  }

  val winner = state.winner
  if (state.phase == GamePhase.GAME_OVER && winner != null) {
    VictoryDialog(
      winner = winner,
      turnCount = state.turnCount,
      themeName = state.matchPalette.themeName,
      onPlayAgain = {
        viewModel.startNewMatch(
          playerCount = state.players.size,
          tokensPerPlayer = state.tokensPerPlayer,
          humanCount = state.players.count { !it.isAi }
        )
      }
    )
  }
}

