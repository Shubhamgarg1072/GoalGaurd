package com.time.applauncher.goalgaurd.di

import com.time.applauncher.goalgaurd.core.crypto.InMemoryVaultKeyManager
import com.time.applauncher.goalgaurd.core.crypto.VaultKeyManager
import com.time.applauncher.goalgaurd.feature.vault.data.DataStoreVaultRepository
import com.time.applauncher.goalgaurd.feature.vault.domain.VaultRepository
import com.time.applauncher.goalgaurd.feature.vault.presentation.VaultViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val vaultModule = module {
    // One shared in-memory key manager for the whole app: holds the unlocked DEK.
    single<VaultKeyManager> { InMemoryVaultKeyManager() }
    single<VaultRepository> { DataStoreVaultRepository(androidContext(), get()) }
    viewModel { VaultViewModel(get()) }
}
