package edu.neu.madcourse.dushyantdeshmukh.util;

import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

public class InternetConnUtil {

  private static final String TAG = "InternetConnUtil";

  public static boolean isNetworkAvailable(Context context) 
  {
      ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
      android.net.NetworkInfo mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

      return wifi.isConnected() || mobile.isConnected();
  }
  
  public static boolean hasActiveInternetConnection(Context context) {
    if (isNetworkAvailable(context)) {
        try {
            HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
            urlc.setRequestProperty("User-Agent", "Test");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1500); 
            urlc.connect();
            return (urlc.getResponseCode() == 200);
        } catch (Exception e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
    } else {
        Log.d(TAG, "No network available!");
    }
    return false;
}
}
