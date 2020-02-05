package com.aws_azure_cognito

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.aws_azure_cognito.aws.AWSAuthService
import com.aws_azure_cognito.aws.AWSCognitoAuthServiceListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.newSingleThreadContext
import android.text.method.ScrollingMovementMethod

class MainActivity : AppCompatActivity() {

    private lateinit var cognitoAuthServiceListener: AWSCognitoAuthServiceListener

    private val cognitoAuthService: AWSAuthService by lazy {
        AWSAuthService(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupViews()
        setupCallbackHandlers()

        if (cognitoAuthService.isAuthenticated ) {
            getSession()
        } else {
            setupUnauthenticated()
        }
    }

    override fun onResume() {
        super.onResume()

        if (intent.data != null) {
            cognitoAuthService.onResumeActivity(intent.data)
        }
    }

    private fun getSession() {
        cognitoAuthService.getSession(cognitoAuthServiceListener)
    }

    private fun setupViews() {
        infoTextView.movementMethod = ScrollingMovementMethod()
    }

    private fun setupCallbackHandlers() {
        cognitoAuthServiceListener = object: AWSCognitoAuthServiceListener() {
            override fun onSignOutSuccess() {
                checkAuthentication()
            }

            override fun onGetSessionSuccess() {
                checkAuthentication()
            }

            override fun onFailure(error: String) {
                Log.i("Logout error", error)
            }
        }

        cognitoAuthService.updateListener(cognitoAuthServiceListener)
    }

    private fun checkAuthentication() {
        val isAuthenticated = cognitoAuthService.isAuthenticated
        if (isAuthenticated) {
            setupAuthenticated()
        } else {
            setupUnauthenticated()
        }
    }

    private fun setupAuthenticated() {
        loginButton.text  = getText(R.string.sign_out_button_title)
        infoTextView.text = cognitoAuthService.username

        loginButton.setOnClickListener{
            cognitoAuthService.logout(cognitoAuthServiceListener)
        }

        val credentialsProvider = cognitoAuthService.credentialsProvider
        launch(newSingleThreadContext("CredentialsProviderThread")) {
            val credentials = credentialsProvider?.credentials

            if (credentials != null) {
                launch(UI) {
                    infoTextView.append("\n\n awsAccessKeyId: \n\n" + credentials.awsAccessKeyId)
                    infoTextView.append("\n\n awsSecretKey: \n\n" + credentials.awsSecretKey)
                    infoTextView.append("\n\n sessionToken: \n\n" + credentials.sessionToken)
                }
            }
        }
    }

    private fun setupUnauthenticated() {
        loginButton.text  = getText(R.string.sign_in_button_title)
        infoTextView.text = getText(R.string.unauthenticated_text)

        loginButton.setOnClickListener{
            getSession()
        }
    }
}