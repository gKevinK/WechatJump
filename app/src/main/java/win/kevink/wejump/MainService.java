package win.kevink.wejump;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainService extends Service {

    Handler handler = new Handler();

    Timer timer;

    public MainService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new RecognizeTask(), 0, 5000);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
        timer = null;
    }

    class RecognizeTask extends TimerTask {

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Toast",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}
