package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class MsgFromOpponent extends Activity implements OnClickListener {

  protected static final String TAG = "MSG FROM OPPONENT ACTIVITY";
  private Intent i;
  Context context;
  BroadcastReceiver receiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      Log.d(TAG, "Inside onReceive of Broadcast receiver");
      String action = intent.getAction();
      if (action.equals("MSG_FOR_OPPONENT")) {
        Log.d(TAG, "Action matches 'MSG_FOR_OPPONENT'");
        String data = intent.getStringExtra("data");
        Log.d(TAG, "data = " + data);
        handleOpponentResponse(data);
      }
    }
  };
  
  int roundNo = 0;
  int oppScore = 0;
  boolean isPlayerOne = false;
  String opponentName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.two_player_wordgame_msg_from_opponent);

    context = getApplicationContext();

    // Set up click listeners for all the buttons
    View continueButton = findViewById(R.id.two_player_wordgame_play_button);
    continueButton.setOnClickListener(this);

    TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg_from_opponent_textview);
    Intent intent = getIntent();

    // this.scoreboard = i.getStringExtra(Constants.EXTRA_SCOREBOARD);
    this.roundNo = intent.getIntExtra(Constants.EXTRA_ROUND, 0);
    this.isPlayerOne = intent.getBooleanExtra(Constants.EXTRA_IS_PLAYER_ONE, false);
    
    Toast.makeText(getApplicationContext(), "fetched roundNo = " + roundNo 
        + "\n isPlayerOne = " + isPlayerOne, 2000).show();
    
    Log.d(TAG, "\nroundNo = " + roundNo 
        + "\n isPlayerOne = " + isPlayerOne);
    
    if (this.roundNo > 0) {
      String oppName = intent.getStringExtra(Constants.EXTRA_OPPONENT_NAME);
      this.oppScore = intent.getIntExtra(Constants.EXTRA_OPP_CURR_SCORE, 0);
      msgTextView.setText("'" + oppName + "' made " + oppScore
          + " points in round " + roundNo + ".\nYour turn!");
      Log.d(TAG, "\n oppName = " + oppName 
          + "\n oppScore = " + oppScore);
    } else {
      String msgTxt = intent.getStringExtra(Constants.EXTRA_MSG);
      msgTextView.setText(msgTxt);
      Log.d(TAG, "msgTxt = " + msgTxt);
    }

    context = getApplicationContext();

    // This will handle the broadcast
    
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
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    // // This needs to be in the activity that will end up receiving the
    // broadcast
     registerReceiver(receiver, new IntentFilter("MSG_FOR_OPPONENT"));

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
    case R.id.two_player_wordgame_play_button:
      // start Game activity
      Toast.makeText(getApplicationContext(), "start Game activity", 2000)
          .show();
      Log.d(TAG, "start Game activity");

      Intent i = new Intent(this, Game.class);
      // i.putExtra(Constants.EXTRA_SCOREBOARD, this.scoreboard);
      i.putExtra(Constants.EXTRA_ROUND, this.roundNo);
      i.putExtra(Constants.EXTRA_IS_PLAYER_ONE, this.isPlayerOne);
      i.putExtra(Constants.EXTRA_INITIATE_GAME, true);
      if (this.roundNo > 0) {
        i.putExtra(Constants.EXTRA_OPP_CURR_SCORE, this.oppScore);
      }
      Toast.makeText(getApplicationContext(), "onClick(), setting roundNo = " + roundNo 
          + "\n isPlayerOne = " + isPlayerOne, 3000).show();
      startActivity(i);
    }
  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(Constants.MSG_TYPE_2P_ACK_ACCEPT)) {
        // Start Game - Go to MsgFromOpponent activity dialog
         opponentName = dataMap.get(Constants.KEY_USERNAME);
//         opponentRegId = dataMap.get(Constants.KEY_REG_ID);
        //
        // // Store opponent name and regId in SP
        // Util.storeOppnentInSharedpref(getSharedPreferences(Constants.SHARED_PREF_CONST,
        // Context.MODE_PRIVATE), opponentName, opponentRegId);
        // // Editor ed = getSharedPreferences(Constants.SHARED_PREF_CONST,
        // // Context.MODE_PRIVATE).edit();
        // // ed.putString(Constants.PREF_OPPONENT_REG_ID, opponentRegId);
        // // ed.putString(Constants.PREF_OPPONENT_NAME, opponentName);
        // // ed.commit();
        // // Log.d(TAG,
        // "Message sent to displayMsg() => Connected to opponent:"
        // // + opponentName + " (" + opponentRegId + ")");
        //
        // // Go to MsgFromOpponent activity dialog
//         i = new Intent(this, MsgFromOpponent.class);
//         i.putExtra(Constants.EXTRA_MSG, "Connected to '" + opponentName +
//         "'.");
        // startActivity(i);
         this.roundNo = 0;
         this.isPlayerOne = false;
         TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg_from_opponent_textview);
         msgTextView.setText("Game started with opponent '" + opponentName + "'.\n"
            + "Your opponent goes first!");

      } else if (msgType.equals(Constants.MSG_TYPE_2P_MOVE)) {
        int roundNo = Integer.parseInt(dataMap.get(Constants.KEY_ROUND));
        int oppScore = Integer.parseInt(dataMap.get(Constants.KEY_OPP_SCORE));
        String oppName = dataMap.get(Constants.KEY_OPP_NAME);
        boolean isPlayerOne = Boolean.parseBoolean(dataMap.get(Constants.KEY_IS_PLAYER_ONE));
        
        TextView msgTextView = (TextView) findViewById(R.id.two_player_wordgame_msg_from_opponent_textview);
        msgTextView.setText("'" + oppName + "' made " + oppScore
            + " points in round " + roundNo + ".\nYour turn!");
        
        Toast.makeText(getApplicationContext(), "fetched roundNo = " + roundNo 
            + "\n isPlayerOne = " + isPlayerOne, 2000).show();
        
        Log.d(TAG, "\nroundNo = " + roundNo 
            + "\n isPlayerOne = " + isPlayerOne);
    
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
}
