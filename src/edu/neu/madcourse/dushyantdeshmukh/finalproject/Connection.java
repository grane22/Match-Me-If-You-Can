package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import java.io.IOException;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.Constants;
import edu.neu.madcourse.dushyantdeshmukh.utilities.InternetConnUtil;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import edu.neu.mhealth.api.KeyValueAPI;

public class Connection extends Activity implements OnClickListener {

	protected static final String TAG = "CONNECTION ACTIVITY";
	private Intent i;
	private Button findOpponentButton, submitUserNameButton, backButton;
	private EditText userNameEditText, opponentNameEditText;
	private TextView messageTextView, welcomeUserTextView;
	private String username, regId, oppName, oppRegId;
	private BroadcastReceiver receiver;
	private SharedPreferences projPreferences;
	private AlertDialog swapAlertDialog, showAcceptRejectDialog;
	private boolean isSwapAlertDialogShown = false;
	GoogleCloudMessaging gcm;
	SoundHelper soundHelper;
	Context context;

	public Connection() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_proj_connection);

		context = this;
		projPreferences = getSharedPreferences();
		soundHelper = new SoundHelper(projPreferences);

		// Set up click listeners for all the buttons
		opponentNameEditText = (EditText) findViewById(R.id.final_proj_opponent_name_edittext);
		findOpponentButton = (Button) findViewById(R.id.final_proj_find_opponent_button);
		findOpponentButton.setOnClickListener(this);

		userNameEditText = (EditText) findViewById(R.id.final_proj_user_name_edittext);
		submitUserNameButton = (Button) findViewById(R.id.final_proj_submit_user_name_button);
		submitUserNameButton.setOnClickListener(this);

		backButton = (Button) findViewById(R.id.final_proj_back_button);
		backButton.setOnClickListener(this);

		welcomeUserTextView = (TextView) findViewById(R.id.final_proj_welcome_user);
		messageTextView = (TextView) findViewById(R.id.final_proj_msg);

		// This will handle the broadcast
		receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.d(TAG,
						"Inside onReceive of Broadcast receiver of ChooseOpponent.class");
				String action = intent.getAction();
				if (action.equals(ProjectConstants.INTENT_ACTION_CONNECTION)) {
					String data = intent.getStringExtra("data");
					Log.d(TAG, "data = " + data);
					handleOpponentResponse(data);
				}
			}
		};
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();

		// Check if user Name is set in shared prefs and display welcome msg
		String userNameValue = projPreferences.getString(
				ProjectConstants.USER_NAME, null);
		if (userNameValue != null) {
			displayUserName(userNameValue);
			updateUIButtons();
			this.username = userNameValue;
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		projPreferences
				.edit()
				.putBoolean(ProjectConstants.IS_SWAP_ALERT_DIALOG_SHOWN,
						isSwapAlertDialogShown).commit();
	}

	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		isSwapAlertDialogShown = projPreferences.getBoolean(
				ProjectConstants.IS_SWAP_ALERT_DIALOG_SHOWN, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		soundHelper.playBgMusic(context);
		
		checkPlayServices();

		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			regId = getRegistrationId(context);
			Log.d(TAG, "registration key already stored: " + regId);

			if (regId.isEmpty()) {
				registerInBackground();
			}
		} else {
			Log.d(TAG, "No valid Google Play Services APK found.");
		}

		// This needs to be in the activity that will end up receiving the
		// broadcast
		registerReceiver(receiver, new IntentFilter(
				ProjectConstants.INTENT_ACTION_CONNECTION));
		handleNotification(projPreferences);

		projPreferences.edit()
				.putBoolean(ProjectConstants.IS_ACTIVITY_PAUSED, false)
				.commit();

		if (isSwapAlertDialogShown) {
			swapAlertDialog = Util.showSwapPhonesAlertDialog(this, this, true,
					projPreferences);
			swapAlertDialog.show();
			isSwapAlertDialogShown = true;
		}
		/*
		 * if (projPreferences.getBoolean(ProjectConstants.IS_SWAP_ALERT_PAUSED,
		 * false)) { swapAlertDialog = Util.showSwapPhonesAlertDialog(context,
		 * this, true); // isSwapAlertDialogShown = true;
		 * swapAlertDialog.show(); projPreferences.edit()
		 * .putBoolean(ProjectConstants.IS_SWAP_ALERT_PAUSED, false) .commit();
		 * // isSwapAlertDialogShown = false; }
		 */

		Log.d(TAG,
				"IS_ACCEPT_REJECT_ALERT_PAUSED: "
						+ projPreferences.getBoolean(
								ProjectConstants.IS_ACCEPT_REJECT_ALERT_PAUSED,
								false));

		if (projPreferences.getBoolean(
				ProjectConstants.IS_ACCEPT_REJECT_ALERT_PAUSED, false)) {
			// isAcceptRejectDialogShown = true;
			showAcceptRejectDialog();
			projPreferences
					.edit()
					.putBoolean(ProjectConstants.IS_ACCEPT_REJECT_ALERT_PAUSED,
							false).commit();
			// isAcceptRejectDialogShown = false;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		soundHelper.stopMusic();
		
		unregisterReceiver(receiver);
		Log.d(TAG, "onPause...");
		Log.d(TAG, "onPause...SwapAlertDialog value: " + swapAlertDialog);
		projPreferences.edit()
				.putBoolean(ProjectConstants.IS_ACTIVITY_PAUSED, true).commit();

		if (swapAlertDialog != null) {
			swapAlertDialog.dismiss();
		}

		// Log.d(TAG, "onPause: isAcceptRejectDialogShown: " +
		// isAcceptRejectDialogShown);
		if (showAcceptRejectDialog != null) {
			// if (isAcceptRejectDialogShown) {
			projPreferences
					.edit()
					.putBoolean(ProjectConstants.IS_ACCEPT_REJECT_ALERT_PAUSED,
							true).commit();
			// }
			showAcceptRejectDialog.dismiss();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.final_proj_submit_user_name_button:

			String userName = userNameEditText.getText().toString();

			if (userName.equals("")) {
				Util.showToast(context, "Please enter a non empty userName!",
						2000);
			} else {
				this.username = userName;
				userNameEditText.setText("");
				saveUserNameToRegisteredUserList(userName, regId);
				updateUserDetailsInSharedPreferences(userName);
				displayUserName(userName);
				updateUIButtons();
			}
			break;
		case R.id.final_proj_find_opponent_button:
			EditText opponentEditText = (EditText) findViewById(R.id.final_proj_opponent_name_edittext);
			String oppUsername = opponentEditText.getText().toString();

			if (oppUsername.equals("")) {
				Util.showToast(context,
						"Please enter a valid opponent username value!",
						2000);
			} else {
				opponentEditText.setText("");
				findOpponent(oppUsername);
			}
			break;
		case R.id.final_proj_back_button:
			Intent mainMenuIntent = new Intent(this, Home.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			break;
		}
	}

	private void saveUserNameToRegisteredUserList(String userName, String regId) {
		if (!InternetConnUtil.isNetworkAvailable(context)) {
			Util.showToast(context, Constants.NETWORK_UNAVAILABLE_MSG,
					4000);
			return;
		}

		Util.addValuesToKeyOnServer(ProjectConstants.REGISTERED_USERS_LIST,
				userName, regId);

	}

	private void updateUserDetailsInSharedPreferences(String userName) {
		Editor editor = projPreferences.edit();
		editor.putString(ProjectConstants.USER_NAME, userName);
		editor.putString(ProjectConstants.USER_REG_ID, regId);
		editor.commit();
	}

	private void displayUserName(String userName) {
		welcomeUserTextView.setText("Welcome " + userName);
	}

	private void updateUIButtons() {
		opponentNameEditText.setVisibility(View.VISIBLE);
		findOpponentButton.setVisibility(View.VISIBLE);

		userNameEditText.setVisibility(View.GONE);
		submitUserNameButton.setVisibility(View.GONE);

		backButton.setVisibility(View.VISIBLE);
		welcomeUserTextView.setVisibility(View.VISIBLE);
		messageTextView.setVisibility(View.VISIBLE);
	}

	private void findOpponent(String oppUsername) {
		if (!InternetConnUtil.isNetworkAvailable(context)) {
			Util.showToast(context, Constants.NETWORK_UNAVAILABLE_MSG,
					4000);
			return;
		}
		Log.w(TAG, "user name: " + projPreferences.getString(ProjectConstants.USER_NAME, "LALALA"));
		if (!oppUsername.equals(projPreferences.getString(ProjectConstants.USER_NAME,
						ProjectConstants.OPPONENT))) {
			// check if user is waiting
			// If user waiting, pair with that user
			// Else, add yourself to waiting user list and start a service
			// polling
			// for
			// another user
			new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... params) {
					String oppUsername = params[0];
					String retVal = "";
					String result = "";
					boolean foundOpponent = false;
					if (KeyValueAPI.isServerAvailable()) {
						String registeredUsersList = KeyValueAPI.get(
								ProjectConstants.TEAM_NAME,
								ProjectConstants.PASSWORD,
								ProjectConstants.REGISTERED_USERS_LIST);

						if (registeredUsersList.toUpperCase().contains("ERROR:")) {
							// No player waiting... put your own regId
							retVal = "Error finding player!";
						} else {
							if (registeredUsersList.trim() != "") {
								String usersArr[] = registeredUsersList
										.split(",");
								// Iterate over list of entries in key
								// 'keyname'and
								// check for val1
								for (int i = 0; i < usersArr.length; i++) {
									String tempArr[] = usersArr[i].split("::");
									String oppName = tempArr[0];
									String oppRegId = tempArr[1];

									if (oppUsername.equalsIgnoreCase(oppName)) {
										Log.d(TAG, "\noppRegId= " + oppRegId
												+ "\n");
										Log.d(TAG, "\nregId= " + regId + "\n");

										// Get opponents regId and connect
										Log.d(TAG,
												"Sending connect request to opponent'"
														+ "opponentName= "
														+ oppName
														+ ", opponentRegId= "
														+ oppRegId);

										try {
											result = Util
													.sendPost(
															"data."
																	+ Constants.KEY_MSG_TYPE
																	+ "="
																	+ ProjectConstants.MSG_TYPE_FP_CONNECT
																	+ "&data."
																	+ ProjectConstants.KEY_REG_ID
																	+ "="
																	+ regId
																	+ "&data."
																	+ ProjectConstants.KEY_USERNAME
																	+ "="
																	+ username,
															oppRegId);
											Log.d(TAG, "Result of HTTP POST: "
													+ result);
											// displayMsg("Connected to user:" +
											// oppName + " (" +
											// oppRegId + ")");
											retVal = "Sent game request to "
													+ oppName;
											// sendPost("data=" + myRegId);
										} catch (Exception e) {
											// TODO Auto-generated catch block
											// displayMsg("Error occurred while making an HTTP post call.");
											retVal = "Error finding player!";
											e.printStackTrace();
										}
										foundOpponent = true;
										break;
									}
								}
							}
						}
					}
					if (!foundOpponent) {
						retVal = ProjectConstants.OPPONENT_NOT_FOUND;
					}
					Log.d(TAG, "retVal: " + retVal);
					return retVal;
				}

				@Override
				protected void onPostExecute(String result) {
					// mDisplay.append(msg + "\n");
					// Toast t = Toast.makeText(getApplicationContext(), result,
					// 2000);
					// t.show();
					Log.d(TAG,
							"\n===================================================\n");
					Log.d(TAG, "result: " + result);
					Util.showToast(context, result, Toast.LENGTH_LONG);
					if (!result.equals(ProjectConstants.OPPONENT_NOT_FOUND)) {
						messageTextView
								.setText("Waiting for opponent's response!");
					}
				}
			}.execute(oppUsername, null, null);
		} else {
			Util.showToast(context, ProjectConstants.OPPONENT_SAME_AS_USER,
					4000);
		}
	}

	protected void handleOpponentResponse(String data) {
		Log.d(TAG, "Inside handleOpponentResponse()");
		HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
		if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
			messageTextView.setText("");

			this.oppName = dataMap.get(ProjectConstants.KEY_USERNAME);

			String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
			Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
			if (msgType.equals(ProjectConstants.MSG_TYPE_FP_CONNECT)) {
				Log.d(TAG, "Inside MSG_TYPE_CONNECT = "
						+ ProjectConstants.MSG_TYPE_FP_CONNECT);
				this.oppRegId = dataMap.get(ProjectConstants.KEY_REG_ID);
				projPreferences
						.edit()
						.putString(ProjectConstants.PREF_OPPONENT_REG_ID,
								this.oppRegId).commit();

				projPreferences
						.edit()
						.putString(ProjectConstants.POTENTIAL_OPPONENT_NAME,
								this.oppName).commit();
				// isAcceptRejectDialogShown = true;
				showAcceptRejectDialog();
			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT)) {
				Log.d(TAG, "Inside MSG_TYPE_FP_ACK_ACCEPT = "
						+ ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT);
				this.oppRegId = dataMap.get(ProjectConstants.KEY_REG_ID);
				projPreferences
						.edit()
						.putString(ProjectConstants.PREF_OPPONENT_REG_ID,
								this.oppRegId).commit();
				Log.d(TAG, "\n\n Setting this.oppRegId in SP: " + this.oppRegId
						+ "\n\n");
				
				projPreferences
				.edit()
				.putString(ProjectConstants.POTENTIAL_OPPONENT_NAME,
						this.oppName).commit();

				// Show 'Connected to Opponent' msg and go to Game activity
				// String opponentName = dataMap.get(Constants.KEY_USERNAME);
				initiateGame(false);

			} else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_ACK_REJECT)) {
				Log.d(TAG, "Inside MSG_TYPE_FP_ACK_REJECT = "
						+ ProjectConstants.MSG_TYPE_FP_ACK_REJECT);
				// Show 'Request reject' toast and stay on Choose Opponent
				// activity
				// String opponentName = dataMap.get(Constants.KEY_USERNAME);
				Util.showToast(context, "Game request denied by "
						+ oppName + ".", 2500);
			}
		}
	}

	private void showAcceptRejectDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Connection.this);
		builder.setCancelable(true);
		builder.setTitle("Game Request");
		builder.setMessage(projPreferences.getString(
						ProjectConstants.POTENTIAL_OPPONENT_NAME,
						ProjectConstants.OPPONENT)
				+ " has sent a game request.");
		builder.setPositiveButton("Accept",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showAcceptRejectDialog = null;
						// isAcceptRejectDialogShown = true;
						initiateGame(true);
					}
				});
		builder.setNegativeButton("Reject",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						showAcceptRejectDialog = null;
						// isAcceptRejectDialogShown = true;
						// send an reject ack to oppponent
						String oppRegIdString = projPreferences.getString(
								ProjectConstants.PREF_OPPONENT_REG_ID, null);
						if (oppRegIdString != null) {
							sendReqAckToOpponent(false, oppRegIdString);
						}

					}
				});
		showAcceptRejectDialog = builder.create();
		Log.d(TAG, "alert created: " + showAcceptRejectDialog);
		showAcceptRejectDialog.show();
	}

	private void initiateGame(boolean asPLayerOne) {
		// Util method to show dialog
		swapAlertDialog = Util.showSwapPhonesAlertDialog(context, this, true,
				projPreferences);
		swapAlertDialog.show();
		isSwapAlertDialogShown = true;

		swapAlertDialog = null;
		String oppRegIdString = projPreferences.getString(
				ProjectConstants.PREF_OPPONENT_REG_ID, null);
		if (oppRegIdString != null) {

			Util.storeOppnentInSharedpref(projPreferences, projPreferences
					.getString(ProjectConstants.POTENTIAL_OPPONENT_NAME,
							ProjectConstants.OPPONENT), oppRegIdString);

			if (asPLayerOne) {
				// send an accept ack to oppponent
				sendReqAckToOpponent(true, oppRegIdString);
			}
		}

	}

	private void handleNotification(SharedPreferences sp) {
		Log.d(TAG, "Inside handle notification method...");
		String data = sp.getString(ProjectConstants.KEY_NOTIFICATION_DATA, "");
		Log.d(TAG, "data " + data);
		if (!data.equals("")) {
			Log.d(TAG, "data exists: " + data);
			handleOpponentResponse(data);
			sp.edit().putString(ProjectConstants.KEY_NOTIFICATION_DATA, "")
					.commit();
		}
	}

	private void sendReqAckToOpponent(boolean accepted, String oppRegId) {
		Log.d(TAG, "Sending request ack: "
				+ (accepted ? ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT
						: ProjectConstants.MSG_TYPE_FP_ACK_REJECT));
		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... params) {
				String retVal;
				boolean accepted = Boolean.parseBoolean(params[0]);
				String oppRegId = params[1];
				try {
					retVal = Util
							.sendPost(
									"data."
											+ Constants.KEY_MSG_TYPE
											+ "="
											+ (accepted ? ProjectConstants.MSG_TYPE_FP_ACK_ACCEPT
													: ProjectConstants.MSG_TYPE_FP_ACK_REJECT)
											+ "&data."
											+ ProjectConstants.KEY_REG_ID + "="
											+ regId + "&data."
											+ ProjectConstants.KEY_USERNAME
											+ "=" + username, oppRegId);
					Log.d(TAG, "Result of HTTP POST: " + retVal);
					// displayMsg("Connected to user:" + oppName + " (" +
					// oppRegId + ")");
					retVal = "Sent request ack to opponent:" + " (" + oppRegId
							+ ")";
					// sendPost("data=" + myRegId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					// displayMsg("Error occurred while making an HTTP post call.");
					retVal = "Error occured while making an HTTP post call.";
					e.printStackTrace();
				}
				return retVal;
			}

			@Override
			protected void onPostExecute(String result) {
				// Toast t = Toast.makeText(getApplicationContext(), result,
				// 2000);
				// t.show();
				Log.d(TAG,
						"\n===================================================\n");
				Log.d(TAG, "result: " + result);
			}
		}.execute(String.valueOf(accepted), oppRegId, null);
	}

	public void startCaptureActivity() {
		Intent captureIntent = new Intent(context, CaptureImage.class);
		captureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(captureIntent);
	}

	private void registerInBackground() {
		if (InternetConnUtil.isNetworkAvailable(context)) {
			Log.d("NOINTERNET", "hey inside registerInBackground");
			new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... params) {
					String msg = "";
					try {
						if (gcm == null) {
							gcm = GoogleCloudMessaging.getInstance(context);
						}
						regId = gcm.register(ProjectConstants.SENDER_ID);
						// msg = "Device registered, registration ID=" + regid;
						msg = "User name stored successfully!";
						storeRegistrationId(context, regId);
					} catch (IOException ex) {
						msg = "Error :" + ex.getMessage();
						// If there is an error, don't just keep trying to
						// register.
						// Require the user to click a button again, or perform
						// exponential back-off.
					}
					return msg;
				}

				@Override
				protected void onPostExecute(String msg) {
					messageTextView.append(msg + "\n");
				}

			}.execute(null, null, null);
		} else {
			Util.showToast(context, "No network available!", 4000);
		}
	}

	private void storeRegistrationId(Context context, String regId) {
		int appVersion = getAppVersion(context);
		Log.d(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = projPreferences.edit();
		editor.putString(ProjectConstants.PROPERTY_REG_ID, regId);
		editor.putInt(ProjectConstants.PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	private String getRegistrationId(Context context) {
		String registrationId = projPreferences.getString(
				ProjectConstants.PROPERTY_REG_ID, "");
		if (registrationId.isEmpty()) {
			Log.d(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = projPreferences.getInt(
				ProjectConstants.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			Log.d(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this,
						ProjectConstants.PLAY_SERVICES_RESOLUTION_REQUEST)
						.show();
			} else {
				Log.d(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE);
	}
}
