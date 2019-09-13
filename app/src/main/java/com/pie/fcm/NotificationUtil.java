package com.pie.fcm;

import java.util.LinkedList;
import java.util.List;

public class NotificationUtil {
    private final static String GROUP_KEY_BUNDLED = "group_key_bundled";

    public static final String KEY_INLINE_REPLY = "KEY_INLINE_REPLY";

    public static final int NOTIFICATION_INLINE_ACTION = 1;
    private static final int NOTIFICATION_BUNDLED_BASE_ID = 1000;

    //simple way to keep track of the number of bundled notifications
    private static int numberOfBundled = 0;
    //Simple way to track text for notifications that have already been issued
    private static List<CharSequence> issuedMessages = new LinkedList<>();

    /*public static void inlineReplyNotification(String message) {
        inlineReplyNotification(message, null);
    }*/

   /* public static void inlineReplyNotification(String message, CharSequence[] history) {
        Context mContext = HabeshaApp.mContext;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        NotificationCompat.Builder builder = inlineReplyBuilder();
        builder.setContentText(message);
        if(history != null) {
            builder.setRemoteInputHistory(history);
        }
        notificationManager.notify(NOTIFICATION_INLINE_ACTION, builder.build());
    }

    private static NotificationCompat.Builder inlineReplyBuilder() {
        Context mContext = HabeshaApp.mContext;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("Inline Action");


        Intent respondIntent;
        PendingIntent respondPendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //we are running Nougat
            //the intent should start a service, so that the response can be handled in the background
            respondIntent = new Intent(mContext, NotificationIntentService.class);
            respondPendingIntent = PendingIntent.getService(mContext, 0, respondIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            //we are running a version prior to Nougat
            //There will be no response included with intent, so we want to start an Activity to allow
            //the user to enter input. This demo app does not have that capability, but in the real world
            //our Activity ought to allow that
            respondIntent = new Intent(mContext, MainActivity.class);
            respondPendingIntent = PendingIntent.getActivity(mContext, 0, respondIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        }

        respondIntent.setAction(NotificationIntentService.ACTION_RESPOND);

        //Build the Action for an inline response, which includes a RemoteInput
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_INLINE_REPLY)
                .setLabel("Your inline response")
                .build();

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_input_get,
                        "Respond", respondPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        builder.addAction(action);

        //Build the Action for "Done", so the IntentService can take appropriate action (e.g. dismiss
        //the notification, clear the history, etc.
        Intent doneIntent = new Intent(mContext, NotificationIntentService.class);
        doneIntent.setAction(NotificationIntentService.ACTION_DONE);
        PendingIntent donePendingIntent = PendingIntent.getService(mContext, 0, doneIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action doneAction =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_close_clear_cancel,
                        "Close", donePendingIntent)
                        .build();

        builder.addAction(doneAction);

        return builder;
    }

    public static void bundledNotification(Integer userId,String message,String username,String senderId,String receiverId) {
        Context mContext = HabeshaApp.mContext;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);


        issuedMessages.add(Html.fromHtml(message));

        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(username);
        String[] arMessages =null;
        arMessages= message.split("\n");
        if(arMessages.length!=0) {
            for (int i = 0; i < arMessages.length; i++) {
                inboxStyle.addLine(arMessages[i]);
            }
        }else{
            for (CharSequence cs : issuedMessages) {
                inboxStyle.addLine(cs);
            }
        }
        long[] v = {500,1000};
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        //Build and issue the group summary. Use inbox style so that all messages are displayed
        NotificationCompat.Builder summaryBuilder = new NotificationCompat.Builder(mContext,mContext.getString(R.string.app_name))
                .setContentTitle(username)
                .setContentText(arMessages[0])
                .setSmallIcon(R.drawable.ic_notification)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_BUNDLED);

        summaryBuilder.setVibrate(v);
        summaryBuilder.setSound(uri);
        *//*for (CharSequence cs : issuedMessages) {
            inboxStyle.addLine(cs);
        }*//*
        summaryBuilder.setStyle(inboxStyle);
        int requestCode1 = userId;
        Intent summeryIntent = new Intent(mContext, MainActivity.class);
        //Each notification needs a unique request code, so that each pending intent is unique. It does not matter
        //in this simple case, but is important if we need to take action on a specific notification, such as
        //deleting a message
        summeryIntent.putExtra("message_notification",true);
        summeryIntent.putExtra("username",username);
        summeryIntent.putExtra("senderId",senderId);
        summeryIntent.putExtra("receiverId",receiverId);
        summeryIntent.putExtra("userId",userId);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(mContext, requestCode1, summeryIntent,Intent.FLAG_ACTIVITY_NEW_TASK);
        summaryBuilder.setContentIntent(pendingIntent1);
        if(Build.VERSION.SDK_INT>=24) {
            notificationManager.notify(NOTIFICATION_BUNDLED_BASE_ID, summaryBuilder.build());
        }else {
            notificationManager.notify(userId, summaryBuilder.build());
        }


        //issue the Bundled notification. Since there is a summary notification, this will only display
        //on systems with Nougat or later
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext,mContext.getString(R.string.app_name))
                .setContentTitle(username)
                .setContentText(arMessages[0])
                .setSmallIcon(R.drawable.ic_notification)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setAutoCancel(true)
                .setGroup(GROUP_KEY_BUNDLED);
        builder.setVibrate(v);
        builder.setSound(uri);
        //Add an action that simply starts the main activity. This is not very useful it is mainly for demonstration

        if(Build.VERSION.SDK_INT>=24) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("message_notification",true);
            intent.putExtra("username",username);
            intent.putExtra("senderId",senderId);
            intent.putExtra("receiverId",receiverId);
            intent.putExtra("userId",userId);

            int requestCode = userId;
            PendingIntent pendingIntent = PendingIntent.getActivity(mContext, requestCode, intent, Intent.FLAG_ACTIVITY_NEW_TASK);
            builder.setContentIntent(pendingIntent);
            notificationManager.notify(userId, builder.build());
        }


    }*/

}
