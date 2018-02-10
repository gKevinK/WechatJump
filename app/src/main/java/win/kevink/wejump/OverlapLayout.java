package win.kevink.wejump;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class OverlapLayout extends RelativeLayout {

    Context mContext;

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
}
