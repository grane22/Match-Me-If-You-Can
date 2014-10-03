package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.MyButton;
import edu.neu.madcourse.dushyantdeshmukh.utilities.MyTextView;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class Tutorial extends Activity implements OnClickListener {

  private static final String TAG = "TUTORIAL ACTIVITY";
  MyButton backButton, skipTutorialButton, practiceButton;
  ImageButton nextButton, prevButton;
  MyTextView tutorialTextView;
  MyTextView[] stepTextView = new MyTextView[4];
  SharedPreferences projPreferences;
  Context context;
  private boolean isSinglePhoneDialogShown = false;
  private AlertDialog singlePhoneDialog;
  boolean isSinglePhoneMode;
  int currStepNo = 1;
  SoundHelper soundHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "Inside onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.final_proj_tutorial);

    context = this;
    projPreferences = getSharedPreferences();
    soundHelper = new SoundHelper(projPreferences);
    
    isSinglePhoneMode = projPreferences.getBoolean(
        ProjectConstants.IS_SINGLE_PHONE_MODE, false);
    Log.d(TAG, "isSinglePhoneMode = " + isSinglePhoneMode);

    // Set up click listeners for all the buttons
    backButton = (MyButton) findViewById(R.id.final_proj_back);
    backButton.setOnClickListener(this);

    skipTutorialButton = (MyButton) findViewById(R.id.final_proj_skip_tutorial);
    skipTutorialButton.setOnClickListener(this);

    prevButton = (ImageButton) findViewById(R.id.final_proj_prev);
    prevButton.setOnClickListener(this);
    
    nextButton = (ImageButton) findViewById(R.id.final_proj_next);
    nextButton.setOnClickListener(this);

    practiceButton = (MyButton) findViewById(R.id.final_proj_practice);
    practiceButton.setOnClickListener(this);

    tutorialTextView = (MyTextView) findViewById(R.id.final_proj_tutorial);
    tutorialTextView.setOnClickListener(this);

    stepTextView[0] = (MyTextView) findViewById(R.id.final_proj_step1);
    stepTextView[1] = (MyTextView) findViewById(R.id.final_proj_step2);
    stepTextView[2] = (MyTextView) findViewById(R.id.final_proj_step3);
    stepTextView[3] = (MyTextView) findViewById(R.id.final_proj_step4);
    
    if (isSinglePhoneMode) {
      stepTextView[0].setText(getString(R.string.final_proj_single_phone_step1));
      stepTextView[1].setText(getString(R.string.final_proj_single_phone_step2));
      stepTextView[2].setText(getString(R.string.final_proj_single_phone_step3));
      stepTextView[3].setText(getString(R.string.final_proj_single_phone_step4));
    } else {
      stepTextView[0].setText(getString(R.string.final_proj_step1));
      stepTextView[1].setText(getString(R.string.final_proj_step2));
      stepTextView[2].setText(getString(R.string.final_proj_step3));
      stepTextView[3].setText(getString(R.string.final_proj_step4));
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    soundHelper.stopMusic();
    
    if(singlePhoneDialog != null){
    	singlePhoneDialog.dismiss();
    }
    
    if(Util.isQuitDialogShown()){
    	Util.dismissQuitDialog();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    soundHelper.playBgMusic(context);
    
    currStepNo--;
    showNextStep();
    if(isSinglePhoneDialogShown){
    	singlePhoneDialog = Util.showSinglePhoneDialog(this,
                ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,projPreferences);
           singlePhoneDialog.show();
           isSinglePhoneDialogShown = true;
    }
  }

  private void restoreState() {
    // restore currStepNo
    currStepNo = projPreferences.getInt(ProjectConstants.CURRENT_STEP_NO, 0);
    isSinglePhoneDialogShown = projPreferences.getBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, false);
    Log.d(TAG, "Reading currStepNo: " + currStepNo);
  }

  private void storeState() {
    // store currStepNo
    Editor e = projPreferences.edit();
    e.putInt(ProjectConstants.CURRENT_STEP_NO, currStepNo);
    e.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, isSinglePhoneDialogShown);
    e.commit();
    Log.d(TAG, "Setting currStepNo: " + currStepNo);
    
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    storeState();
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    restoreState();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    case R.id.final_proj_back:
      // Go to connection activity
      Intent homeIntent = new Intent(context, Home.class);
      homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(homeIntent);
      break;
    case R.id.final_proj_skip_tutorial:
      Log.d(TAG, "isSinglePhoneMode = " + isSinglePhoneMode);
      if (isSinglePhoneMode) {
       singlePhoneDialog = Util.showSinglePhoneDialog(this,
            ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,projPreferences);
       singlePhoneDialog.show();
       isSinglePhoneDialogShown = true;
      } else {
        // Go to connection activity
        Intent connectionIntent = new Intent(context, Connection.class);
        connectionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(connectionIntent);
      }
      break;
    case R.id.final_proj_prev:
      showPrevStep();
      break;
    case R.id.final_proj_next:
      showNextStep();
      break;
    case R.id.final_proj_practice:
      Intent practiceIntent = new Intent(this, Practice.class);
      practiceIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(practiceIntent);
      break;
    }
  }

  private void showPrevStep() {
    if (currStepNo == 2) {
      // hide prev button
      prevButton.setVisibility(View.GONE);
    }
    practiceButton.setVisibility(View.GONE);
    nextButton.setVisibility(View.VISIBLE);
    hideAllSteps();
    currStepNo--;
    stepTextView[currStepNo - 1].setVisibility(View.VISIBLE);
  }
  
  private void showNextStep() {
    Log.d(TAG, "Inside showNextStep(), currStepNo = " + currStepNo);
    if (currStepNo == 3) {
      // hide next button and show practice button
      nextButton.setVisibility(View.GONE);
      practiceButton.setVisibility(View.VISIBLE);
    }
    if (currStepNo > 0) {
      prevButton.setVisibility(View.VISIBLE);
    }
    hideAllSteps();
    currStepNo++;
    stepTextView[currStepNo - 1].setVisibility(View.VISIBLE);
  }

  private void hideAllSteps() {
    for (int i = 0; i < stepTextView.length; i++) {
      stepTextView[i].setVisibility(View.GONE);
    }
  }

  private SharedPreferences getSharedPreferences() {
    return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
  }

}
