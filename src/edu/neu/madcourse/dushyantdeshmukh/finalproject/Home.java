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
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;

public class Home extends Activity implements OnClickListener {

	private static final String TAG = "HOME ACIVITY";
	Context context;
	AlertDialog alertDialog;
	ImageButton dualPhoneModeButton, singlePhoneModeButton, settingsButton,
			exitGameButton;
	boolean isDualPhoneModeSelected = false;
	private boolean isSinglePhoneDialogShown = false;
	private AlertDialog singlePhoneDialog;
	private SharedPreferences projPreferences;
	boolean showTutorial;
	SoundHelper soundHelper;

	public Home() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.final_proj_home);

		context = this;
		projPreferences = getSharedPreferences();
		soundHelper = new SoundHelper(projPreferences);

		// Set up click listeners for all the buttons
		dualPhoneModeButton = (ImageButton) findViewById(R.id.final_proj_dual_phone_mode_button);
		dualPhoneModeButton.setOnClickListener(this);

		singlePhoneModeButton = (ImageButton) findViewById(R.id.final_proj_single_phone_mode_button);
		singlePhoneModeButton.setOnClickListener(this);

		settingsButton = (ImageButton) findViewById(R.id.final_proj_settings_button);
		settingsButton.setOnClickListener(this);

		exitGameButton = (ImageButton) findViewById(R.id.final_proj_exit_game_button);
		exitGameButton.setOnClickListener(this);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d(TAG, "Inside onSaveInstance()");
		projPreferences
				.edit()
				.putBoolean(ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN,
						isSinglePhoneDialogShown).commit();
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		Log.d(TAG, "Inside onRestoreInstance()");
		isSinglePhoneDialogShown = projPreferences.getBoolean(
				ProjectConstants.IS_SINGLE_PHONE_DIALOG_SHOWN, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "Inside onPause()");
		soundHelper.stopMusic();

		if (singlePhoneDialog != null) {
			singlePhoneDialog.dismiss();
		}

		if (Util.isQuitDialogShown()) {
			Util.dismissQuitDialog();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "Inside onResume()");
		soundHelper.playBgMusic(context);

		if (isSinglePhoneDialogShown) {
			singlePhoneDialog = Util.showSinglePhoneDialog(this,
					ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,
					projPreferences);
			singlePhoneDialog.show();
			isSinglePhoneDialogShown = true;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.final_proj_single_phone_mode_button:
			initiateGameInSinglePhoneMode();
			if (showTutorial) {
				Intent tutorialIntent = new Intent(this, Tutorial.class);
				startActivity(tutorialIntent);
			} else {
				singlePhoneDialog = Util.showSinglePhoneDialog(this,
						ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE,
						projPreferences);
				singlePhoneDialog.show();
				isSinglePhoneDialogShown = true;
			}
			break;
		case R.id.final_proj_dual_phone_mode_button:
			initiateGameInDualPhoneMode();
			if (showTutorial) {
				Intent tutorialIntent = new Intent(this, Tutorial.class);
				startActivity(tutorialIntent);
			} else {
				Intent dualPhoneIntent = new Intent(this, Connection.class);
				startActivity(dualPhoneIntent);
			}
			break;
		case R.id.final_proj_settings_button:
			startActivity(new Intent(this, Prefs.class));
			break;
		case R.id.final_proj_exit_game_button:
			finish();
			break;
		}
	}

	/**
	 * Initializes vars in shared preferences and starts a game in single phone
	 * mode
	 */
	private void initiateGameInSinglePhoneMode() {
		Editor e = projPreferences.edit();
		e.putBoolean(ProjectConstants.IS_SINGLE_PHONE_MODE, true);
		e.putInt(ProjectConstants.SINGLE_PHONE_CURR_STATE,
				ProjectConstants.SINGLE_PHONE_P1_CAPTURE_STATE);
		e = setPrefSettings(e);
		e.commit();
		Log.d(TAG,
				"isSinglePhoneMode = "
						+ projPreferences.getBoolean(
								ProjectConstants.IS_SINGLE_PHONE_MODE, false));
	}

	/**
	 * Initializes vars in shared preferences and starts a game in dual phone
	 * mode
	 */
	private void initiateGameInDualPhoneMode() {
		Editor e = projPreferences.edit();
		e.putBoolean(ProjectConstants.IS_SINGLE_PHONE_MODE, false);
		e.putBoolean(ProjectConstants.IS_OPPONENT_GAME_OVER, false);
		e.putBoolean(ProjectConstants.IS_OPPONENT_GAME_OVER, false);
		e.putInt(ProjectConstants.PLAYER_IMAGE_COUNT, 0);
		e = setPrefSettings(e);
		e.commit();
		Log.d(TAG,
				"isSinglePhoneMode = "
						+ projPreferences.getBoolean(
								ProjectConstants.IS_SINGLE_PHONE_MODE, false));
	}

	/**
	 * Fetches user preferences from settings and sets them in shared preference
	 * 
	 * @param e
	 * @return
	 */
	private Editor setPrefSettings(Editor e) {
		Log.d(TAG,
				"Reading preference settings and putting in sared preferences:");
		boolean musinOn = Prefs.getMusic(this);
		showTutorial = Prefs.getShowTutorial(this);
		int noOfImgs = Prefs.getNoOfImgs(this);
		int matchingDifficulty = Prefs.getMatchingDifficultyLevel(this);

		e.putBoolean(ProjectConstants.PREF_MUSIC_ON, musinOn);
		e.putBoolean(ProjectConstants.PREF_SHOW_TUTORIAL, showTutorial);
		e.putInt(ProjectConstants.PREF_TOTAL_NO_OF_IMAGES, noOfImgs);
		e.putInt(ProjectConstants.PREF_MATCHING_DIFFICULTY, matchingDifficulty);

		Log.d(TAG, "ProjectConstants.PREF_MUSIC_ON = " + musinOn);
		Log.d(TAG, "ProjectConstants.PREF_SHOW_TUTORIAL = " + showTutorial);
		Log.d(TAG, "ProjectConstants.PREF_TOTAL_NO_OF_IMAGES = " + noOfImgs);
		Log.d(TAG, "ProjectConstants.PREF_MATCHING_DIFFICULTY = "
				+ matchingDifficulty);

		return e;
	}

	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(ProjectConstants.FINAL_PROJECT,
				Context.MODE_PRIVATE);
	}

}
