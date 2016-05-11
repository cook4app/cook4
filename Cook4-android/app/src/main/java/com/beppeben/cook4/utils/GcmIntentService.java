package com.beppeben.cook4.utils;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.beppeben.cook4.MainActivity;
import com.beppeben.cook4.R;
import com.beppeben.cook4.RegistrationActivity;
import com.beppeben.cook4.domain.C4Conversation;
import com.beppeben.cook4.domain.C4Conversation.Message;
import com.beppeben.cook4.ui.ChatFragment;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;
import java.util.Date;


//manages the visualization of all notifications
public class GcmIntentService extends IntentService {

    private NotificationManager mNotificationManager;
    private Long from_id;
    private String from_user;
    private String msgString;
    private Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    private int icon = R.drawable.icon_white;


    private ChatUtils chatUtils;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM Service";

    @Override
    protected void onHandleIntent(Intent intent) {
        Long my_id = Globals.getMe(getApplicationContext()).getId();
        if (my_id == null) my_id = -1L;
        chatUtils = new ChatUtils(getApplicationContext());
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        if (extras == null || messageType == null) return;
        if (!extras.isEmpty() && GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
            String check = extras.getString("check_id");
            if (check != null && !check.equals(my_id.toString())) return;
            String type = extras.getString("type");
            mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            if (type == null) return;
            if (type.equals("chat")) {
                from_id = Long.parseLong(extras.getString("from_id"));
                C4Conversation conv = chatUtils.getConversation(from_id);
                if (conv == null) {
                    conv = new C4Conversation();
                    conv.messages = new ArrayList<Message>();
                }
                conv.username = extras.getString("from_user");
                String msgText = extras.getString("msg");
                msgString = msgText.replaceAll("^\"", "").replaceAll("\"$", "");

                Message msg = new Message(false, msgString, new Date());
                conv.messages.add(msg);


                from_user = extras.getString("from_user");

                if (ChatFragment.activityVisible == null || !ChatFragment.activityVisible) {
                    sendChatNotification(from_user + ": " + msgString);
                    chatUtils.storeConversation(from_id, conv, false);
                } else {
                    chatUtils.storeConversation(from_id, conv, true);
                    Intent in = new Intent("com.beppeben.cook4.CONV_REFRESH");
                    in.putExtra("user_id", from_id);
                    in.putExtra("msg", msgString);
                    sendBroadcast(in);
                }
            } else if (type.equals("notification_newtransaction")) {
                String foodie = extras.getString("foodie");
                String dish = extras.getString("dish");
                sendNewTransNotification(foodie, dish);
                if (MainActivity.activityVisible) MainActivity.refresh(false, false, false, true);
            } else if (type.equals("notification_removetransaction")) {
                String fromUser = extras.getString("from_user");
                String dish = extras.getString("dish");
                sendRemoveTransNotification(fromUser, dish);
                if (MainActivity.activityVisible) MainActivity.refresh(false, false, false, true);
            } else if (type.equals("notification_swap_proposal")) {
                String fromCook = extras.getString("from_cook");
                String targetDish = extras.getString("target_dish");
                String rewardDish = extras.getString("reward_dish");
                sendNewSwapProposalNotification(fromCook, targetDish, rewardDish);
                if (MainActivity.activityVisible) MainActivity.refresh(false, false, true, false);
            } else if (type.equals("notification_swap_accept")) {
                String fromUser = extras.getString("from_user");
                sendNewSwapAcceptNotification(fromUser);
                if (MainActivity.activityVisible) MainActivity.refresh(false, false, true, true);
            } else if (type.equals("notification_general")) {
                String text = extras.getString("text");
                String title = extras.getString("title");
                sendGeneralNotification(title, text);
            }

        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }


    private void sendChatNotification(String msg) {
        Intent chatIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        chatIntent.putExtra("select_fragment", "chat");
        chatIntent.putExtra("user_name", from_user);
        chatIntent.putExtra("user_id", from_id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(chatIntent);
        PendingIntent chatPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(getString(R.string.notification_newmessage))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(chatPendingIntent);
        mNotificationManager.notify(Utils.longToInt(from_id), mBuilder.build());
    }


    private void sendNewTransNotification(String foodie, String dish) {
        Intent transIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        transIntent.putExtra("select_fragment", "pending");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(transIntent);
        PendingIntent transPendingIntent =
                stackBuilder.getPendingIntent(-1, PendingIntent.FLAG_UPDATE_CURRENT);
        String msg = getString(R.string.user) + " " + foodie + " "
                + getString(R.string.has_just_ordered) + " " + dish + ". " + getString(R.string.click_details) + ".";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(getString(R.string.notification_newtransaction))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(transPendingIntent);
        mNotificationManager.notify(-1, mBuilder.build());
    }


    private void sendRemoveTransNotification(String from_user, String dish) {
        Intent transIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        transIntent.putExtra("select_fragment", "pending");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(transIntent);
        PendingIntent transPendingIntent =
                stackBuilder.getPendingIntent(-2, PendingIntent.FLAG_UPDATE_CURRENT);
        String msg = getString(R.string.user) + " " + from_user + " " + getString(R.string.has_cancelled_trans_dish) + " " + dish;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setSmallIcon(icon)
                        .setContentTitle(getString(R.string.notification_transcanc))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(transPendingIntent);
        mNotificationManager.notify(-2, mBuilder.build());
    }

    private void sendNewSwapProposalNotification(String fromCook, String targetDish, String rewardDish) {
        Intent transIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        transIntent.putExtra("select_fragment", "pending_swaps");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(transIntent);
        PendingIntent transPendingIntent =
                stackBuilder.getPendingIntent(-3, PendingIntent.FLAG_UPDATE_CURRENT);
        String msg = getString(R.string.user) + " " + fromCook + " " + getString(R.string.has_proposed_exchange) +
                " " + targetDish + " " + getString(R.string.against) + " " + rewardDish;

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext())
                        .setSmallIcon(icon)
                        .setContentTitle(getString(R.string.notification_newswapprop))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(transPendingIntent);
        mNotificationManager.notify(-3, mBuilder.build());
    }

    private void sendNewSwapAcceptNotification(String fromUser) {
        Intent transIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        transIntent.putExtra("select_fragment", "pending");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(transIntent);
        PendingIntent transPendingIntent =
                stackBuilder.getPendingIntent(-4, PendingIntent.FLAG_UPDATE_CURRENT);
        String msg = getString(R.string.user) + " " + fromUser + " " + getString(R.string.has_accepted_swap) + ".";
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(getString(R.string.notification_swapaccepted))
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setAutoCancel(true)
                        .setContentText(msg)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(transPendingIntent);
        mNotificationManager.notify(-4, mBuilder.build());
    }

    private void sendGeneralNotification(String title, String text) {
        Intent transIntent = new Intent(this.getApplicationContext(), RegistrationActivity.class);
        if (text.contains("upcoming transactions")) {
            text = getString(R.string.notification_upcomingtrans);
            transIntent.putExtra("select_fragment", "pending");
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(RegistrationActivity.class);
        stackBuilder.addNextIntent(transIntent);
        PendingIntent transPendingIntent =
                stackBuilder.getPendingIntent(-5, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(icon)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(text))
                        .setAutoCancel(true)
                        .setContentText(text)
                        .setSound(alarmSound);

        mBuilder.setContentIntent(transPendingIntent);
        mNotificationManager.notify(-5, mBuilder.build());
    }

}