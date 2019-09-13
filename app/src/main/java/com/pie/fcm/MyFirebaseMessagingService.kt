package com.pie.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.app.utils.RxBus
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.pie.BuildConfig
import com.pie.PieApp
import com.pie.model.PushNotificationModel
import com.pie.utils.AppConstant
import com.pie.utils.AppGlobal
import com.pie.utils.AppLogger
import com.pie.utils.*

class MyFirebaseMessagingService : FirebaseMessagingService() {
   /* val pref by lazy {
        PieApp.getInstance().getAppPreferencesHelper()
    }
    val somoDao by lazy {
        PieApp.getInstance().somoDao
    }
    private val friendDataBaseAction by lazy {
        FriendDataBaseAction()
    }

    companion object {
        private var NOTIFICATION_ID = 111
        private fun createNotification(messageBody: String?, title: String?, type: String?, data: String?, vararg showIt: Boolean) {
            var pushNotification: PushNotificationModel? = null
            if (showIt.isEmpty() || !showIt[0])
                return
            try {
                pushNotification = Gson().fromJson(data, PushNotificationModel::class.java)
                if (type == N_TYPE_APP_UPDATE && pushNotification.app_version.toIntOrNull()
                    ?: 0 <= BuildConfig.VERSION_CODE) {
                    return
                }
            } catch (e: Exception) {
                return
            }
            val displayTitle = if (AppGlobal.isEmpty(title)) getString(R.string.app_name) else title
            val intent = Intent(this, SplashActivity::class.java)

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(EXTRA_NOTIFICATION_DATA, data)
            val resultIntent = PendingIntent.getActivity(this, NOTIFICATION_ID++, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationSoundURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val mNotificationBuilder = NotificationCompat.Builder(this, getString(R.string.channel_name))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentTitle(displayTitle)
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText(messageBody))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(notificationSoundURI)
                .setContentIntent(resultIntent)

            if (type == N_TYPE_WITHDRAW_COMPLETE || type == N_TYPE_APP_UPDATE || (type == N_TYPE_LINK_REDIRECTION &&
                        !pushNotification?.redirection_link.isNullOrEmpty())) {
                val contentView = RemoteViews(packageName, R.layout.notification_small)
                mNotificationBuilder.setCustomContentView(contentView)
                val expandedView = RemoteViews(packageName, R.layout.notification_expanded)
                mNotificationBuilder.setCustomBigContentView(expandedView)

                contentView.setTextViewText(R.id.tvTitle, title ?: getString(R.string.app_name))
                contentView.setTextViewText(R.id.tvDesc, messageBody)

                expandedView.setTextViewText(R.id.tvTitle, title ?: getString(R.string.app_name))
                expandedView.setTextViewText(R.id.tvDesc, messageBody)

                when (type) {
                    N_TYPE_WITHDRAW_COMPLETE -> {
                        contentView.setTextViewText(R.id.tvAction, getString(R.string.rate_us))
                        expandedView.setTextViewText(R.id.tvAction, getString(R.string.rate_us))

                        val rateUsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        val rateUsPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID++, rateUsIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)
                        mNotificationBuilder.setContentIntent(rateUsPendingIntent)
                    }
                    N_TYPE_APP_UPDATE -> {
                        contentView.setTextViewText(R.id.tvAction, getString(R.string.update_now))
                        expandedView.setTextViewText(R.id.tvAction, getString(R.string.update_now))

                        val rateUsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
                        val rateUsPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID++, rateUsIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)
                        mNotificationBuilder.setContentIntent(rateUsPendingIntent)
                    }
                    N_TYPE_LINK_REDIRECTION -> {
                        contentView.setTextViewText(R.id.tvAction, "Click Here")
                        expandedView.setTextViewText(R.id.tvAction, "Click Here")

                        if (!pushNotification?.redirection_link.isNullOrEmpty()) {
                            val myIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pushNotification?.redirection_link))
                            val myPendingIntent = PendingIntent.getActivity(this, NOTIFICATION_ID++, myIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT)
                            mNotificationBuilder.setContentIntent(myPendingIntent)
                        }
                    }
                }
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_HIGH
                val name = getString(R.string.channel_name)
                val mChannel = NotificationChannel(getString(R.string.channel_name), name, importance)
                mChannel.enableLights(true)
                mChannel.lightColor = Color.MAGENTA
                mChannel.enableVibration(true)
                notificationManager.createNotificationChannel(mChannel)

            }

            val notification = mNotificationBuilder.build()

            val smallIconId = resources
                .getIdentifier("right_icon", "id", android.R::class.java.getPackage()?.name)
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

            if (smallIconId != 0) {
                @Suppress("DEPRECATION")
                if (notification.contentView != null) {
                    notification.contentView.setViewVisibility(smallIconId, View.INVISIBLE)
                }
            }

            notificationManager.notify(NOTIFICATION_ID++, notification)

        }

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("NEW_TOKEN", token)
    }

    @SuppressLint("ShowToast")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
//        super.onMessageReceived(remoteMessage)
//        {"message":"Alia Bhatt and 19 others people liked your post","sender_id":77,"receiver_id":123,"post_id":319,"type":"like_post"}

        //region Like
*//*        {"data":"{\"post_id\":461,\"receiver_id\":6,\"message\":\"User 58 liked your post\",\"type\":\"like_post\",\"user\":{\"is_private\":0,\"name\":\"User 58\",\"id\":136,\"avatar\":\"http:\\/\\/somo.virgoexchange.com\\/user\\/avatar\\/1537334979.SOMO1537334977809_.jpg\",\"username\":\"user58\"},\"sender_id\":136}","mtype":"like_post","message":"User 58 liked your post"}*//*
        //endregion

        // region post Comment
        *//*{"data":"{\"post_id\":468,\"receiver_id\":6,\"description\":\"nice post....\",\"message\":\"User 58 commented on your post.\",\"comment_id\":488,\"type\":\"comment_post\",\"user\":{\"is_private\":0,\"name\":\"User 58\",\"id\":136,\"avatar\":\"http:\\/\\/somo.virgoexchange.com\\/user\\/avatar\\/1537334979.SOMO1537334977809_.jpg\",\"username\":\"user58\"},\"sender_id\":136}","mtype":"comment_post","message":"User 58 commented on your post."}*//*
        //endregion

        //region replied comment

        *//*{"data":"{\"post_id\":468,\"receiver_id\":6,\"description\":\"hiiii\",\"id\":1133,\"message\":\"User 58 and 1 other people replied on your comment.\",\"comment_id\":503,\"type\":\"comment_reply\",\"user\":{\"is_private\":0,\"name\":\"User 58\",\"id\":136,\"avatar\":\"http:\\/\\/somo.virgoexchange.com\\/user\\/avatar\\/1537334979.SOMO1537334977809_.jpg\",\"username\":\"user58\"},\"sender_id\":136}","mtype":"comment_reply","message":"User 58 and 1 other people replied on your comment."}*//*
        //endregion


        try {
            val data = remoteMessage.data
            val pushNotification = Gson().fromJson(data["data"], PushNotificationModel::class.java)

            when (pushNotification.type) {
                N_TYPE_FRIEND -> {
                   // friendDataBaseAction.friendUser(pushNotification.sender_id, pushNotification.sender)
                }
              *//*  N_TYPE_MESSAGE -> {
                    val messageBody = Gson().fromJson(pushNotification.message, MessageBody::class.java)
                    PushUtils.sendMessagePush(this, messageBody.payload, true)
                    return
                }
                N_TYPE_ACCEPT_FRIEND -> {
                    friendDataBaseAction.acceptFriend(pushNotification.sender_id, pushNotification.sender)
                    friendDataBaseAction.deleteRequestById(pushNotification.sender_id)
                }

                N_TYPE_UN_FRIEND -> {
                    friendDataBaseAction.unFriendUser(pushNotification.sender_id)
                    friendDataBaseAction.deleteRequestById(pushNotification.sender_id)
                }
                N_TYPE_PROFILE_VIEW -> pref.addViewProfileCount(1)
                N_TYPE_FOLLOW -> {
                    friendDataBaseAction.followUser(pushNotification.sender_id, pushNotification.sender)
                }
                N_TYPE_ACCEPT_FOLLOW -> {
                    friendDataBaseAction.acceptFollow(pushNotification.sender_id, pushNotification.sender)
                    friendDataBaseAction.deleteRequestById(pushNotification.sender_id)
                }
                N_TYPE_REJECT_FOLLOW, N_TYPE_DECLINE_FRIEND, N_TYPE_CANCEL_FRIEND_REQUEST -> {
                    friendDataBaseAction.deleteRequestById(pushNotification.sender_id)
                }
                N_TYPE_UN_FOLLOW -> {
                    friendDataBaseAction.unFollowUser(pushNotification.sender_id)
                    friendDataBaseAction.deleteRequestById(pushNotification.sender_id)

                }

                N_TYPE_BLOCK_USER -> {
                    friendDataBaseAction.notifyBlockUser(pref.getUserData().id, pushNotification.sender_id)
                }
                N_TYPE_JUST_JOIN -> {
                    friendDataBaseAction.deleteContact(pushNotification.mobile)
                }
                N_TYPE_TRANSFER_COIN, N_TYPE_REJECT_PAYMENT, N_TYPE_REFFERAL -> pref.setWalletCoin(pushNotification.wallet)
       *//*     }

           *//* // todo if not require to show notification and perform action send data to RxBus
            setCount(pushNotification.type)

            if ((pushNotification.type == N_TYPE_LIKE_COMMENT || pushNotification.type == N_TYPE_LIKE_POST) && !pref.isEnableLikesNotification()) {
                return
            }
            if ((pushNotification.type == N_TYPE_COMMENT_POST || pushNotification.type == N_TYPE_COMMENT_REPLY) && !pref.isEnableCommentNotification()) {
                return
            }

            if ((pushNotification.type == N_TYPE_FRIEND || pushNotification.type == N_TYPE_FOLLOW) && !pref.isEnableFriendFollowNotification()) {
                return
            }*//*


          *//*  val bundle = Bundle()
            bundle.putBoolean(AppConstant.ARG_RECEIVE_NOTIFICATION, true)
            bundle.putSerializable(AppConstant.NOTIFICATION_DATA, pushNotification)
            bundle.putSerializable(AppConstant.NOTIFICATION_TYPE, pushNotification.type)
            bundle.putString(AppConstant.EXTRA_USER_ID, pushNotification.sender_id)
            if (AppGlobal.isAppIsInBackground(applicationContext)) {
                if (pushNotification.type == N_TYPE_PROFILE_VIEW || pushNotification.type == N_TYPE_UN_FOLLOW ||
                        pushNotification.type == N_TYPE_REJECT_FOLLOW || pushNotification.type == N_TYPE_DECLINE_FRIEND ||
                        pushNotification.type == N_TYPE_UN_FRIEND || pushNotification.type == N_TYPE_REPORT_POST ||
                        pushNotification.type == N_TYPE_BLOCK_USER || pushNotification.type == N_TYPE_CANCEL_FRIEND_REQUEST) {
                    return
                } else {
                    createNotification(pushNotification.message, pushNotification.title,
                            pushNotification.type, data["data"], true)
                }
            } else {
                when {
                    pushNotification.type == N_TYPE_TRANSFER_COIN || pushNotification.type == N_TYPE_GIFT ||
                            pushNotification.type == N_TYPE_REJECT_PAYMENT || pushNotification.type == N_TYPE_REFFERAL ||
                            pushNotification.type == N_TYPE_REPLY_PROBLEM || pushNotification.type == N_TYPE_JUST_JOIN ||
                            pushNotification.type == N_TYPE_BEACTIVE || pushNotification.type == N_TYPE_FRIENDS_LAST_MONTH_EARNING ||
                            pushNotification.type == N_TYPE_SHARE_LAST_MONTH_EARNING -> {
                        createNotification(pushNotification.message, pushNotification.title,
                                pushNotification.type, data["data"], true)
                        RxBus.publish(Bundle(bundle))
                    }
                    else -> {
                        if (pushNotification.type != N_TYPE_PROFILE_VIEW && pushNotification.type != N_TYPE_UN_FOLLOW &&
                                pushNotification.type != N_TYPE_REJECT_FOLLOW && pushNotification.type != N_TYPE_DECLINE_FRIEND &&
                                pushNotification.type != N_TYPE_UN_FRIEND && pushNotification.type != N_TYPE_REPORT_POST &&
                                pushNotification.type != N_TYPE_BLOCK_USER && pushNotification.type != N_TYPE_CANCEL_FRIEND_REQUEST) {
                            bundle.putBoolean(AppConstant.SETBADGE, true)
                        }
                        RxBus.publish(Bundle(bundle))
                    }
                }
            }
            AppLogger.e("remoteMessage", Gson().toJson(remoteMessage.data))
        } catch (e: Exception) {
            AppLogger.e("onMessageReceived", e)
        }*//*
   *//* }

    private fun setCount(type: String) {
       *//**//* if (type == N_TYPE_FOLLOW || type == N_TYPE_ACCEPT_FOLLOW || type == N_TYPE_FRIEND || type == N_TYPE_ACCEPT_FRIEND) {
            pref.setNotificationCount(NotificationCount(request = pref.getNotificationCount()!!.request + 1))
        } else if (type == N_TYPE_LIKE_POST || type == N_TYPE_COMMENT_POST ||
                type == N_TYPE_COMMENT_REPLY || type == N_TYPE_LIKE_COMMENT || type == N_TYPE_REPORT_POST) {
            pref.setNotificationCount(NotificationCount(activity = pref.getNotificationCount()!!.activity + 1))
        }*//**//*

*/
}




