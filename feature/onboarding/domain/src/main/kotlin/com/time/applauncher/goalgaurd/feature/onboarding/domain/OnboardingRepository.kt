package com.time.applauncher.goalgaurd.feature.onboarding.domain

import com.time.applauncher.goalgaurd.core.domain.DataError
import com.time.applauncher.goalgaurd.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface OnboardingRepository {
    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun completeOnboarding(name: String, age: Int, occupation: String?): EmptyResult<DataError.Local>
}
