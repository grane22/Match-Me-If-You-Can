package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class SoundHelper {

  private static final String TAG = "SOUND HELPER";
  private static SharedPreferences projPreferences;
  private static MediaPlayer bgMediaPlayer, eventMediaPlayer;
  private static int bgResId = R.raw.final_proj_bg;
  private static int matchResId = R.raw.final_proj_match_bg;
  private static int captureResId = R.raw.final_proj_capture;
  private static int matchSuccessResId = R.raw.final_proj_match_success;
  private static int maychFailResId = R.raw.final_proj_match_fail;
  private static int gameFinishResId = R.raw.final_proj_game_finish;
  
  public SoundHelper(SharedPreferences projPref) {
    projPreferences = projPref;
  }

  /**
   * Check user's preference and returns true if music is set
   * @param context
   * @return
   */
  private boolean isMusicOn(Context context) {
    boolean isMusicOn = Prefs.getMusic(context);
    projPreferences.edit().putBoolean(ProjectConstants.PREF_MUSIC_ON, isMusicOn).commit();
    return isMusicOn;
  }

  /**
   * Play given music in loop
   * @param context
   * @param musicResId
   */
  void playMusic(Context context, int musicResId) {
    Log.d(TAG, "Playing bg music...");
    bgMediaPlayer = play(context, bgMediaPlayer, musicResId, true);
  }

  /**
   * Plays the given sound once
   * @param context
   * @param soundResId
   */
  void playSound(Context context, int soundResId) {
    eventMediaPlayer = play(context, eventMediaPlayer, soundResId, false);
  }
  
  ////////////////// STOP MUSIC /////////////////////
  
  /**
   * Stop the given looping music
   */
  void stopMusic() {
    Log.d(TAG, "Stopping bg music...");
    if (bgMediaPlayer != null) {
      bgMediaPlayer.release();
    }
  }
  
  ////////////// BACKGROUND MUSIC //////////////
  
  /**
   * Plays general background music of the game
   * @param context
   */
  void playBgMusic(Context context) {
    playMusic(context, bgResId);
  }

  /**
   * Plays background music for the Match activity
   * @param context
   */
  void playMatchMusic(Context context) {
    playMusic(context, matchResId);
  }
  
  //////////////// EVENT MUSIC //////////////
  
  /**
   * Plays the capture image sound
   * @param context
   */
  void playCaptureSound(Context context) {
    playSound(context, captureResId);
  }
  
  /**
   * Plays the match success sound
   * @param context
   */
  void playMatchSuccessSound(Context context) {
    playSound(context, matchSuccessResId);
  }
  
  /**
   * Plays the match fail sound
   * @param context
   */
  void playMatchFailSound(Context context) {
    playSound(context, maychFailResId);
  }
  
  /**
   * Plays the game finish sound
   * @param context
   */
  void playGameFinishSound(Context context) {
    playSound(context, gameFinishResId);
  }
  
  ////////////// PLAY //////////////
  
  /**
   * Plays the given sound using the given media player if the music is ON
   * @param context
   * @param mp
   * @param soundResId
   * @param loop
   * @return
   */
  public MediaPlayer play(Context context, MediaPlayer mp, int soundResId,
      boolean loop) {
    Log.d(TAG, "isMusicOn() = " + isMusicOn(context));
    if (isMusicOn(context)) {
      // Release any resources from previous MediaPlayer
      if (mp != null) {
        mp.release();
      }
      // Create a new MediaPlayer to play this sound
      mp = MediaPlayer.create(context, soundResId);
      mp.start();
      mp.setLooping(loop);
    }
    return mp;
  }
}
