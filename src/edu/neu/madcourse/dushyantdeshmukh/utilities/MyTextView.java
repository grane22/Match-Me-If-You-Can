package edu.neu.madcourse.dushyantdeshmukh.utilities;

import edu.neu.madcourse.dushyantdeshmukh.R;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class MyTextView extends TextView {
   
  public MyTextView(Context context) {
    super(context);
  }

  public MyTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setFont(context);
  }

  public MyTextView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setFont(context);
  }

  private void setFont(Context context) {
     Typeface font = Typeface.createFromAsset(context.getAssets(), "font/COOPBL.TTF");
     setTypeface(font);
     setTextColor(getResources().getColor(R.color.final_proj_txt_fore_color));
     setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
  }
}