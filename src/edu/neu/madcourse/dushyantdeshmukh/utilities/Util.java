package edu.neu.madcourse.dushyantdeshmukh.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.CaptureImage;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.Connection;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.Home;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.MatchImage;
import edu.neu.madcourse.dushyantdeshmukh.finalproject.ProjectConstants;
import edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame.Constants;
import edu.neu.mhealth.api.KeyValueAPI;

public class Util {

	private static final String TAG = "Utility class";
	static Object staticObjectInstance;
	static boolean isCaptureEvent;
	static boolean skipTutorial;
	static SharedPreferences staticSP;
	static Context staticContext;
	static Activity staticActivity;
	static int currState;
	static Dialog quitDialog;

	public static HashMap<String, String> getDataMap(String bundlesStr,
			String tag) {
		HashMap<String, String> dataMap = new HashMap<String, String>();
		bundlesStr = bundlesStr.substring(8, bundlesStr.length() - 2);
		String keyValArr[] = bundlesStr.split(", ");
		for (String str : keyValArr) {
			String tempArr[] = str.split("=");
			String tempKey = tempArr[0];
			String tempVal = tempArr[1];
			dataMap.put(tempKey, tempVal);
			Log.d(tag, "'" + tempKey + "' : '" + tempVal + "'");
		}
		return dataMap;
	}

	public static void playSound(Context context, MediaPlayer mp,
			int soundResId, boolean loop) {
		// Release any resources from previous MediaPlayer
		if (mp != null) {
			mp.release();
		}
		// Create a new MediaPlayer to play this sound
		mp = MediaPlayer.create(context, soundResId);
		mp.start();
		mp.setLooping(loop);
	}

	public static BloomFilter<String> loadBitsetFromFile(String filepath,
			AssetManager am) {
		BloomFilter<String> bloomFilter = null;
		;
		try {
			int fileLength = (int) am.openFd(filepath).getLength();
			Log.d(TAG, "compressed file length = " + fileLength);
			InputStream is = am.open(filepath);

			byte[] fileData = new byte[fileLength];
			DataInputStream dis = new DataInputStream(is);
			dis.readFully(fileData);
			dis.close();
			bloomFilter = new BloomFilter<String>(0.0001, 450000);
			bloomFilter = BloomFilter.loadBitsetWithByteArray(fileData,
					bloomFilter);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bloomFilter;
	}

	public static void addValuesToKeyOnServer(String keyname, String val1,
			String val2) {
		Log.d(TAG, "\n\n\n Adding (" + val1 + "-" + val2 + ") on server.\n\n\n");
		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... params) {
				Log.d(TAG, "inside addValuesToKeyOnServer():doInBackground()");
				String keyname = params[0];
				String val1 = params[1];
				String val2 = params[2];
				String retVal = "Server Unavailable";
				String result = "";
				if (KeyValueAPI.isServerAvailable()) {
					Log.d(TAG, "Inside server available");
					String availableUsersVal = KeyValueAPI.get(
							Constants.TEAM_NAME, Constants.PASSWORD, keyname);

					if (availableUsersVal.toUpperCase().contains("ERROR:")) {
						Log.d(TAG, "no such key: " + keyname);
						Log.d(TAG, "availableUsersVal:error= " + availableUsersVal);
						// No player waiting... put your own regId
						result = KeyValueAPI
								.put(Constants.TEAM_NAME, Constants.PASSWORD,
										keyname, val1 + "::" + val2);
						if (!result.toUpperCase().contains("ERROR")) {
							// displayMsg("No player waiting... putting your regId "
							// + myRegId + " in WAITING_PLAYER.");
							retVal = "No player waiting... storing your Username::RegistrationId "
									+ val1 + "::" + val2 + " on server.";
						} else {
							// displayMsg("Error while putting your regId on server: "
							// + result);
							retVal = "Error while putting your username::regId on server: "
									+ result;
						}
					} else {
						Log.d(TAG, "key exists: " + keyname);
						Log.d(TAG, "availableUsersVal: " + availableUsersVal);
						boolean valuePresent = false;

						if (availableUsersVal.trim() != "") {
							String usersArr[] = availableUsersVal.split(",");
							// Iterate over list of entries in key 'keyname'and
							// check for val1
							Log.d(TAG, "usersArr.length: " + usersArr.length);
							for (int i = 0; i < usersArr.length; i++) {
								Log.d(TAG, "usersArr[" + i + "] = "
										+ usersArr[i]);
								if (usersArr[i].trim() != "") {
									String tempArr[] = usersArr[i].split("::");
									String tempUsername = tempArr[0];
									String tempRegId = tempArr[1];
									if (tempUsername.equals(val1)) {
										valuePresent = true;
										break;
									}
								}
							}
						}
						if (!valuePresent) {
							Log.d(TAG, "Inside valuePresent = false");
							if (availableUsersVal.trim() != "") {
								availableUsersVal += ",";
							}
							// append val1-val2 to value of key 'keyname'
							availableUsersVal += val1 + "::" + val2;
							
							Log.d(TAG, "storing availableUsersVal: " + availableUsersVal);
							// store on server
							result = KeyValueAPI.put(Constants.TEAM_NAME,
									Constants.PASSWORD, keyname,
									availableUsersVal);

							if (!result.contains("Error")) {
								// displayMsg("Stored your Username-RegistrationId "
								// + val1 + "::" + val2 + " on server.");
								retVal = "SUCCESS: Stored your Username::RegistrationId "
										+ val1 + "::" + val2 + " on server.";
							} else {
								// displayMsg("Error while putting your username-regId on server: "
								// + result);
								retVal = "Error while putting your username::regId on server: "
										+ result;
							}
						}
					}
				}
				return retVal;
			}

			@Override
			protected void onPostExecute(String result) {
				// mDisplay.append(msg + "\n");
				Log.d(TAG, "addValuesToKeyOnServer, result = " + result);
			}
		}.execute(keyname, val1, val2);
	}

	public static void removeValuesFromKeyOnServer(String keyname, String val1,
			String val2) {
		Log.d(TAG, "\n\n\n Removing (" + val1 + "-" + val2
				+ ") from server.\n\n\n");
		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... params) {
				String keyname = params[0];
				String val1 = params[1];
				String val2 = params[2];
				String retVal = "";
				String result = "";
				if (KeyValueAPI.isServerAvailable()) {
					String availableUsersVal = KeyValueAPI.get(
							Constants.TEAM_NAME, Constants.PASSWORD, keyname);

					if (availableUsersVal.toUpperCase().contains("ERROR:")) {
						// Specified key does not exist on server
						retVal = "Specified key does not exist on server.";
					} else {
						StringBuilder newVal = new StringBuilder();
						if (availableUsersVal.trim() != "") {
							String usersArr[] = availableUsersVal.split(",");
							// Iterate over list of entries in key 'keyname'and
							// check for val1
							for (int i = 0; i < usersArr.length; i++) {
								Log.d(TAG, "usersArr[" + i + "] = "
										+ usersArr[i]);
								if (usersArr[i].trim() != "") {
									String tempArr[] = usersArr[i].split("::");
									String tempUsername = tempArr[0];
									String tempRegId = tempArr[1];
									if (!tempUsername.equals(val1)
											|| !tempRegId.equals(val2)) {
										newVal.append(",");
										newVal.append(usersArr[i]);
									}
								}
							}
							String newStrVal = "";
							// String newStrVal = newVal.substring(0,
							// newVal.length() - 1);
							if (newVal.length() > 0 && newVal.charAt(0) == ',') {
								newStrVal = newVal.substring(1);
							}

							// store new val on server
							result = KeyValueAPI.put(Constants.TEAM_NAME,
									Constants.PASSWORD, keyname, newStrVal);

							if (!result.contains("Error")) {
								// displayMsg("Removed your val1-val2 from  "
								// + val1 + "-" + val2 + " on server.");
								retVal = "Removed your val1::val2 from " + val1
										+ "::" + val2 + " on server.";
							} else {
								// displayMsg("Error while removing val1-val2 from server: "
								// + result);
								retVal = "Error while removing val1::val2 from server: "
										+ result;
							}
						}
					}
				}
				return retVal;
			}

			@Override
			protected void onPostExecute(String result) {
				// mDisplay.append(msg + "\n");
				Log.d(TAG, "addValuesToKeyOnServer" + result);
			}
		}.execute(keyname, val1, val2);
	}

	// HTTP POST request
	public static String sendPost(String dataStr, String opponentRegId)
			throws Exception {

		String url = "https://selfsolve.apple.com/wcResults.do";
		URL obj = new URL(Constants.SERVER_URL);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("Authorization", "key="
				+ Constants.BROWSER_API_KEY);
		// con.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

		String urlParameters = dataStr + "&registration_id=" + opponentRegId;

		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		// displayMsg("\n HTTP Post response: " + response.toString());
		// Log.d(TAG, "HTTP POST response" + response.toString());
		return response.toString();
	}

	public static void storeOppnentInSharedpref(SharedPreferences sp,
			String opponentName, String opponentRegId) {
		// Store opponent name and regId in SP
		Editor ed = sp.edit();
		Log.d(TAG, "Opp Name: " + opponentName);
		ed.putString(Constants.PREF_OPPONENT_REG_ID, opponentRegId);
		ed.putString(Constants.PREF_OPPONENT_NAME, opponentName);
		ed.commit();
		Log.d(TAG, "Message sent to displayMsg() => Connected to opponent:"
				+ opponentName + " (" + opponentRegId + ")");
	}

	public static int[][] getInitialScoreboard() {
		int[][] scoreboardArr = new int[Constants.NO_OF_ROUNDS][2];
		for (int i = 0; i < Constants.NO_OF_ROUNDS; i++) {
			scoreboardArr[i][0] = 0;
			scoreboardArr[i][0] = 0;
		}
		return scoreboardArr;
	}

	public static int[][] scoreboardStrToArr(String scoreboardStr) {
		// Log.d(TAG, "Converting scoreboard str to array");
		// Log.d(TAG, "Input scoreboardStr = " + scoreboardStr);
		int[][] scoreboardArr = new int[Constants.NO_OF_ROUNDS][2];
		String[] roundArr = scoreboardStr.split(",");
		for (int i = 0; i < roundArr.length; i++) {
			String[] scoreArr = roundArr[i].split("-");
			// Log.d(TAG, "i = " + i);
			// Log.d(TAG, "scoreArr[0] = " + scoreArr[0]);
			// Log.d(TAG, "Integer.parseInt(scoreArr[0]) = " +
			// Integer.parseInt(scoreArr[0]));
			// Log.d(TAG, "scoreArr[1] = " + scoreArr[1]);
			// Log.d(TAG, "Integer.parseInt(scoreArr[1]) = " +
			// Integer.parseInt(scoreArr[1]));

			scoreboardArr[i][0] = Integer.parseInt(scoreArr[0]);
			scoreboardArr[i][1] = Integer.parseInt(scoreArr[1]);
		}
		return scoreboardArr;
	}

	public static String scoreboardArrToStr(int[][] scoreboardArr) {
		StringBuilder scoreboardStr = new StringBuilder();
		for (int i = 0; i < scoreboardArr.length; i++) {
			int[] scoreArr = scoreboardArr[i];
			scoreboardStr.append(scoreArr[0]);
			scoreboardStr.append("-");
			scoreboardStr.append(scoreArr[1]);
			scoreboardStr.append(",");
		}
		scoreboardStr.substring(0, scoreboardStr.length() - 1);
		return scoreboardStr.toString();
	}

	public static void printScoreboard(int[][] scoreboardArr) {
		Log.d(TAG, "\n Scoreboard:");
		for (int i = 0; i < scoreboardArr.length; i++) {
			Log.d(TAG, "Round " + (i + 1) + ": " + scoreboardArr[i][0] + " - "
					+ scoreboardArr[i][1]);
		}
	}

	public static String getScoreboardDisplayStr(String scoreboardStr) {
		StringBuilder retStr = new StringBuilder();
		int[][] scoreboardArr = Util.scoreboardStrToArr(scoreboardStr);
		int p1Total = 0, p2Total = 0;
		for (int i = 0; i < scoreboardArr.length; i++) {
			retStr.append(" Round " + (i + 1) + ": \t \t" + scoreboardArr[i][0]
					+ "  \t \t   " + scoreboardArr[i][1] + "\n");
			p1Total += scoreboardArr[i][0];
			p2Total += scoreboardArr[i][1];
		}
		retStr.append("\n Total: \t \t \t" + p1Total + "  \t \t   " + p2Total
				+ "\n");
		return retStr.toString();
	}

	public static String getCurrentDateTime() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	public static String getTotalScore(String scoreboardStr) {
		int[][] scoreboardArr = Util.scoreboardStrToArr(scoreboardStr);
		int total = 0;
		for (int i = 0; i < scoreboardArr.length; i++) {
			total += scoreboardArr[i][0];
		}
		return String.valueOf(total);
	}

	public static void updateTopScorersList(String username, String totalScore,
			String currentDateTime) {
		Log.d(TAG, "\n\n\n Adding (" + totalScore + "-" + username + "-"
				+ currentDateTime + ") on server.\n\n\n");
		new AsyncTask<String, Integer, String>() {
			@Override
			protected String doInBackground(String... params) {
				Log.d(TAG, "inside addValuesToKeyOnServer():doInBackground()");
				String totalScore = params[0];
				String username = params[1];
				String currentDateTime = params[2];
				String retVal = "";
				String result = "";
				if (KeyValueAPI.isServerAvailable()) {
					String topScorersVal = KeyValueAPI.get(Constants.TEAM_NAME,
							Constants.PASSWORD, Constants.TOP_SCORERS_LIST);

					if (topScorersVal.toUpperCase().contains("ERROR:")) {
						Log.d(TAG, "no such key: " + Constants.TOP_SCORERS_LIST);
						// No player waiting... put your own regId
						result = KeyValueAPI.put(Constants.TEAM_NAME,
								Constants.PASSWORD, Constants.TOP_SCORERS_LIST,
								totalScore + "-" + username + "-"
										+ currentDateTime);
						if (!result.contains("Error")) {
							// displayMsg("No player waiting... putting your regId "
							// + myRegId + " in WAITING_PLAYER.");
							retVal = "Top scorers list empty... storing your entry: '"
									+ totalScore
									+ "-"
									+ username
									+ "-"
									+ currentDateTime + "' on server.";
						} else {
							// displayMsg("Error while putting your regId on server: "
							// + result);
							retVal = "Error while putting 'totalScore-username-currentDateTime' on server: "
									+ result;
						}
					} else {
						Log.d(TAG, "key exists: " + Constants.TOP_SCORERS_LIST);
						Log.d(TAG, "\n topScorersVal: " + topScorersVal);

						String sortedTopScorersVal = getSortedTopScorersVal(
								topScorersVal, totalScore, username,
								currentDateTime);

						Log.d(TAG, "\n Sorted topScorersVal: "
								+ sortedTopScorersVal);

						// store on server
						result = KeyValueAPI.put(Constants.TEAM_NAME,
								Constants.PASSWORD, Constants.TOP_SCORERS_LIST,
								sortedTopScorersVal);

						if (!result.contains("Error")) {
							// displayMsg("Stored 'totalScore-username-currentDateTime' : '"
							// +
							// totalScore
							// + "-" + username + "-" + currentDateTime +
							// "' on server.");
							retVal = "Stored 'totalScore-username-currentDateTime' : '"
									+ totalScore
									+ "-"
									+ username
									+ "-"
									+ currentDateTime + "' on server.";
						} else {
							// displayMsg("Error while putting your username-regId on server: "
							// + result);
							retVal = "Error while putting 'totalScore-username-currentDateTime' on server: "
									+ result;
						}

					}
				}
				return retVal;
			}

			@Override
			protected void onPostExecute(String result) {
				// mDisplay.append(msg + "\n");
				Log.d(TAG, "updateTopScorersList" + result);
			}
		}.execute(totalScore, username, currentDateTime);
	}

	@SuppressWarnings("unchecked")
	protected static String getSortedTopScorersVal(String topScorersVal,
			String totalScore, String username, String currentDateTime) {
		StringBuilder sortedTopScorersVal = new StringBuilder();
		Map<Integer, String> topScorersMap = getTopScorersMap(topScorersVal);
		topScorersMap.put(Integer.parseInt(totalScore), username + "-"
				+ currentDateTime);

		List<Integer> sortedKeys = new ArrayList<Integer>(
				topScorersMap.keySet());
		Collections.sort(sortedKeys, Collections.reverseOrder());

		for (int score : sortedKeys) {
			sortedTopScorersVal.append(",");
			String tempStr = score + "-" + topScorersMap.get(score);
			sortedTopScorersVal.append(tempStr);
		}
		return sortedTopScorersVal.substring(1);
	}

	private static Map<Integer, String> getTopScorersMap(String topScorersVal) {
		Map<Integer, String> retmap = new HashMap<Integer, String>();
		String[] itemsArr = topScorersVal.split(",");
		for (int i = 0; i < itemsArr.length; i++) {
			String currItem = itemsArr[i];
			String[] tempStrArr = currItem.split("-");
			int currtotaScore = Integer.parseInt(tempStrArr[0]);
			String currUsername = tempStrArr[1];
			String currDate = tempStrArr[2];
			retmap.put(currtotaScore, currUsername + "-" + currDate);
		}
		return retmap;
	}

	public static String getFormatedTopScorersStr(String topScorersVal) {
		StringBuilder formattedTopScorersVal = new StringBuilder();
		Map<Integer, String> topScorersMap = getTopScorersMap(topScorersVal);

		List<Integer> sortedKeys = new ArrayList<Integer>(
				topScorersMap.keySet());
		Collections.sort(sortedKeys, Collections.reverseOrder());

		formattedTopScorersVal.append(" Score \t Name \t \t Date-Time \n \n");
		for (int score : sortedKeys) {
			String tempStr = topScorersMap.get(score);
			String[] tempStrArr = tempStr.split("-");
			formattedTopScorersVal.append("\t " + score + "  \t \t "
					+ tempStrArr[0] + "  " + tempStrArr[1] + "\n\n");
		}
		return formattedTopScorersVal.toString();
	}

	public static void showToast(Context ctx, String msg, int duration) {
		Toast t = Toast.makeText(ctx, msg, duration);
		t.show();
		Log.d(TAG, "\n===================================================\n");
		Log.d(TAG, "Toast msg: " + msg);
		Log.d(TAG, "\n===================================================\n");
	}

	/**
	 * Converts given time in seconds to a string format 'mm:ss'
	 * 
	 * @param time
	 * @return
	 */
	public static String getTimeStr(int time) {
		int min, sec;
		min = time / 60;
		sec = time % 60;
		return (((min < 10) ? ("0" + min) : min) + ":" + ((sec < 10) ? ("0" + sec)
				: sec));
	}

	/**
	 * Reads the specified no of images from file system (internal memory
	 * default location)
	 * 
	 * @param totalNoOfImgs
	 * @return
	 */
	public static Bitmap[] getImgsToMatch(int totalNoOfImgs, int currState,
			Context context) {
		Log.d(TAG, "Inside getImgsToMatch(), currState = " + currState);
		Bitmap[] bitmapImgArr = new Bitmap[totalNoOfImgs];
		File mediaStorageDir = new File(
				context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				ProjectConstants.IMG_DIR_NAME);
		BitmapFactory.Options options = new BitmapFactory.Options();

		for (int i = 0; i < totalNoOfImgs; i++) {
			Bitmap currBitmap = BitmapFactory.decodeFile(
					mediaStorageDir.getPath() + File.separator
							+ getImagePrefix(currState) + (i + 1), options);
			Log.d(TAG, "Reading img to match: " + getImagePrefix(currState)
					+ (i + 1));
			bitmapImgArr[i] = currBitmap;
		}
		return bitmapImgArr;
	}

	/**
	 * Store the given image on file system (internal memory default location)
	 * suffixed with given image no
	 * 
	 * @param currImgData
	 * @param currImgNo
	 */
	public static void storeImg(byte[] imgData, int imgNo, int currState,
			Context context) {
		String imgName = getImagePrefix(currState) + imgNo;
		File pictureFile = getOutputMediaFile(imgName, context);
		if (pictureFile == null) {
			Log.d(TAG, "Error creating media file, check storage permissions!");
			return;
		}
		try {
			FileOutputStream fos = new FileOutputStream(pictureFile);
			fos.write(imgData);
			fos.close();
		} catch (FileNotFoundException e) {
			Log.d(TAG, "File not found: " + e.getMessage());
		} catch (IOException e) {
			Log.d(TAG, "Error accessing file: " + e.getMessage());
		}
	}

	/**
	 * 
	 * @param currState
	 * @return
	 */
	private static String getImagePrefix(int currState) {
		String imgPrefix = "";
		if ((ProjectConstants.SINGLE_PHONE_P2_CAPTURE_STATE == currState)
				|| (ProjectConstants.SINGLE_PHONE_P2_MATCH_STATE == currState)) {
			imgPrefix = ProjectConstants.P2_IMG_NAME_PREFIX;
		} else {
			imgPrefix = ProjectConstants.P1_IMG_NAME_PREFIX;
		}
		return imgPrefix;
	}

	/**
	 * Create a File for saving an image
	 * 
	 * @param imgName
	 * @param context
	 * @return
	 */
	public static File getOutputMediaFile(String imgName, Context context) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(
				context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				ProjectConstants.IMG_DIR_NAME);
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d(TAG, "failed to create directory: "
						+ ProjectConstants.IMG_DIR_NAME);
				return null;
			}
		}

		// Create a media file name
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ imgName);
		Log.d(TAG, "Storing img at path: " + mediaStorageDir.getPath()
				+ File.separator + imgName);
		return mediaFile;
	}

	/**
	 * Calculates the PSNR between the matrices for given two images
	 * 
	 * @param i1
	 *            - matrix for 1st image
	 * @param i2
	 *            - matrix for 2nd image
	 * @return
	 */
	public static double getPSNR(Mat i1, Mat i2) {
		Log.d(TAG, "Inside getPSNR()");
		Log.d(TAG, "Rows i1: " + i1.rows() + " Cols i1: " + i1.cols());
		Log.d(TAG, "Rows i2: " + i2.rows() + " Cols i2: " + i2.cols());
		Mat s1 = new Mat();
		Core.absdiff(i1, i2, s1); // |I1 - I2|
		s1.convertTo(s1, CvType.CV_32F); // cannot make a square on 8 bits
		s1 = s1.mul(s1); // |I1 - I2|^2

		Scalar s = Core.sumElems(s1); // sum elements per channel

		double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

		Log.d(TAG, "sse: " + sse);

		if (sse <= 1e-10) // for small values return zero
			return 0;
		else {
			double mse = sse / (double) (i1.channels() * i1.total());
			double psnr = 10.0 * Math.log10((255 * 255) / mse);
			Log.d(TAG, "psnr: " + psnr);
			return psnr;
		}
	}

	/**
	 * Converts the given byte[] into a Bitmap for the corresponding image
	 * 
	 * @param imgData
	 * @param windowManager
	 * @return
	 */
	public static Bitmap convertByteArrToBitmap(byte[] imgData) {
		Log.d(TAG, "\n\n Inside convertByteArrToBitmap(), imgData length = "
				+ imgData.length);
		Bitmap bmpImg = BitmapFactory.decodeByteArray(imgData, 0,
				imgData.length, null);
		return bmpImg;
	}

	/**
	 * Convert given Bitmap to Mat
	 * 
	 * @param bmpImg
	 * @return
	 */
	public static Mat convertBmpToMat(Bitmap bmpImg) {
		Mat mat = new Mat(bmpImg.getWidth(), bmpImg.getHeight(), CvType.CV_8UC1);
		Utils.bitmapToMat(bmpImg, mat);
		return mat;
	}

	public static boolean imagesMatch(Mat imgMat1, Mat imgMat2, int matchingDifficultyLevel) {
		boolean isMatching = false;
		double psnr = getPSNR(imgMat1, imgMat2);

		if (psnr >= getThresholdValPSNR(matchingDifficultyLevel)) {
			isMatching = true;
		} else {
			isMatching = false;
		}
		return isMatching;
	}

	/**
	 * Returns the threshold value of PSNR depending of the matching difficulty level
	 * @param matchingDifficultyLevel
	 * @return
	 */
	private static double getThresholdValPSNR(int matchingDifficultyLevel) {
	  double psnrThresholdVal = ProjectConstants.PSNR_THRESHOLD_MEDIUM;
    switch(matchingDifficultyLevel) {
    case 1:
      psnrThresholdVal = ProjectConstants.PSNR_THRESHOLD_EASY;
      break;
    case 2:
      psnrThresholdVal = ProjectConstants.PSNR_THRESHOLD_MEDIUM;
      break;
    case 3:
      psnrThresholdVal = ProjectConstants.PSNR_THRESHOLD_HARD;
      break;
    }
    Log.d(TAG, "matchingDifficultyLevel = " + matchingDifficultyLevel);
    Log.d(TAG, "psnrThresholdVal = " + psnrThresholdVal);
    return psnrThresholdVal;
  }

  public static AlertDialog showSwapPhonesAlertDialog(Context context,
			Object obj, boolean isCaptureEventTrue, SharedPreferences sp) {

		staticObjectInstance = obj;
		isCaptureEvent = isCaptureEventTrue;
		staticSP = sp;

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		// set title
		alertDialogBuilder.setTitle(ProjectConstants.SWAP_TITLE);

		// set dialog message
		alertDialogBuilder
				.setMessage(
						isCaptureEventTrue ? ProjectConstants.CAPTURE_SWAP_MSG
								: ProjectConstants.MATCH_SWAP_MSG)
				.setCancelable(false)
				.setPositiveButton(ProjectConstants.START,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								staticSP.edit()
										.putBoolean(
												ProjectConstants.IS_SWAP_ALERT_DIALOG_SHOWN,
												false).commit();
								if (isCaptureEvent) {
									((Connection) staticObjectInstance)
											.startCaptureActivity();
								} else {
									((CaptureImage) staticObjectInstance)
											.startMatchActivity();
								}
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		return alertDialog;
	}

	/**
	 * Given the start time in secs, returns the time elapsed in secs
	 * 
	 * @param startTime
	 * @return
	 */
	public static int getTimeElapsed(int startTime) {
		int currTime = (int) System.currentTimeMillis() / 1000;
		return currTime - startTime;
	}

	/**
	 * Increments the state of the Single phone mode game and returns it
	 * 
	 * @param sp
	 * @return
	 */
	public static int nextState(SharedPreferences sp) {
		int currState = sp.getInt(ProjectConstants.SINGLE_PHONE_CURR_STATE, 1);
		int nextState = currState + 1;
		Log.d(TAG, "currState = " + currState);
		Log.d(TAG, "nextState = " + nextState);
		sp.edit().putInt(ProjectConstants.SINGLE_PHONE_CURR_STATE, nextState)
				.commit();
		return nextState;
	}

	/**
	 * Returns the message to be displayed on the alert dialog depending on the
	 * current state of the single phone mode
	 * 
	 * @param currState
	 * @return
	 */
	public static String getSinglePhoneDialogMsg(int currState) {
		String msg = "";
		switch (currState) {
		case ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE:
			msg = ProjectConstants.SINGLE_PHONE_P1_CAPTURE_MSG;
			break;
		case ProjectConstants.SINGLE_PHONE_P2_CAPTURE_STATE:
			msg = ProjectConstants.SINGLE_PHONE_P2_CAPTURE_MSG;
			break;
		case ProjectConstants.SINGLE_PHONE_P1_MATCH_STATE:
			msg = ProjectConstants.SINGLE_PHONE_P1_MATCH_MSG;
			break;
		case ProjectConstants.SINGLE_PHONE_P2_MATCH_STATE:
			msg = ProjectConstants.SINGLE_PHONE_P2_MATCH_MSG;
			break;
		}
		return msg;
	}

	/**
	 * Returns the title to be displayed on the alert dialog depending on the
	 * current state of the single phone mode
	 * 
	 * @param currState
	 * @return
	 */
	public static String getSinglePhoneDialogTitle(int currState) {
		String title = "";
		if (ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE == currState
				|| ProjectConstants.SINGLE_PHONE_P2_CAPTURE_STATE == currState) {
			title = ProjectConstants.CAPTURE_TITLE;
		} else {
			title = ProjectConstants.MATCH_TITLE;
		}
		return title;
	}

	/**
	 * Show alert dialog to proceed to CaptureImage or MatchImage activity
	 * depending on the current state of the game
	 * 
	 * @param context
	 * @param currentState
	 */
	public static AlertDialog showSinglePhoneDialog(Activity context, int currentState, SharedPreferences sp) {
		staticContext = context;
		staticActivity = context;
		currState = currentState;
		staticSP = sp;
		Log.d(TAG, "inside showSinglePhoneDialog(), currState = " + currState);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);
		// set title
		alertDialogBuilder.setTitle(getSinglePhoneDialogTitle(currState));
		// set dialog message
		alertDialogBuilder
				.setMessage(getSinglePhoneDialogMsg(currState))
				.setCancelable(false)
				.setPositiveButton(ProjectConstants.START,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
								Intent i;
								if (ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE == currState
										|| ProjectConstants.SINGLE_PHONE_P2_CAPTURE_STATE == currState) {
									i = new Intent(staticContext,
											CaptureImage.class);
								} else {
									i = new Intent(staticContext,
											MatchImage.class);

									// set start time for matching activity
									SharedPreferences projPreferences = staticContext
											.getSharedPreferences(
													ProjectConstants.FINAL_PROJECT,
													Context.MODE_PRIVATE);
									projPreferences
											.edit()
											.putInt(ProjectConstants.START_TIME,
													(int) System
															.currentTimeMillis() / 1000)
											.commit();
								}
								i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
								staticContext.startActivity(i);
								
								staticSP.edit()
								.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN,
										false).commit();
							}
						})
				.setNegativeButton(ProjectConstants.END_GAME,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {
								quitDialog = showCustomQuitDialog(staticActivity,true,false);
								quitDialog.show();
							}
						});

		AlertDialog alertDialog = alertDialogBuilder.create();
		return alertDialog;
	}
	
	public static Dialog showCustomQuitDialog(Activity context,boolean isSinglePhoneModeOn, boolean isWaitingEventOn){
		staticContext = context;
		final boolean isSinglePhoneMode = isSinglePhoneModeOn; 
		final boolean isWaitingMode = isWaitingEventOn; 
 		final Dialog quitDialog = new Dialog(context);
		quitDialog.setContentView(R.layout.final_proj_custom_quit_dialog);
		quitDialog.setTitle(ProjectConstants.QUIT_TITLE);

		// set the custom dialog components - text, image and button
		TextView text = (TextView) quitDialog.findViewById(R.id.final_proj_quit_dialog_textview);
		text.setText(ProjectConstants.QUIT_GAME_MESSAGE);

		Button yesButton = (Button) quitDialog.findViewById(R.id.final_proj_quit_dialog_yes_button);
		// if button is clicked, close the custom dialog
		yesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quitDialog.dismiss();
				Intent mainMainActivity = new Intent(
						staticContext, Home.class);
				mainMainActivity
						.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				staticContext.startActivity(mainMainActivity);
				/*if(!isSinglePhoneMode){	
					Editor editor = staticSP.edit();
		    	    editor.putBoolean(ProjectConstants.IS_MY_GAME_OVER, true);
		    	    editor.putBoolean(ProjectConstants.IS_OPPONENT_GAME_OVER, false);
		    	    editor.commit();
				}*/
			}
		});
		
		Button noButton = (Button) quitDialog.findViewById(R.id.final_proj_quit_dialog_no_button);
		// if button is clicked, close the custom dialog
		noButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quitDialog.cancel();
				if(isSinglePhoneMode){
					AlertDialog singlePhoneDialog = showSinglePhoneDialog(staticActivity, currState, staticSP);
					singlePhoneDialog.show();
				}else{
					Editor editor = staticSP.edit();
					if(isWaitingMode){
						editor.putBoolean(ProjectConstants.IS_WAITING_ALERT_DIALOG_SHOWN, true);
					}
					editor.commit();
				}
			}
		});
		
		return quitDialog;
	}
	
	public static boolean isQuitDialogShown(){
		if(quitDialog == null){
			return false;
		}else{
			return quitDialog.isShowing();
		}
	}

	public static void dismissQuitDialog() {
		quitDialog.dismiss();
		
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
