package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class WaitingForOpponent extends Activity implements OnClickListener {

  protected static final String TAG = "WAITING FOR OPPONENT ACTIVITY";
  private Intent i;
  Context context;
  BroadcastReceiver receiver;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.two_player_wordgame_waiting_for_opponent);
    
    context = getApplicationContext();

    // Set up click listeners for all the buttons
    View backButton = findViewById(R.id.two_player_wordgame_back_button);
    backButton.setOnClickListener(this);
    
// // This will handle the broadcast
//    receiver = new BroadcastReceiver() {
//      // @Override
//      public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "Inside onReceive of Broadcast receiver");
//        String action = intent.getAction();
//        if (action.equals("INTENT_ACTION")) {
//          String data = intent.getStringExtra("data");
//          Log.d(TAG, "data = " + data);
////          handleOpponentResponse(data);
//        }
//      }
//    };
  }
  
  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
// // This needs to be in the activity that will end up receiving the broadcast
//    registerReceiver(receiver, new IntentFilter("INTENT_ACTION"));

//    handleNotification(sp);
  }
  
  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
//    unregisterReceiver(receiver);
  }
  
  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.two_player_wordgame_back_button:
      finish();
    }
  }
  
  
}
