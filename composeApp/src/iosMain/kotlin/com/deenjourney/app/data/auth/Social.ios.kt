package com.deenjourney.app.data.auth

import androidx.compose.runtime.Composable

/**
 * iOS: Google Sign-In and Sign in with Apple need the GoogleSignIn SDK / AuthenticationServices wiring in the
 * Xcode project (and the Firebase GoogleService-Info.plist). Until those are added the buttons report "unavailable".
 */
actual val googleSignInAvailable: Boolean = false
actual val appleSignInAvailable: Boolean = true

@Composable
actual fun rememberGoogleSignIn(onResult: (SocialResult) -> Unit): () -> Unit = { onResult(SocialResult.Failed("unavailable")) }

@Composable
actual fun rememberAppleSignIn(onResult: (SocialResult) -> Unit): () -> Unit = { AppleSignInBridge.start(onResult) }

/** Implemented from Swift (iosApp/AppleSignIn.swift) — sets [handler] at launch. */
object AppleSignInBridge {
    var handler: ((callback: (idToken: String?, rawNonce: String?, error: String?) -> Unit) -> Unit)? = null
    fun start(onResult: (SocialResult) -> Unit) {
        val h = handler ?: return onResult(SocialResult.Failed("unavailable"))
        h { token, nonce, err -> onResult(if (token != null && nonce != null) SocialResult.Apple(token, nonce) else SocialResult.Failed(err ?: "cancelled")) }
    }
}
