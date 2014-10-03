package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.InternetConnUtil;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import edu.neu.mhealth.api.KeyValueAPI;

public class TwoPlayerWordGame extends Activity implements OnClickListener {

  private static final String TAG = "Two Player Word Game";

  private MediaPlayer mpMenuMusic;
  private int menuMusicResId = R.raw.wordgame_menu_music;
  private boolean playBgMusic = false;

  private Intent i;
  private boolean isContinueAvailable;

  GoogleCloudMessaging gcm;
  Context context;

  private String username, opponentName, myRegId, opponentRegId,
      msgForOpponent;
  BroadcastReceiver receiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.two_player_wordgame_main);

    // Set up click listeners for all the buttons
    View newgameButton = findViewById(R.id.two_player_wordgame_newgame_button);
    newgameButton.setOnClickListener(this);

    View continueButton = findViewById(R.id.two_player_wordgame_continue_button);
    continueButton.setOnClickListener(this);
    
    View topScorersButton = findViewById(R.id.two_player_wordgame_top_scorers_button);
    topScorersButton.setOnClickListener(this);

    View ackButton = findViewById(R.id.two_player_wordgame_ack_button);
    ackButton.setOnClickListener(this);

    View instructionsButton = findViewById(R.id.two_player_wordgame_instructions_button);
    instructionsButton.setOnClickListener(this);

    View settingsButton = findViewById(R.id.two_player_wordgame_settings_button);
    settingsButton.setOnClickListener(this);

    View returnButton = findViewById(R.id.two_player_wordgame_return_button);
    returnButton.setOnClickListener(this);

    context = getApplicationContext();

//    // This will handle the broadcast
//    receiver = new BroadcastReceiver() {
//      // @Override
//      public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "Inside onReceive of Broadcast receiver");
//        String action = intent.getAction();
//        if (action.equals("INTENT_ACTION")) {
//          String data = intent.getStringExtra("data");
//          Log.d(TAG, "data = " + data);
//          handleOpponentResponse(data);
//        }
//      }
//    };
    
    //  Read and set username from SP
    this.username = getSharedPreferences(Constants.SHARED_PREF_CONST,
        context.MODE_PRIVATE).getString(Constants.PREF_USERNAME, "");
  }

  @Override
  protected void onStart() {
    // TODO Auto-generated method stub
    super.onStart();
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        if (!InternetConnUtil.hasActiveInternetConnection(context)) {
          return Constants.NETWORK_UNAVAILABLE_MSG;
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
    if (this.username == null || this.username == "") {
      showWelcomeMsg(false, null);
    } else {
      showWelcomeMsg(true, this.username);
    }
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
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        context.MODE_PRIVATE);
    opponentRegId = sp.getString(Constants.PREF_OPPONENT_REG_ID, "");
    opponentName = sp.getString(Constants.PREF_OPPONENT_NAME, "");
    Log.d(TAG, "OnResume() - opponentName: " + opponentName
        + ", opponentRegId: " + opponentRegId);
    // This needs to be in the activity that will end up receiving the broadcast
//    registerReceiver(receiver, new IntentFilter("INTENT_ACTION"));
//
//    handleNotification(sp);
    // ////////
    playBgMusic = Prefs.getMusic(this);

    this.isContinueAvailable = getSharedPreferences("WORD_GAME", MODE_PRIVATE)
        .getBoolean(Constants.PREF_CONTINUE_GAME, false);

    View continueButton = findViewById(R.id.two_player_wordgame_continue_button);
    if (isContinueAvailable) {
      continueButton.setVisibility(View.VISIBLE);
    } else {
      continueButton.setVisibility(View.GONE);
    }

    if (playBgMusic) {
      if (mpMenuMusic != null) {
        mpMenuMusic.release();
      }
      // Create a new MediaPlayer to play this sound
      mpMenuMusic = MediaPlayer.create(this, menuMusicResId);
      mpMenuMusic.start();
      mpMenuMusic.setLooping(true);
    }
  }

  private void showWelcomeMsg(boolean showWelMsg, String username) {
    TextView welMsgTextView = (TextView) findViewById(R.id.two_player_wordgame_welcome_msg);
    EditText usernameEditText = (EditText) findViewById(R.id.two_player_wordgame_username_edittext);
    if (showWelMsg) {
      usernameEditText.setVisibility(View.GONE);
      welMsgTextView.setVisibility(View.VISIBLE);
      welMsgTextView.setText("Hello " + username);
    } else {
      welMsgTextView.setVisibility(View.GONE);
      usernameEditText.setVisibility(View.VISIBLE);
    }
  }

  protected void onPause() {
    super.onPause();
    if (mpMenuMusic != null) {
      mpMenuMusic.release();
    }
    setActivityActive(false);
    //unregisterReceiver(receiver);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.two_player_wordgame_newgame_button:
      Log.d(TAG, "new game button clicked! this.username : " + this.username);
      if (this.username == null || this.username == "") {
        EditText usernameEditText = (EditText) findViewById(R.id.two_player_wordgame_username_edittext);
        this.username = usernameEditText.getText().toString();

        if (this.username.equals("")) {
          displayMsg("Please enter a valid username value.");
        } else {
          Editor ed = getSharedPreferences(Constants.SHARED_PREF_CONST,
              Context.MODE_PRIVATE).edit();
          ed.putString(Constants.PREF_USERNAME, this.username).commit();          
        }
      }
      Log.d(TAG, "adding username to Available users list on server");
      // Add username to AVAILABLE_USERS list on server
      Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST,
          this.username, this.myRegId);

      Log.d(TAG, "Starting ChooseOpponent activity");
      // Go to new activity for choosing an opponent
      i = new Intent(this, ChooseOpponent.class);
      startActivity(i);

      break;
    case R.id.two_player_wordgame_continue_button:
      i = new Intent(this, Game.class);
      i.putExtra(Game.CONTINUE_GAME, true);
      startActivity(i);
      break;
    case R.id.two_player_wordgame_top_scorers_button:
//      FetchAndDisplayTopScorersList();
      Intent i = new Intent(this, TopScorers.class);
      startActivity(i);
//      showTopScorersDialog();
      break;
    case R.id.two_player_wordgame_settings_button:
      startActivity(new Intent(this, Prefs.class));
      break;
    case R.id.two_player_wordgame_ack_button:
      i = new Intent(this, Acknowledgements.class);
      startActivity(i);
      break;
    case R.id.two_player_wordgame_instructions_button:
      i = new Intent(this, Instructions.class);
      startActivity(i);
      break;
    case R.id.two_player_wordgame_return_button:
      if (mpMenuMusic != null) {
        mpMenuMusic.release();
      }
      finish();
      break;
    }
  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(Constants.MSG_TYPE_2P_CONNECT)) {
        // Log.d(TAG, "Inside MSG_TYPE_CONNECT = " + MSG_TYPE_CONNECT);
        opponentName = dataMap.get(Constants.KEY_USERNAME);
        opponentRegId = dataMap.get(Constants.KEY_REG_ID);

        i = new Intent(this, RequestFromOpponent.class);
        i.putExtra(Constants.EXTRA_OPPONENT_NAME, opponentName);
        i.putExtra(Constants.EXTRA_OPPONENT_REDID, opponentRegId);
        startActivity(i);
        // ///
        // Editor ed = getSharedPreferences(Constants.SHARED_PREF_CONST,
        // Context.MODE_PRIVATE).edit();
        // ed.putString(Constants.PREF_OPPONENT_REG_ID, opponentRegId);
        // ed.putString(Constants.PREF_OPPONENT_NAME, opponentName);
        // ed.commit();
        // Log.d(TAG, "Message sent to displayMsg() => Connected to opponent:"
        // + opponentName + " (" + opponentRegId + ")");
        // displayMsg("Connected to opponent:" + opponentName + " ("
        // + opponentRegId + ")");
      } else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_ACCEPT)) {
        // Start Game - Go to MsgFromOpponent activity dialog
        opponentName = dataMap.get(Constants.KEY_USERNAME);
        opponentRegId = dataMap.get(Constants.KEY_REG_ID);

        //  Store opponent name and regId in SP
        Util.storeOppnentInSharedpref(getSharedPreferences(Constants.SHARED_PREF_CONST,
            Context.MODE_PRIVATE), opponentName, opponentRegId);
//        Editor ed = getSharedPreferences(Constants.SHARED_PREF_CONST,
//            Context.MODE_PRIVATE).edit();
//        ed.putString(Constants.PREF_OPPONENT_REG_ID, opponentRegId);
//        ed.putString(Constants.PREF_OPPONENT_NAME, opponentName);
//        ed.commit();
//        Log.d(TAG, "Message sent to displayMsg() => Connected to opponent:"
//            + opponentName + " (" + opponentRegId + ")");
        
        //  Go to MsgFromOpponent activity dialog
        i = new Intent(this, MsgFromOpponent.class);
        i.putExtra(Constants.EXTRA_MSG, "Connected to '" + opponentName + "'.");
        startActivity(i);

      } else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_REJECT)) {
        // Show reject msg and return to ChooseOpponent activity
        opponentName = dataMap.get(Constants.KEY_USERNAME);
        displayMsg("Game request denied by user '" + opponentName + "'.");
        i = new Intent(this, ChooseOpponent.class);
        startActivity(i);

      } else if (msgType.equals(Constants.MSG_TYPE_2P_MOVE)) {
        // Log.d(TAG, "Inside MSG_TYPE_MOVE = " + MSG_TYPE_MOVE);
        String msgFromOpponent = dataMap.get(Constants.KEY_MESSAGE);
        displayMsg("Message from opponent '" + opponentName + "': "
            + msgFromOpponent);
      }
    }
  }

  private void handleNotification(SharedPreferences sp) {
    String data = sp.getString(Constants.KEY_NOTIFICATION_DATA, "");
    if (!data.equals("")) {
      handleOpponentResponse(data);
      sp.edit().putString(Constants.KEY_NOTIFICATION_DATA, "").commit();
    }
  }

  private void setActivityActive(boolean active) {
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        context.MODE_PRIVATE);
    Editor ed = sp.edit();
    ed.putBoolean(Constants.ACTIVITY_ACTIVE_PREF, active);
    ed.commit();
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
            Constants.PLAY_SERVICES_RESOLUTION_REQUEST).show();
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
    final SharedPreferences prefs = getSharedPreferences(
        Constants.SHARED_PREF_CONST, context.MODE_PRIVATE);
    String registrationId = prefs.getString(Constants.PREF_REG_ID, "");
    if (registrationId.equals("")) {
      Log.i(TAG, "Registration not found.");
      return "";
    }
    // Check if app was updated; if so, it must clear the registration ID
    // since the existing regID is not guaranteed to work with the new
    // app version.
    int registeredVersion = prefs.getInt(Constants.PROPERTY_APP_VERSION,
        Integer.MIN_VALUE);
    int currentVersion = getAppVersion(context);
    if (registeredVersion != currentVersion) {
      Log.i(TAG, "App version changed.");
      return "";
    }
    return registrationId;
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
          myRegId = gcm.register(Constants.SENDER_ID);
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
    final SharedPreferences prefs = getSharedPreferences(
        Constants.SHARED_PREF_CONST, context.MODE_PRIVATE);
    int appVersion = getAppVersion(context);
    Log.i(TAG, "Saving regId on app version " + appVersion);
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString(Constants.PREF_REG_ID, regId);
    editor.putInt(Constants.PROPERTY_APP_VERSION, appVersion);
    editor.commit();
  }

  private void displayMsg(String msg) {
    Toast t = Toast.makeText(getApplicationContext(), msg, 2000);
    t.show();
    Log.d(TAG, "\n===================================================\n");
    Log.d(TAG, msg);
    Log.d(TAG, "\n===================================================\n");
    // TextView msgTxtView = (TextView)
    // findViewById(R.id.communication_interphone_comm_msg_textview);
    // msgTxtView.setText(msg);
  }
  
  
  
//  public static void showTopScorersDialog(String formattedStr) {
//    AlertDialog.Builder builder = new AlertDialog.Builder(new TwoPlayerWordGame());
//    builder.setCancelable(true);
//    builder.setTitle("Top Scorers List");
//    builder.setMessage("\t Score \t \t Name \t \t \t Date-Time \n \n" 
//        + formattedStr);
////    builder.setInverseBackgroundForced(true);
//    builder.setPositiveButton("Back to Menu", new DialogInterface.OnClickListener() {
//      @Override
//      public void onClick(DialogInterface dialog, int which) {
//        dialog.dismiss();
//      }
//    });
//    
//    AlertDialog alert = builder.create();
//    alert.show();
//  
//  }

}
