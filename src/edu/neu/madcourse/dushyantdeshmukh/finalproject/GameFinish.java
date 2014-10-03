package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class GameFinish extends Activity implements OnClickListener {

	protected static final String TAG = "GAME FINISH ACTIVITY";

	private TextView finalScoreText;
	private Button mainMenuButton;
	private SharedPreferences projPreferences;
	Context context;
	boolean isSinglePhoneMode;
	boolean isOpponentGameOver;
	int totalNoOfImgs;
	SoundHelper soundHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_proj_game_finish);

		context = this;
		projPreferences = getSharedPreferences();
		soundHelper = new SoundHelper(projPreferences);
		soundHelper.playGameFinishSound(context);

		totalNoOfImgs = Prefs.getNoOfImgs(this);
		isSinglePhoneMode = projPreferences.getBoolean(
				ProjectConstants.IS_SINGLE_PHONE_MODE, false);

		// Set up click listeners for all the buttons
		mainMenuButton = (Button) findViewById(R.id.final_proj_main_menu_button);
		mainMenuButton.setOnClickListener(this);
		finalScoreText = (TextView) findViewById(R.id.final_proj_show_result);

		projPreferences = getSharedPreferences();

		clearAllImages();

		//clearFlags();

	}

	

	/**
	 * delete all images captured during the game
	 */
	private void clearAllImages() {
		File mediaStorageDir = new File(
				context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				ProjectConstants.IMG_DIR_NAME);
		deleteRecursive(mediaStorageDir);
	}

	/**
	 * Recursively delete the given file or directory
	 * 
	 * @param fileOrDirectory
	 */
	private void deleteRecursive(File fileOrDirectory) {
		if (fileOrDirectory.isDirectory())
			for (File child : fileOrDirectory.listFiles()) {
				Log.d(TAG, "Deleting: " + child.getAbsolutePath());
				child.delete();
				deleteRecursive(child);
			}

		fileOrDirectory.delete();
	}

	@Override
	protected void onResume() {
		super.onResume();
		soundHelper.playBgMusic(context);
		showFinalResultToPlayer();
	}
	
	@Override
	public void onBackPressed(){
	}

	private void showFinalResultToPlayer() {
		Log.d(TAG, "Inside showFinalResultToPlayer(), isSinglePhoneMode = "
				+ isSinglePhoneMode);

		String resultMsg;
		String resultDetailsMsg;

		if (isSinglePhoneMode) {
			int p1Time = projPreferences.getInt(ProjectConstants.PLAYER_1_TIME,
					-1);
			int p2Time = projPreferences.getInt(ProjectConstants.PLAYER_2_TIME,
					-1);
			int p1ImageCount = projPreferences.getInt(
					ProjectConstants.PLAYER_1_IMAGE_COUNT, -1);
			int p2ImageCount = projPreferences.getInt(
					ProjectConstants.PLAYER_2_IMAGE_COUNT, -1);

			Log.d(TAG, "p1Time = " + p1Time);
			Log.d(TAG, "p2Time = " + p2Time);
			Log.d(TAG, "p1ImageCount = " + p1ImageCount);
			Log.d(TAG, "p2ImageCount = " + p2ImageCount);

			resultMsg = getSinglePhoneResultMsg(p1Time, p2Time, p1ImageCount,
					p2ImageCount);
			resultDetailsMsg = getResultDetailMsg("Player 1", "Player 2",
					p1Time, p2Time, p1ImageCount, p2ImageCount);
		} else {
			int playerTime = projPreferences.getInt(
					ProjectConstants.PLAYER_TIME, -1);
			int oppTime = projPreferences.getInt(
					ProjectConstants.OPPONENT_TIME, -1);
			int playerImageCount = projPreferences.getInt(
					ProjectConstants.PLAYER_IMAGE_COUNT, -1);
			int oppImageCount = projPreferences.getInt(
					ProjectConstants.OPPONENT_IMAGE_COUNT, -1);

			resultMsg = getDualPhoneResultMsg(playerTime, oppTime,
					playerImageCount, oppImageCount);
			resultDetailsMsg = getResultDetailMsg("You", "Your opponent",
					playerTime, oppTime, playerImageCount, oppImageCount);
		}
		finalScoreText.setText(resultMsg + "\n" + resultDetailsMsg);
	}

	@Override
	protected void onPause() {
		super.onPause();
		soundHelper.stopMusic();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.final_proj_main_menu_button:
			Intent mainMenuIntent = new Intent(context, Home.class);
			mainMenuIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(mainMenuIntent);
			break;
		}
	}

	private String getSinglePhoneResultMsg(int p1Time, int p2Time,
			int p1ImageCount, int p2ImageCount) {
		String resultMsg = "";
		if (p1ImageCount == p2ImageCount && p1Time == p2Time) {
			resultMsg = "Scores tied!";
		} else if (p1ImageCount > p2ImageCount) {
			resultMsg = "Player 1 Won!";
		} else if (p1ImageCount < p2ImageCount) {
			resultMsg = "Player 2 Won!";
		} else if (p1Time < p2Time) {
			resultMsg = "Player 1 Won!";
		} else {
			resultMsg = "Player 2 Won!";
		}
		return resultMsg;
	}

	private String getDualPhoneResultMsg(int playerTime, int oppTime,
			int playerImageCount, int oppImageCount) {
		String resultMsg = "";
		if (playerImageCount == oppImageCount && playerTime == oppTime) {
			resultMsg = "Scores tied!";
		} else if (playerImageCount > oppImageCount) {
			resultMsg = "You Won!";
		} else if (playerImageCount < oppImageCount) {
			resultMsg = "You Lost!";
		} else if (playerTime < oppTime) {
			resultMsg = "You Won!";
		} else {
			resultMsg = "You Lost!";
		}
		return resultMsg;
	}

	public String getResultDetailMsg(String p1Name, String p2Name, int p1Time,
			int p2Time, int p1ImageCount, int p2ImageCount) {
		String msg = p1Name + " matched " + p1ImageCount + " out of "
				+ totalNoOfImgs + " images in " + Util.getTimeStr(p1Time)
				+ " mins \n" + p2Name + " matched " + p2ImageCount
				+ " out of " + totalNoOfImgs + " images in "
				+ Util.getTimeStr(p2Time) + " mins";

		return msg;
	}

	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE);
	}
}