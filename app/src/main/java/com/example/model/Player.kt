package com.example.model

data class Player(
  val index: Int,
  val name: String,
  val avatar: String,
  val isAi: Boolean,
  val quadrantColor: MatchQuadrantColor,
  val tokens: List<Token>,
  val storedSuperpower: SuperpowerType? = null,
  val isFrozenForTurn: Boolean = false,
  val hasWon: Boolean = false,
  val finishRank: Int = 0,
  val avatarConfig: AvatarConfig = AvatarCatalog.defaultConfigForPlayer(index),
  val knockoutCount: Int = 0,
  val superpowersUsedCount: Int = 0
) {
  val hasFinishedAllTokens: Boolean get() = tokens.isNotEmpty() && tokens.all { it.isHome }
  val finishedCount: Int get() = tokens.count { it.isHome }
}
