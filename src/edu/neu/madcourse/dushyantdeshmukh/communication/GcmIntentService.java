package edu.neu.madcourse.dushyantdeshmukh.communication;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class GcmIntentService extends IntentService {
  public static final int NOTIFICATION_ID = 1;
  private static final String TAG = "GCMIntentService";
  private NotificationManager mNotificationManager;
  NotificationCompat.Builder builder;

  public GcmIntentService() {
    super("GcmIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "Inside onHandleIntent");
    Bundle extras = intent.getExtras();
    Log.d(TAG, "extras = " + extras.toString());
    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
    // The getMessageType() intent parameter must be the intent you received
    // in your BroadcastReceiver.
    String messageType = gcm.getMessageType(intent);

    if (!extras.isEmpty()) { // has effect of unparcelling Bundle
      /*
       * Filter messages based on message type. Since it is likely that GCM will
       * be extended in the future with new message types, just ignore any
       * message types you're not interested in, or that you don't recognize.
       */
      if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
        sendNotification("Send error: " + extras.toString());
      } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
        sendNotification("Deleted messages on server: " + extras.toString());
        // If it's a regular GCM message, do some work.
      } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
        String bundleVal = extras.toString();
        Log.d(TAG, "bundleVal = " + bundleVal);
        // This loop represents the service doing some work.
        // for (int i=0; i<5; i++) {
        // Log.i(TAG, "Working... " + (i+1)
        // + "/5 @ " + SystemClock.elapsedRealtime());
        // try {
        // Thread.sleep(5000);
        // } catch (InterruptedException e) {
        // }
        // }
        Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
        Log.i(TAG, "Received: " + extras.toString());

        processMsg(extras.toString());
      }
    }
    // Release the wake lock provided by the WakefulBroadcastReceiver.
    GcmBroadcastReceiver.completeWakefulIntent(intent);
  }

  private boolean isActivityActive() {
    SharedPreferences prefs = getSharedPreferences(
        TestInterphoneComm.class.getSimpleName(), Context.MODE_PRIVATE);
    boolean isActive = prefs.getBoolean(TestInterphoneComm.ACTIVITY_ACTIVE_PREF, false);
    return isActive;
  }

  private void processMsg(String data) {
    if (isActivityActive()) {
      // send broadcast
      SendBroadcast(data);
    } else {
      // Post notification of received message.
      sendNotification(data);
    }

  }

  private void SendBroadcast(String data) {
    Log.d(TAG, "Inside SendBroadcast()");
    Intent i = new Intent();
    i.setAction("INTENT_ACTION");
    i.putExtra("data", data);
    this.sendBroadcast(i);
  }

  // Put the message into a notification and post it.
  // This is just one simple example of what you might choose to do with
  // a GCM message.
  private void sendNotification(String data) {
    getSharedPreferences(TestInterphoneComm.class.getSimpleName(),
        Context.MODE_PRIVATE).edit().putString(TestInterphoneComm.KEY_NOTIFICATION_DATA, data).commit();
    Log.d(TAG, "Storing NOTIFICATION_DATA in SharedPref = " + data);

    mNotificationManager = (NotificationManager) this
        .getSystemService(Context.NOTIFICATION_SERVICE);

    Intent i = new Intent(this, TestInterphoneComm.class);
    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.stat_notify_more)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle().bigText("New message received from opponent."))
        .setContentText("New message received from opponent.")
        .setAutoCancel(true);

    mBuilder.setContentIntent(contentIntent);
    mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

  }
}