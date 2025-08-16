# Microsoft Authenticator SSO Android App

This Android app demonstrates how to implement Single Sign-On (SSO) using Microsoft Authenticator and MSAL shared device mode for Entra ID authentication.

## Features

- **Shared Device Mode**: Supports MSAL shared device mode for seamless SSO
- **Microsoft Authenticator Integration**: Leverages existing Authenticator app login
- **Device-wide Sign-out**: Proper sign-out handling in shared device scenarios
- **Modern UI**: Built with Jetpack Compose and Material 3

## Prerequisites

1. **Microsoft Authenticator App**: Must be installed and signed in with an Entra ID account
2. **Entra ID App Registration**: Your app must be registered in Azure Active Directory
3. **Android Device**: API level 24+ (Android 7.0+)

## Setup Instructions

### 1. Azure Active Directory Setup

1. Go to [Azure Portal](https://portal.azure.com) → Azure Active Directory → App registrations
2. Create a new app registration or use existing one
3. In **Authentication** settings:
   - Add Android platform
   - Package name: `com.example.b2csso`
   - Signature hash: Get this from your development certificate
4. In **API permissions**:
   - Add `User.Read` permission
   - Grant admin consent if required

### 2. Get Your Signature Hash

Run this command to get your signature hash:

```bash
keytool -exportcert -alias androiddebugkey -keystore ~/.android/debug.keystore | openssl sha1 -binary | openssl base64
```

Default password for debug keystore is: `android`

### 3. Update Configuration

1. Open `app/src/main/res/raw/msal_config.json`
2. Replace `YOUR_ENTRA_ID_CLIENT_ID` with your actual Client ID from Azure
3. Update the redirect URI with your signature hash:
   ```json
   "redirect_uri": "msauth://com.example.b2csso/YOUR_SIGNATURE_HASH"
   ```

### 4. Microsoft Authenticator Setup

1. Install Microsoft Authenticator app on your device
2. Sign in with your Entra ID account
3. Ensure the account is active and not expired

## How It Works

### Shared Device Mode

The app automatically detects if it's running in shared device mode:

- **Shared Device**: Uses device-wide authentication via Microsoft Authenticator
- **Personal Device**: Falls back to standard MSAL authentication

### Authentication Flow

1. **Initial Check**: App checks if user is already authenticated via Authenticator
2. **Silent Authentication**: Attempts to get tokens silently if user is signed in
3. **Interactive Authentication**: Prompts for sign-in if needed
4. **Token Management**: Handles refresh tokens and silent renewals

### Sign-out Behavior

- **Shared Device**: Signs out device-wide (affects all apps)
- **Personal Device**: Signs out only from this app

## Code Structure

```
app/src/main/java/com/example/b2csso/
├── MainActivity.kt                 # Main activity with Compose UI
├── MsalViewModel.kt               # Authentication state management
├── msal/
│   └── MsalClient.kt             # MSAL wrapper with shared device support
└── ui/
    ├── HomeScreen.kt             # Post-authentication UI
    ├── LoginScreen.kt            # Authentication gate
    └── Theme.kt                  # Material 3 theming
```

## Configuration Details

### MSAL Config (`msal_config.json`)

```json
{
  "client_id": "YOUR_CLIENT_ID",
  "redirect_uri": "msauth://com.example.b2csso/SIGNATURE_HASH",
  "shared_device_mode_supported": true,
  "broker_redirect_uri_registered": true,
  "authorities": [
    {
      "type": "AAD",
      "authority_url": "https://login.microsoftonline.com/common",
      "default": true
    }
  ]
}
```

Key settings:
- `shared_device_mode_supported: true` - Enables shared device mode
- `broker_redirect_uri_registered: true` - Uses Microsoft Authenticator as broker
- `type: "AAD"` - Uses Azure Active Directory (not B2C)

## Troubleshooting

### Common Issues

1. **"Broker not available"**
   - Ensure Microsoft Authenticator is installed
   - Check that the account is signed in to Authenticator
   - Verify redirect URI matches your signature hash

2. **"Invalid client"**
   - Verify Client ID in `msal_config.json`
   - Check app registration in Azure portal
   - Ensure package name matches registration

3. **"Authority validation failed"**
   - Verify authority URL in configuration
   - Check tenant settings in Azure

### Debug Tips

1. Enable MSAL logging in `msal_config.json`:
   ```json
   "logging": {
     "pii_enabled": false,
     "log_level": "VERBOSE",
     "logcat_enabled": true
   }
   ```

2. Check Android logs:
   ```bash
   adb logcat | grep -i msal
   ```

## Security Considerations

- **Shared Device Mode**: Only enable on managed/corporate devices
- **Token Storage**: MSAL handles secure token storage automatically
- **Device Registration**: Requires admin consent for shared device scenarios
- **Account Policies**: Respects organizational policies set in Entra ID

## Testing

### Test Scenarios

1. **First Time Setup**: Fresh install with Authenticator already signed in
2. **Token Refresh**: App usage after token expiry
3. **Network Changes**: Authentication during network switching
4. **Sign-out**: Both individual and device-wide sign-out

### Test Devices

- Test on both shared devices (managed) and personal devices
- Verify behavior with multiple accounts in Authenticator
- Test with expired/revoked accounts

## Additional Resources

- [MSAL Android Documentation](https://docs.microsoft.com/en-us/azure/active-directory/develop/msal-android-overview)
- [Shared Device Mode Guide](https://docs.microsoft.com/en-us/azure/active-directory/develop/msal-android-shared-devices)
- [Microsoft Authenticator App](https://www.microsoft.com/en-us/security/mobile-authenticator-app)
