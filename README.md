# Setup AWS Cognito User and Identity Pools with an Azure AD identity provider to perform single sign-on (SSO) authentication in mobile app (Part 3).

This is the third part of the tutorial of how to setup AWS Cognito User and Identity Pools with an Azure AD identity provider to perform SSO authentication. It aims to setup your Android Project.

The complete sign-in flow described on the [diagram](https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-integrating-user-pools-with-identity-pools.html):

[![](https://lh5.googleusercontent.com/8uoS3mKNYRpm58BOzG7lcJLJXh5sv_HXa1H-58N08zR0U8lheQol-bgmU2woWaQTT3AiZnyehPgru75UhJVV5dkRyTnYcTPlm99OFxpundSqD3BeX2n9kG6zvNu6UIZOJKdoUdgd)](https://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-integrating-user-pools-with-identity-pools.html)

Please refer to the [first part](https://github.com/2ZGroupSolutionsArticles/Article_KZ001) of the tutorial for more information about sign-in process and settings for AWS Cognito and Azure AD.

After finishing the settings in AWS console and Azure portal you should have all of this parameters:

-   Amazon Cognito User Pool ID;
-   Amazon Cognito Domain associated with User Pool;
-   App client ID; 
-   App client secret;  
-   Callback URL;  
-   Sign Out URL;  
-   Region.
 
You should also have a test user account, assigned to the corresponding Azure AD enterprise application in Azure portal. Refer the [tutorial](https://github.com/2ZGroupSolutionsArticles/Article_KZ001/blob/master/README.md) how to invite a user.

The Android Project setup consists of 4 steps:
1.  Add dependencies to your ```app/build.gradle```.
2.  Add corresponding AWS keys to your project.
3.  Configure the Custom URL Scheme via intent-filter.
4.  Make corresponding changed in your source code.

 
### 1.  Add dependencies to your app/build.gradle.

AWS Cognito Auth requires Chrome Custom Tabs to opens Cognito's hosted web pages on Chrome. This also means that Chrome app should be installed on the device to use this SDK.
```
dependency {
  implementation 'com.amazonaws:aws-android-sdk-cognitoidentityprovider:2.11.0'  
  implementation 'com.amazonaws:aws-android-sdk-cognitoauth:2.11.0'
  implementation 'com.android.support:customtabs:28.0.0+'  
}
```
Make sync now you're ready to go further.

### 2. Add corresponding AWS keys to your project.

To authenticate users in a UserPool, get tokens, retrieve the last authenticated user on the device, and sign-out users you need to work with ``Auth`` object. ``Auth`` require from client app the set of keys to verify app client id, connect to the correct User and Identity Pools, redirect after sign-in and sign-out operations. You can keep these keys in the project in a strings file or separate object ( always keep in mind about security and storing sensitive information).

This is an example of how to create Auth object:
```
val builder = Auth.Builder().setAppClientId(_SETME_)
  .setAppClientSecret(_SETME_)
  .setAppCognitoWebDomain(_SETME_)
  .setApplicationContext(applicationContext)
  .setAuthHandler(this)
  .setUserPoolId(_SETME_)
  .setSignInRedirect(_SETME_)
  .setSignOutRedirect(_SETME_)
    
val auth = builder.build()
```
To provide AWS credentials to your app for access to AWS resources and services you can use ```CognitoCachingCredentialsProvider```. Before using this object you need to assign IAM role with your identity pool via the Cognito console. Otherwise you will have an InvalidIdentityPoolConfigurationException when access to the service.
```
val credProvider = CognitoCachingCredentialsProvider(applicationContext, 
                                                    _SETME_COGNITO_IDENTITY_POOL_ID,
                                                    _SETME_COGNITO_IDENTITY_POOL_REGION)
```
  

Fill the corresponding keys by replacing ```_SETME_``` with keys which you’ve prepared at the first part of the tutorial. To be sure that you set the correct keys to check the description for each key:

1. ```AppClientId``` and ```AppClientSecret``` - the client id and secret of your app, assigned to the User Pool in AWS Cognito. See the [tutorial](https://github.com/2ZGroupSolutionsArticles/Article_KZ001/blob/master/README.md) how to setup it.
    
2.  ```AWSCognitoWebDomain``` - [Amazon Cognito hosted domain](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-assign-domain.html) for the address of your sign-up and sign-in webpages. You can also use own web address as a custom domain. See the [tutorial](https://github.com/2ZGroupSolutionsArticles/Article_KZ001/blob/master/README.md) how to set Amazon Cognito hosted domain. Nothe that for the Android project AWSCognitoWebDomain should be used without protocol value (without the ``http://`` or ``https://`` at the beginning). For example, If your domain in AWS Cognito Console is:
    [https://ios-app-tutorial.auth.us-east-1.amazoncognito.com](https://ios-app-tutorial.auth.us-east-1.amazoncognito.com)
    
    You should set ```AWSCognitoWebDomain``` as:
    [ios-app-tutorial.auth.us-east-1.amazoncognito.com](https://ios-app-tutorial.auth.us-east-1.amazoncognito.com)

3.  ```UserPoolId``` - id of your Cognito User Pool. See the [tutorial](https://github.com/2ZGroupSolutionsArticles/Article_KZ001/blob/master/README.md) how to setup it.
    
4.  ```SignInRedirect``` - the URL which will be called after your app performs signIn operation. For the Andoid project, this is custom deep link like androidAppScheme:// (see the Defining a [Create Deep Links to App Content](https://developer.android.com/training/app-links/deep-linking) ). Note that in ```intent-filter``` ```scheme``` should be set without "://".
    
5.  ```SignOutRedirect``` - the URL which will be called after your app performs sign-out operation. You can use the same custom scheme as for CognitoAuthSignInRedirectUri or define another scheme. Make sure that CognitoAuthSignInRedirectUri and CognitoAuthSignOutRedirectUri which you set in the app match with corresponding values for your app client (in AWS User Pool app client settings).
    
6. ```COGNITO_IDENTITY_POOL_ID ```- id of your Cognito Identity Pool. See the [tutorial](https://github.com/2ZGroupSolutionsArticles/Article_KZ001/blob/master/README.md) how to setup it.
    
7.  ```COGNITO_IDENTITY_POOL_REGION``` -  [AWS Region](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/Concepts.RegionsAndAvailabilityZones.html) is separate geographic area where Amazon cloud computing resources are hosted. 
    
You should also update manifest:
-   add permissions:
```
<uses-permission  android:name="android.permission.INTERNET" />  
<uses-permission  android:name="android.permission.ACCESS_NETWORK_STATE" />
```
-   add intent-filter to invoke app specific Activity after successful authentication and signout.
   
```
<intent-filter>
  <action android:name="android.intent.action.VIEW" />
  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
  <data android:scheme="_SETME_>  
</intent-filter>
```
### 5.  Make corresponding changed in your source code
    
To handle authentication result Cognito Auth ```AuthHandler``` callback should be implemented. Make your class conforms to ```AuthHandler``` or create new handler object:
```
class AWSAuthService: AuthHandler {
  override fun onSuccess(session: AuthUserSession) {
  }
  
  override fun onSignout() {
  }
  
  override fun onFailure(e: Exception) {
  }
}
```
##### Initialize auth object:
 
 ``` 
val builder = Auth.Builder().setAppClientId(AWSConstants.COGNITO_CLIENT_ID)
.setAppClientSecret(AWSConstants.COGNITO_CLIENT_SECRET)
.setAppCognitoWebDomain(AWSConstants.COGNITO_WEB_DOMAIN)
.setApplicationContext(applicationContext)
.setAuthHandler(this)
.setUserPoolId(AWSConstants.COGNITO_USER_POOL_ID)
.setSignInRedirect(AWSConstants.COGNITO_APP_REDIRECT)
.setSignOutRedirect(AWSConstants.COGNITO_APP_REDIRECT)

val auth: Auth = builder.build()
```
 
Handle app redirect with Cognito tokens and pass them to ```auth```:
```
override fun onResume() {
  super.onResume()
  
  if (intent.data != null) {
    auth.getTokens(intent.data)
  }
}

```
##### Get session:
```
auth.getSession()
```
##### Get user identity credentials:
```
val credentialsProvider = CognitoCachingCredentialsProvider(applicationContext,   
                                                            AWSConstants.COGNITO_IDENTITY_POOL_ID, 
                                                            Regions.fromName(AWSConstants.COGNITO_IDENTITY_POOL_REGION))
val credentials = credentialsProvider?.credentials
```
Avoid call `credentials` and `identityId` from  the main thread. In the sample app for background invocation used Kotlin [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)

##### Sign-out: 
```
auth.signOut()
```
You can check the [demo project](https://github.com/2ZGroupSolutionsArticles/Article_KZ003/tree/master/AWS_Azure_Cognito), replace ```_SETME_``` values in AWSConstants file with own to make it work.

#### Additional Sources:

- https://github.com/awslabs/aws-sdk-android-samples/tree/master/AmazonCognitoAuthDemo
- [https://github.com/awslabs/aws-sdk-android-samples/tree/master/AmazonCognitoYourUserPoolsDemo](https://github.com/awslabs/aws-sdk-android-samples/tree/master/AmazonCognitoYourUserPoolsDemo)
- [https://docs.aws.amazon.com/cognito/latest/developerguide/getting-started-with-identity-pools.html](https://docs.aws.amazon.com/cognito/latest/developerguide/getting-started-with-identity-pools.html)
- [http://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-federation-beta-release-overview.html](http://docs.aws.amazon.com/cognito/latest/developerguide/amazon-cognito-federation-beta-release-overview.html)
- [https://docs.aws.amazon.com/cognito/latest/developerguide/identity-pools.html](https://docs.aws.amazon.com/cognito/latest/developerguide/identity-pools.html)
- [https://docs.microsoft.com/en-us/azure/active-directory/saas-apps/amazon-web-service-tutorial](https://docs.microsoft.com/en-us/azure/active-directory/saas-apps/amazon-web-service-tutorial)
- [https://aws.amazon.com/blogs/mobile/amazon-cognito-user-pools-supports-federation-with-saml](https://aws.amazon.com/blogs/mobile/amazon-cognito-user-pools-supports-federation-with-saml)
- [https://docs.microsoft.com/en-us/azure/active-directory/active-directory-enterprise-apps-manage-sso](https://docs.microsoft.com/en-us/azure/active-directory/active-directory-enterprise-apps-manage-sso)
- [https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-token-and-claims](https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-token-and-claims)
- [https://go.microsoft.com/fwLink/?LinkID=717349#configuring-and-testing-azure-ad-single-sign-on](https://go.microsoft.com/fwLink/?LinkID=717349#configuring-and-testing-azure-ad-single-sign-on)
- [https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-saml-idp.html](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-saml-idp.html)


#### Author

Kseniia Zozulia

Email:  [kseniiazozulia@2zgroup.net](mailto:kseniiazozulia@2zgroup.net)

LinkedIn:  [Kseniia Zozulia](https://www.linkedin.com/in/629bb187)
