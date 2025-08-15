
package com.example.b2csso.ui

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.b2csso.AuthState
import com.example.b2csso.MsalViewModel

@Composable
fun AuthGate(viewModel: MsalViewModel, onSignedIn: (String) -> Unit) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        activity?.let { viewModel.signIn(it) }
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        when (val s = state) {
            is AuthState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Signing you in...")
                }
            }
            is AuthState.SignedIn -> {
                onSignedIn(s.accessToken)
                Text("Signed in")
            }
            is AuthState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = s.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { activity?.let { viewModel.signIn(it) } }) {
                        Text("Try Again")
                    }
                }
            }
        }
    }
}
