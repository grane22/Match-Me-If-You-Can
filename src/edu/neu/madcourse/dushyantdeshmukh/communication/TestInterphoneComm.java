package edu.neu.madcourse.dushyantdeshmukh.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.InternetConnUtil;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import edu.neu.mhealth.api.KeyValueAPI;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TestInterphoneComm extends Activity implements OnClickListener {

  private static final String TAG = "COMMUNICATION - INTERPHONE COMM";
  private static final String PASSWORD = "numad14s";
  private static final String TEAM_NAME = "Dushyant";
  public static final String EXTRA_MESSAGE = "message";
  public static final String PROPERTY_REG_ID = "registration_id";
  private static final String PROPERTY_APP_VERSION = "appVersion";
  private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

  private static final String SERVER_URL = "https://android.googleapis.com/gcm/send";
  private static final String BROWSER_API_KEY = "AIzaSyBu3zhNf6EIipgPtgE00kFZ3PQv86cSsys";
  private static final String SENDER_ID = "94466405712";

  protected static final String PREF_OPPONENT_REG_ID = "OPPONENT_REG_ID";
  protected static final String PREF_OPPONENT_NAME = "OPPONENT_NAME";

  protected static final String KEY_WAITING_PLAYER = "WAITING_PLAYER";
  public static final String ACTIVITY_ACTIVE_PREF = "ACTIVITY_ACTIVE";
  protected static final String KEY_REG_ID = "REG_ID";
  protected static final String KEY_USERNAME = "USERNAME";
  protected static final String KEY_MSG_TYPE = "MSG_TYPE";
  protected static final String MSG_TYPE_MOVE = "MOVE";
  protected static final String MSG_TYPE_CONNECT = "CONNECT";
  protected static final String KEY_MESSAGE = "MESSAGE";
  public static final String KEY_NOTIFICATION_DATA = "NOTIFICATION_DATA";
  private static final String NETWORK_UNAVAILABLE_MSG = "Network unavailable. Please make sure you are connected to the internet.";

  /**
   * This is the project number you got from the API Console, as described in
   * "Getting Started."
   */

  GoogleCloudMessaging gcm;
  Context context;

  private String username, opponentName, myRegId, opponentRegId,
      msgForOpponent;
  BroadcastReceiver receiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.communication_interphone_comm);

    // Set up click listeners for all the buttons
    View startgameButton = findViewById(R.id.communication_interphone_comm_startgame_button);
    startgameButton.setOnClickListener(this);

    View sendmsgButton = findViewById(R.id.communication_interphone_comm_sendmsg_button);
    sendmsgButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.communication_interphone_comm_quit_button);
    quitButton.setOnClickListener(this);

    context = getApplicationContext();

    // This will handle the broadcast
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Inside onReceive of Broadcast receiver");
        String action = intent.getAction();
        if (action.equals("TEST COMM INTENT_ACTION")) {
          String data = intent.getStringExtra("data");
          Log.d(TAG, "data = " + data);
          handleOpponentResponse(data);
        }
      }
    };

  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    if (dataMap.containsKey(KEY_MSG_TYPE)) {
      String msgType = dataMap.get(KEY_MSG_TYPE);
      Log.d(TAG, KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(MSG_TYPE_CONNECT)) {
        // Log.d(TAG, "Inside MSG_TYPE_CONNECT = " + MSG_TYPE_CONNECT);
        opponentName = dataMap.get(KEY_USERNAME);
        opponentRegId = dataMap.get(KEY_REG_ID);
        Editor ed = getCommSharedPreferences(context).edit();
        ed.putString(PREF_OPPONENT_REG_ID, opponentRegId);
        ed.putString(PREF_OPPONENT_NAME, opponentName);
        ed.commit();
        Log.d(TAG, "Message sent to displayMsg() => Connected to opponent:"
            + opponentName + " (" + opponentRegId + ")");
        displayMsg("Connected to opponent:" + opponentName + " ("
            + opponentRegId + ")");
      } else if (msgType.equals(MSG_TYPE_MOVE)) {
        // Log.d(TAG, "Inside MSG_TYPE_MOVE = " + MSG_TYPE_MOVE);
        String msgFromOpponent = dataMap.get(KEY_MESSAGE);
        displayMsg("Message from opponent '" + opponentName + "': "
            + msgFromOpponent);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        if (!InternetConnUtil.hasActiveInternetConnection(context)) {
          return NETWORK_UNAVAILABLE_MSG;
        }
        return "";
      }

      @Override
      protected void onPostExecute(String result) {
        // mDisplay.append(msg + "\n");
        if (!result.equals("")) {
          displayMsg(result);
        }
      }
    }.execute(null, null, null);
  }

  @Override
  protected void onResume() {
    super.onResume();
    setActivityActive(true);
    checkPlayServices();
    // Check device for Play Services APK. If check succeeds, proceed with
    // GCM registration.
    if (checkPlayServices()) {
      gcm = GoogleCloudMessaging.getInstance(this);
      myRegId = getRegistrationId(context);

      if (myRegId.equals("")) {
        registerInBackground();
      }
    } else {
      Log.i(TAG, "No valid Google Play Services APK found.");
    }
    SharedPreferences sp = getCommSharedPreferences(context);
    opponentRegId = sp.getString(PREF_OPPONENT_REG_ID, "");
    opponentName = sp.getString(PREF_OPPONENT_NAME, "");
    Log.d(TAG, "OnResume() - opponentName: " + opponentName
        + ", opponentRegId: " + opponentRegId);
    // This needs to be in the activity that will end up receiving the broadcast
    registerReceiver(receiver, new IntentFilter("TEST COMM INTENT_ACTION"));

    handleNotification(sp);
  }

  private void handleNotification(SharedPreferences sp) {
    String data = sp.getString(KEY_NOTIFICATION_DATA, "");
    if (!data.equals("")) {
      handleOpponentResponse(data);
      sp.edit().putString(KEY_NOTIFICATION_DATA, "").commit();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    setActivityActive(false);
    unregisterReceiver(receiver);
  }

  private void setActivityActive(boolean active) {
    SharedPreferences sp = getCommSharedPreferences(context);
    Editor ed = sp.edit();
    ed.putBoolean(ACTIVITY_ACTIVE_PREF, active);
    ed.commit();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.communication_interphone_comm_startgame_button:
      if (opponentRegId != null && !opponentRegId.equals("")) {
        displayMsg("Already connected to '" + opponentName
            + "' (RegistrationId = " + opponentRegId + ").");
        return;
      }
      EditText usernameEditText = (EditText) findViewById(R.id.communication_interphone_comm_username_editText);
      this.username = usernameEditText.getText().toString();

      if (this.username.equals("")) {
        displayMsg("Please enter a valid username value.");
      } else {
        displayMsg("Starting game...");
        usernameEditText.setText("");

        if (!InternetConnUtil.isNetworkAvailable(context)) {
          displayMsg(NETWORK_UNAVAILABLE_MSG);
          return;
        }
        // check if user is waiting
        // If user waiting, pair with that user
        // Else, add yourself to waiting user list and start a service polling
        // for
        // another user
        new AsyncTask<String, Integer, String>() {
          @Override
          protected String doInBackground(String... params) {
            String retVal = "";
            String result = "";
            if (KeyValueAPI.isServerAvailable()) {
              String waitingPlayerVal = KeyValueAPI.get(TEAM_NAME, PASSWORD,
                  KEY_WAITING_PLAYER);

              if (waitingPlayerVal.contains("Error: No Such Key")) {
                // No player waiting... put your own regId
                result = KeyValueAPI.put(TEAM_NAME, PASSWORD,
                    KEY_WAITING_PLAYER, myRegId + "," + username);
                if (!result.contains("Error")) {
                  // displayMsg("No player waiting... putting your regId "
                  // + myRegId + " in WAITING_PLAYER.");
                  retVal = "No player waiting... storing your RegistrationId "
                      + myRegId + " on server.";
                } else {
                  // displayMsg("Error while putting your regId on server: "
                  // + result);
                  retVal = "Error while putting your regId on server: "
                      + result;
                }
              } else {
                String tempArr[] = waitingPlayerVal.split(",");
                String oppRegId = tempArr[0];
                String oppName = tempArr[1];
                if (oppRegId.equals(myRegId)) {
                  // displayMsg("Waiting for another player to join...");
                  retVal = "Waiting for another player to join...";
                } else {
                  // Get opponents regId and connect
                  KeyValueAPI.clear(TEAM_NAME, PASSWORD);
                  opponentRegId = oppRegId;
                  opponentName = oppName;
                  Log.d(TAG, "Storing data got from server '"
                      + waitingPlayerVal + "' in shared preference...\n"
                      + "opponentName= " + opponentName + ", opponentRegId= "
                      + opponentRegId);
                  Editor ed = getCommSharedPreferences(context).edit();
                  ed.putString(PREF_OPPONENT_REG_ID, opponentRegId);
                  ed.putString(PREF_OPPONENT_NAME, opponentName);
                  ed.commit();
                  try {
                    result = sendPost("data." + KEY_MSG_TYPE + "="
                        + MSG_TYPE_CONNECT + "&data." + KEY_REG_ID + "="
                        + myRegId + "&data." + KEY_USERNAME + "=" + username);
                    Log.d(TAG, "Result of HTTP POST: " + result);
                    // displayMsg("Connected to user:" + oppName + " (" +
                    // oppRegId + ")");
                    retVal = "Connected to opponent:" + oppName + " ("
                        + oppRegId + ")";
                    // sendPost("data=" + myRegId);
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // displayMsg("Error occurred while making an HTTP post call.");
                    retVal = "Error occured while making an HTTP post call.";
                    e.printStackTrace();
                  }
                }
              }
            }
            return retVal;
          }

          @Override
          protected void onPostExecute(String result) {
            // mDisplay.append(msg + "\n");
            displayMsg(result);
          }
        }.execute(null, null, null);
      }
      break;
    case R.id.communication_interphone_comm_sendmsg_button:
      // new TestCommAsynTask().execute(MAKE_MOVE + "", username);
      EditText sendmsgEditText = (EditText) findViewById(R.id.communication_interphone_comm_sendmsg_editText);
      this.msgForOpponent = sendmsgEditText.getText().toString();

      if (opponentRegId == null || opponentRegId.equals("")) {
        displayMsg("Start a game to connect to your opponent before sending messages.");
        sendmsgEditText.setText("");
      } else if (msgForOpponent.equals("")) {
        displayMsg("Please enter a message for your opponent.");
      } else {
        displayMsg("Sending message to opponent...");
        sendmsgEditText.setText("");

        if (!InternetConnUtil.isNetworkAvailable(context)) {
          displayMsg(NETWORK_UNAVAILABLE_MSG);
          return;
        }
        new AsyncTask<String, Integer, String>() {
          @Override
          protected String doInBackground(String... params) {
            String retVal = "";
            String result = "";
            try {
              result = sendPost("data." + KEY_MSG_TYPE + "=" + MSG_TYPE_MOVE
                  + "&data." + KEY_MESSAGE + "=" + msgForOpponent);
              Log.d(TAG, "Result of HTTP POST: " + result);
              // displayMsg("Message sent to opponent (" + opponentName + ")");
              retVal = "Message sent to opponent '" + opponentName + "': "
                  + msgForOpponent;
              // sendPost("data=" + myRegId);
            } catch (Exception e) {
              // TODO Auto-generated catch block
              // displayMsg("Error occured while making an HTTP post call.");
              retVal = "Error occured while making an HTTP post call.";
              e.printStackTrace();
            }
            return retVal;
          }

          @Override
          protected void onPostExecute(String result) {
            // mDisplay.append(msg + "\n");
            displayMsg(result);
          }
        }.execute(null, null, null);
      }
      break;
    case R.id.communication_interphone_comm_quit_button:
      finish();
      break;
    }
  }

  private void displayMsg(String msg) {
    // Toast t = Toast.makeText(getApplicationContext(), msg, 2000);
    // t.show();
    Log.d(TAG, "\n===================================================\n");
    Log.d(TAG, msg);
    Log.d(TAG, "\n===================================================\n");
    TextView msgTxtView = (TextView) findViewById(R.id.communication_interphone_comm_msg_textview);
    msgTxtView.setText(msg);
  }

  /**
   * Check the device to make sure it has the Google Play Services APK. If it
   * doesn't, display a dialog that allows users to download the APK from the
   * Google Play Store or enable it in the device's system settings.
   */
  private boolean checkPlayServices() {
    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
    if (resultCode != ConnectionResult.SUCCESS) {
      if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
        GooglePlayServicesUtil.getErrorDialog(resultCode, this,
            PLAY_SERVICES_RESOLUTION_REQUEST).show();
      } else {
        Log.i(TAG, "This device is not supported.");
        finish();
      }
      return false;
    }
    return true;
  }

  /**
   * Gets the current registration ID for application on GCM service.
   * <p>
   * If result is empty, the app needs to register.
   * 
   * @return registration ID, or empty string if there is no existing
   *         registration ID.
   */
  private String getRegistrationId(Context context) {
    final SharedPreferences prefs = getCommSharedPreferences(context);
    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
    if (registrationId.equals("")) {
      Log.i(TAG, "Registration not found.");
      return "";
    }
    // Check if app was updated; if so, it must clear the registration ID
    // since the existing regID is not guaranteed to work with the new
    // app version.
    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
        Integer.MIN_VALUE);
    int currentVersion = getAppVersion(context);
    if (registeredVersion != currentVersion) {
      Log.i(TAG, "App version changed.");
      return "";
    }
    return registrationId;
  }

  /**
   * @return Application's {@code SharedPreferences}.
   */
  protected SharedPreferences getCommSharedPreferences(Context context) {
    // This sample app persists the registration ID in shared preferences, but
    // how you store the regID in your app is up to you.
    return getSharedPreferences(TestInterphoneComm.class.getSimpleName(),
        Context.MODE_PRIVATE);
  }

  /**
   * @return Application's version code from the {@code PackageManager}.
   */
  private static int getAppVersion(Context context) {
    try {
      PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
          context.getPackageName(), 0);
      return packageInfo.versionCode;
    } catch (NameNotFoundException e) {
      // should never happen
      throw new RuntimeException("Could not get package name: " + e);
    }
  }

  /**
   * Registers the application with GCM servers asynchronously.
   * <p>
   * Stores the registration ID and app versionCode in the application's shared
   * preferences.
   */
  private void registerInBackground() {
    if (!InternetConnUtil.isNetworkAvailable(context)) {
      return;
    }
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        String msg = "";
        try {
          if (gcm == null) {
            gcm = GoogleCloudMessaging.getInstance(context);
          }
          myRegId = gcm.register(SENDER_ID);
          msg = "Device registered, registration ID=" + myRegId;

          // You should send the registration ID to your server over HTTP,
          // so it can use GCM/HTTP or CCS to send messages to your app.
          // The request to your server should be authenticated if your app
          // is using accounts.
          sendRegistrationIdToBackend();

          // For this demo: we don't need to send it because the device
          // will send upstream messages to a server that echo back the
          // message using the 'from' address in the message.

          // Persist the regID - no need to register again.
          storeRegistrationId(context, myRegId);
        } catch (IOException ex) {
          msg = "Error :" + ex.getMessage();
          // If there is an error, don't just keep trying to register.
          // Require the user to click a button again, or perform
          // exponential back-off.
        }
        return msg;
      }

      @Override
      protected void onPostExecute(String msg) {
        // mDisplay.append(msg + "\n");
        displayMsg(msg + "\n");
      }
    }.execute(null, null, null);
    // ...
  }

  /**
   * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
   * or CCS to send messages to your app. Not needed for this demo since the
   * device sends upstream messages to a server that echoes back the message
   * using the 'from' address in the message.
   */
  private void sendRegistrationIdToBackend() {
    // Your implementation here.
  }

  /**
   * Stores the registration ID and app versionCode in the application's
   * {@code SharedPreferences}.
   * 
   * @param context
   *          application's context.
   * @param regId
   *          registration ID
   */
  private void storeRegistrationId(Context context, String regId) {
    final SharedPreferences prefs = getCommSharedPreferences(context);
    int appVersion = getAppVersion(context);
    Log.i(TAG, "Saving regId on app version " + appVersion);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(PROPERTY_REG_ID, regId);
    editor.putInt(PROPERTY_APP_VERSION, appVersion);
    editor.commit();
  }

  // private class TestCommAsynTask extends AsyncTask<String, Integer, String> {
  //
  // @Override
  // protected String doInBackground(String... params) {
  // String result = "";
  // if (KeyValueAPI.isServerAvailable()) {
  // int operation = Integer.parseInt(params[0]);
  // String key, value;
  // switch (operation) {
  // case START_GAME:
  // username = params[1];
  // // check if user is waiting
  //
  // String userWaiting = KeyValueAPI.get(TEAM_NAME, PASSWORD,
  // PollingKeys.USER_WAITING);
  // if (!userWaiting.contains("Error")) {
  // // add yourself to waiting user list and start a service polling for
  // // another user
  // // result = "Saved (" + key + ", " + value + ")";
  // } else {
  // // If user waiting, pair with that user
  // }
  // break;
  // case MAKE_MOVE:
  // key = params[1];
  // result = KeyValueAPI.get(TEAM_NAME, PASSWORD, key);
  // if (!result.contains("Error")) {
  // result = "Fetched value of key " + key + ": " + result;
  // }
  // break;
  // }
  // } else {
  // result =
  // "Server is currently unavailable. Please try again after some time.";
  // }
  // return result;
  // }
  //
  // @Override
  // protected void onPreExecute() {
  // super.onPreExecute();
  // // displayProgressBar("Downloading...");
  // }
  //
  // @Override
  // protected void onPostExecute(String result) {
  // super.onPostExecute(result);
  // // dismissProgressBar();
  // displayMsg(result);
  // }
  //
  // }

  // HTTP POST request
  private String sendPost(String dataStr) throws Exception {

    String url = "https://selfsolve.apple.com/wcResults.do";
    URL obj = new URL(SERVER_URL);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

    // add reuqest header
    con.setRequestMethod("POST");
    con.setRequestProperty("Authorization", "key=" + BROWSER_API_KEY);
    // con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

    String urlParameters = dataStr + "&registration_id=" + opponentRegId;

    // Send post request
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(urlParameters);
    wr.flush();
    wr.close();

    int responseCode = con.getResponseCode();
    System.out.println("\nSending 'POST' request to URL : " + url);
    System.out.println("Post parameters : " + urlParameters);
    System.out.println("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(new InputStreamReader(
        con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();

    // print result
    // displayMsg("\n HTTP Post response: " + response.toString());
    // Log.d(TAG, "HTTP POST response" + response.toString());
    return response.toString();
  }
}
