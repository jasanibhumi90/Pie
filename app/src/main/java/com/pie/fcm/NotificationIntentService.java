package com.pie.fcm;


import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

public class NotificationIntentService extends IntentService {
    public NotificationIntentService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }
/*
    private static final String TAG = NotificationIntentService.class.getSimpleName();

    public static final String ACTION_DONE = "action_done";
    public static final String ACTION_RESPOND = "action_respond";

    //Simple way to track the user's response history from the notification.
    //This is for demonstration purposes only; it is not recommended.
    private static List<CharSequence> responseHistory = new LinkedList<>();

    public NotificationIntentService() {
        super("NotificationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ACTION_DONE.equals(intent.getAction())) {
            responseHistory.clear();
            notificationManager.cancel(NOTIFICATION_INLINE_ACTION);
            return;
        }

        CharSequence reply = null;
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            reply = remoteInput.getCharSequence(NotificationUtil.KEY_INLINE_REPLY);
            responseHistory.add(0, reply);
            Log.i(TAG, reply.toString());
        } else {
            Intent activityIntent = new Intent(this, HomeActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //cancel the notification since we starting an Activity
            notificationManager.cancel(NOTIFICATION_INLINE_ACTION);
            startActivity(activityIntent);
            return;
        }


        //Do one of the following to stop the progress spinner in the Notification:

        //Option 1: Cancel the notification
        //notificationManager.cancel(NOTIFICATION_INLINE_ACTION);

        //Option 2: Issue a new Notification with the same ID
      *//*  if(!responseHistory.isEmpty()) {
            CharSequence[] history = new CharSequence[responseHistory.size()];
            NotificationUtil.inlineReplyNotification("Response has been processed", responseHistory.toArray(history));
        } else {
            NotificationUtil.inlineReplyNotification("Response has been processed");
        }*//*

    }*/
}
