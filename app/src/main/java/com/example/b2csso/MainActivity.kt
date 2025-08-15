
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MsalClient.init(this) {
            setContent {
                AppTheme {
                    val vm: MsalViewModel = viewModel()
                    var token by remember { mutableStateOf<String?>(null) }

                    if (token == null) {
                        AuthGate(viewModel = vm, onSignedIn = { token = it })
                    } else {
                        HomeScreen(accessToken = token!!)
                    }
                }
            }
        }
    }
}
