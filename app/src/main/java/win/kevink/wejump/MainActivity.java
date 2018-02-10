package win.kevink.wejump;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    boolean serviceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                if (!serviceStarted) {
                    startService(new Intent(MainActivity.this, MainService.class));
                } else {
                    stopService(new Intent(MainActivity.this, MainService.class));
                }
                switchFabState();
            }
        });
    }

    private void switchFabState() {
        if (!serviceStarted) {
            serviceStarted = true;
            ((FloatingActionButton) findViewById(R.id.fab))
                    .setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            serviceStarted = false;
            ((FloatingActionButton) findViewById(R.id.fab))
                    .setImageResource(android.R.drawable.ic_menu_send);
        }
    }

//    private static boolean isServiceRunning(Context context, String serviceName){
//        ActivityManager am = (ActivityManager) context
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> services = am.getRunningServices(1000);
//        for (ActivityManager.RunningServiceInfo info : services) {
//            String name = info.service.getClassName();
//            if (serviceName.equals(name)) {
//                return true;
//            }
//        }
//        return false;
//    }

}
