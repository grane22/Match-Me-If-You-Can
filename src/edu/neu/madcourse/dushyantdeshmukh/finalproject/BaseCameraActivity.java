package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import java.io.IOException;
import java.util.List;
import org.opencv.android.OpenCVLoader;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.trickiestpart.CameraPreview;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public abstract class BaseCameraActivity extends Activity implements
    OnClickListener {

  protected static final String TAG = "BASE CAMERA ACTIVITY";
  protected Context context;
  protected Camera mCamera;
  protected CameraPreview mPreview;
  protected FrameLayout preview;
  protected ProgressDialog progress;
  protected Bitmap currBmpImg;
  protected int totalNoOfImgs;
  protected int matchingDifficultyLevel;
  protected SharedPreferences projPreferences;
  protected static WindowManager windowManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initializeControls();
  }

  private SharedPreferences getSharedPreferences() {
    return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
  }

  private void initializeControls() {
    Log.d(TAG, "Inside onCreate()");

    context = this;
    projPreferences = getSharedPreferences();
    totalNoOfImgs = projPreferences.getInt(ProjectConstants.PREF_TOTAL_NO_OF_IMAGES, 3);
    matchingDifficultyLevel = projPreferences.getInt(ProjectConstants.PREF_MATCHING_DIFFICULTY, 2);

    // set camera preview as main layout for this activity
    setContentView(R.layout.final_proj_cam_preview);

    // initialize WindowManager instance
    windowManager = getWindowManager();

    // Create an instance of Camera
    mCamera = getCameraInstance();

    // Create our Preview view and set it as the content of our activity.
    mPreview = new CameraPreview(this, mCamera);
    preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(mPreview);

    try {
      mCamera.setPreviewDisplay(mPreview.getHolder());
    } catch (IOException ex) {
      Log.e(TAG, "Error setting camera preview display");
      ex.printStackTrace();
      Util.showToast(context, "Error setting camera preview display", 3000);
    }

    // Initialize ProgressDialog
    progress = new ProgressDialog(this);
    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progress.setIndeterminate(true);
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Inside onResume()");

    if (mCamera == null) {
      mCamera = getCameraInstance();
      mPreview = new CameraPreview(this, mCamera);
      preview.addView(mPreview);
    }
    mCamera.startPreview();
  }

  @Override
  protected void onPause() {
    super.onPause();
    releaseCamera();
    preview.removeView(mPreview);
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release(); // release the camera for other applications
      mCamera = null;
    }
  }

  protected void takePicture() {
    new AsyncTask<String, Integer, String>() {
      @Override
      protected void onPreExecute() {
        super.onPreExecute();
        progress.setMessage(getTakePictureWaitMsg());
        progress.show();
      }

      @Override
      protected String doInBackground(String... params) {
        String retVal = "";
        mCamera.takePicture(null, null, mPicture);
        Log.d(TAG, "retVal: " + retVal);
        return retVal;
      }

      @Override
      protected void onPostExecute(String result) {
        super.onPostExecute(result);

      }
    }.execute(null, null, null);
  }

  private PictureCallback mPicture = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      Log.d(TAG, "Inside onPictureTaken()");

      mCamera.startPreview();

      processCapturedPicture(data);

    }
  };

  /** A safe way to get an instance of the Camera object. */
  public Camera getCameraInstance() {
    Camera camera = null;
    try {
      camera = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      Log.e(TAG, "Camera is not available (in use or does not exist)");
      // Camera is not available (in use or does not exist)
      Util.showToast(context,
          "Camera is not available (in use or does not exist)", 4000);
      return camera;
    }
    Parameters parameters = camera.getParameters();

    DisplayMetrics displaymetrics = new DisplayMetrics();
    windowManager.getDefaultDisplay().getMetrics(displaymetrics);

    int screenHeight = displaymetrics.heightPixels;
    int screenWidth = displaymetrics.widthPixels;

    List<Size> sizes = parameters.getSupportedPreviewSizes();
    Size optimalSize = getOptimalPreviewSize(sizes, screenWidth, screenHeight,
        camera);
    parameters.setPreviewSize(optimalSize.width, optimalSize.height);

    Log.d(
        TAG,
        "Inside getcameraInstance(), setting picture size to screenWidth X screenHeight = "
            + optimalSize.width + " X " + optimalSize.height);

    // Log.d(TAG,
    // "Inside getcameraInstance(), setting picture size to screenWidth X screenHeight = "
    // + screenWidth + " X " + screenHeight);
    parameters.setPictureSize(optimalSize.width, optimalSize.height);
    camera.setParameters(parameters);
    return camera; // returns null if camera is unavailable
  }

  private Size getOptimalPreviewSize(List<Size> sizes, int scrnWidth,
      int scrnHeight, Camera camera) {
    Log.d(TAG, "Inside getOptimalPreviewSize()");
    Log.d(TAG, "scrnHeight - " + scrnHeight);
    Log.d(TAG, "screenWidth - " + scrnWidth);

    Log.d(TAG, "================= After checking max n min====================");

//    int screenWidth = Math.max(scrnHeight, scrnWidth);
    int screenWidth = scrnWidth;
    Log.d(
        TAG,
        "Math.max(" + scrnHeight + ", " + scrnWidth + ") = "
            + Math.max(scrnHeight, scrnWidth));
//    int screenHeight = Math.min(scrnHeight, scrnWidth);
    int screenHeight = scrnHeight;
    Log.d(
        TAG,
        "Math.min(" + scrnHeight + ", " + scrnWidth + ") = "
            + Math.min(scrnHeight, scrnWidth));

    Log.d(TAG, "screenHeight - " + screenHeight);
    Log.d(TAG, "screenWidth - " + screenWidth);

    Log.d(TAG, "=====================================");

    Size optSize = camera.new Size(0, 0);
    int minHeightDiff = 1000;
    int minWidthDiff = 1000;
    for (Size s : sizes) {
      int currHeight = s.height;
      Log.d(TAG, "currHeight - " + currHeight);
      Log.d(TAG, "currWidth - " + s.width);
      int currHeightDiff = Math.abs(currHeight - screenHeight);
      if (currHeightDiff <= minHeightDiff) {
        Log.d(TAG, "only minHeightDiff:");
        Log.d(TAG, "currHeight - " + currHeight);
        int currWidth = s.width;
        int currWidthDiff = Math.abs(currWidth - screenWidth);
        if (currWidthDiff < minWidthDiff) {
          Log.d(TAG, "Also minWidthDiff:");
          Log.d(TAG, "currHeight - " + currHeight);
          Log.d(TAG, "currWidth - " + currWidth);
          minHeightDiff = currHeightDiff;
          minWidthDiff = currWidthDiff;
          optSize = camera.new Size(currWidth, currHeight);
        }
      }
    }
    Log.d(TAG, "=====================================");
//    optSize = camera.new Size(scrnWidth, scrnHeight);
    Log.d(TAG, "optSize.height - " + optSize.height);
    Log.d(TAG, "optSize.width - " + optSize.width);
    return optSize;
  }

  @Override
  public abstract void onClick(View arg0);

  protected abstract CharSequence getTakePictureWaitMsg();

  protected abstract void processCapturedPicture(byte[] data);
  
  /**
   * Initializes the openCV library
   */
  protected static void initializeOpenCV() {
	  Log.d(TAG, "inside initializeOpenCV: initializing openCV");
    if (OpenCVLoader.initDebug()) {
      Log.i(TAG, "OpenCV library initialized successfully");
    } else {
      // Handle initialization error
      Log.e(TAG, "Error while initializing OpenCV library.");
    }
  }

}
