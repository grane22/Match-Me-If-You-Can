package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.R.integer;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.Constants;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class MatchImage extends BaseCameraActivity {

  protected static final String TAG = "MATCH ACTIVITY";
  private LayoutInflater controlInflater = null;
  private View matchButton, skipButton, endButton;
  private TextView imgCountView, timeElapsedView;
  private ImageView imgToMatchView;
  private Bitmap imgsToMatchArr[];
  private boolean isImgMatchedArr[];
  private AlertDialog waitingAlertDialog;
  private Dialog quitAlertDialog;
  private BroadcastReceiver receiver;
  private int startTime = 0;
  private int imagesMatched = 0, currImgIndex = 0;
  boolean isSinglePhoneMode,isWaitingAlertDialogShown;
  private boolean isSinglePhoneDialogShown = false;
  private AlertDialog singlePhoneDialog;
  int currState;
  String oppRegId;
  SoundHelper soundHelper;

  Timer myTimer = new Timer();
  final Handler myTimerHandler = new Handler();

  TimerTask timeElapsedTimerTask;

  final Runnable timeElapsedRunnable = new Runnable() {
    public void run() {
      if(!isSinglePhoneDialogShown){
        // increment & update time elapsed text view
    	  timeElapsedView.setText(Util.getTimeStr(Util.getTimeElapsed(startTime)));
      }else{
    	  int timeToShow = projPreferences.getInt(ProjectConstants.PLAYER_1_TIME, 0);
    	  timeElapsedView.setText(Util.getTimeStr(timeToShow));
      }
    }
  };

  private void startTimeElapsedTimer() {
    timeElapsedTimerTask = new TimerTask() {
      @Override
      public void run() {
        myTimerHandler.post(timeElapsedRunnable);
      }
    };
    myTimer.schedule(timeElapsedTimerTask, 0, 1000);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initializeOpenCV();
    soundHelper = new SoundHelper(projPreferences);
    
    // set final_proj_image_to_match and final_proj_match layouts as
    // overlayed layouts on top of the camera preview layout
    controlInflater = LayoutInflater.from(getBaseContext());
    View imgToMatchViewControl = controlInflater.inflate(
        R.layout.final_proj_img_to_match, null);
    View matchViewControl = controlInflater.inflate(R.layout.final_proj_match,
        null);

    LayoutParams layoutParamsControl = new LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

    this.addContentView(imgToMatchViewControl, layoutParamsControl);
    this.addContentView(matchViewControl, layoutParamsControl);

    // Set up click listeners for all the buttons
    matchButton = findViewById(R.id.final_proj_match);
    matchButton.setOnClickListener(this);

    skipButton = findViewById(R.id.final_proj_skip);
    skipButton.setOnClickListener(this);

    endButton = findViewById(R.id.final_proj_end_matching);
    endButton.setOnClickListener(this);

    imgCountView = (TextView) findViewById(R.id.img_count);
    timeElapsedView = (TextView) findViewById(R.id.time_elapsed);
    imgToMatchView = (ImageView) findViewById(R.id.image_to_match);

    // This will handle the broadcast
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Inside onReceive of Broadcast receiver of MatchImage class");
        String action = intent.getAction();
        if (action.equals(ProjectConstants.INTENT_ACTION_GAME_MOVE_AND_FINISH)) {
          String data = intent.getStringExtra("data");
          Log.d(TAG, "data = " + data);
          handleOpponentResponse(data);
        }
      }
    };

    initializeClassVars();

    // start timer
    startTimeElapsedTimer();
  }

  private void initializeClassVars() {
    Log.d(TAG, "initializeClassVars(), startTime = " + startTime);
    startTime = projPreferences.getInt(ProjectConstants.START_TIME, 0);
    isSinglePhoneMode = projPreferences.getBoolean(ProjectConstants.IS_SINGLE_PHONE_MODE, false);
    currState = projPreferences.getInt(ProjectConstants.SINGLE_PHONE_CURR_STATE, 1);
    oppRegId = projPreferences.getString(ProjectConstants.PREF_OPPONENT_REG_ID, null);

    imgsToMatchArr = Util.getImgsToMatch(totalNoOfImgs, currState, context);
    Log.d(TAG, "imgArr.length = " + imgsToMatchArr.length
        + "\n\n totalNoOfImgs = " + totalNoOfImgs);
    isImgMatchedArr = new boolean[totalNoOfImgs];

    // render first image to match
    renderImgToMatch(0);
  }

  private void renderImgToMatch(int imgIndex) {
    imgToMatchView.setImageBitmap(imgsToMatchArr[imgIndex]);
    imgToMatchView.setAlpha(ProjectConstants.IMG_ALPHA);
  }

  @Override
  protected void onResume() {
    super.onResume();
    soundHelper.playMatchMusic(context);
    
    // This needs to be in the activity that will end up receiving the
    // broadcast
    registerReceiver(receiver, new IntentFilter(
        ProjectConstants.INTENT_ACTION_GAME_MOVE_AND_FINISH));
    handleNotification(projPreferences);
    projPreferences.edit().putBoolean(ProjectConstants.IS_ACTIVITY_PAUSED, false).commit();
    Log.d(TAG, "Inside onResume(), startTime = " + startTime);
    
    if(isWaitingAlertDialogShown){
    	showWaitingAlertDialog();
    	isWaitingAlertDialogShown = true;
    }
    
    if(isSinglePhoneDialogShown){
      singlePhoneDialog = Util.showSinglePhoneDialog(this,
          currState,projPreferences);
           singlePhoneDialog.show();
           isSinglePhoneDialogShown = true;
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    soundHelper.stopMusic();
    
    unregisterReceiver(receiver);
    Log.d(TAG, "Inside onPause(), startTime = " + startTime);
    projPreferences.edit().putBoolean(ProjectConstants.IS_ACTIVITY_PAUSED, true).commit();
    
    if(waitingAlertDialog != null){
    	waitingAlertDialog.dismiss();
    }
    
    if(Util.isQuitDialogShown()){
    	Util.dismissQuitDialog();
    }
	
    if(singlePhoneDialog != null){
    	singlePhoneDialog.dismiss();
    }
  }

  private void restoreState() {
    // restore images matched so far
    imagesMatched = projPreferences.getInt(
        ProjectConstants.NUMBER_OF_IMAGES_MATCHED, 0);
    imgCountView.setText("Img Count: " + imagesMatched + "/" + totalNoOfImgs);
    isWaitingAlertDialogShown = projPreferences.getBoolean(ProjectConstants.IS_WAITING_ALERT_DIALOG_SHOWN, false);
	isSinglePhoneDialogShown = projPreferences.getBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, false);
    Log.d(TAG, "Reading Img Count: " + imagesMatched);
  }

  private void storeState() {
    // store no of images captured so far
    Editor e = projPreferences.edit();
    e.putInt(ProjectConstants.NUMBER_OF_IMAGES_MATCHED, imagesMatched);
    e.putBoolean(ProjectConstants.IS_WAITING_ALERT_DIALOG_SHOWN, isWaitingAlertDialogShown);
	e.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, isSinglePhoneDialogShown);
    e.commit();
    Log.d(TAG, "Setting imagesMatched: " + imagesMatched);
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
    Log.d(TAG, "Inside onClick()");
    switch (v.getId()) {
    case R.id.final_proj_match:
      // Log.d(TAG, "Clicked on Capture button... taking picture...");
      soundHelper.playCaptureSound(context);
      takePicture();
      break;
    case R.id.final_proj_skip:
      skipToNextimg();
      break;
    case R.id.final_proj_end_matching:
    	if(isSinglePhoneMode){
    		skipToNextEvent();
    	}else{
    		myTimer.cancel();
    		int timeElapsed = Util.getTimeElapsed(startTime);
    		Editor editor = projPreferences.edit();
    	    editor.putBoolean(ProjectConstants.IS_MY_GAME_OVER, true);
    	    editor.commit();
    		handleDualPhoneEndOfMatching(timeElapsed);
    	}
      break;
    }
  }

  @Override
  public void onBackPressed() {
  }
  
  private void skipToNextEvent() {
	  myTimer.cancel();
	  int timeElapsed = Util.getTimeElapsed(startTime);
	  Editor editor = projPreferences.edit();
	    if (currState == ProjectConstants.SINGLE_PHONE_P1_MATCH_STATE) {
	      // Save P1's time and no of imgs
	      editor.putInt(ProjectConstants.PLAYER_1_TIME, timeElapsed);
	      editor.putInt(ProjectConstants.PLAYER_1_IMAGE_COUNT, imagesMatched);
	      singlePhoneDialog = Util.showSinglePhoneDialog(this,
	    		  Util.nextState(projPreferences),projPreferences);
	         singlePhoneDialog.show();
	         isSinglePhoneDialogShown = true;
	    } else if(currState == ProjectConstants.SINGLE_PHONE_P2_MATCH_STATE){
	      // Save P2's time and no of imgs
	      editor.putInt(ProjectConstants.PLAYER_2_TIME, timeElapsed);
	      editor.putInt(ProjectConstants.PLAYER_2_IMAGE_COUNT, imagesMatched);
	      startGameFinishActivity();
	    }
	    editor.commit();
  }

private void skipToNextimg() {
    if (imagesMatched == (totalNoOfImgs - 1)) {
      Util.showToast(context, ProjectConstants.SKIP_FAIL_MSG, 1500);
    } else {
      // Show next image to match
      currImgIndex = getNextImgIndex(currImgIndex);
      renderImgToMatch(currImgIndex);
    }
  }

  protected CharSequence getTakePictureWaitMsg() {
    return ProjectConstants.MATCH_WAIT_MSG;
  }

  /**
   * Given the index of currently matched image, returns the index of next image
   * to be matched
   * 
   * @param currMatchedImgIndex
   * @return
   */
  protected int getNextImgIndex(int currMatchedImgIndex) {
    int nextImgIndex = (currMatchedImgIndex + 1) % totalNoOfImgs;
    while (isImgMatchedArr[nextImgIndex]) {
      nextImgIndex = (nextImgIndex + 1) % totalNoOfImgs;
    }
    Log.d(TAG, "inside getNextImgIndex() \n" + "currMatchedImgIndex = "
        + currMatchedImgIndex + ", nextImgIndex = " + nextImgIndex);
    return nextImgIndex;
  }

  @Override
  protected void processCapturedPicture(byte[] data) {
    if (currBmpImg != null) {
      currBmpImg.recycle();
    }
    currBmpImg = Util.convertByteArrToBitmap(data);

    // match imgsToMatchArr[currImgNo - 1] with bmpImg
    Mat imgMat1 = Util.convertBmpToMat(currBmpImg);
    Mat imgMat2 = Util.convertBmpToMat(imgsToMatchArr[currImgIndex]);

    if (Util.imagesMatch(imgMat1, imgMat2, matchingDifficultyLevel)) {
      // if match successful, increment img count and set
      // isImgMatchedArr[currImgNo]
      soundHelper.playMatchSuccessSound(context);
      progress.cancel();
      Util.showToast(context, ProjectConstants.MATCH_SUCCESS_MSG, 1500);

      imagesMatched++;
      isImgMatchedArr[currImgIndex] = true;
      imgCountView.setText("Img Count: " + (imagesMatched) + "/"
          + totalNoOfImgs);

      if (!isSinglePhoneMode) {
        // Send game move message to opponent.
        sendGameMoveOrFinishToOpponent(true, imagesMatched, 0);
      }

      if (imagesMatched == totalNoOfImgs) {
        // stop timer and save time.
        myTimer.cancel();
        // finished matching images
        Util.showToast(context, "Finished matching " + totalNoOfImgs
            + " images", 1500);

        int timeElapsed = Util.getTimeElapsed(startTime);

        if (isSinglePhoneMode) {
          handleSinglePhoneEndOfMatching(timeElapsed);
        } else {
          handleDualPhoneEndOfMatching(timeElapsed);
        }

      } else {
        // Show next image to match
        currImgIndex = getNextImgIndex(currImgIndex);
        renderImgToMatch(currImgIndex);
      }
    } else {
      soundHelper.playMatchFailSound(context);
      progress.cancel();
      Util.showToast(context, ProjectConstants.MATCH_FAIL_MSG, 1500);
    }
  }

  private void handleSinglePhoneEndOfMatching(int timeElapsed) {
    Log.d(TAG, "Inside handleSinglePhoneEndOfMatching() method, currState = "
        + currState);
    Editor editor = projPreferences.edit();
    if (currState == ProjectConstants.SINGLE_PHONE_P1_MATCH_STATE) {
      // Save P1's time and no of imgs
      editor.putInt(ProjectConstants.PLAYER_1_TIME, timeElapsed);
      editor.putInt(ProjectConstants.PLAYER_1_IMAGE_COUNT, imagesMatched);
      singlePhoneDialog = Util.showSinglePhoneDialog(this,
    		  Util.nextState(projPreferences),projPreferences);
         singlePhoneDialog.show();
         isSinglePhoneDialogShown = true;
    } else {
      // Save P2's time and no of imgs
      editor.putInt(ProjectConstants.PLAYER_2_TIME, timeElapsed);
      editor.putInt(ProjectConstants.PLAYER_2_IMAGE_COUNT, imagesMatched);
      startGameFinishActivity();
    }
    editor.commit();
  }

  private void handleDualPhoneEndOfMatching(int timeElapsed) {
    Editor editor = projPreferences.edit();
    // TODO: change time
    editor.putInt(ProjectConstants.PLAYER_TIME, timeElapsed);
    editor.putInt(ProjectConstants.PLAYER_IMAGE_COUNT, imagesMatched);
    editor.putBoolean(ProjectConstants.IS_MY_GAME_OVER, true);
    editor.commit();

    Log.d(TAG, "PLAYER TIME: " + timeElapsed + "PLAYER_IMAGE_COUNT: "
        + imagesMatched);

    sendGameMoveOrFinishToOpponent(false, imagesMatched, timeElapsed);

    // TODO: Check flag and call the activity
    boolean isOppGameOver = projPreferences.getBoolean(
        ProjectConstants.IS_OPPONENT_GAME_OVER, false);
    if (isOppGameOver) {
      startGameFinishActivity();
    } else {
      showWaitingAlertDialog();
      isWaitingAlertDialogShown = true;
    }
  }

	protected void showWaitingAlertDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MatchImage.this);
	      // set title
	      alertDialogBuilder.setTitle(ProjectConstants.WAITING_TITLE);
	
	      // set dialog message
	      alertDialogBuilder
	          .setMessage(ProjectConstants.WAITING_MESSAGE)
	          .setCancelable(false)
	          .setPositiveButton(ProjectConstants.QUIT_BUTTON,
	              new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                  dialog.cancel();
	                  isWaitingAlertDialogShown = false;
	                  quitAlertDialog = Util.showCustomQuitDialog(MatchImage.this,false,true);
	                  quitAlertDialog.show();	              
	                }
	              });
	
	      waitingAlertDialog = alertDialogBuilder.create();
	      waitingAlertDialog.show();
	}


  private void handleNotification(SharedPreferences sp) {
    String data = sp.getString(ProjectConstants.KEY_NOTIFICATION_DATA, "");
    if (!data.equals("")) {
      handleOpponentResponse(data);
      sp.edit().putString(ProjectConstants.KEY_NOTIFICATION_DATA, "").commit();
    }
  }

  protected void handleOpponentResponse(String data) {
    Log.d(TAG, "Inside handleOpponentResponse()");
    HashMap<String, String> dataMap = Util.getDataMap(data, TAG);
    if (dataMap.containsKey(Constants.KEY_MSG_TYPE)) {
      String msgType = dataMap.get(Constants.KEY_MSG_TYPE);
      Log.d(TAG, Constants.KEY_MSG_TYPE + ": " + msgType);
      if (msgType.equals(ProjectConstants.MSG_TYPE_FP_GAME_OVER)) {
        Log.d(TAG, "Inside MSG_TYPE_FP_GAME_OVER = "
            + ProjectConstants.MSG_TYPE_FP_GAME_OVER);
        String opponent_num_of_images = dataMap
            .get(ProjectConstants.NUMBER_OF_IMAGES);
        String opponent_matchingTime = dataMap
            .get(ProjectConstants.TOTAL_MATCHING_TIME);

        Editor editor = projPreferences.edit();
        editor.putBoolean(ProjectConstants.IS_OPPONENT_GAME_OVER, true);
        editor.putInt(ProjectConstants.OPPONENT_TIME, Integer.parseInt(opponent_matchingTime));
        editor.putInt(ProjectConstants.OPPONENT_IMAGE_COUNT,
            Integer.parseInt(opponent_num_of_images));
        editor.commit();

        boolean isMyGameOver = projPreferences.getBoolean(ProjectConstants.IS_MY_GAME_OVER, false);
        if (imagesMatched == totalNoOfImgs || isMyGameOver) {
          startGameFinishActivity();
        }
        
      } else if (msgType.equals(ProjectConstants.MSG_TYPE_FP_MOVE)) {
        Log.d(TAG, "Inside MSG_TYPE_FP_MOVE = "
            + ProjectConstants.MSG_TYPE_FP_MOVE);
        // Show toast that opponent found out new Image
        String oppName = projPreferences.getString(ProjectConstants.PREF_OPPONENT_NAME, ProjectConstants.OPPONENT);
        Log.d(TAG, "Handle Opp Response: Opp name: " + projPreferences.getString(ProjectConstants.PREF_OPPONENT_NAME, ProjectConstants.OPPONENT));
        String imageNumber = dataMap.get(ProjectConstants.NUMBER_OF_IMAGES);
        Util.showToast(this,  oppName + " matched image number " + imageNumber + "!", 1500);
      }
    }
  }

  public void startGameFinishActivity() {
    Intent gameFinishIntent = new Intent(context, GameFinish.class);
    gameFinishIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    startActivity(gameFinishIntent);
  }

  private void sendGameMoveOrFinishToOpponent(boolean isGameMoveEvent,
      int imagesMatched, int timeForMatching) {
    Log.d(TAG, "Sending request ack: "
        + (isGameMoveEvent ? ProjectConstants.MSG_TYPE_FP_MOVE
            : ProjectConstants.MSG_TYPE_FP_GAME_OVER));
    new AsyncTask<String, Integer, String>() {
      @Override
      protected String doInBackground(String... params) {
        String retVal;
        boolean isGameMove = Boolean.parseBoolean(params[0]);
        int imagesMatched = Integer.parseInt(params[1]);
        int matchingTime = Integer.parseInt(params[2]);
        String oppRegId = params[3];
        try {
          retVal = Util.sendPost("data."
              + Constants.KEY_MSG_TYPE
              + "="
              + (isGameMove ? ProjectConstants.MSG_TYPE_FP_MOVE
                  : ProjectConstants.MSG_TYPE_FP_GAME_OVER)
              + "&data."
              + ProjectConstants.NUMBER_OF_IMAGES
              + "="
              + imagesMatched
              + (isGameMove ? "" : "&data."
                  + ProjectConstants.TOTAL_MATCHING_TIME + "=" + matchingTime),
              oppRegId);
          Log.d(TAG, "Result of HTTP POST: " + retVal);
          // displayMsg("Connected to user:" + oppName + " (" +
          // oppRegId + ")");
          retVal = "Sent game message to opponent:" + " (" + oppRegId + ")";
          // sendPost("data=" + myRegId);
        } catch (Exception e) {
          retVal = "Error occured while making an HTTP post call.";
          e.printStackTrace();
        }
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        // Toast t = Toast.makeText(getApplicationContext(), result,
        // 2000);
        // t.show();
        Log.d(TAG, "\n===================================================\n");
        Log.d(TAG, "result: " + result);
      }
    }.execute(String.valueOf(isGameMoveEvent), String.valueOf(imagesMatched),
        String.valueOf(timeForMatching), oppRegId, null);
  }

}
