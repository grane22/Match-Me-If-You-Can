package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class Acknowledgements extends Activity implements OnClickListener {

  SoundHelper soundHelper;
  SharedPreferences projPreferences;
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_proj_acknowledgements);

    projPreferences = getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
    soundHelper = new SoundHelper(projPreferences);
    
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
	
	@Override
	protected void onResume() {
	  super.onResume();
	  soundHelper.playBgMusic(this);
	}
	
	@Override
	protected void onPause() {
	  super.onPause();
	  soundHelper.stopMusic();
	}
}
