package win.kevink.wejump;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_MEDIA_PROJECTION = 0x2893;

    boolean serviceStarted = false;
    MainService.Binder binder;
    ServiceConnection mSC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setEnabled(false);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if (serviceStarted) {
                    unbindService(mSC);
                    binder = null;
                    stopService(new Intent(MainActivity.this, MainService.class));
                    setServiceState(false);
                    return;
                }
                if (binder == null)
                    return;
                MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
            }
        });

        startMainService();
    }

    private void startMainService() {
        startMainService(false);
    }

    private void startMainService(final boolean begin) {
        mSC = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                binder = (MainService.Binder) service;
                if (begin) {
                    MediaProjectionManager mpm = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
                }
                setServiceState(binder.getState());
                findViewById(R.id.fab).setEnabled(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        startService(new Intent(MainActivity.this, MainService.class));
        bindService(new Intent(this, MainService.class), mSC, BIND_AUTO_CREATE);
    }

    private void setServiceState(boolean isStarted) {
        serviceStarted = isStarted;
        if (serviceStarted) {
            ((FloatingActionButton) findViewById(R.id.fab))
                    .setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            ((FloatingActionButton) findViewById(R.id.fab))
                    .setImageResource(android.R.drawable.ic_menu_send);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    try {
                        binder.getService().start(resultCode, data);
                        setServiceState(binder.getState());
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.toString(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "截屏请求被拒绝",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSC != null && binder != null) {
            unbindService(mSC);
            binder = null;
            if (serviceStarted == false)
                stopService(new Intent(MainActivity.this, MainService.class));
        }
    }
}
