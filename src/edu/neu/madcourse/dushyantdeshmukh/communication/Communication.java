package edu.neu.madcourse.dushyantdeshmukh.communication;

import edu.neu.madcourse.dushyantdeshmukh.About;
import edu.neu.madcourse.dushyantdeshmukh.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Communication extends Activity implements OnClickListener {

  Intent i;
  
  public Communication() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.communication_main);

    // Set up click listeners for all the buttons

    View storingDataButton = findViewById(R.id.communication_storing_data_button);
    storingDataButton.setOnClickListener(this);

    View interphoneCommButton = findViewById(R.id.communication_interphone_comm_button);
    interphoneCommButton.setOnClickListener(this);
    
    View ackButton = findViewById(R.id.communication_ack_button);
    ackButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.communication_quit_button);
    quitButton.setOnClickListener(this);
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.communication_storing_data_button:
      i = new Intent(this, TestStoringData.class);
      startActivity(i);
      break;
    case R.id.communication_interphone_comm_button:
      i = new Intent(this, TestInterphoneComm.class);
      startActivity(i);
      break;
    case R.id.communication_ack_button:
      i = new Intent(this, Acknowledgements.class);
      startActivity(i);
      break;
    case R.id.communication_quit_button:
      finish();
      break;
    }
  }

}
