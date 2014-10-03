package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import edu.neu.mhealth.api.KeyValueAPI;

public class TopScorers extends Activity implements OnClickListener {

	private static final String TAG = "TOP SCORERS Activity";

  public TopScorers() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.two_player_wordgame_top_scorers);

//		Intent i = getIntent();
//		String topScorersList = i.getStringExtra(Constants.EXTRA_TOP_SCORERS_LIST);
//		TextView topScorersTextView = (TextView) findViewById(R.id.two_player_wordgame_top_scorers_list);
//		topScorersTextView.setText(topScorersList);
		fetchAndDisplayTopScorersList();
		
		// Set up click listeners for all the buttons
		View okButton = findViewById(R.id.ok_button);
		okButton.setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok_button:
			finish();
			break;
		}
	}
	
	public String fetchAndDisplayTopScorersList() {
    Log.d(TAG, "\n\n\n Fetching Top scorers from server.\n\n\n");
    String temp = "";
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        Log.d(TAG, "inside addValuesToKeyOnServer():doInBackground()");
        String totalScore = params[0];
        String username = params[1];
        String currentDateTime = params[2];
        String retVal = "";
        String result = "";
        if (KeyValueAPI.isServerAvailable()) {
          String topScorersVal = KeyValueAPI.get(Constants.TEAM_NAME,
              Constants.PASSWORD, Constants.TOP_SCORERS_LIST);

          if (topScorersVal.contains("Error: No Such Key")) {
            Log.d(TAG, "no such key: " + Constants.TOP_SCORERS_LIST);
            retVal = "No scores available.";
          } else {
            Log.d(TAG, "key exists: " + Constants.TOP_SCORERS_LIST);
            Log.d(TAG, "\n topScorersVal: " + topScorersVal);
            
            retVal = Util.getFormatedTopScorersStr(topScorersVal);
            
            Log.d(TAG, "\n Sorted formatedTopScorersVal: " + retVal);
          }
        }
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        // mDisplay.append(msg + "\n");
        Log.d(TAG, "updateTopScorersList" + result);

        TextView topScorersTextView = (TextView) findViewById(R.id.two_player_wordgame_top_scorers_list);
        topScorersTextView.setText(result);
      }
    }.execute(null, null, null);
    return null;
  }
	
	
}
