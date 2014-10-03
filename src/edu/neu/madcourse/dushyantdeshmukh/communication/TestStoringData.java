package edu.neu.madcourse.dushyantdeshmukh.communication;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.mhealth.api.KeyValueAPI;

public class TestStoringData extends Activity implements OnClickListener {

  private static final String TAG = "COMMUNICATION - STORING DATA";
  private static final String PASSWORD = "numad14s";
  private static final String TEAM_NAME = "Dushyant";
  private static final int SAVE = 1;
  private static final int FETCH = 2;
  private static final int CLEAR = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.communication_storing_data);

    // Set up click listeners for all the buttons

    View saveButton = findViewById(R.id.communication_storing_data_save_button);
    saveButton.setOnClickListener(this);

    View fetchButton = findViewById(R.id.communication_storing_data_fetch_button);
    fetchButton.setOnClickListener(this);

    View clearButton = findViewById(R.id.communication_storing_data_clear_button);
    clearButton.setOnClickListener(this);

    View quitButton = findViewById(R.id.communication_storing_data_quit_button);
    quitButton.setOnClickListener(this);

  }

  @Override
  public void onClick(View v) {
    String key, value;
    EditText keyEditText = (EditText) findViewById(R.id.communication_storing_data_key_editText);
    switch (v.getId()) {
    case R.id.communication_storing_data_save_button:
      key = keyEditText.getText().toString();
      EditText valEditText = (EditText) findViewById(R.id.communication_storing_data_value_editText);
      value = valEditText.getText().toString();
      if (key.equals("") || value.equals("")) {
        displayMsg("Please enter both key and value.");
      } else {
        new TestAsynTask().execute(SAVE + "", key, value);
        displayMsg("Saving data...");
        keyEditText.setText("");
        valEditText.setText("");
      }
      break;
    case R.id.communication_storing_data_fetch_button:
      key = keyEditText.getText().toString();
      if (key.equals("")) {
        displayMsg("Please enter key.");
      } else {
        new TestAsynTask().execute(FETCH + "", key);
        displayMsg("Fetching data...");
        keyEditText.setText("");
      }
      break;
    case R.id.communication_storing_data_clear_button:
      new TestAsynTask().execute(CLEAR + "");
      displayMsg("Clearing all data...");
      keyEditText.setText("");
      break;
    case R.id.communication_storing_data_quit_button:
      finish();
      break;
    }
  }

  private class TestAsynTask extends AsyncTask<String, Integer, String> {

    @Override
    protected String doInBackground(String... params) {
      String result = "";
      if (KeyValueAPI.isServerAvailable()) {
        int operation = Integer.parseInt(params[0]);
        String key, value;
        switch (operation) {
        case SAVE:
          key = params[1];
          value = params[2];
          result = KeyValueAPI.put(TEAM_NAME, PASSWORD, key, value);
          if (!result.contains("Error")) {
            result = "Saved (" + key + ", " + value + ")";
          }
          break;
        case FETCH:
          key = params[1];
          result = KeyValueAPI.get(TEAM_NAME, PASSWORD, key);
          if (!result.contains("Error")) {
            result = "Fetched value of key " + key + ": " + result;
          }
          break;
        case CLEAR:
          result = KeyValueAPI.clear(TEAM_NAME, PASSWORD);
          if (!result.contains("Error")) {
            result = "Cleared all key, value pairs";
          }
          break;
        }
      } else {
        result = "Server is currently unavailable. Please try again after some time.";
      }
      return result;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      // displayProgressBar("Downloading...");
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      // dismissProgressBar();
      displayMsg(result);
    }

  }

  private void displayMsg(String msg) {
    TextView resultTextView = (TextView) findViewById(R.id.communication_storing_data_result_textview);
    resultTextView.setText(msg);
  }

}
