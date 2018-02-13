package win.kevink.wejump;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class OverlapLayout extends RelativeLayout {

    Context mContext;
    int mDensity = 480;
    int mWidth;
    int mHeight;
    boolean isMoving = false;
    Handler handler = new Handler();
    Point mPoint;

    public OverlapLayout(Context context) {
        super(context);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.overlap_layout, this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE){
            beginAnimator();
        }
        return true;
    }

    void beginAnimator() {
        isMoving = true;
        View line;
        if (mPoint.x * 2 < mWidth) {
            line = findViewById(R.id.right_line);
            findViewById(R.id.left_line).setVisibility(INVISIBLE);
        } else {
            line = findViewById(R.id.left_line);
            findViewById(R.id.right_line).setVisibility(INVISIBLE);
        }
        float leng = 2 / ((float) Math.sqrt(3)) * Math.abs(mWidth - 2 * mPoint.x);
        float ratio = 1;
        float time = leng * ratio;
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(line, "translationX", mPoint.x, mWidth - mPoint.x);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(line, "translationY", mPoint.y, mPoint.y - leng / 2);
        animatorX.setDuration((int) time);
        animatorY.setDuration((int) time);
        animatorX.setInterpolator(new LinearInterpolator());
        animatorX.start();
        animatorY.start();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.left_line).setVisibility(VISIBLE);
                findViewById(R.id.right_line).setVisibility(VISIBLE);
                setCoordinate(mPoint);
                isMoving = false;
            }
        }, (int) time);
    }

    public void setCoordinate(Point point) {
        mPoint = point;
//        int offset = (int)(25 / 160.0f * mDensity);
        View leftLine = findViewById(R.id.left_line);
        View rightLine = findViewById(R.id.right_line);
        leftLine.setTranslationX(point.x);
        leftLine.setTranslationY(point.y);
        rightLine.setTranslationX(point.x);
        rightLine.setTranslationY(point.y);
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) leftLine.getLayoutParams();
//        params.leftMargin = point.x - offset;
//        params.topMargin = point.y;
//        leftLine.setLayoutParams(params);
    }
}
