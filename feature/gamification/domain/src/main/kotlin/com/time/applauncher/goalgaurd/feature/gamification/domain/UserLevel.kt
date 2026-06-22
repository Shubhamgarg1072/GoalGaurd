package com.time.applauncher.goalgaurd.feature.gamification.domain

data class UserLevel(
    val xp: Int,
    val level: Int,
    val levelName: String,
    val xpForNextLevel: Int,
    val badges: List<Badge>,
) {
    val progressToNextLevel: Float
        get() = if (xpForNextLevel == 0) 1f else xp.toFloat() / xpForNextLevel

    companion object {
        fun fromXp(xp: Int): UserLevel {
            val (level, name, nextXp) = when {
                xp < 500 -> Triple(1, "Beginner", 500)
                xp < 1500 -> Triple(2, "Builder", 1500)
                xp < 3000 -> Triple(3, "Achiever", 3000)
                xp < 6000 -> Triple(4, "Master", 6000)
                else -> Triple(5, "Legend", Int.MAX_VALUE)
            }
            return UserLevel(xp, level, name, nextXp, emptyList())
        }
    }
}

data class Badge(val id: String, val emoji: String, val name: String, val description: String, val isUnlocked: Boolean)
