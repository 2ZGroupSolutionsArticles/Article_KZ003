package com.aws_azure_cognito.aws

import android.content.Context
import android.net.Uri
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.cognitoauth.Auth
import com.amazonaws.mobileconnectors.cognitoauth.AuthUserSession
import com.amazonaws.mobileconnectors.cognitoauth.handlers.AuthHandler
import com.amazonaws.mobileconnectors.cognitoauth.tokens.IdToken
import com.amazonaws.mobileconnectors.cognitoauth.util.LocalDataManager
import com.amazonaws.regions.Regions
import java.util.HashMap

class AWSAuthService(context: Context): AuthHandler {

    private var auth: Auth
    private var appRedirect: Uri

    private var authListener: AWSCognitoAuthServiceListener? = null
    private var authSession:  AuthUserSession? = null
    private var applicationContext: Context = context
    private var credProvider: CognitoCachingCredentialsProvider? = null


    /**
     * Setup authentication with Cognito.
     */

    init {
        val builder = Auth.Builder().setAppClientId(AWSConstants.COGNITO_CLIENT_ID)
                .setAppClientSecret(AWSConstants.COGNITO_CLIENT_SECRET)
                .setAppCognitoWebDomain(AWSConstants.COGNITO_WEB_DOMAIN)
                .setApplicationContext(applicationContext)
                .setAuthHandler(this)
                .setUserPoolId(AWSConstants.COGNITO_USER_POOL_ID)
                .setSignInRedirect(AWSConstants.COGNITO_APP_REDIRECT_URL)
                .setSignOutRedirect(AWSConstants.COGNITO_APP_REDIRECT_URL)
        auth = builder.build()
        appRedirect = Uri.parse(AWSConstants.COGNITO_APP_REDIRECT_URL)
    }

    fun getSession(listener: AWSCognitoAuthServiceListener) {
        authListener = listener
        auth.getSession()
    }

    fun logout(listener: AWSCognitoAuthServiceListener) {
        authListener = listener
        auth.signOut()
    }

    // This is workaround used instead of this.auth.isAuthenticated()
    // because an error in android-sdk-cognitoauth:
    // https://github.com/aws/aws-sdk-android/issues/508
    val isAuthenticated: Boolean
        get() {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val scope = setOf<String>()
                val session = LocalDataManager.getCachedSession(applicationContext, AWSConstants.COGNITO_CLIENT_ID, currentUser.username, scope)
                return session.isValidForThreshold
            }

            return false
        }


    fun updateListener(listener: AWSCognitoAuthServiceListener) {
        authListener = listener
    }

    /**
     * Obtain Cognito Tokens.
     */

    fun onResumeActivity(uri: Uri) {
        if (appRedirect.host == uri.host) {
            auth.getTokens(uri)
        }
    }

    /**
     * Credentials Provider
     * Create a credentials provider, or use the existing provider.
     * Set up as a credentials provider.
     */

    val credentialsProvider: CognitoCachingCredentialsProvider?
        get() {
            if (credProvider == null) {
                val idToken = idToken?.jwtToken

                if (idToken != null) {
                    credProvider = CognitoCachingCredentialsProvider(applicationContext,
                            AWSConstants.COGNITO_IDENTITY_POOL_ID,
                            Regions.fromName(AWSConstants.COGNITO_IDENTITY_POOL_REGION))

                    // set logins from jwtToken
                    val key = "cognito-idp." + AWSConstants.COGNITO_IDENTITY_POOL_REGION + ".amazonaws.com/" + AWSConstants.COGNITO_USER_POOL_ID
                    val logins   = HashMap<String, String>()
                    logins[key]  = idToken
                    credProvider?.logins = logins
                    return credProvider
                }
            }

            return credProvider
        }

    val username: String?
        get() = auth?.username

    private val idToken: IdToken?
        get() = authSession?.idToken


    /**
     * AuthHandler methods
     */

    override fun onSuccess(session: AuthUserSession?) {
        authSession = session
        authListener?.onGetSessionSuccess()
    }

    override fun onSignout() {
        authListener?.onSignOutSuccess()
    }

    override fun onFailure(ex: Exception) {
        /**
         * AWS Cognito Auth requires Chrome Custom Tabs to opens Cognito's hosted webpage's on Chrome.
         * Note Chrome is required on the Android device to use this SDK.
         * https://github.com/awslabs/aws-sdk-android-samples/tree/master/AmazonCognitoAuthDemo
         * This workaround handling exception when Cognito can't open custom tabs
         */
        val message = ex.message
        if (message != null && message.contains("No Activity found to handle Intent") && message.contains(AWSConstants.COGNITO_WEB_DOMAIN)) {
            authListener?.onFailure("Please install Chrome to be able to sign-in")
        } else {
            authListener?.onFailure(ex.toString())
        }
    }
}

