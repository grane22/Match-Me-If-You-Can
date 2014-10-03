package edu.neu.madcourse.dushyantdeshmukh.utilities;

import edu.neu.madcourse.dushyantdeshmukh.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class MyButton extends Button {
  public MyButton(Context context) {
    super(context);
  }

  public MyButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    setCustomFont(context, attrs);
  }

  public MyButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    setCustomFont(context, attrs);
  }

  /**
   * Sets a font on a textview based on the custom com.my.package:font attribute
   * If the custom font attribute isn't found in the attributes nothing happens
   * 
   * @param textview
   * @param context
   * @param attrs
   */
  public void setCustomFont(Context context,
      AttributeSet attrs) {
    TypedArray a = context
        .obtainStyledAttributes(attrs, R.styleable.CustomFont);
    String font = a.getString(R.styleable.CustomFont_font);
    setCustomFont(font, context);
    a.recycle();
  }

  /**
   * Sets a font on a textview
   * 
   * @param textview
   * @param font
   * @param context
   */
  public void setCustomFont(String font,
      Context context) {
    if (font == null) {
      return;
    }
    Typeface tf = FontCache.get(font, context);
    if (tf != null) {
      setTypeface(tf);
    }
    setTextColor(getResources().getColor(R.color.final_proj_btn_fore_color));
    setBackgroundDrawable(getResources().getDrawable(R.drawable.mybutton));
   /* LayoutParams params = new LayoutParams(
            LayoutParams.WRAP_CONTENT,      
            LayoutParams.WRAP_CONTENT
    );
    params.setMargins(25,25,25,25);
    setLayoutParams(params);*/
  }
  
  
}