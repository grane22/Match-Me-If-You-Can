package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class RequestFromOpponent extends Activity implements OnClickListener {

  protected static final String TAG = "REQUEST FROM OPPONENT ACTIVITY";
  private Intent i;
  Context context;
  BroadcastReceiver receiver;
  private String username, regId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.two_player_wordgame_request_from_opponent);
    
    context = getApplicationContext();

    // Set up click listeners for all the buttons
    View acceptButton = findViewById(R.id.two_player_wordgame_accept_button);
    acceptButton.setOnClickListener(this);
    
    View rejectButton = findViewById(R.id.two_player_wordgame_reject_button);
    rejectButton.setOnClickListener(this);
    
// // This will handle the broadcast
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
  }
  
  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    
    SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREF_CONST,
        context.MODE_PRIVATE);
    this.username = sp.getString(Constants.PREF_USERNAME, "");
    this.regId = sp.getString(Constants.PREF_REG_ID, "");
    
//    // This needs to be in the activity that will end up receiving the broadcast
//    registerReceiver(receiver, new IntentFilter("INTENT_ACTION"));

//    handleNotification(getSharedPreferences(Constants.SHARED_PREF_CONST,
//      Context.MODE_PRIVATE));
  }
  
  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
//    unregisterReceiver(receiver);
  }
  
  @Override
  public void onClick(View v) {
    String oppName, oppRegId;
    Intent intent = getIntent();
    
    switch (v.getId()) {
    case R.id.two_player_wordgame_accept_button:
      //  Go to ConnectedToOponent activity
      
      oppName = intent.getStringExtra(Constants.EXTRA_OPPONENT_NAME);
      oppRegId = intent.getStringExtra(Constants.EXTRA_OPPONENT_REDID);
        
      // Store opponent name and regId in SP
      Util.storeOppnentInSharedpref(getSharedPreferences(Constants.SHARED_PREF_CONST,
          Context.MODE_PRIVATE), oppName, oppRegId);
      
      i = new Intent(this, MsgFromOpponent.class);
      i.putExtra(Constants.EXTRA_MSG, "Game started with opponent '" + oppName + "'.\n" 
          + "You go first!");
      i.putExtra(Constants.EXTRA_ROUND, 0);
      i.putExtra(Constants.EXTRA_IS_PLAYER_ONE, true);
//      i.putExtra(Constants.EXTRA_SCOREBOARD, Util.getInitialScoreboard());
      startActivity(i);
      
      //  send an accept ack to oppponent
      sendReqAckToOpponent(true, oppRegId);
      
      //  remove urself from AVAILABLE_USERS_LIST
      Util.removeValuesFromKeyOnServer(Constants.AVAILABLE_USERS_LIST, this.username, this.regId);
      break;
    case R.id.two_player_wordgame_reject_button:
      oppRegId = intent.getStringExtra(Constants.EXTRA_OPPONENT_REDID);
      
      //  send an reject ack to oppponent
      sendReqAckToOpponent(false, oppRegId);
      finish();
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
        // mDisplay.append(msg + "\n");
//        Toast t = Toast.makeText(getApplicationContext(), result, 2000);
//        t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
//        if (!result.equals(Constants.NO_PLAYER_ONLINE)) {
//          // go to WaitingForOpponent activity dialog
//          Intent i = new Intent(getApplicationContext(),
//              WaitingForOpponent.class);
//          startActivity(i);
//        }
      }
    }.execute(String.valueOf(accepted), oppRegId, null);
  }
  
//  protected void handleOpponentResponse(String data) {
//    Log.d(TAG, "Inside handleOpponentResponse()");
//    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
//    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
//      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
//      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
//      if (msgType.equals(Constants.MSG_TYPE_2P_CONNECT)) {
//        // Log.d(TAG, "Inside MSG_TYPE_CONNECT = " + MSG_TYPE_CONNECT);
////        opponentName = dataMap.get(Constants.KEY_USERNAME);
////        opponentRegId = dataMap.get(Constants.KEY_REG_ID);
////
////        i = new Intent(this, RequestFromOpponent.class);
////        i.putExtra(Constants.EXTRA_OPPONENT_NAME, opponentName);
////        i.putExtra(Constants.EXTRA_OPPONENT_REDID, opponentRegId);
////        startActivity(i);
//        // ///
//        // Editor ed = getSharedPreferences(Constants.SHARED_PREF_CONST,
//        // Context.MODE_PRIVATE).edit();
//        // ed.putString(Constants.PREF_OPPONENT_REG_ID, opponentRegId);
//        // ed.putString(Constants.PREF_OPPONENT_NAME, opponentName);
//        // ed.commit();
//        // Log.d(TAG, "Message sent to displayMsg() => Connected to opponent:"
//        // + opponentName + " (" + opponentRegId + ")");
//        // displayMsg("Connected to opponent:" + opponentName + " ("
//        // + opponentRegId + ")");
//      }
//    }
//  }

//  private void handleNotification(SharedPreferences sp) {
//    String data = sp.getString(Constants.KEY_NOTIFICATION_DATA, "");
//    if (!data.equals("")) {
//      handleOpponentResponse(data);
//      sp.edit().putString(Constants.KEY_NOTIFICATION_DATA, "").commit();
//    }
//  }
}
