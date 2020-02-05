package com.aws_azure_cognito.aws

import com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession



abstract class AWSCognitoAuthServiceListener {
    abstract fun onSignOutSuccess()
    abstract fun onGetSessionSuccess()
    abstract fun onFailure(error: String)
}


