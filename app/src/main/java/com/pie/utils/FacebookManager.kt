package com.pie.utils

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.share.ShareApi
import com.facebook.share.Sharer
import com.facebook.share.model.ShareLinkContent
import com.facebook.share.model.ShareVideo
import com.facebook.share.model.ShareVideoContent
import com.facebook.share.widget.ShareDialog
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

//import listeners.OnLoginListener;

class FacebookManager(private val activity: Activity) {
    private var callbackManager: CallbackManager? = null


    val facebookSession: AccessToken?
        get() = if (AccessToken.getCurrentAccessToken() == null)
            null
        else {
            AccessToken.getCurrentAccessToken()


        }

    init {
        initFacebook()
    }

    private fun initFacebook() {
        FacebookSdk.sdkInitialize(activity.applicationContext)
        AppEventsLogger.activateApp(activity)
        callbackManager = CallbackManager.Factory.create()
    }

    fun login(activity: Activity, onLoginListener: FacebookCallback<LoginResult>) {
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends", "email"))
        LoginManager.getInstance().registerCallback(callbackManager!!, onLoginListener)
    }

    fun loginWithPublishPermission(onLoginListener: FacebookCallback<LoginResult>) {
        LoginManager.getInstance()
            .logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends", "email"))
        //        LoginManager.getInstance().logInWithPublishPermissions(
        //                activity,
        //                Arrays.asList("publish_actions"));
        LoginManager.getInstance().registerCallback(callbackManager!!, onLoginListener)
    }

    fun login_share() {

        val permissions = AccessToken.getCurrentAccessToken().permissions
        if (permissions.contains("publish_actions")) {
            LoginManager.getInstance().registerCallback(callbackManager!!, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    shareDialog("SnapTask", "Desciption", "www.snaptask.com")
                }

                override fun onCancel() {

                }

                override fun onError(e: FacebookException) {

                }
            })
        } else {

            LoginManager.getInstance().logInWithPublishPermissions(
                activity,
                Arrays.asList("publish_actions")
            )


        }

        //        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends"));

        //        if(AccessToken.getCurrentAccessToken().ha)

        //        LoginManager.getInstance().logInWithPublishPermissions(
        //                activity_discover,
        //                Arrays.asList("publish_actions"));


    }

    fun logout() {
        LoginManager.getInstance().logOut()
    }

    fun shareDialog(title: String, description: String, contentURL: String) {
        if (ShareDialog.canShow(ShareLinkContent::class.java)) {
            val linkContent = ShareLinkContent.Builder()
                .setContentTitle(title)
                .setContentDescription(description)
                .setContentUrl(Uri.parse(contentURL))
                .build()
            val shareDialog = ShareDialog(activity)
            shareDialog.show(linkContent)

        }

    }


    fun shareLinks(title: String, imgUrl: String, url: String, onShareListener: FacebookCallback<Sharer.Result>) {
        val permissions = AccessToken.getCurrentAccessToken().permissions
        Log.e("FB Pemissions", (permissions + "-").toString())
        if (permissions.contains("publish_actions")) {
            Log.e("FB Pemissions", "has publish_permissions-")
            val content = ShareLinkContent.Builder()
                .setContentTitle(title)
                .setImageUrl(Uri.parse(imgUrl))
                .setContentUrl(Uri.parse(url))
                .build()
            ShareApi.share(content, onShareListener)
        } else {
            Log.e("FB Pemissions", "no has publish_permissions-")
            LoginManager.getInstance().logInWithPublishPermissions(
                activity,
                Arrays.asList("publish_actions")
            )
            LoginManager.getInstance().registerCallback(callbackManager!!, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {


                    Log.e("on success fb login", "$loginResult-")
                    shareLinks(title, imgUrl, url, onShareListener)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {
                    Log.e("on fail fb login", "$error-")
                    error.printStackTrace()
                }
            })

        }


    }

    /*   public void sharePhoto(String title, Bitmap image, FacebookCallback<Sharer.Result> onShareListener) {


        Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();
        if (permissions.contains("publish_actions")) {
            Log.e("permission", "true " + permissions);

            LoginManager.getInstance().logInWithPublishPermissions(
                    activity,
                    Arrays.asList("publish_actions"));
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image).setCaption(title)
                    .build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();
            ShareApi.share(content, onShareListener);
        } else {
            Log.e("permission", "false " + permissions);
        }

    }
*/
    /*
    public void appInvite() {


        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile", "user_friends"));

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        if (loginResult != null && !AppGlobal.isEmpty(loginResult.getAccessToken().getToken())) {
                            String appLinkUrl, previewImageUrl;
                            appLinkUrl = "https://fb.me/792182127552013"; */
    /*"https://play.google.com/store/apps/details?id=net.blipped.blipd";*//*

                            previewImageUrl = "https://gust-production.s3.amazonaws.com/uploads/startup/logo_image/601575/SnapTask-Profile-700x700.png";
//                            Toast.makeText(activity, "Login Done.", Toast.LENGTH_LONG).show();
                            if (AppInviteDialog.canShow()) {
                                AppInviteContent content = new AppInviteContent.Builder()
                                        .setApplinkUrl(appLinkUrl)
                                        .setPreviewImageUrl(previewImageUrl)
                                        .build();
                                AppInviteDialog.show(activity, content);
                            }
                        } else {
                            Toast.makeText(activity, "Something went wrong, Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(activity, "on Cancel.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        Toast.makeText(activity, "On Error." + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                }

        );


    }
*/

    fun shareVideo(title: String, videoFile: File, onShareListener: FacebookCallback<Sharer.Result>) {
        val videoFileUri = Uri.fromFile(videoFile)
        val video = ShareVideo.Builder()
            .setLocalUrl(videoFileUri)
            .build()
        val content = ShareVideoContent.Builder()
            .setVideo(video).setContentTitle(title)
            .build()
        ShareApi.share(content, onShareListener)
    }

    fun resetAccessToken() {
        AccessToken.setCurrentAccessToken(null)
    }

    fun hasAccessToken(): Boolean {

        return if (AccessToken.getCurrentAccessToken() == null)
            false
        else {
            !AccessToken.getCurrentAccessToken().isExpired
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    fun getKeyHash() {
        // Add code to print out the key hash
        try {
            val info = activity.packageManager.getPackageInfo(
                activity.packageName,
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                Log.e("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT))
            }
        } catch (e: PackageManager.NameNotFoundException) {

        } catch (e: NoSuchAlgorithmException) {

        }

    }

}