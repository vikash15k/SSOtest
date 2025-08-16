
package com.example.b2csso.msal

import android.app.Activity
import android.content.Context
import com.example.b2csso.R
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException

object MsalClient {
    private var multipleAccountApp: IMultipleAccountPublicClientApplication? = null

    fun init(context: Context, onReady: () -> Unit) {
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            context,
            R.raw.msal_config,
            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    multipleAccountApp = application
                    onReady()
                }
                override fun onError(exception: MsalException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    fun isSharedDevice(callback: (Boolean) -> Unit) {
        val app = multipleAccountApp
        if (app != null) {
            app.isSharedDevice(object : IPublicClientApplication.IsSharedDeviceCallback {
                override fun onResult(isSharedDevice: Boolean) {
                    callback(isSharedDevice)
                }
                override fun onError(exception: MsalException) {
                    exception.printStackTrace()
                    callback(false)
                }
            })
        } else {
            callback(false)
        }
    }

    fun getCurrentAccount(callback: (IAccount?) -> Unit) {
        val app = multipleAccountApp ?: return callback(null)
        
        app.getDeviceMode(object : IPublicClientApplication.DeviceModeCallback {
            override fun onResult(deviceMode: IDeviceMode) {
                if (deviceMode.isSharedDevice) {
                    // In shared device mode, get the current account
                    app.getCurrentAccountAsync(object : IMultipleAccountPublicClientApplication.CurrentAccountCallback {
                        override fun onAccountLoaded(activeAccount: IAccount?) {
                            callback(activeAccount)
                        }
                        override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                            callback(currentAccount)
                        }
                        override fun onError(exception: MsalException) {
                            exception.printStackTrace()
                            callback(null)
                        }
                    })
                } else {
                    // Not in shared device mode, get accounts normally
                    app.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
                        override fun onTaskCompleted(result: MutableList<IAccount>?) {
                            callback(result?.firstOrNull())
                        }
                        override fun onError(exception: MsalException) {
                            callback(null)
                        }
                    })
                }
            }
            override fun onError(exception: MsalException) {
                exception.printStackTrace()
                callback(null)
            }
        })
    }

    fun acquireTokenSilentOrInteractive(
        activity: Activity,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = multipleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))

        getCurrentAccount { account ->
            if (account != null) {
                // Try silent authentication first
                app.acquireTokenSilentAsync(
                    scopes.toTypedArray(),
                    account,
                    "https://login.microsoftonline.com/common",
                    object : SilentAuthenticationCallback {
                        override fun onSuccess(authenticationResult: IAuthenticationResult) {
                            onSuccess(authenticationResult)
                        }
                        override fun onError(exception: MsalException) {
                            // If silent fails, try interactive
                            acquireInteractive(activity, scopes, onSuccess, onError)
                        }
                    }
                )
            } else {
                // No account found, start interactive authentication
                acquireInteractive(activity, scopes, onSuccess, onError)
            }
        }
    }

    private fun acquireInteractive(
        activity: Activity,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = multipleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))
        
        val parameters = AcquireTokenParameters.Builder()
            .startAuthorizationFromActivity(activity)
            .withScopes(scopes)
            .withCallback(object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    onSuccess(authenticationResult)
                }
                override fun onError(exception: MsalException) {
                    onError(exception)
                }
                override fun onCancel() {
                    onError(Exception("User cancelled"))
                }
            })
            .build()

        app.acquireToken(parameters)
    }

    fun signOutFromSharedDevice(
        activity: Activity,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = multipleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))

        app.getDeviceMode(object : IPublicClientApplication.DeviceModeCallback {
            override fun onResult(deviceMode: IDeviceMode) {
                if (deviceMode.isSharedDevice) {
                    // In shared device mode, sign out from device
                    app.signOutFromSharedDevice(object : IMultipleAccountPublicClientApplication.SignOutCallback {
                        override fun onSignOut() {
                            onSuccess()
                        }
                        override fun onError(exception: MsalException) {
                            onError(exception)
                        }
                    })
                } else {
                    // Regular sign out for non-shared devices
                    getCurrentAccount { account ->
                        if (account != null) {
                            app.removeAccount(account, object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
                                override fun onRemoved() {
                                    onSuccess()
                                }
                                override fun onError(exception: MsalException) {
                                    onError(exception)
                                }
                            })
                        } else {
                            onSuccess()
                        }
                    }
                }
            }
            override fun onError(exception: MsalException) {
                onError(exception)
            }
        })
    }
}
