
package com.example.b2csso

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.b2csso.msal.MsalClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Loading : AuthState()
    data class SignedIn(val accessToken: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class MsalViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    // EDIT: Your B2C authority and scopes
    private val b2cAuthority =
        "https://<b2c-tenant>.b2clogin.com/tfp/<b2c-tenant>.onmicrosoft.com/B2C_1A_SignUpOrSignIn"

    private val scopes = listOf(
        "openid",
        "offline_access",
        "https://<b2c-tenant>.onmicrosoft.com/api/read"
    )

    fun signIn(activity: Activity) {
        _state.value = AuthState.Loading
        MsalClient.acquireTokenSilentOrInteractive(
            activity = activity,
            scopes = scopes,
            authority = b2cAuthority,
            onSuccess = { result ->
                _state.value = AuthState.SignedIn(result.accessToken)
            },
            onError = { ex ->
                _state.value = AuthState.Error(ex.message ?: "Authentication failed")
            }
        )
    }
}
