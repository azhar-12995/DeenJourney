package com.deenjourney.app.data.auth

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.deenjourney.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

actual val googleSignInAvailable: Boolean get() = BuildConfig.GOOGLE_WEB_CLIENT_ID.isNotBlank()
actual val appleSignInAvailable: Boolean = false

@Composable
actual fun rememberGoogleSignIn(onResult: (SocialResult) -> Unit): () -> Unit {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    return {
        if (!googleSignInAvailable) onResult(SocialResult.Failed("unavailable"))
        else scope.launch {
            runCatching {
                val option = GetSignInWithGoogleOption.Builder(BuildConfig.GOOGLE_WEB_CLIENT_ID).build()
                val res = CredentialManager.create(ctx).getCredential(ctx as? Activity ?: ctx, GetCredentialRequest.Builder().addCredentialOption(option).build())
                val c = res.credential
                if (c is CustomCredential && c.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) GoogleIdTokenCredential.createFrom(c.data).idToken
                else error("unexpected credential")
            }.onSuccess { onResult(SocialResult.Google(it)) }
                .onFailure { onResult(SocialResult.Failed(if (it is GetCredentialCancellationException) "cancelled" else "unknown")) }
        }
    }
}

@Composable
actual fun rememberAppleSignIn(onResult: (SocialResult) -> Unit): () -> Unit = { onResult(SocialResult.Failed("unavailable")) }
