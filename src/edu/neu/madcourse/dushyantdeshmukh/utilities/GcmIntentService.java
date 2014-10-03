package edu.neu.madcourse.dushyantdeshmukh.utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.communication.TestInterphoneComm;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.Connection;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.MatchImage;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.ProjectConstants;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.ChooseOpponent;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.Constants;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.Game;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.MsgFromOpponent;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.RequestFromOpponent;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.TwoPlayerWordGame;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
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
import android.widget.Toast;

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
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
					.equals(messageType)) {
				sendNotification("Send error: " + extras.toString(),
						new Intent(this, TwoPlayerWordGame.class));
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
					.equals(messageType)) {
				sendNotification(
						"Deleted messages on server: " + extras.toString(),
						new Intent(this, TwoPlayerWordGame.class));
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
					.equals(messageType)) {
				String bundleVal = extras.toString();
				Log.d(TAG, "bundleVal = " + bundleVal);
				Log.i(TAG, "Completed work @ " + SystemClock.elapsedRealtime());
				Log.i(TAG, "Received: " + extras.toString());

				processMsg(extras.toString());
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private boolean isFinalProjectActivityActive(Intent intent) {
		boolean isActive = false;
		boolean isActivityPaused = getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE).getBoolean(ProjectConstants.IS_ACTIVITY_PAUSED, false);
		Log.d("GCM_INTENT_SERVICE", "isActivityPaused: " + isActivityPaused);
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager
				.getRunningTasks(Integer.MAX_VALUE);
		if (services.get(0).topActivity.getClassName().equalsIgnoreCase(
				intent.getComponent().getClassName()) && !isActivityPaused) {
			isActive = true;
		}
		return isActive;
	}

	private boolean isTestCommActivityActive() {
		SharedPreferences prefs = getSharedPreferences(
				TestInterphoneComm.class.getSimpleName(), Context.MODE_PRIVATE);
		boolean isActive = prefs.getBoolean(
				TestInterphoneComm.ACTIVITY_ACTIVE_PREF, false);
		return isActive;
	}

	private void processMsg(String data) {
		Log.d(TAG, "Inside processMsg()");
		Intent i = null;
		String opponentName, opponentRegId;
		HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
		if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
			String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
			Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
			if (msgType.equals(ProjectConstants.MSG_TYPE_FP_CONNECT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ ProjectConstants.MSG_TYPE_FP_CONNECT);
				i = new Intent(this, Connection.class);
				processFinalProjectMsg(data, i,
						ProjectConstants.INTENT_ACTION_CONNECTION,dataMap.get(ProjectConstants.KEY_USERNAME));
			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT);
				i = new Intent(this, Connection.class);
				processFinalProjectMsg(data, i,
						ProjectConstants.INTENT_ACTION_CONNECTION,dataMap.get(ProjectConstants.KEY_USERNAME));

			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_ACK_REJECT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ ProjectConstants.MSG_TYPE_FP_ACK_REJECT);
				i = new Intent(this, Connection.class);
				processFinalProjectMsg(data, i,
						ProjectConstants.INTENT_ACTION_CONNECTION,dataMap.get(ProjectConstants.KEY_USERNAME));

			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_MOVE)) {
				Log.d(TAG, "Inside MSG_TYPE_FP_MOVE = "
						+ ProjectConstants.MSG_TYPE_FP_MOVE);
				i = new Intent(this, MatchImage.class);
				processFinalProjectMsg(data, i,
						ProjectConstants.INTENT_ACTION_GAME_MOVE_AND_FINISH,ProjectConstants.OPPONENT);

			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_GAME_OVER)) {
				Log.d(TAG, "Inside MSG_TYPE_FP_GAME_OVER = "
						+ ProjectConstants.MSG_TYPE_FP_GAME_OVER);
				i = new Intent(this, MatchImage.class);
				processFinalProjectMsg(data, i,
						ProjectConstants.INTENT_ACTION_GAME_MOVE_AND_FINISH,ProjectConstants.OPPONENT);

			} else if (msgType.equals(Constants.MSG_TYPE_2P_CONNECT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ Constants.MSG_TYPE_2P_CONNECT);
				opponentName = dataMap.get(Constants.KEY_USERNAME);
				opponentRegId = dataMap.get(Constants.KEY_REG_ID);
				i = new Intent(this, RequestFromOpponent.class);
				i.putExtra(Constants.EXTRA_OPPONENT_NAME, opponentName);
				i.putExtra(Constants.EXTRA_OPPONENT_REDID, opponentRegId);

				// process2PMsg(data, i);
				process2PMsgBroadcast(data, i,
						Constants.INTENT_ACTION_CHOOSE_OPPONENT);

			} else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_ACCEPT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ Constants.MSG_TYPE_2P_ACK_ACCEPT);
				opponentName = dataMap.get(Constants.KEY_USERNAME);
				opponentRegId = dataMap.get(Constants.KEY_REG_ID);
				i = new Intent(this, MsgFromOpponent.class);
				i.putExtra(Constants.EXTRA_MSG, "Game started with opponent '"
						+ opponentName + "'.\n" + "Your opponent goes first!");
				i.putExtra(Constants.EXTRA_ROUND, 0);
				i.putExtra(Constants.EXTRA_IS_PLAYER_ONE, false);

				process2PMsgBroadcast(data, i,
						Constants.INTENT_ACTION_CHOOSE_OPPONENT);

			} else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_REJECT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ Constants.MSG_TYPE_2P_ACK_REJECT);
				opponentName = dataMap.get(Constants.KEY_USERNAME);
				Log.d(TAG, "Game request denied by user '" + opponentName
						+ "'.");
				i = new Intent(this, ChooseOpponent.class);

				process2PMsgBroadcast(data, i,
						Constants.INTENT_ACTION_CHOOSE_OPPONENT);

			} else if (msgType.equals(Constants.MSG_TYPE_2P_MOVE)) {
				Log.d(TAG, "Inside MSG_TYPE_MOVE = "
						+ Constants.MSG_TYPE_2P_MOVE);
				i = new Intent(this, Game.class);
				process2PMsgBroadcast(data, i,
						Constants.INTENT_ACTION_2P_WORD_GAME);

			} else if (msgType.equals(Constants.MSG_TYPE_2P_QUIT)) {
				Log.d(TAG, "Inside MSG_TYPE_MOVE = "
						+ Constants.MSG_TYPE_2P_QUIT);
				i = new Intent(this, Game.class);
				process2PMsgBroadcast(data, i,
						Constants.INTENT_ACTION_2P_WORD_GAME);
			} else {
				// Msg for TestInterPhoneCommunication Activity
				processTestCommMsg(data);
			}
		}
	}

	private void process2PMsgBroadcast(String data, Intent i,
			String intentAction) {
		Log.d(TAG, "Inside process2PMsgBroadcast()");
		if (isAppActive()) {
			// send broadcast
			Log.d(TAG, "sending Broadcast");
			SendBroadcast(data, intentAction);
		} else {
			sendNotification(data, i);
		}
	}

	private void processFinalProjectMsg(String data, Intent i,
			String intentAction,String oppName) {
		Log.d(TAG, "Inside process2PMsg()");
		if (isFinalProjectActivityActive(i)) {
			Log.d(TAG, "isAppActive - starting activity: " + i.toString());
			SendBroadcast(data, intentAction);
		} else {
			sendFinalProjectNotification(data, i,oppName);
		}
	}

	private boolean isAppActive() {
		ActivityManager activityManager = (ActivityManager) getApplicationContext()
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> services = activityManager
				.getRunningTasks(Integer.MAX_VALUE);
		boolean isActivityFound = false;

		if (services.get(0).topActivity
				.getPackageName()
				.toString()
				.equalsIgnoreCase(
						getApplicationContext().getPackageName().toString())) {
			isActivityFound = true;
		}
		return isActivityFound;
	}

	private void processTestCommMsg(String data) {
		if (isTestCommActivityActive()) {
			// send broadcast
			SendBroadcast(data, "TEST COMM INTENT_ACTION");
		} else {
			// Post notification of received message.
			getSharedPreferences(TestInterphoneComm.class.getSimpleName(),
					Context.MODE_PRIVATE).edit()
					.putString(TestInterphoneComm.KEY_NOTIFICATION_DATA, data)
					.commit();
			Log.d(TAG, "Storing NOTIFICATION_DATA in SharedPref = " + data);

			Intent i = new Intent(this, TestInterphoneComm.class);
			sendNotification(data, i);
		}
	}

	private void SendBroadcast(String data, String intentAction) {
		Log.d(TAG, "Inside SendBroadcast()");
		Intent i = new Intent();
		i.setAction(intentAction);
		i.putExtra("data", data);
		this.sendBroadcast(i);
	}

	private void sendNotification(String data, Intent i) {
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.stat_notify_more)
				.setContentTitle("GCM Notification")
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText("Received new message from opponent."))
				.setContentText("New message received from opponent.")
				.setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}

	private void sendFinalProjectNotification(String data, Intent i,String oppName){
		mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		
		getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE).edit()
				.putString(ProjectConstants.KEY_NOTIFICATION_DATA, data)
				.commit();
		
		String opponentName = getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE).getString(ProjectConstants.PREF_OPPONENT_NAME, ProjectConstants.OPPONENT);
		
		if(opponentName.equals(ProjectConstants.OPPONENT)){
			opponentName = oppName;
		}
		
		Log.d("ProjectNotifications", "opponent name: " + opponentName);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.final_proj_notification_icon)
				.setContentTitle("Match Me If You Can")
				.setStyle(
						new NotificationCompat.BigTextStyle()
								.bigText("Received new game message from " + opponentName + "."))
				.setContentText("New game message received from " + opponentName + ".")
				.setAutoCancel(true);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());	
	}
	
	public void showDebugToast(String msg) {
		// Toast t = Toast.makeText(getApplicationContext(), msg, 2000);
		// t.show();
		Log.d(TAG, "\n===================================================\n");
		Log.d(TAG, msg);
		Log.d(TAG, "\n===================================================\n");
		// TextView msgTxtView = (TextView)
		// findViewById(R.id.communication_interphone_comm_msg_textview);
		// msgTxtView.setText(msg);
	}

}