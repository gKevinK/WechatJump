package win.kevink.wejump;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    Handler handler = new Handler();
    Binder binder = new Binder();

    Timer timer;

    OverlapLayout layout;

    int mResultCode;
    Intent mData;

    WindowManager wm;
    MediaProjectionManager mpm;
    MediaProjection mMP;
    ImageReader mIR;
    VirtualDisplay mVD;
    int mDensity;
    int mWidth;
    int mHeight;
    boolean mFirst = true;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        layout = new OverlapLayout(MainService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            removeOverlapLayer();
            timer.cancel();
            timer = null;
        }
        if (mVD != null) {
            mVD.release();
            mVD = null;
            mMP.stop();
        }
        Toast.makeText(getApplicationContext(), "服务已结束",
                Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    class RecognizeTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (layout != null && !layout.isMoving)
                        beginCapture();
                }
            });
        }
    }

    class Binder extends android.os.Binder {
        MainService getService() {
            return MainService.this;
        }

        boolean getState() {
            return timer != null;
        }
    }

    public void start(int resultCode, Intent data) {
        mResultCode = resultCode;
        mData = data;
        wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (timer == null) {
            try {
                addOverlapLayer();
                timer = new Timer();
                timer.scheduleAtFixedRate(new RecognizeTask(), 2000, 1000);
                Toast.makeText(getApplicationContext(), "服务已启动",
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                layout = null;
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    void addOverlapLayer() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        wm.addView(layout, params);
    }

    void removeOverlapLayer() {
        if (layout != null) {
            wm.removeView(layout);
            layout = null;
        }
    }

    private void prepare() {
        if (wm == null)
             wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (mMP == null) {
            mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMP = mpm.getMediaProjection(Activity.RESULT_OK, mData);
        }
        mDensity = getResources().getDisplayMetrics().densityDpi;
        mWidth = getResources().getDisplayMetrics().widthPixels;
//        mHeight = getResources().getDisplayMetrics().heightPixels;
        mHeight = getH();
        if (mVD == null) {
            mIR = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
            mVD = mMP.createVirtualDisplay("screen-mirror",
                    mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    mIR.getSurface(), null, null);
        }

        layout.mDensity = mDensity;
        layout.mWidth = mWidth;
        layout.mHeight = mHeight;
    }

    private int getH() {
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        try {
            @SuppressWarnings("unchecked")
            Method method = display.getClass().getMethod("getRealMetrics", DisplayMetrics.class);
            method.invoke(display, dm);
        } catch (Exception e) {
            display.getMetrics(dm);
        }
        return dm.heightPixels;
    }

    void beginCapture() {
        layout.setVisibility(View.INVISIBLE);
        prepare();

        if (mFirst) {
            mFirst = false;
            return;
        }
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                Image image = mIR.acquireLatestImage();
                int width = image.getWidth();
                int height = image.getHeight();
                final Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
                image.close();

                afterCapture(bitmap);
//            }
//        }, 200);
    }

    void afterCapture(Bitmap bitmap) {
        if (layout != null) {
            layout.setVisibility(View.VISIBLE);
            Point p = match(bitmap);
            layout.setCoordinate(p);
        }
    }

    boolean compare(int pixel) {
        int target = 0xFF35353B;
        int err = 6;
        return (Math.abs(Color.red(pixel) - Color.red(target)) <= err
                && Math.abs(Color.green(pixel) - Color.green(target)) <= err
                && Math.abs(Color.blue(pixel) - Color.blue(target)) <= err);
    }

    Point match(Bitmap bitmap) {
        for (int y = 600; y < bitmap.getHeight(); y = y + 2) {
            int l = 0, r = 0;
            for (int x = 100; x < bitmap.getWidth() - 100; x = x + 2) {
                int pixel = bitmap.getPixel(x, y);
                if (compare(pixel)) {
                    if (l == 0) l = x;
                    r = x;
                }
                if (l > 0)
                    return new Point((l + r) / 2, y + 192);
            }
        }
        int x = bitmap.getWidth() / 2;
        int y = bitmap.getHeight() / 2;
        return new Point(x, y);
    }

}
