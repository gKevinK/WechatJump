package win.kevink.wejump;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

public class OverlapLayout extends RelativeLayout {

    public OverlapLayout(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.overlap_layout, this);
    }
}
