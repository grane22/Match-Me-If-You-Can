package edu.neu.madcourse.dushyantdeshmukh.two_player_wordgame;

import java.util.HashSet;
import java.util.Iterator;

import edu.neu.madcourse.dushyantdeshmukh.R;
import edu.neu.madcourse.dushyantdeshmukh.utilities.Util;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {

    private static final String TAG = "Word Game";
    private final Game game;
    private float width; // width of one tile
    private float height; // height of one tile

    private final Rect selRect = new Rect();

    Paint bgPaint = new Paint();
    Paint gridlinePaint = new Paint();
    Paint tilePaint = new Paint();
    Paint selectedTilePaint = new Paint();
    Paint letterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private MediaPlayer mpBtnPress;
    private int btnPressResId = R.raw.button_press;

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.game = (Game) context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        bgPaint.setColor(getResources().getColor(R.color.board_background));
        gridlinePaint
                .setColor(getResources().getColor(R.color.board_gridlines));

        tilePaint.setColor(getResources().getColor(R.color.board_tile));
        selectedTilePaint.setColor(getResources().getColor(
                R.color.board_selected));

        letterPaint.setColor(getResources().getColor(R.color.board_letter));
        letterPaint.setStyle(Style.FILL);
        letterPaint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = (float) (w / (this.game.total_cols * 1.0));
        height = (float) (h / (this.game.total_rows * 1.0));
        // getRect(selX, selY, selRect);
        Log.d(TAG, "onSizeChanged: width " + width + ", height " + height);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Draw the background...

        canvas.drawRect(0, 0, getWidth(), getHeight(), this.bgPaint);
        // Draw the board...
        for (int i = 0; i < this.game.total_rows; i++) {
            canvas.drawLine(0, i * height, getWidth(), i * height,
                    this.gridlinePaint);
        }
        for (int i = 0; i < this.game.total_cols; i++) {
            canvas.drawLine(i * width, 0, i * width, getHeight(),
                    this.gridlinePaint);
        }

        // Draw the letters...
        for (int i = 0; i < this.game.total_rows; i++) {
            for (int j = 0; j < this.game.total_cols; j++) {
                char currChar = this.game.board[i][j];
                if (currChar != '\u0000') {
                    if (this.game.currSelections.contains(i + "," + j)) {
                        drawLetter(canvas, currChar, j * width, i * height,
                                true);
                    } else {
                        drawLetter(canvas, currChar, j * width, i * height,
                                false);
                    }
                }
            }
        }

        // Draw the selection...
    }

    private void drawLetter(Canvas canvas, char c, float x, float y,
            boolean selected) {
        // canvas.
        // Log.d(TAG, "Draw letter '" + c + "' at x:" + x + ", y:" + y);
        getRect(x, y, selRect);
        if (selected) {
//            Log.d(TAG, "selRect: " + selRect);
            canvas.drawRect(selRect, this.selectedTilePaint);
        } else {
            canvas.drawRect(selRect, this.tilePaint);
        }

        letterPaint.setTextAlign(Paint.Align.CENTER);
        // Draw the letter in the center of the tile
        FontMetrics fm = letterPaint.getFontMetrics();
        // Centering in X: use alignment (and X at midpoint)
        float x2 = this.width / 2;
        // Centering in Y: measure ascent/descent first
        float y2 = this.height / 2 - (fm.ascent + fm.descent) / 2;
        letterPaint.setTextSize(height * 0.75f);
        letterPaint.setTextScaleX(width / height);

        canvas.drawText("" + c, x + x2, y + y2, letterPaint);
    }

    private void getRect(float x, float y, Rect rect) {
        rect.set((int) (x), (int) (y), (int) (x + width), (int) (y + height));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return super.onTouchEvent(event);
        Util.playSound(this.game.getApplicationContext(), mpBtnPress, btnPressResId, false);
        if (!this.game.isPaused) {
            int xIndex = (int) (event.getX() / width);
            int yIndex = (int) (event.getY() / height);
            // Log.d(TAG, "Selecting letter at x:" + xIndex + ", y:" + yIndex);
            this.game.selectLetter(yIndex, xIndex);

            // Log.d(TAG, "onTouchEvent: x " + selX + ", y " + selY);
            // drawSelectedletter(event.getX(), event.getY());
            invalidateRect(xIndex, yIndex);
        }
        return true;
    }

    public void invalidateRect(int j, int i) {
        getRect((j * width), (i * height), selRect);
        invalidate(selRect);
    }

    public void inValidateMultipleRects(HashSet<String> rectList) {
        Iterator<String> iterator = rectList.iterator();
        while (iterator.hasNext()) {
            String tempStrArr[] = iterator.next().split(",");
            int i = Integer.parseInt(tempStrArr[0]);
            int j = Integer.parseInt(tempStrArr[1]);
            invalidateRect(j, i);
        }
    }
}
