package win.kevink.wejump;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    Handler handler = new Handler();

    Timer timer;

    OverlapLayout layout;

    public MainService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        layout = new OverlapLayout(MainService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RecognizeTask(), 5000, 5000);
            try {
                addOverlapLayer();
            } catch (Exception e) {
                layout = null;
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
        Toast.makeText(getApplicationContext(), "服务已启动",
                Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        removeOverlapLayer();
        timer.cancel();
        timer = null;
        super.onDestroy();
    }

    class RecognizeTask extends TimerTask {
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // TODO
                    Toast.makeText(getApplicationContext(), "Toast",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void addOverlapLayer() {
        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
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
            WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            wm.removeView(layout);
            layout = null;
        }
    }

    Point match(Bitmap bitmap) {
        int x = 0;
        int y = 0;
        return new Point(x, y);
    }

    void beginCapture() {
        layout.setVisibility(View.GONE);
        startActivity(new Intent(MainService.this, ShotActivity.class));
    }

//    void capture2(Intent data) {
//        MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//        MediaProjection mp = mpm.getMediaProjection(Activity.RESULT_OK, data);
//        ImageReader ir = ImageReader.newInstance(
//                getScreenWidth(),
//                getScreenHeight(),
//                PixelFormat.RGB_888,
//                1);
//        mVirtualDisplay = mp.createVirtualDisplay("screen-mirror",
//                mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
//                ir.getSurface(), null, null);
//        Image image = mImageReader.acquireLatestImage();
//        int width = image.getWidth();
//        int height = image.getHeight();
//        final Image.Plane[] planes = image.getPlanes();
//        final ByteBuffer buffer = planes[0].getBuffer();
//        int pixelStride = planes[0].getPixelStride();
//        int rowStride = planes[0].getRowStride();
//        int rowPadding = rowStride - pixelStride * width;
//        Bitmap bitmap = Bitmap.createBitmap(width+rowPadding/pixelStride, height, Bitmap.Config.ARGB_8888);
//        bitmap.copyPixelsFromBuffer(buffer);
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0,width, height);
//        image.close();
//    }

    void afterCapture(Bitmap bitmap) {
        // TODO

        Point p = match(bitmap);
        layout.setCoordinate(p);

        layout.setVisibility(View.VISIBLE);
    }

}
