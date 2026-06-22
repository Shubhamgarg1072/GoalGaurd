package com.time.applauncher.goalgaurd.feature.goals.data

import com.time.applauncher.goalgaurd.core.database.entity.GoalEntity
import com.time.applauncher.goalgaurd.feature.goals.domain.Goal
import com.time.applauncher.goalgaurd.feature.goals.domain.GoalPriority
import java.time.Instant

fun GoalEntity.toGoal(): Goal = Goal(
    id = id,
    name = name,
    emoji = emoji,
    targetValue = targetValue,
    currentValue = currentValue,
    unit = unit,
    targetDate = targetDate,
    priority = GoalPriority.valueOf(priority),
    createdAt = createdAt,
)

fun Goal.toGoalEntity(): GoalEntity = GoalEntity(
    id = id,
    name = name,
    emoji = emoji,
    targetValue = targetValue,
    currentValue = currentValue,
    unit = unit,
    targetDate = targetDate,
    priority = priority.name,
    createdAt = createdAt,
    updatedAt = Instant.now(),
)
