package com.deenjourney.app.data.auth

import androidx.compose.runtime.Composable

/** Result of a native social sign-in: an ID token for Firebase, or an error code (see [AuthError]). */
sealed interface SocialResult {
    data class Google(val idToken: String) : SocialResult
    data class Apple(val idToken: String, val rawNonce: String) : SocialResult
    data class Failed(val code: String) : SocialResult
}

/** Whether "Continue with Google" is configured on this build (needs the Firebase web client id). */
expect val googleSignInAvailable: Boolean

/** Whether Sign in with Apple is offered (iOS only). */
expect val appleSignInAvailable: Boolean

@Composable
expect fun rememberGoogleSignIn(onResult: (SocialResult) -> Unit): () -> Unit

@Composable
expect fun rememberAppleSignIn(onResult: (SocialResult) -> Unit): () -> Unit
