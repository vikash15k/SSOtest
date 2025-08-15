
package com.example.b2csso.msal

import android.app.Activity
import android.content.Context
import com.example.b2csso.R
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalException

object MsalClient {
    private var singleAccountApp: ISingleAccountPublicClientApplication? = null

    fun init(context: Context, onReady: () -> Unit) {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            context,
            R.raw.msal_config,
            object : ISingleAccountPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    singleAccountApp = application
                    onReady()
                }
                override fun onError(exception: MsalException) {
                    exception.printStackTrace()
                }
            }
        )
    }

    fun acquireTokenSilentOrInteractive(
        activity: Activity,
        scopes: List<String>,
        authority: String,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = singleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))

        app.getCurrentAccountAsync(object :
            ISingleAccountPublicClientApplication.CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                if (activeAccount != null) {
                    app.acquireTokenSilentAsync(
                        scopes.toTypedArray(),
                        authority,
                        object : SilentAuthenticationCallback {
                            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                                onSuccess(authenticationResult)
                            }
                            override fun onError(exception: MsalException) {
                                acquireInteractive(activity, scopes, onSuccess, onError)
                            }
                        }
                    )
                } else {
                    acquireInteractive(activity, scopes, onSuccess, onError)
                }
            }
            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {}
            override fun onError(exception: MsalException) {
                acquireInteractive(activity, scopes, onSuccess, onError)
            }
        })
    }

    private fun acquireInteractive(
        activity: Activity,
        scopes: List<String>,
        onSuccess: (IAuthenticationResult) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val app = singleAccountApp ?: return onError(IllegalStateException("MSAL not initialized"))
        app.acquireToken(
            activity,
            scopes.toTypedArray(),
            object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    onSuccess(authenticationResult)
                }
                override fun onError(exception: MsalException) {
                    onError(exception)
                }
                override fun onCancel() {
                    onError(Exception("User cancelled"))
                }
            }
        )
    }
}
