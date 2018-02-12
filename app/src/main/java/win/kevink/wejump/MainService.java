package win.kevink.wejump;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

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
    VirtualDisplay mVD;
    int mDensity;
    int mWidth;
    int mHeight;

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
        removeOverlapLayer();
        timer.cancel();
        timer = null;
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
                timer.scheduleAtFixedRate(new RecognizeTask(), 5000, 3000);
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
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        mWidth = point.x;
        mHeight = point.y;
    }

    Point match(Bitmap bitmap) {
        int x = 0;
        int y = 0;
        return new Point(x, y);
    }

    void beginCapture() {
        layout.setVisibility(View.GONE);
        prepare();

        ImageReader imageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
        mVD = mMP.createVirtualDisplay("screen-mirror",
                mWidth, mHeight, mDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(), null, null);

        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();
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

                if (mVD != null) {
                    mVD.release();
                    mVD = null;
                }
                afterCapture(bitmap);
            }
        }, null);
    }

    void afterCapture(Bitmap bitmap) {
        // TODO
        Toast.makeText(getApplicationContext(), "Screenshot received " + bitmap.getWidth() + " " + bitmap.getHeight(),
                Toast.LENGTH_SHORT).show();

        Point p = match(bitmap);
        layout.setCoordinate(p);

        layout.setVisibility(View.VISIBLE);
    }

}
