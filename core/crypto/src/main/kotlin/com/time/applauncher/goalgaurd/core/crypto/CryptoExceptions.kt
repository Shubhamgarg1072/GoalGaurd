package com.time.applauncher.goalgaurd.core.crypto

import java.security.GeneralSecurityException

/** Thrown when a passphrase fails to unwrap the data-encryption key (wrong passphrase or tampered envelope). */
class WrongPassphraseException : GeneralSecurityException("Passphrase did not unlock the vault")

/** Thrown when a decryption is requested but the vault is locked (no DEK in memory). */
class VaultLockedException : IllegalStateException("Vault is locked; unlock with the passphrase first")
