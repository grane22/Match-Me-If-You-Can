package edu.neu.madcourse.dushyantdeshmukh.finalproject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import edu.neu.madcourse.dushyantdeshmukh.R;

public class Prefs extends PreferenceActivity {
  // Option names and default values
  private static final String OPT_MUSIC = "music";
  private static final boolean OPT_MUSIC_DEFAULT = true;
  private static final String OPT_SHOW_TUTORIAL = "show_tutorial";
  private static final boolean OPT_SHOW_TUTORIAL_DEFAULT = true;
  private static final String OPT_NO_OF_IMGS = "no_of_imgs";
  private static final String OPT_NO_OF_IMGS_DEFAULT = "3";
  private static final String OPT_MATCHING_DIFF = "matching_difficulty";
  private static final String OPT_MATCHING_DIFF_DEFAULT = "2";
  SharedPreferences projPreferences;
  SoundHelper soundHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    projPreferences = getSharedPreferences(ProjectConstants.FINAL_PROJECT,
        Context.MODE_PRIVATE);
    soundHelper = new SoundHelper(projPreferences);
    
    addPreferencesFromResource(R.xml.final_proj_preferences);
  }

  @Override
  protected void onResume() {
    super.onResume();
    soundHelper.playBgMusic(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    soundHelper.stopMusic();
  }
  
  /**
   * Get the current value of the music option
   * 
   * @param context
   * @return
   */
  public static boolean getMusic(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        OPT_MUSIC, OPT_MUSIC_DEFAULT);
  }
  
  /**
   * Get the current value of the show tutorial option
   * 
   * @param context
   * @return
   */
  public static boolean getShowTutorial(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
        OPT_SHOW_TUTORIAL, OPT_SHOW_TUTORIAL_DEFAULT);
  }

  /**
   * Get the current value of the no of images option
   * 
   * @param context
   * @return
   */
  public static int getNoOfImgs(Context context) {
    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
        context).getString(OPT_NO_OF_IMGS, OPT_NO_OF_IMGS_DEFAULT));
  }

  /**
   * Get the current value of the matching difficulty level option
   * 
   * @param context
   * @return
   */
  public static int getMatchingDifficultyLevel(Context context) {
    return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
        context).getString(OPT_MATCHING_DIFF, OPT_MATCHING_DIFF_DEFAULT));
  }

}
