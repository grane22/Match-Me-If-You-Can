package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import edu.neu.madcourse.dushyantdeshmukh.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class IntroScreen extends Activity implements OnClickListener {
  Context context;
  Button goToAcknowledgements, goToGame, backBtn;
  SoundHelper soundHelper;
  SharedPreferences projPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.final_proj_intro_screen);

    context = this;
    projPreferences = getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
    soundHelper = new SoundHelper(projPreferences);

    // Set up click listeners for all the buttons
    goToAcknowledgements = (Button) findViewById(R.id.final_proj_go_to_ack);
    goToAcknowledgements.setOnClickListener(this);

    goToGame = (Button) findViewById(R.id.final_proj_go_to_game);
    goToGame.setOnClickListener(this);

    backBtn = (Button) findViewById(R.id.final_proj_back);
    backBtn.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.final_proj_go_to_ack:
      Intent ackIntent = new Intent(this, Acknowledgements.class);
      ackIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(ackIntent);
      break;
    case R.id.final_proj_go_to_game:
      Intent gameIntent = new Intent(this, Home.class);
      gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(gameIntent);
      break;
    case R.id.final_proj_back:
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
