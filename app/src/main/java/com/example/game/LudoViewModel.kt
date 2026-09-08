package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioTranscriptionManager
import com.example.audio.LiveAudioState
import com.example.data.LeaderboardEntry
import com.example.data.LeaderboardRepository
import com.example.data.LudoDatabase
import com.example.model.AvatarCatalog
import com.example.model.AvatarConfig
import com.example.model.MatchColorPalette
import com.example.model.MatchColorPalettes
import com.example.model.Player
import com.example.model.SuperpowerRune
import com.example.model.SuperpowerType
import com.example.model.Token
import com.example.model.TokenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class LudoViewModel(application: Application) : AndroidViewModel(application) {

  private val _gameState = MutableStateFlow(GameState())
  val gameState: StateFlow<GameState> = _gameState.asStateFlow()

  private val audioManager = AudioTranscriptionManager(application.applicationContext, viewModelScope)
  val liveAudioState: StateFlow<LiveAudioState> = audioManager.state

  private val database = LudoDatabase.getDatabase(application.applicationContext)
  private val leaderboardRepository = LeaderboardRepository(database.leaderboardDao())
  val topLeaderboard: StateFlow<List<LeaderboardEntry>> = leaderboardRepository.topPlayers
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  private var aiJob: Job? = null
  private var notificationJob: Job? = null

  init {
    viewModelScope.launch {
      leaderboardRepository.ensureSeeded()
    }
    startNewMatch()
  }

  fun toggleMic() {
    audioManager.toggleListening()
  }

  fun broadcastVoiceCommand(phrase: String) {
    audioManager.broadcastVoiceCommand(phrase)
  }

  fun updatePlayerAvatar(playerIndex: Int, config: AvatarConfig) {
    _gameState.update { state ->
      val updatedPlayers = state.players.map { player ->
        if (player.index == playerIndex) {
          player.copy(
            avatarConfig = config,
            avatar = config.baseEmoji
          )
        } else {
          player
        }
      }
      state.copy(players = updatedPlayers)
    }
  }

  /**
   * Start or restart a match with configurable player count, tokens, and random positional colors.
   */
  fun startNewMatch(
    playerCount: Int = 4,
    tokensPerPlayer: Int = 2,
    humanCount: Int = 2,
    customPalette: MatchColorPalette? = null
  ) {
    aiJob?.cancel()
    val palette = customPalette ?: MatchColorPalettes.getRandomShuffledPalette()
    val defaultAvatars = listOf("⚡ Nova", "🛡️ Aegis", "🌀 Warp", "👑 Rex")
    val defaultEmojis = listOf("⚡", "🛡️", "🌀", "👑")

    val clampedPlayerCount = playerCount.coerceIn(2, 4)
    val existingPlayers = _gameState.value.players
    val players = (0 until clampedPlayerCount).map { i ->
      val isHuman = i < humanCount
      val quadrantColor = palette.quadrants[i % palette.quadrants.size]
      val tokens = (0 until tokensPerPlayer).map { t ->
        Token(
          id = "p${i}_t$t",
          playerIndex = i,
          tokenIndex = t,
          state = TokenState.IN_YARD
        )
      }
      val avatarConfig = existingPlayers.getOrNull(i)?.avatarConfig ?: AvatarCatalog.defaultConfigForPlayer(i)
      Player(
        index = i,
        name = if (isHuman) "Player ${i + 1}" else "Bot ${defaultAvatars[i]}",
        avatar = avatarConfig.baseEmoji,
        isAi = !isHuman,
        quadrantColor = quadrantColor,
        tokens = tokens,
        avatarConfig = avatarConfig,
        knockoutCount = 0,
        superpowersUsedCount = 0
      )
    }

    // Spawn 3 initial random superpower runes on perimeter track
    val initialRunes = spawnRandomSuperpowers(count = 3, existingRunes = emptyList())

    _gameState.value = GameState(
      players = players,
      currentPlayerIndex = 0,
      phase = GamePhase.WAITING_TO_ROLL,
      diceValue = 1,
      isDiceRolling = false,
      superpowerRunes = initialRunes,
      matchPalette = palette,
      tokensPerPlayer = tokensPerPlayer,
      turnCount = 1,
      matchLog = listOf("Match started with ${palette.themeName} palette!")
    )

    showNotification(
      title = "New Match Started!",
      subtitle = "${palette.themeName} theme applied. Tap the dice to roll!"
    )

    checkAndTriggerAi()
  }

  /**
   * Shuffles and randomizes the positional match colors for the active game.
   */
  fun shuffleMatchColors() {
    val newPalette = MatchColorPalettes.getRandomShuffledPalette()
    _gameState.update { state ->
      val updatedPlayers = state.players.mapIndexed { idx, player ->
        player.copy(quadrantColor = newPalette.quadrants[idx % newPalette.quadrants.size])
      }
      state.copy(
        matchPalette = newPalette,
        players = updatedPlayers,
        matchLog = listOf("Colors reshuffled to ${newPalette.themeName}") + state.matchLog.take(20)
      )
    }
    showNotification(
      title = "Positional Colors Shuffled!",
      subtitle = "Quad colors updated to ${_gameState.value.matchPalette.themeName}"
    )
  }

  /**
   * Toggle game speed between 1x and Fast (2x).
   */
  fun toggleSpeed() {
    _gameState.update { it.copy(fastSpeed = !it.fastSpeed) }
  }

  /**
   * Interactive dice roll trigger.
   */
  fun onRollDiceClicked() {
    val state = _gameState.value
    if (state.phase != GamePhase.WAITING_TO_ROLL || state.isDiceRolling) return

    val currentPlayer = state.currentPlayer ?: return
    if (currentPlayer.isFrozenForTurn) {
      showNotification(
        title = "Frozen!",
        subtitle = "${currentPlayer.name} is frozen and skips this turn.",
        type = SuperpowerType.CRYO_FREEZE
      )
      _gameState.update { s ->
        val updated = s.players.map { if (it.index == currentPlayer.index) it.copy(isFrozenForTurn = false) else it }
        s.copy(players = updated)
      }
      viewModelScope.launch {
        delay(if (state.fastSpeed) 200L else 350L)
        advanceTurn()
      }
      return
    }

    viewModelScope.launch {
      _gameState.update { it.copy(isDiceRolling = true) }

      // Ultra-snappy dice roll animation frames
      val rollSteps = if (state.fastSpeed) 3 else 4
      for (i in 0 until rollSteps) {
        val tempRoll = Random.nextInt(1, 7)
        _gameState.update { it.copy(diceValue = tempRoll) }
        delay(25L)
      }

      val finalRoll = Random.nextInt(1, 7)
      val isSix = finalRoll == 6
      val newSixStreak = if (isSix) state.consecutiveSixes + 1 else 0

      // In Ludo, 3 consecutive sixes voids the turn
      if (newSixStreak >= 3) {
        _gameState.update {
          it.copy(
            isDiceRolling = false,
            diceValue = finalRoll,
            consecutiveSixes = 0,
            phase = GamePhase.WAITING_TO_ROLL
          )
        }
        showNotification(
          title = "3 Sixes in a Row!",
          subtitle = "${currentPlayer.name} rolled 3 sixes! Turn forfeited."
        )
        delay(if (state.fastSpeed) 220L else 400L)
        advanceTurn()
        return@launch
      }

      _gameState.update {
        it.copy(
          isDiceRolling = false,
          diceValue = finalRoll,
          consecutiveSixes = newSixStreak,
          extraTurnGranted = isSix
        )
      }

      // Check valid moves for current player
      val validTokens = currentPlayer.tokens.filter { LudoBoardLogic.canMoveToken(it, finalRoll) }
      if (validTokens.isEmpty()) {
        val sixHint = if (currentPlayer.tokens.all { it.isInYard }) " (Need a 6 to summon)" else ""
        showNotification(
          title = "No Moves Available",
          subtitle = "${currentPlayer.name} rolled $finalRoll$sixHint. Passing turn."
        )
        _gameState.update {
          it.copy(
            highlightedTokenIds = emptySet(),
            phase = GamePhase.WAITING_TO_ROLL
          )
        }
        delay(if (_gameState.value.fastSpeed) 220L else 380L)
        advanceTurn()
      } else {
        _gameState.update {
          it.copy(
            phase = GamePhase.SELECTING_TOKEN,
            highlightedTokenIds = validTokens.map { t -> t.id }.toSet()
          )
        }

        // If AI or only 1 move available for AI, handle automatically
        if (currentPlayer.isAi) {
          delay(if (_gameState.value.fastSpeed) 100L else 200L)
          val chosenToken = chooseBestAiToken(validTokens, finalRoll)
          executeMove(chosenToken, finalRoll)
        } else if (validTokens.size == 1) {
          // Highlight it clearly so human can tap or auto-move after brief moment
          // Keeping human in control: they can tap the pulsating pawn!
        }
      }
    }
  }

  /**
   * Preview path when tapping a token.
   */
  fun onTokenTapped(token: Token) {
    val state = _gameState.value
    if (state.phase != GamePhase.SELECTING_TOKEN) return
    if (token.playerIndex != state.currentPlayerIndex) return
    if (!state.highlightedTokenIds.contains(token.id)) return

    executeMove(token, state.diceValue)
  }

  /**
   * Execute token move with step-by-step animation and superpower checks.
   */
  private fun executeMove(token: Token, roll: Int) {
    viewModelScope.launch {
      _gameState.update {
        it.copy(
          phase = GamePhase.ANIMATING_MOVE,
          highlightedTokenIds = emptySet()
        )
      }

      val path = LudoBoardLogic.calculatePath(token, roll)
      val stepDelay = if (_gameState.value.fastSpeed) 35L else 65L

      // Step-by-step intermediate progression
      var runningToken = token
      for (stepCoord in path) {
        delay(stepDelay)
        // Update position for visual smoothness
        val stepSteps = runningToken.stepsTaken + 1
        runningToken = if (runningToken.state == TokenState.IN_YARD) {
          runningToken.copy(
            state = TokenState.ON_TRACK,
            trackPos = LudoBoardLogic.StartTrackIndices[token.playerIndex],
            stepsTaken = 0
          )
        } else if (stepSteps < 35) {
          runningToken.copy(
            trackPos = (runningToken.trackPos + 1) % LudoBoardLogic.TRACK_COUNT,
            stepsTaken = stepSteps
          )
        } else if (stepSteps < 35 + LudoBoardLogic.HOME_CORRIDOR_LENGTH) {
          runningToken.copy(
            state = TokenState.IN_HOME_STRETCH,
            homeStretchPos = stepSteps - 35,
            stepsTaken = stepSteps
          )
        } else {
          runningToken.copy(
            state = TokenState.FINISHED,
            stepsTaken = LudoBoardLogic.TOTAL_STEPS_TO_HOME
          )
        }
        updateTokenInState(runningToken)
      }

      // Final move state calculation
      var finalToken = LudoBoardLogic.applyMove(token, roll)
      updateTokenInState(finalToken)

      // 1. Check Superpower Rune collision
      var bonusTurnFromPower = false
      if (finalToken.state == TokenState.ON_TRACK) {
        val rune = _gameState.value.superpowerRunes.firstOrNull { it.tileIndex == finalToken.trackPos }
        if (rune != null) {
          // Consume rune and track player stat
          _gameState.update { s ->
            val updatedPlayers = s.players.map { p ->
              if (p.index == s.currentPlayerIndex) {
                p.copy(superpowersUsedCount = p.superpowersUsedCount + 1)
              } else p
            }
            s.copy(
              players = updatedPlayers,
              superpowerRunes = s.superpowerRunes.filterNot { it.tileIndex == rune.tileIndex }
            )
          }
          broadcastVoiceCommand("${_gameState.value.currentPlayer?.name ?: "Player"} activated ${rune.type.title}!")
          finalToken = applySuperpowerEffect(rune.type, finalToken)
          updateTokenInState(finalToken)
          if (rune.type == SuperpowerType.BONUS_ROLL) {
            bonusTurnFromPower = true
          }
          delay(if (_gameState.value.fastSpeed) 200L else 380L)
        }
      }

      // 2. Check Capture opponent token
      var capturedEnemy = false
      if (finalToken.state == TokenState.ON_TRACK) {
        val enemy = LudoBoardLogic.findCapturableToken(finalToken, _gameState.value.players)
        if (enemy != null) {
          if (enemy.hasShield) {
            // Shield absorbs capture!
            val unshieldedEnemy = enemy.copy(hasShield = false)
            updateTokenInState(unshieldedEnemy)
            showNotification(
              title = "Titan Shield Absorbed!",
              subtitle = "Shield protected ${enemy.id} from capture!",
              type = SuperpowerType.TITAN_SHIELD
            )
            broadcastVoiceCommand("Titan Shield absorbed the blow!")
          } else {
            // Captured! Send back to yard and track knockout stat
            val resetEnemy = enemy.copy(
              state = TokenState.IN_YARD,
              trackPos = -1,
              homeStretchPos = -1,
              stepsTaken = 0
            )
            updateTokenInState(resetEnemy)
            capturedEnemy = true

            _gameState.update { s ->
              val updatedPlayers = s.players.map { p ->
                if (p.index == s.currentPlayerIndex) {
                  p.copy(knockoutCount = p.knockoutCount + 1)
                } else p
              }
              s.copy(players = updatedPlayers)
            }

            showNotification(
              title = "Pawn Captured!",
              subtitle = "${_gameState.value.currentPlayer?.name} captured an opponent's pawn! Extra roll granted!"
            )
            broadcastVoiceCommand("Knockout! ${_gameState.value.currentPlayer?.name} strikes an opposing pawn!")
          }
        }
      }

      // 3. Check Victory condition
      val currentPlayer = _gameState.value.currentPlayer
      if (currentPlayer != null && currentPlayer.tokens.all { it.isHome }) {
        _gameState.update {
          it.copy(
            phase = GamePhase.GAME_OVER,
            winner = currentPlayer,
            matchLog = listOf("🏆 ${currentPlayer.name} WON THE MATCH!") + it.matchLog
          )
        }
        showNotification(
          title = "Victory!",
          subtitle = "🏆 ${currentPlayer.name} conquered the board!"
        )
        broadcastVoiceCommand("Victory Royale! ${currentPlayer.name} conquered the board!")
        leaderboardRepository.recordMatchOutcome(currentPlayer.index, _gameState.value.players)
        return@launch
      }

      // 4. Decide next turn or extra roll
      val hasExtraTurn = _gameState.value.extraTurnGranted || capturedEnemy || bonusTurnFromPower
      if (hasExtraTurn) {
        showNotification(
          title = "Bonus Roll!",
          subtitle = "${currentPlayer?.name} gets another turn!"
        )
        _gameState.update {
          it.copy(
            phase = GamePhase.WAITING_TO_ROLL,
            extraTurnGranted = false
          )
        }
        checkAndTriggerAi()
      } else {
        advanceTurn()
      }
    }
  }

  /**
   * Applies the triggered superpower immediately to the game.
   */
  private suspend fun applySuperpowerEffect(type: SuperpowerType, token: Token): Token {
    val playerName = _gameState.value.currentPlayer?.name ?: "Player"
    var updatedToken = token

    showNotification(
      title = "${type.symbol} ${type.title} Activated!",
      subtitle = "$playerName: ${type.description}",
      type = type
    )

    when (type) {
      SuperpowerType.THUNDER_DASH -> {
        // Surge +3 steps forward
        val extraSteps = 3
        val boosted = LudoBoardLogic.applyMove(token, extraSteps)
        updatedToken = boosted
      }
      SuperpowerType.TITAN_SHIELD -> {
        updatedToken = token.copy(hasShield = true)
      }
      SuperpowerType.WARP_PORTAL -> {
        // Warp to next safe haven
        val nextSafe = LudoBoardLogic.getNextSafeHaven(token.trackPos)
        updatedToken = token.copy(trackPos = nextSafe)
      }
      SuperpowerType.SONIC_BLAST -> {
        // Push back enemy tokens within 4 tiles
        _gameState.update { state ->
          val updatedPlayers = state.players.map { p ->
            if (p.index == token.playerIndex) p
            else {
              val newTokens = p.tokens.map { enemy ->
                if (enemy.state == TokenState.ON_TRACK) {
                  val diff = (enemy.trackPos - token.trackPos + 36) % 36
                  if (diff in 1..4) {
                    val pushedBackTrack = (enemy.trackPos - 2 + 36) % 36
                    enemy.copy(
                      trackPos = pushedBackTrack,
                      stepsTaken = (enemy.stepsTaken - 2).coerceAtLeast(0)
                    )
                  } else enemy
                } else enemy
              }
              p.copy(tokens = newTokens)
            }
          }
          state.copy(players = updatedPlayers)
        }
      }
      SuperpowerType.CRYO_FREEZE -> {
        // Freeze the next player
        val nextIdx = (_gameState.value.currentPlayerIndex + 1) % _gameState.value.players.size
        _gameState.update { state ->
          val updated = state.players.mapIndexed { idx, p ->
            if (idx == nextIdx) p.copy(isFrozenForTurn = true) else p
          }
          state.copy(players = updated)
        }
      }
      SuperpowerType.BONUS_ROLL -> {
        // Handled via bonus roll flag
      }
    }
    return updatedToken
  }

  /**
   * Advance to the next player's turn.
   */
  private fun advanceTurn() {
    _gameState.update { state ->
      val nextIndex = (state.currentPlayerIndex + 1) % state.players.size
      val nextTurnNum = state.turnCount + 1

      // Periodically spawn a new superpower rune every 3 turns
      val currentRunes = state.superpowerRunes
      val updatedRunes = if (nextTurnNum % 3 == 0 && currentRunes.size < 5) {
        currentRunes + spawnRandomSuperpowers(count = 1, existingRunes = currentRunes)
      } else {
        currentRunes
      }

      state.copy(
        currentPlayerIndex = nextIndex,
        phase = GamePhase.WAITING_TO_ROLL,
        consecutiveSixes = 0,
        extraTurnGranted = false,
        highlightedTokenIds = emptySet(),
        superpowerRunes = updatedRunes,
        turnCount = nextTurnNum
      )
    }

    checkAndTriggerAi()
  }

  /**
   * Helper to trigger AI turn if current player is AI.
   */
  private fun checkAndTriggerAi() {
    aiJob?.cancel()
    val state = _gameState.value
    if (state.phase == GamePhase.GAME_OVER) return

    val player = state.currentPlayer ?: return
    if (player.isAi) {
      aiJob = viewModelScope.launch {
        _gameState.update { it.copy(isAiThinking = true) }
        delay(if (state.fastSpeed) 120L else 220L)
        _gameState.update { it.copy(isAiThinking = false) }
        onRollDiceClicked()
      }
    }
  }

  /**
   * AI Strategy to select best token.
   */
  private fun chooseBestAiToken(tokens: List<Token>, roll: Int): Token {
    val state = _gameState.value
    // 1. Check if any move captures an opponent token
    for (token in tokens) {
      val simulated = LudoBoardLogic.applyMove(token, roll)
      val enemy = LudoBoardLogic.findCapturableToken(simulated, state.players)
      if (enemy != null) return token
    }

    // 2. Check if any move lands on a Superpower rune
    for (token in tokens) {
      val simulated = LudoBoardLogic.applyMove(token, roll)
      if (simulated.state == TokenState.ON_TRACK &&
        state.superpowerRunes.any { it.tileIndex == simulated.trackPos }
      ) {
        return token
      }
    }

    // 3. Move token out of yard on a 6
    if (roll == 6) {
      val yardToken = tokens.firstOrNull { it.isInYard }
      if (yardToken != null) return yardToken
    }

    // 4. Token closest to finish
    return tokens.maxByOrNull { it.stepsTaken } ?: tokens.first()
  }

  /**
   * Helper to update token inside gameState.
   */
  private fun updateTokenInState(token: Token) {
    _gameState.update { state ->
      val updatedPlayers = state.players.map { player ->
        if (player.index == token.playerIndex) {
          val updatedTokens = player.tokens.map { if (it.id == token.id) token else it }
          player.copy(tokens = updatedTokens)
        } else player
      }
      state.copy(players = updatedPlayers)
    }
  }

  /**
   * Spawns random superpower runes on open perimeter track tiles.
   */
  private fun spawnRandomSuperpowers(
    count: Int,
    existingRunes: List<SuperpowerRune>
  ): List<SuperpowerRune> {
    val occupiedIndices = existingRunes.map { it.tileIndex }.toSet()
    val availableIndices = (0 until LudoBoardLogic.TRACK_COUNT).filterNot { occupiedIndices.contains(it) }

    val shuffledIndices = availableIndices.shuffled().take(count)
    val powerTypes = SuperpowerType.entries

    return shuffledIndices.map { tileIdx ->
      SuperpowerRune(
        tileIndex = tileIdx,
        type = powerTypes.random()
      )
    }
  }

  /**
   * Post a brief floating notification message.
   */
  private fun showNotification(
    title: String,
    subtitle: String,
    type: SuperpowerType? = null
  ) {
    notificationJob?.cancel()
    _gameState.update {
      it.copy(
        activeNotification = GameEventNotification(
          title = title,
          subtitle = subtitle,
          type = type
        )
      )
    }
    notificationJob = viewModelScope.launch {
      delay(if (_gameState.value.fastSpeed) 1500L else 2200L)
      _gameState.update { it.copy(activeNotification = null) }
    }
  }

  override fun onCleared() {
    super.onCleared()
    audioManager.destroy()
  }
}
