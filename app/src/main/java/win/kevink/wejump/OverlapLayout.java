package win.kevink.wejump;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
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
            Toast.makeText(mContext, "Touch event detected",
                    Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void setCoordinate(Point point) {
        mPoint = point;
        int offset = (int)(50 / 160.0f * mDensity);
        View leftLine = findViewById(R.id.left_line);
        View rightLine = findViewById(R.id.right_line);

//        View line = leftLine;
//        line.setLeft(point.x - offset);
//        line.setTop(point.y);
//        line = rightLine;
//        line.setLeft(point.x - offset);
//        line.setTop(point.y);
        View line = leftLine;
        line.setLeft(100);
        line.setTop(100);
    }
}
