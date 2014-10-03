package edu.neu.madcourse.dushyantdeshmukh.trickiestpart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;

import edu.neu.madcourse.dushyantdeshmukh.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class Test extends Activity implements OnClickListener {

  protected static final String TAG = "TRICKIEST PART TEST ACTIVITY";
  private static final String IMG_1_NAME = "IMG_1.jpg";
  private static final String IMG_2_NAME = "IMG_2.jpg";
  private Intent i;
  Context context;
  private Camera mCamera;
  private CameraPreview mPreview;
  private FrameLayout preview;
  View matchButton, captureButton, clearButton, quitButton;
  ImageView img1View;
  protected boolean isImg1Present = false;
  LayoutInflater controlInflater = null;
  
  ProgressDialog progress;
  
  private CameraBridgeViewBase mOpenCvCameraView;

  private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
  /** A safe way to get an instance of the Camera object. */
  public static Camera getCameraInstance() {
    Camera c = null;
    try {
      c = Camera.open(); // attempt to get a Camera instance
    } catch (Exception e) {
      Log.e(TAG, "Camera is not available (in use or does not exist)");
      // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.d(TAG, "Inside onCreate()");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.trickiest_part_test);

    context = getApplicationContext();

    // Set up click listeners for all the buttons
    captureButton = findViewById(R.id.trickiest_part_capture_img_button);
    captureButton.setOnClickListener(this);

    matchButton = findViewById(R.id.trickiest_part_match_img_button);
    matchButton.setOnClickListener(this);

    clearButton = findViewById(R.id.trickiest_part_clear_img_button);
    clearButton.setOnClickListener(this);

    quitButton = findViewById(R.id.trickiest_part_quit_button);
    quitButton.setOnClickListener(this);

    //  read and display Image1 if present
    Log.d(TAG, "isImg1Present = " + isImg1Present);
    if (isImg1Present) {
      dislayImg(readImgBmp(IMG_1_NAME));
    }
    
    // Create an instance of Camera
    mCamera = getCameraInstance();

    // Create our Preview view and set it as the content of our activity.
    mPreview = new CameraPreview(this, mCamera);
    preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(mPreview);

    try {
      mCamera.setPreviewDisplay(mPreview.getHolder());
    } catch (IOException ex) {
      Log.e(TAG, "Error setting preview display");
      ex.printStackTrace();
    }
    
    setCamPreviewHeight();
    
    progress = new ProgressDialog(this);
    
    progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progress.setIndeterminate(true);
    
    //////////////////////////////
//    controlInflater = LayoutInflater.from(getBaseContext());
//    View viewControl = controlInflater.inflate(R.layout.trickiest_part_overlay, null);
//    LayoutParams layoutParamsControl
//     = new LayoutParams(LayoutParams.FILL_PARENT,
//     LayoutParams.FILL_PARENT);
//    this.addContentView(viewControl, layoutParamsControl);
  }

  private void setCamPreviewHeight() {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
    int screenHeight = displaymetrics.heightPixels;
    int screenWidth = displaymetrics.widthPixels;
    
    int btnHeight = clearButton.getHeight();
    btnHeight = 100;
    
    ViewGroup.LayoutParams imgViewParams = preview.getLayoutParams();
    imgViewParams.height = (screenHeight * 63) / 100;
    preview.setLayoutParams(imgViewParams);
    
    Log.d(TAG, "screenHeight = " + screenHeight);
    Log.d(TAG, "screenWidth = " + screenWidth);
    Log.d(TAG, "btnHeight = " + btnHeight);
    Log.d(TAG, "Setting preview height to = " + (screenHeight - btnHeight - 90));
    
  }

  private Bitmap readImgBmp(String imgName) {
    File mediaStorageDir = new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "MyCameraApp");
    BitmapFactory.Options options = new BitmapFactory.Options();
//    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
    Bitmap bitmap = BitmapFactory.decodeFile(mediaStorageDir.getPath() + File.separator
        + imgName, options);
//    selected_photo.setImageBitmap(bitmap);
//    Bitmap bitmap;
//    uUri ri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + urls[0]);
//    
//    /**************  Decode an input stream into a bitmap. *********/
//    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
    return bitmap;
  }

  private void dislayImg(Bitmap bmImg) {
    img1View = (ImageView) findViewById(R.id.image_to_match);
    img1View.setImageBitmap(bmImg);
    img1View.setAlpha(75);
    
    Log.d(TAG, "\n Cancelling progress dialog... \n");
    progress.cancel();
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.d(TAG, "Inside onResume()");
    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback); 
    
    if (mCamera == null) {
      mCamera = getCameraInstance();
      mPreview = new CameraPreview(this, mCamera);
      preview.addView(mPreview);
    }
    mCamera.startPreview();
    
    showMatchBtn(isImg1Present);
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
    releaseCamera();
    preview.removeView(mPreview);
  }

  private void releaseCamera() {
    if (mCamera != null) {
      mCamera.release(); // release the camera for other applications
      mCamera = null;
    }
  }

  private PictureCallback mPicture = new PictureCallback() {

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      Log.d(TAG, "Inside onPictureTaken()");

      mCamera.startPreview();

      if (isImg1Present) {
        Log.d(TAG, "\n Matching image... \n");
        // Match the current Img with Img1
        storeImg(IMG_2_NAME, data);
        matchImages(IMG_1_NAME, IMG_2_NAME);
      } else {
        Log.d(TAG, "\n Storing image... \n");
      // Store current Img as Img1

        storeImg(IMG_1_NAME, data);
        isImg1Present = true;
        
        //  Replace Capture btn with Match btn
        showMatchBtn(true);
        
        //  show IMG 1 in image view
        dislayImg(readImgBmp(IMG_1_NAME));
      }
    }
  };

  @Override
  public void onClick(View v) {
    Log.d(TAG, "Inside onClick()");
    switch (v.getId()) {
    case R.id.trickiest_part_capture_img_button:
      progress.setMessage("Storing image...");
      progress.show();
      Log.d(TAG, "Clicked on Capture button... taking picture...");
        mCamera.takePicture(null, null, mPicture);
      break;
    case R.id.trickiest_part_match_img_button:
      progress.setMessage("Matching image... ");
      progress.show();
      Log.d(TAG, "Clicked on Match button... taking & matching picture...");
      mCamera.takePicture(null, null, mPicture);
      break;
    case R.id.trickiest_part_clear_img_button:
      if (img1View != null) {
        Log.d(TAG, "Clearing captured image 1...");
        img1View.setImageDrawable(null);
        isImg1Present = false;
      }
      showMatchBtn(false);
      break;
    case R.id.trickiest_part_quit_button:
      finish();
      break;
    }
  }

  protected void matchImages(String img1Name, String img2Name) {
    File mediaStorageDir = new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "MyCameraApp");
    String img1Path = mediaStorageDir.getPath()
        + File.separator + img1Name;
    String img2Path = mediaStorageDir.getPath()
        + File.separator + img2Name;
    
    boolean isMatching = false;
    Mat imageMatrix1 = Highgui.imread(img1Path);
      Mat imageMatrix2 = Highgui.imread(img2Path);
       
      double psnr = getPSNR(imageMatrix1, imageMatrix2);
      
      if(psnr >= 14.0){
        isMatching = true;
      }else{
        isMatching = false;
      }
      Log.d(TAG, "\n Cancelling progress dialog... \n");
      progress.cancel();
      showResult(isMatching);
  }

  private void showResult(boolean isMatching) {
    String msg = (isMatching? "Images matched!" : "Images did NOT match!"); 
    Toast t = Toast.makeText(getApplicationContext(), msg, 3000);
    t.show();
    Log.d(TAG, "\n===================================================\n");
    Log.d(TAG, (msg));
    Log.d(TAG, "\n===================================================\n");
  }

  protected void storeImg(String imgName, byte[] data) {
    File pictureFile = getOutputMediaFile(imgName);
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

  /** Create a file Uri for saving an image or video */
  private Uri getOutputMediaFileUri(int type, String imgName) {
    return Uri.fromFile(getOutputMediaFile(imgName));
  }

  /** Create a File for saving an image or video */
  private File getOutputMediaFile(String imgName) {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = new File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "MyCameraApp");
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (!mediaStorageDir.exists()) {
      if (!mediaStorageDir.mkdirs()) {
        Log.d("MyCameraApp", "failed to create directory");
        return null;
      }
    }

    // Create a media file name
    // String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
    // .format(new Date());
    File mediaFile = new File(mediaStorageDir.getPath() + File.separator
        +imgName);
    Log.d(TAG, "Storing img at path: " + mediaStorageDir.getPath()
        + File.separator + imgName);
    return mediaFile;
  }

  double getPSNR(Mat i1, Mat i2)
  {
    Log.d(TAG, "Inside getPSNR()");
    Log.d(TAG, "Rows i1: "+ i1.rows() + " Cols i1: "+ i1.cols());
    Log.d(TAG, "Rows i2: "+ i2.rows() + " Cols i2: "+ i2.cols());
   Mat s1= new Mat();
   Core.absdiff(i1, i2, s1);       // |I1 - I2|
   s1.convertTo(s1, CvType.CV_32F);  // cannot make a square on 8 bits
   s1 = s1.mul(s1);           // |I1 - I2|^2

   Scalar s = Core.sumElems(s1);         // sum elements per channel

   double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

   Log.d(TAG, "sse: " + sse);
   
   if( sse <= 1e-10) // for small values return zero
       return 0;
   else
   {
       double  mse =sse /(double)(i1.channels() * i1.total());
       double psnr = 10.0* Math.log10((255*255)/mse);
       Log.d(TAG, "psnr: " + psnr);
       return psnr;
   }
  }
  
}
