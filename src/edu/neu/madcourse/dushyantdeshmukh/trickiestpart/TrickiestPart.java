package edu.neu.madcourse.dushyantdeshmukh.trickiestpart;

import edu.neu.madcourse.dushyantdeshmukh.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class TrickiestPart extends Activity implements OnClickListener {

  protected static final String TAG = "TRICKIEST PART MAIN ACTIVITY";
  private Intent i;
  Context context;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "Inside onCreate()");
    
    super.onCreate(savedInstanceState);
    setContentView(R.layout.trickiest_part_main);

    context = getApplicationContext();

    // Set up click listeners for all the buttons
    View testButton = findViewById(R.id.trickiest_part_test_button);
    testButton.setOnClickListener(this);
    
    View ackButton = findViewById(R.id.trickiest_part_ack_button);
    ackButton.setOnClickListener(this);
    
    View instructionsButton = findViewById(R.id.trickiest_part_instructions_button);
    instructionsButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.trickiest_part_quit_button);
    quitButton.setOnClickListener(this);

  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  public void onClick(View v) {
    Log.d(TAG, "Inside onClick()");
    switch (v.getId()) {
    case R.id.trickiest_part_test_button:
      Log.d(TAG, "Inside Test click");
      i = new Intent(this, Test.class);
      startActivity(i);
      break;
    case R.id.trickiest_part_ack_button:
      i = new Intent(this, Acknowledgements.class);
      startActivity(i);
      break;
    case R.id.trickiest_part_instructions_button:
      i = new Intent(this, Instructions.class);
      startActivity(i);
      break;
    case R.id.trickiest_part_quit_button:
      finish();
      break;
    }
  }
  
}
