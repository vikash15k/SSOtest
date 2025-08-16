
package com.example.b2csso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.b2csso.msal.MsalClient
import com.example.b2csso.ui.AppTheme
import com.example.b2csso.ui.AuthGate
import com.example.b2csso.ui.HomeScreen

data class UserState(
    val token: String,
    val userName: String?,
    val isSharedDevice: Boolean
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MsalClient.init(this) {
            setContent {
                AppTheme {
                    val vm: MsalViewModel = viewModel()
                    var userState by remember { mutableStateOf<UserState?>(null) }

                    if (userState == null) {
                        AuthGate(
                            viewModel = vm,
                            onSignedIn = { token, userName, isSharedDevice ->
                                userState = UserState(token, userName, isSharedDevice)
                            },
                            onSignedOut = {
                                userState = null
                            }
                        )
                    } else {
                        HomeScreen(
                            accessToken = userState!!.token,
                            userName = userState!!.userName,
                            isSharedDevice = userState!!.isSharedDevice,
                            onSignOut = {
                                vm.signOut(this@MainActivity)
                                userState = null
                            }
                        )
                    }
                }
            }
        }
    }
}
