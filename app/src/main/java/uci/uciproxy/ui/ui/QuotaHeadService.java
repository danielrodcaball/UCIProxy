package uci.uciproxy.ui.ui;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import uci.uciproxy.R;

import static uci.uciproxy.ui.ui.UCIntlmDialog.UPDATE_QUOTA_STATE;

/**
 * Created by daniel on 22/02/17.
 */

public class QuotaHeadService extends Service {

    private WindowManager windowManager;
    private View quotaHead;
    private QuotaUpdateReceiver quotaUpdateReceiver;
    private TextView tvQuotaState;
    private WindowManager.LayoutParams params;
    private int pixelsDeviceWidth;
    private int pixelsDeviceHeight;
    private GestureDetector gestureDetector;

    private int NOTIFICATION = 1234;


    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        quotaUpdateReceiver = new QuotaUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter(UPDATE_QUOTA_STATE);
        registerReceiver(quotaUpdateReceiver, intentFilter);
        gestureDetector = new GestureDetector(this, new SingleTapConfirm());

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater layoutInflater =
                (LayoutInflater) getBaseContext()
                        .getSystemService(LAYOUT_INFLATER_SERVICE);
        quotaHead = layoutInflater.inflate(R.layout.bubble_quota_layout, null);

        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelsDeviceWidth = size.x;
        pixelsDeviceHeight = size.y;

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        quotaHead.setOnTouchListener(new View.OnTouchListener() {
            int orgX, orgY;
            int offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)){
                    Intent i  = new Intent(getApplicationContext(), UCIntlmDialog.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return true;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        orgX = (int) event.getX();
                        orgY = (int) event.getY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        params.x = offsetX;
                        params.y = offsetY;
                        windowManager.updateViewLayout(v, params);
                        break;

                    case MotionEvent.ACTION_UP:
                        offsetX = (int) event.getRawX() - orgX;
                        offsetY = (int) event.getRawY() - orgY;
                        params.gravity = Gravity.TOP | Gravity.LEFT;
                        params.y = offsetY;
                        params.x = offsetX;
                        if (offsetX <= pixelsDeviceWidth / 2) {
                            while (offsetX > 0) {
                                params.x = --offsetX;
                                windowManager.updateViewLayout(v, params);
                            }
                        } else {
                            while (offsetX < pixelsDeviceWidth) {
                                params.x = ++offsetX;
                                windowManager.updateViewLayout(v, params);
                            }
                        }
                        break;
                }
                return true;
            }
        });

        tvQuotaState = (TextView) quotaHead.findViewById(R.id.quotaStateTextView);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = (pixelsDeviceHeight / 2) - (pixelsDeviceHeight / 4);
        windowManager.addView(quotaHead, params);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        double usedQuota = intent.getDoubleExtra("USED_QUOTA", 0);
        double quota = intent.getDoubleExtra("QUOTA", 0);
        double usedPercent = (usedQuota * 100) / quota;
        int theme = intent.getIntExtra("THEME", UCIntlmDialog.LIGHT_THEME);
        if (theme == UCIntlmDialog.LIGHT_THEME){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                tvQuotaState.setBackgroundDrawable(getResources().getDrawable(R.drawable.light_box));
            }
            else{
                tvQuotaState.setBackground(getResources().getDrawable(R.drawable.light_box));
            }
        }
        else if (theme == UCIntlmDialog.DARK_THEME){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
                tvQuotaState.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_box));
            }
            else{
                tvQuotaState.setBackground(getResources().getDrawable(R.drawable.dark_box));
            }
        }


        tvQuotaState.setText(String.format("%.0f", usedPercent) + "%");

        notifyit();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        if (quotaHead != null) windowManager.removeView(quotaHead);
        if (quotaUpdateReceiver != null) unregisterReceiver(quotaUpdateReceiver);
        super.onDestroy();
    }

    private class QuotaUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UPDATE_QUOTA_STATE)) {
                double usedQuotaPercent = intent.getDoubleExtra("USED_PERCENT", 0.0);
                Log.e("quota", usedQuotaPercent + "");
                String usedQuotaPercentString = String.format("%.0f", usedQuotaPercent);
                tvQuotaState.setText(usedQuotaPercentString + "%");
            }
        }
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }
    }

    public void notifyit() {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
		 * */
        Notification.Builder builder = new Notification.Builder(this)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_bubble_notification)
                .setContentText(getResources().getString(R.string.bubbleNotificationRunning))
                .setWhen(System.currentTimeMillis());

        Notification notification;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            notification = builder.getNotification();
        }
        else{
            notification = builder.build();
        }

        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(NOTIFICATION, notification);
    }

}
