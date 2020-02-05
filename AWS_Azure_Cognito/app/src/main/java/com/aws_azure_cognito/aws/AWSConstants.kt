package com.aws_azure_cognito.aws

object AWSConstants {
    /*
     * USER POOL Settings: ID of User Pool with SAML identity provider from Cognito Console.
     */
    const val COGNITO_USER_POOL_ID = "_SETME_"
    // IMPORTANT: without protocol value e.g without the http:// or https:// at the beginning
    const val COGNITO_WEB_DOMAIN = "_SETME_"

    /*
     * App client settings: CLIENT_ID, CLIENT_SECRET, APP_REDIRECT for app client
     * associated with User Pull in AWS Cognito Console.
     */
    const val COGNITO_CLIENT_ID = "_SETME_"
    const val COGNITO_CLIENT_SECRET = "_SETME_"

    // example: redirect URL in Cognito console "androidAppScheme://".
    // IMPORTANT: In the app you should set "androidAppScheme" without "://".
    const val COGNITO_APP_REDIRECT_URL = "_SETME_"

    /*
     * IDENTITY POOL Settings.
     */
    const val COGNITO_IDENTITY_POOL_ID = "_SETME_"
    const val COGNITO_IDENTITY_POOL_REGION = "_SETME_"
}

