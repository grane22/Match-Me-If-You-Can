package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.util.HashMap;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.AccelerometerListener;
import edu.neu.madcourse.dushyantdeshmukh.utilities.AccelerometerManager;
import edu.neu.madcourse.dushyantdeshmukh.utilities.InternetConnUtil;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import edu.neu.mhealth.api.KeyValueAPI;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseOpponent extends Activity implements OnClickListener, AccelerometerListener {

  protected static final String TAG = "CHOOSE OPPONENT ACTIVITY";
  private Intent i;
  private String username, regId, oppName, oppRegId;
  Context context;
  BroadcastReceiver receiver;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.two_player_wordgame_choose_opponent);

    context = getApplicationContext();

    // Set up click listeners for all the buttons
    View findOpponentButton = findViewById(R.id.two_player_wordgame_find_opponent_button);
    findOpponentButton.setOnClickListener(this);

    View randomOpponentButton = findViewById(R.id.two_player_wordgame_random_opponent_button);
    randomOpponentButton.setOnClickListener(this);

    View backButton = findViewById(R.id.two_player_wordgame_back_button);
    backButton.setOnClickListener(this);

    // // This will handle the broadcast
    // receiver = new BroadcastReceiver() {
    // // @Override
    // public void onReceive(Context context, Intent intent) {
    // Log.d(TAG, "Inside onReceive of Broadcast receiver");
    // String action = intent.getAction();
    // if (action.equals("INTENT_ACTION")) {
    // String data = intent.getStringExtra("data");
    // Log.d(TAG, "data = " + data);
    // handleOpponentResponse(data);
    // }
    // }
    // };

    // This will handle the broadcast
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG,
            "Inside onReceive of Broadcast receiver of ChooseOpponent.class");
//        displayMsg("Inside onReceive of Broadcast receiver of ChooseOpponent.");
        String action = intent.getAction();
        if (action.equals(Constants.INTENT_ACTION_CHOOSE_OPPONENT)) {
          String data = intent.getStringExtra("data");
          Log.d(TAG, "data = " + data);
          handleOpponentResponse(data);
        }
      }
    };
  }

  @Override
  protected void onStart() {
    super.onStart();
    // Put username in AVAILABLE_USERS_LIST
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        context.MODE_PRIVATE);
    this.username = sp.getString(Constants.PREF_USERNAME, "");
    this.regId = sp.getString(Constants.PREF_REG_ID, "");

    Util.addValuesToKeyOnServer(Constants.AVAILABLE_USERS_LIST, this.username,
        this.regId);
  }

  @Override
  protected void onStop() {
    super.onStop();
    // Remove username from AVAILABLE_USERS_LIST
    Util.removeValuesFromKeyOnServer(Constants.AVAILABLE_USERS_LIST,
        this.username, this.regId);
    
  //Check device supported Accelerometer senssor or not
    if (AccelerometerManager.isListening()) {
         
        //Start Accelerometer Listening
        AccelerometerManager.stopListening();
         
//        Toast.makeText(getBaseContext(), "onStop Accelerometer Stoped", 
//                 Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onDestroy() {
      super.onDestroy();
      //Check device supported Accelerometer senssor or not
      if (AccelerometerManager.isListening()) {
           
          //Start Accelerometer Listening
          AccelerometerManager.stopListening();
           
//          Toast.makeText(getBaseContext(), "onDestroy Accelerometer Stoped", 
//                 Toast.LENGTH_SHORT).show();
      }
           
  }
  
  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    // This needs to be in the activity that will end up receiving the broadcast
    registerReceiver(receiver, new IntentFilter(
        Constants.INTENT_ACTION_CHOOSE_OPPONENT));

  //Check device supported Accelerometer senssor or not
    if (AccelerometerManager.isSupported(this)) {
         displayMsg("Starting accelerometer listening...");
        //Start Accelerometer Listening
        AccelerometerManager.startListening(this);
    }
    
    handleNotification(getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE));
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    unregisterReceiver(receiver);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.two_player_wordgame_find_opponent_button:
      EditText opponentEditText = (EditText) findViewById(R.id.two_player_wordgame_opponent_name_edittext);
      String oppUsername = opponentEditText.getText().toString();

      if (oppUsername.equals("")) {
        displayMsg("Please enter a valid opponent username value.");
      } else {
//        displayMsg("Starting game...");
        opponentEditText.setText("");
        
        findOpponent(oppUsername);
      }
      break;
    case R.id.two_player_wordgame_random_opponent_button:
      connectToRandomOpponent(this.username, this.regId);
      break;
    case R.id.two_player_wordgame_back_button:
      finish();
      break;
    }
  }

  private void findOpponent(String oppUsername) {
    if (!InternetConnUtil.isNetworkAvailable(context)) {
      displayMsg(Constants.NETWORK_UNAVAILABLE_MSG);
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
        String oppUsername = params[0];
        String retVal = "";
        String result = "";
        boolean foundOpponent = false;
        if (KeyValueAPI.isServerAvailable()) {
          String availableUsersList = KeyValueAPI.get(Constants.TEAM_NAME,
              Constants.PASSWORD, Constants.AVAILABLE_USERS_LIST);

          if (availableUsersList.contains("Error: No Such Key")) {
            // No player waiting... put your own regId
            retVal = "Error while putting your regId on server: " + result;
          } else {
            if (availableUsersList.trim() != "") {
              String usersArr[] = availableUsersList.split(",");
              // Iterate over list of entries in key 'keyname'and check for val1
              for (int i = 0; i < usersArr.length; i++) {
                String tempArr[] = usersArr[i].split("::");
                String oppName = tempArr[0];
                String oppRegId = tempArr[1];

                if (oppUsername.equalsIgnoreCase(oppName)) {
                  Log.d(TAG, "\noppRegId= " + oppRegId + "\n");
                  Log.d(TAG, "\nregId= " + regId + "\n");

                  // Get opponents regId and connect
                  Log.d(TAG, "Sending connect request to opponent'"
                      + "opponentName= " + oppName + ", opponentRegId= "
                      + oppRegId);

                  try {
                    result = Util.sendPost("data." + Constants.KEY_MSG_TYPE
                        + "=" + Constants.MSG_TYPE_2P_CONNECT + "&data."
                        + Constants.KEY_REG_ID + "=" + regId + "&data."
                        + Constants.KEY_USERNAME + "=" + username, oppRegId);
                    Log.d(TAG, "Result of HTTP POST: " + result);
                    // displayMsg("Connected to user:" + oppName + " (" +
                    // oppRegId + ")");
                    retVal = "Sent connect request to opponent:" + oppName
                        + " (" + oppRegId + ")";
                    // sendPost("data=" + myRegId);
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // displayMsg("Error occurred while making an HTTP post call.");
                    retVal = "Error occured while making an HTTP post call.";
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
          retVal = Constants.OPPONENT_NOT_FOUND;
        }
        Log.d(TAG, "retVal: " + retVal);
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        // mDisplay.append(msg + "\n");
//        Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//        t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
        displayMsg(result);
        if (!result.equals(Constants.OPPONENT_NOT_FOUND)) {
//           go to WaitingForOpponent activity dialog
//          Intent i = new Intent(getApplicationContext(),
//              WaitingForOpponent.class);
//          startActivity(i);
          //  show msg waiting for user
          TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg);
          msgTextView.setText("Waiting for opponent's response... ");
        }
      }
    }.execute(oppUsername, null, null);
    }

  private void connectToRandomOpponent(String uname, String rId) {
    // check if user is waiting
    // If user waiting, pair with that user (send game request)
    // Else, add yourself to waiting user list and show toast (no random
    // opponent available)

    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        String retVal = "";
        String result = "";
        boolean foundOpponent = false;
        if (KeyValueAPI.isServerAvailable()) {
          String availableUsersList = KeyValueAPI.get(Constants.TEAM_NAME,
              Constants.PASSWORD, Constants.AVAILABLE_USERS_LIST);

          if (availableUsersList.contains("Error: No Such Key")) {
            // No player waiting... put your own regId
            retVal = "Error while putting your regId on server: " + result;
          } else {
            if (availableUsersList.trim() != "") {
              String usersArr[] = availableUsersList.split(",");
              // Iterate over list of entries in key 'keyname'and check for val1
              for (int i = 0; i < usersArr.length; i++) {
                String tempArr[] = usersArr[i].split("::");
                String oppName = tempArr[0];
                String oppRegId = tempArr[1];

                if (!oppRegId.equals(regId)) {
                  Log.d(TAG, "\noppRegId= " + oppRegId + "\n");
                  Log.d(TAG, "\nregId= " + regId + "\n");

                  // Get opponents regId and connect
                  Log.d(TAG, "Sending connect request to opponent'"
                      + "opponentName= " + oppName + ", opponentRegId= "
                      + oppRegId);

                  try {
                    result = Util.sendPost("data." + Constants.KEY_MSG_TYPE
                        + "=" + Constants.MSG_TYPE_2P_CONNECT + "&data."
                        + Constants.KEY_REG_ID + "=" + regId + "&data."
                        + Constants.KEY_USERNAME + "=" + username, oppRegId);
                    Log.d(TAG, "Result of HTTP POST: " + result);
                    // displayMsg("Connected to user:" + oppName + " (" +
                    // oppRegId + ")");
                    retVal = "Sent connect request to opponent:" + oppName
                        + " (" + oppRegId + ")";
                    // sendPost("data=" + myRegId);
                  } catch (Exception e) {
                    // TODO Auto-generated catch block
                    // displayMsg("Error occurred while making an HTTP post call.");
                    retVal = "Error occured while making an HTTP post call.";
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
          retVal = Constants.NO_PLAYER_ONLINE;
        }
        Log.d(TAG, "retVal: " + retVal);
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        // mDisplay.append(msg + "\n");
//        Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//        t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
        if (!result.equals(Constants.NO_PLAYER_ONLINE)) {
//           go to WaitingForOpponent activity dialog
//          Intent i = new Intent(getApplicationContext(),
//              WaitingForOpponent.class);
//          startActivity(i);
          //  show msg waiting for user
          TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg);
          msgTextView.setText("Waiting for opponent's response... ");
        }
      }
    }.execute(null, null, null);
  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
      TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg);
      msgTextView.setText("");
          
      this.oppName = dataMap.get(Constants.KEY_USERNAME);
        
      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(Constants.MSG_TYPE_2P_CONNECT)) {
        Log.d(TAG, "Inside MSG_TYPE_CONNECT = " + Constants.MSG_TYPE_2P_CONNECT);
        this.oppRegId = dataMap.get(Constants.KEY_REG_ID);   
        getSharedPreferences(Constants.SHARED_PREF_CONST, Context.MODE_PRIVATE)
        .edit().putString(Constants.PREF_OPPONENT_REG_ID, this.oppRegId).commit();
//        Log.d(TAG, "\n\n Setting this.oppRegId in SP: " + this.oppRegId + "\n\n");
//        displayMsg("Setting this.oppRegId in SP: " + this.oppRegId);
        // show [Accept, Reject] dialog
        showAcceptRejectDialog();
      } else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_ACCEPT)) {
        this.oppRegId = dataMap.get(Constants.KEY_REG_ID);   
        getSharedPreferences(Constants.SHARED_PREF_CONST, Context.MODE_PRIVATE)
        .edit().putString(Constants.PREF_OPPONENT_REG_ID, this.oppRegId).commit();
        Log.d(TAG, "\n\n Setting this.oppRegId in SP: " + this.oppRegId + "\n\n");
        
        // Show 'Connected to Opponent' msg and go to Game activity
//        String opponentName = dataMap.get(Constants.KEY_USERNAME);
        initiateGame(false);
        
        //  Remove urself from AVAILABLE_USERS_LIST
        Util.removeValuesFromKeyOnServer(Constants.AVAILABLE_USERS_LIST,
            this.username, this.regId);

      } else if (msgType.equals(Constants.MSG_TYPE_2P_ACK_REJECT)) {
        // Show 'Request reject' toast and stay on Choose Opponent activity
//        String opponentName = dataMap.get(Constants.KEY_USERNAME);
        displayMsg("Game request denied by user '" + oppName + "'.");
      }
    }
  }

  private void showAcceptRejectDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    builder.setTitle("Game Request");
    builder.setMessage("User '" + oppName + "' has sent a game request.");
//    builder.setInverseBackgroundForced(true);
    builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        initiateGame(true);
        // Remove urself from AVAILABLE_USERS_LIST
        Util.removeValuesFromKeyOnServer(Constants.AVAILABLE_USERS_LIST,
            username, regId);
      }
    });
    builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
//      send an reject ack to oppponent
        sendReqAckToOpponent(false, oppRegId);
      }
    });
    AlertDialog alert = builder.create();
    alert.show();
  }

  private void initiateGame(boolean asPLayerOne) {
    Intent i = new Intent(ChooseOpponent.this, Game.class);
    // i.putExtra(Constants.EXTRA_SCOREBOARD, this.scoreboard);
    i.putExtra(Constants.EXTRA_ROUND, 0);
    i.putExtra(Constants.EXTRA_IS_PLAYER_ONE, asPLayerOne);
    i.putExtra(Constants.EXTRA_INITIATE_GAME, false);
    i.putExtra(Constants.EXTRA_OPPONENT_NAME, oppName);
    
//    Toast.makeText(ChooseOpponent.this, "on Accept, setting roundNo = " + roundNo 
//        + "\n isPlayerOne = " + isPlayerOne, 3000).show();
 // Store opponent name and regId in SP
    
    Util.storeOppnentInSharedpref(getSharedPreferences(Constants.SHARED_PREF_CONST,
        Context.MODE_PRIVATE), oppName, oppRegId);
    
    startActivity(i);
    
    if (asPLayerOne) {
      //    send an accept ack to oppponent
      sendReqAckToOpponent(true, oppRegId);
    }
    
    //  remove urself from AVAILABLE_USERS_LIST
    Util.removeValuesFromKeyOnServer(Constants.AVAILABLE_USERS_LIST, username, regId);
    
  }

  private void handleNotification(SharedPreferences sp) {
    String data = sp.getString(Constants.KEY_NOTIFICATION_DATA, "");
    if (!data.equals("")) {
      handleOpponentResponse(data);
      sp.edit().putString(Constants.KEY_NOTIFICATION_DATA, "").commit();
    }
  }

  
  private void sendReqAckToOpponent(boolean accepted, String oppRegId) {
    Log.d(TAG, "Sending request ack: " 
  + (accepted? Constants.MSG_TYPE_2P_ACK_ACCEPT : Constants.MSG_TYPE_2P_ACK_REJECT));
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        String retVal;
        boolean accepted = Boolean.parseBoolean(params[0]);
        String oppRegId = params[1];
        try {
          retVal = Util.sendPost("data." + Constants.KEY_MSG_TYPE
              + "=" + (accepted? Constants.MSG_TYPE_2P_ACK_ACCEPT : Constants.MSG_TYPE_2P_ACK_REJECT) 
              + "&data." + Constants.KEY_REG_ID + "=" + regId + "&data."
              + Constants.KEY_USERNAME + "=" + username, oppRegId);
          Log.d(TAG, "Result of HTTP POST: " + retVal);
          // displayMsg("Connected to user:" + oppName + " (" +
          // oppRegId + ")");
          retVal = "Sent request ack to opponent:"
              + " (" + oppRegId + ")";
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
//        Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//        t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
      }
    }.execute(String.valueOf(accepted), oppRegId, null);
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

  @Override
  public void onAccelerationChanged(float x, float y, float z) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onShake(float force) {
 // Called when Motion Detected
//    displayMsg("Inside onShake(");
    displayMsg(" Shake detected. \n Connecting to random opponent...");
    connectToRandomOpponent(this.username, this.regId);
    
  }

}
