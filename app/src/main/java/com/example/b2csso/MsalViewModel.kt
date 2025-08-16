
package com.example.b2csso

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.example.b2csso.msal.MsalClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed class AuthState {
    object Loading : AuthState()
    data class SignedIn(val accessToken: String, val userName: String?, val isSharedDevice: Boolean) : AuthState()
    object SignedOut : AuthState()
    data class Error(val message: String) : AuthState()
}

class MsalViewModel : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Loading)
    val state: StateFlow<AuthState> = _state

    // Standard Microsoft Graph scopes for Entra ID
    private val scopes = listOf(
        "User.Read",
        "openid",
        "profile",
        "offline_access"
    )

    fun checkCurrentState() {
        _state.value = AuthState.Loading
        
        MsalClient.isSharedDevice { isSharedDevice ->
            MsalClient.getCurrentAccount { account ->
                if (account != null) {
                    // User is already signed in
                    _state.value = AuthState.SignedIn(
                        accessToken = "Already signed in",
                        userName = account.username,
                        isSharedDevice = isSharedDevice
                    )
                } else {
                    _state.value = AuthState.SignedOut
                }
            }
        }
    }

    fun signIn(activity: Activity) {
        _state.value = AuthState.Loading
        MsalClient.acquireTokenSilentOrInteractive(
            activity = activity,
            scopes = scopes,
            onSuccess = { result ->
                MsalClient.isSharedDevice { isSharedDevice ->
                    _state.value = AuthState.SignedIn(
                        accessToken = result.accessToken,
                        userName = result.account.username,
                        isSharedDevice = isSharedDevice
                    )
                }
            },
            onError = { ex ->
                _state.value = AuthState.Error(ex.message ?: "Authentication failed")
            }
        )
    }

    fun signOut(activity: Activity) {
        _state.value = AuthState.Loading
        MsalClient.signOutFromSharedDevice(
            activity = activity,
            onSuccess = {
                _state.value = AuthState.SignedOut
            },
            onError = { ex ->
                _state.value = AuthState.Error(ex.message ?: "Sign out failed")
            }
        )
    }
}
