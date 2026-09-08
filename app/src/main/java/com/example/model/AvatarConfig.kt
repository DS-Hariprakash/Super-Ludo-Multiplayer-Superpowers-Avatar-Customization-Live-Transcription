package com.example.model

import androidx.compose.ui.graphics.Color

enum class AccessoryType {
  HAT,
  GLASSES,
  AURA
}

data class AvatarAccessory(
  val id: String,
  val name: String,
  val emoji: String,
  val type: AccessoryType
)

data class AvatarModel(
  val id: String,
  val name: String,
  val emoji: String,
  val defaultAuraColor: Long
)

data class AvatarConfig(
  val modelId: String = "ninja",
  val modelName: String = "Shadow Ninja",
  val baseEmoji: String = "🥷",
  val hat: AvatarAccessory? = null,
  val glasses: AvatarAccessory? = null,
  val auraColorHex: Long = 0xFF00E5FF,
  val trailSymbol: String = "⚡"
) {
  val auraColor: Color get() = Color(auraColorHex)
}

object AvatarCatalog {
  val PreMadeModels = listOf(
    AvatarModel("ninja", "Shadow Ninja", "🥷", 0xFF00E5FF),
    AvatarModel("bot", "Cyber Bot", "🤖", 0xFFD500F9),
    AvatarModel("mage", "Arcane Mage", "🧙", 0xFFFFD700),
    AvatarModel("volt", "Volt Striker", "⚡", 0xFFFF1744),
    AvatarModel("king", "Royal Sovereign", "👑", 0xFFFFAB00),
    AvatarModel("pilot", "Star Pilot", "🚀", 0xFF00E676),
    AvatarModel("draco", "Draco Rider", "🐉", 0xFFFF3D00),
    AvatarModel("cat", "Neon Feline", "🐱", 0xFFE040FB)
  )

  val AvailableHats = listOf(
    AvatarAccessory("none", "None", "", AccessoryType.HAT),
    AvatarAccessory("crown", "Royal Crown", "👑", AccessoryType.HAT),
    AvatarAccessory("wizard", "Wizard Hat", "🧙‍♂️", AccessoryType.HAT),
    AvatarAccessory("cap", "Cool Cap", "🧢", AccessoryType.HAT),
    AvatarAccessory("helm", "Viking Helm", "🪖", AccessoryType.HAT),
    AvatarAccessory("halo", "Angel Halo", "😇", AccessoryType.HAT),
    AvatarAccessory("band", "Ninja Band", "🥷", AccessoryType.HAT)
  )

  val AvailableGlasses = listOf(
    AvatarAccessory("none", "None", "", AccessoryType.GLASSES),
    AvatarAccessory("shades", "Cyber Shades", "🕶️", AccessoryType.GLASSES),
    AvatarAccessory("vr", "VR Goggles", "🥽", AccessoryType.GLASSES),
    AvatarAccessory("monocle", "Monocle", "🧐", AccessoryType.GLASSES),
    AvatarAccessory("specs", "Retro Specs", "👓", AccessoryType.GLASSES)
  )

  val AuraPalettes = listOf(
    Pair("Neon Cyan", 0xFF00E5FF),
    Pair("Electric Pink", 0xFFFF1744),
    Pair("Sun Gold", 0xFFFFD700),
    Pair("Cyber Violet", 0xFFD500F9),
    Pair("Emerald Green", 0xFF00E676),
    Pair("Solar Flare", 0xFFFF3D00)
  )

  fun defaultConfigForPlayer(playerIndex: Int): AvatarConfig {
    val model = PreMadeModels.getOrElse(playerIndex % PreMadeModels.size) { PreMadeModels.first() }
    val hat = when (playerIndex % 4) {
      0 -> AvailableHats.find { it.id == "crown" }
      1 -> AvailableHats.find { it.id == "cap" }
      2 -> AvailableHats.find { it.id == "halo" }
      else -> AvailableHats.find { it.id == "helm" }
    }
    val glasses = when (playerIndex % 4) {
      0 -> AvailableGlasses.find { it.id == "shades" }
      1 -> AvailableGlasses.find { it.id == "vr" }
      else -> null
    }
    return AvatarConfig(
      modelId = model.id,
      modelName = model.name,
      baseEmoji = model.emoji,
      hat = hat,
      glasses = glasses,
      auraColorHex = model.defaultAuraColor
    )
  }
}
