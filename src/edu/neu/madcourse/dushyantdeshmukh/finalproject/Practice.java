package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class Practice extends BaseCameraActivity implements OnClickListener {

  protected static final String TAG = "PRACTICE ACTIVITY";

  private static final String IMG_1_NAME = ProjectConstants.P1_IMG_NAME_PREFIX + 1;
  private static final String IMG_2_NAME = ProjectConstants.P1_IMG_NAME_PREFIX + 2;

  LayoutInflater controlInflater = null;
  View captureButton, matchButton, clearButton, quitButton;
  ImageView capturedImgView, imgToMatchView;
  protected boolean isImg1Present = false;
  private boolean isSinglePhoneDialogShown = false;
  private AlertDialog singlePhoneDialog;
  boolean isSinglePhoneMode;
  SoundHelper soundHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "inside oncreate: initializing openCV");
    initializeOpenCV();
    soundHelper = new SoundHelper(projPreferences);
    
    isSinglePhoneMode = projPreferences.getBoolean(
        ProjectConstants.IS_SINGLE_PHONE_MODE, false);
    Log.d(TAG, "isSinglePhoneMode = " + isSinglePhoneMode);

    // set final_proj_image_to_match and final_proj_match layouts as
    // overlayed layouts on top of the camera preview layout
    controlInflater = LayoutInflater.from(getBaseContext());
    View imgToMatchViewControl = controlInflater.inflate(
        R.layout.final_proj_img_to_match, null);
    View practiceViewControl = controlInflater.inflate(
        R.layout.final_proj_practice, null);

    LayoutParams layoutParamsControl = new LayoutParams(
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

    this.addContentView(imgToMatchViewControl, layoutParamsControl);
    this.addContentView(practiceViewControl, layoutParamsControl);

    // Set up click listeners for all the buttons
    captureButton = findViewById(R.id.final_proj_capture);
    captureButton.setOnClickListener(this);

    matchButton = findViewById(R.id.final_proj_match);
    matchButton.setOnClickListener(this);

    clearButton = findViewById(R.id.final_proj_clear);
    clearButton.setOnClickListener(this);

    quitButton = findViewById(R.id.final_proj_quit);
    quitButton.setOnClickListener(this);

    // read and display Image1 if present
    Log.d(TAG, "isImg1Present = " + isImg1Present);
    if (isImg1Present) {
      displayImg(readImgBmp(1));
    }
  }

  	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		projPreferences
				.edit()
				.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN,
						isSinglePhoneDialogShown).commit();
	}
	
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		isSinglePhoneDialogShown = projPreferences.getBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, false);
	}
	
  @Override
  protected void onResume() {
    super.onResume();
    soundHelper.playBgMusic(context);
    
    showMatchBtn(isImg1Present);
    
    if(isSinglePhoneDialogShown){
    	singlePhoneDialog = Util.showSinglePhoneDialog(this,
                ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,projPreferences);
    	singlePhoneDialog.show();
    	isSinglePhoneDialogShown = true;
    }
  }

  private void showMatchBtn(boolean show) {
    if (show) {
      captureButton.setVisibility(View.GONE);
      matchButton.setVisibility(View.VISIBLE);
    } else {
      captureButton.setVisibility(View.VISIBLE);
      matchButton.setVisibility(View.GONE);
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
  public void onClick(View v) {
    Log.d(TAG, "Inside onClick()");
    switch (v.getId()) {
    case R.id.final_proj_capture:
      Log.d(TAG, "Clicked on Capture button... taking picture...");
      soundHelper.playCaptureSound(context);
      takePicture();
      break;
    case R.id.final_proj_match:
      Log.d(TAG, "Clicked on Match button... taking & matching picture...");
      soundHelper.playCaptureSound(context);
      takePicture();
      break;
    case R.id.final_proj_clear:
      if (imgToMatchView != null) {
        Log.d(TAG, "Clearing captured image 1...");
        imgToMatchView.setVisibility(View.GONE);
        isImg1Present = false;
      }
      showMatchBtn(false);
      break;
    case R.id.final_proj_quit:
      Log.d(TAG, "isSinglePhoneMode = " + isSinglePhoneMode);
      if (isSinglePhoneMode) {
        singlePhoneDialog = Util.showSinglePhoneDialog(this,
            ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,projPreferences);
        singlePhoneDialog.show();
        isSinglePhoneDialogShown = true;
      } else {
        // Go to Connection Activity
        Intent captureIntent = new Intent(context, Connection.class);
        captureIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(captureIntent);
      }
      break;
    }
  }

  private Bitmap readImgBmp(int imgNo) {
    File mediaStorageDir = new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        ProjectConstants.IMG_DIR_NAME);
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bitmap = BitmapFactory
        .decodeFile(mediaStorageDir.getPath() + File.separator
            + ProjectConstants.P1_IMG_NAME_PREFIX + imgNo, options);

    Log.d(TAG, "Reading img at path: " + mediaStorageDir.getPath()
        + File.separator + ProjectConstants.P1_IMG_NAME_PREFIX + imgNo);
    return bitmap;
  }

  private void displayImg(Bitmap bmImg) {
    Log.d(TAG, "Inside dislayImg()");
    Log.d(TAG, "bmImg = " + bmImg);
    imgToMatchView = (ImageView) findViewById(R.id.image_to_match);
    imgToMatchView.setVisibility(View.VISIBLE);
    imgToMatchView.setImageBitmap(bmImg);
    imgToMatchView.setAlpha(ProjectConstants.IMG_ALPHA);
  }

  protected void matchImages(int img1No, int img2No) {
    File mediaStorageDir = new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        ProjectConstants.IMG_DIR_NAME);
    String img1Path = mediaStorageDir.getPath() + File.separator
        + ProjectConstants.P1_IMG_NAME_PREFIX + img1No;
    String img2Path = mediaStorageDir.getPath() + File.separator
        + ProjectConstants.P1_IMG_NAME_PREFIX + img2No;

    Mat imageMatrix1 = Highgui.imread(img1Path);
    Mat imageMatrix2 = Highgui.imread(img2Path);

    Log.d(TAG, "\n Cancelling progress dialog... \n");
    progress.cancel();
    showResult(Util.imagesMatch(imageMatrix1, imageMatrix2, matchingDifficultyLevel));
  }

  private void showResult(boolean isMatching) {
    if (isMatching) {
      soundHelper.playMatchSuccessSound(context);
    } else {
      soundHelper.playMatchFailSound(context);
    }
    String msg = (isMatching ? "Images matched!" : "Images did NOT match!");
    Util.showToast(context, msg, 2000);
  }

  @Override
  protected CharSequence getTakePictureWaitMsg() {
    if (isImg1Present) {
      return ProjectConstants.MATCH_WAIT_MSG;
    } else {
      return ProjectConstants.CAPTURE_WAIT_MSG;
    }
  }

  @Override
  protected void processCapturedPicture(byte[] data) {
    if (isImg1Present) {
      Log.d(TAG, "\n Matching image... \n");
      // Match the current Img with Img1
      storeImg(IMG_2_NAME, data);
      matchImages(1, 2);
    } else {
      Log.d(TAG, "\n Storing image... \n");
      // Store current Img as Img1

      storeImg(IMG_1_NAME, data);
      isImg1Present = true;

      // Replace Capture btn with Match btn
      showMatchBtn(true);

      // show IMG 1 in image view
      displayImg(readImgBmp(1));
    }
    Log.d(TAG, "\n Cancelling progress dialog... \n");
    progress.cancel();
  }

  protected void storeImg(String imgName, byte[] data) {
    File pictureFile = Util.getOutputMediaFile(imgName, context);
    if (pictureFile == null) {
      Log.d(TAG, "Error creating media file, check storage permissions!");
      return;
    }
    try {
      FileOutputStream fos = new FileOutputStream(pictureFile);
      fos.write(data);
      fos.close();
    } catch (FileNotFoundException e) {
      Log.d(TAG, "File not found: " + e.getMessage());
    } catch (IOException e) {
      Log.d(TAG, "Error accessing file: " + e.getMessage());
    }
  }

  private SharedPreferences getSharedPreferences() {
    return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
  }
  
}
