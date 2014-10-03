package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class CaptureImage extends BaseCameraActivity {

	protected static final String TAG = "CAPTURE ACTIVITY";
	LayoutInflater controlInflater = null;
	View captureButton, acceptButton, rejectButton, endGameButton;
	TextView imgCountView;
	ImageView capturedImgView;
	byte[] currImgData;
	Bitmap imgArr[];
	int currImgNo = 0;
	boolean isSinglePhoneMode, isSwapAlertDialogShown;
	private boolean isSinglePhoneDialogShown = false;
	private AlertDialog singlePhoneDialog;
	private Dialog endGameDialog;
	int currState;
	private AlertDialog swapPhonesAlertDialog;
	SoundHelper soundHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		soundHelper = new SoundHelper(projPreferences);
		// set final_proj_capture layout as an overlayed layout
		// on top of the camera preview layout
		controlInflater = LayoutInflater.from(getBaseContext());
		View viewControl = controlInflater.inflate(R.layout.final_proj_capture,
				null);
		LayoutParams layoutParamsControl = new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		this.addContentView(viewControl, layoutParamsControl);

		// Set up click listeners for all the buttons
		captureButton = findViewById(R.id.final_proj_capture);
		captureButton.setOnClickListener(this);

		acceptButton = findViewById(R.id.final_proj_accept);
		acceptButton.setOnClickListener(this);

		rejectButton = findViewById(R.id.final_proj_reject);
		rejectButton.setOnClickListener(this);
		
		endGameButton = findViewById(R.id.final_proj_capture_game_end);
		endGameButton.setOnClickListener(this);
		
		imgCountView = (TextView) findViewById(R.id.img_count);
		capturedImgView = (ImageView) findViewById(R.id.captured_image);

		// Show capture btn and hide accept/reject btns
		// Show camera preview and hide the captured img imageView
		showCapturedImg(false);

		initializeClassVars();
	}

	private void initializeClassVars() {
		Log.d(TAG, "Inside initializeClassVars()");
		imgArr = new Bitmap[totalNoOfImgs];
		isSinglePhoneMode = projPreferences.getBoolean(
				ProjectConstants.IS_SINGLE_PHONE_MODE, false);
		currState = projPreferences.getInt(
				ProjectConstants.SINGLE_PHONE_CURR_STATE, 1);
		Log.d(TAG, "isSinglePhoneMode = " + isSinglePhoneMode);
		Log.d(TAG, "currState = " + currState);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Inside onResume() ");
		soundHelper.playBgMusic(context);
		
		if (isSwapAlertDialogShown){
			swapPhonesAlertDialog = Util.showSwapPhonesAlertDialog(this, this,
					false,projPreferences);
			swapPhonesAlertDialog.show();
			isSwapAlertDialogShown = true;
		}
		
		if(isSinglePhoneDialogShown){
	    	singlePhoneDialog = Util.showSinglePhoneDialog(this,
	    			currState,projPreferences);
	           singlePhoneDialog.show();
	           isSinglePhoneDialogShown = true;
	    }
	}

	private void restoreState() {
		// restore images captured so far
		currImgNo = projPreferences
				.getInt(ProjectConstants.NUMBER_OF_IMAGES, 0);
		imgCountView.setText("Img Count: " + currImgNo + "/" + totalNoOfImgs);
		isSwapAlertDialogShown = projPreferences.getBoolean(ProjectConstants.IS_SWAP_ALERT_DIALOG_SHOWN, false);
		isSinglePhoneDialogShown = projPreferences.getBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, false);
		Log.d(TAG, "restoreState(): isSwapAlertDialogShown: " + isSwapAlertDialogShown);
		Log.d(TAG, "Reading Img Count: " + currImgNo);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Inside onPause() ");
		soundHelper.stopMusic();
		
		if (swapPhonesAlertDialog != null) {
			swapPhonesAlertDialog.dismiss();
		}
		if(singlePhoneDialog != null){
	    	singlePhoneDialog.dismiss();
	    }
	    
	    if(Util.isQuitDialogShown()){
	    	Util.dismissQuitDialog();
	    }
	}

	private void storeState() {
		// store no of images captured so far
		Editor e = projPreferences.edit();
		e.putInt(ProjectConstants.NUMBER_OF_IMAGES, currImgNo);
		e.putBoolean(ProjectConstants.IS_SWAP_ALERT_DIALOG_SHOWN, isSwapAlertDialogShown);
		e.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, isSinglePhoneDialogShown);
		e.commit();
		Log.d(TAG, "storeState(): isSwapAlertDialogShown: " + isSwapAlertDialogShown);
		Log.d(TAG, "Setting currImgNo: " + currImgNo);
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
		case R.id.final_proj_capture:
			Log.d(TAG, "Clicked on Capture button... taking picture...");
			soundHelper.playCaptureSound(context);
			takePicture();
			break;
		case R.id.final_proj_accept:
			storeCurrImg();
			showCapturedImg(false);
			break;
		case R.id.final_proj_reject:
			showCapturedImg(false);
			break;
		case R.id.final_proj_capture_game_end:
			endGameDialog = Util.showCustomQuitDialog(CaptureImage.this,false,false);
			endGameDialog.show();
			break;
		}
	}

	@Override
  public void onBackPressed() {
  }
	
	private void storeCurrImg() {
		currImgNo++;
		imgCountView.setText("Img Count: " + currImgNo + "/" + totalNoOfImgs);
		if (imgArr == null) {
			imgArr = new Bitmap[totalNoOfImgs];
		}
		imgArr[currImgNo - 1] = currBmpImg;
		Util.storeImg(currImgData, currImgNo, currState, context);

		if (currImgNo >= totalNoOfImgs) {
			// finished capturing images
			Util.showToast(context, "Finished capturing " + totalNoOfImgs
					+ " images", 1500);
			if (isSinglePhoneMode) {
				singlePhoneDialog = Util.showSinglePhoneDialog(this,
			            Util.nextState(projPreferences),projPreferences);
			       singlePhoneDialog.show();
			       isSinglePhoneDialogShown = true;
			} else {
				swapPhonesAlertDialog = Util.showSwapPhonesAlertDialog(context,
						this, false,projPreferences);
				swapPhonesAlertDialog.show();
				isSwapAlertDialogShown = true;
			}
		}
	}

	protected CharSequence getTakePictureWaitMsg() {
		return ProjectConstants.CAPTURE_WAIT_MSG;
	}

	private void showCaptureBtn(boolean show) {
		if (show) {
			captureButton.setVisibility(View.VISIBLE);
			acceptButton.setVisibility(View.GONE);
			rejectButton.setVisibility(View.GONE);
		} else {
			captureButton.setVisibility(View.GONE);
			acceptButton.setVisibility(View.VISIBLE);
			rejectButton.setVisibility(View.VISIBLE);
		}
	}

	private void showCapturedImg(boolean show) {
		if (show) {
			capturedImgView.setVisibility(View.VISIBLE);
			preview.setVisibility(View.GONE);
		} else {
			capturedImgView.setVisibility(View.GONE);
			preview.setVisibility(View.VISIBLE);
		}
		showCaptureBtn(!show);
	}

	public void startMatchActivity() {
		// set start time for matching activity
		projPreferences
				.edit()
				.putInt(ProjectConstants.START_TIME,
						(int) System.currentTimeMillis() / 1000).commit();

		Intent captureIntent = new Intent(context, MatchImage.class);
		captureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(captureIntent);
	}

	@Override
	protected void processCapturedPicture(byte[] data) {

		if (currBmpImg != null) {
			currBmpImg.recycle();
		}
		currBmpImg = Util.convertByteArrToBitmap(data);
		currImgData = data;

		// show captured image in image view
		capturedImgView.setImageBitmap(currBmpImg);
		showCapturedImg(true);
		progress.cancel();
	}

	/*
	 * new AsyncTask<String, Integer, String>() {
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute();
	 * progress.setMessage("Processing captured image..."); progress.show(); }
	 * 
	 * @Override protected String doInBackground(String... params) { String
	 * retVal = "";
	 * 
	 * Log.d(TAG, "retVal: " + retVal); return retVal; }
	 * 
	 * @Override protected void onPostExecute(String result) {
	 * super.onPostExecute(result); progress.cancel(); } }.execute(null, null,
	 * null);
	 */
}
