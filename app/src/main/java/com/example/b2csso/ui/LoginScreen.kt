
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
fun AuthGate(
    viewModel: MsalViewModel, 
    onSignedIn: (String, String?, Boolean) -> Unit,
    onSignedOut: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        viewModel.checkCurrentState()
    }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        when (val s = state) {
            is AuthState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Checking authentication state...")
                }
            }
            is AuthState.SignedIn -> {
                onSignedIn(s.accessToken, s.userName, s.isSharedDevice)
                // This will be handled by the parent composable
            }
            is AuthState.SignedOut -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Microsoft Authenticator SSO",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text(
                                text = "Sign in using the account already logged into Microsoft Authenticator on this device",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Button(
                                onClick = { activity?.let { viewModel.signIn(it) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Sign In with Microsoft")
                            }
                        }
                    }
                }
            }
            is AuthState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Authentication Error",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            Text(
                                text = s.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Button(
                                onClick = { activity?.let { viewModel.signIn(it) } },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Try Again")
                            }
                        }
                    }
                }
            }
        }
    }
}
