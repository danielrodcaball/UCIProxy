package uci.uciproxy.service.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import uci.uciproxy.R;
import uci.uciproxy.Utils;
import uci.uciproxy.ntlm.core.HttpForwarder;
import uci.uciproxy.ui.ui.UCIntlmDialog;


public class NTLMProxyService extends Service {
    /*
     * Este es el servicio que inicia el servidor
     * Permanece en el área de notificación
     * */
    private String user = "";
    private String pass = "";
    private String domain = "";
    private String server = "";
    private int inputport = 8080;
    private int outputport = 8080;
    private String bypass = "";
//    private ServerTask s;
    private HttpForwarder proxyThread;
    private boolean set_global_proxy;

    private int NOTIFICATION = 1337;

    private ScheduledExecutorService qtExecutor;
    private boolean notificationToast;
    private boolean notificationBubble;
    private boolean notificationNotification;
    private int delay;

    private ExecutorService executor;

    @Override
    public void onCreate() {
        executor = Executors.newSingleThreadExecutor();
        qtExecutor = Executors.newScheduledThreadPool(1);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        proxyThread.halt();
        executor.shutdown();
        qtExecutor.shutdown();
//        mNM.cancel(NOTIFICATION);

//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && wifiAutoConfigReceiver != null){
//            unregisterReceiver(wifiAutoConfigReceiver);
//        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && set_global_proxy) {
            Utils.unsetWifiProxySettings(this);
            Toast.makeText(this, getString(R.string.OnNoProxy), Toast.LENGTH_LONG).show();
        }

//        UCIntlmWidget.actualizarWidget(this.getApplicationContext(),
//                AppWidgetManager.getInstance(this.getApplicationContext()),
//                "off");
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (android.os.Debug.isDebuggerConnected()) {
            android.os.Debug.waitForDebugger();
        }

        if (intent.getExtras() == null) {
            Log.e(getClass().getName(), "Error starting service");
        }

        user = intent.getStringExtra("user");
        pass = intent.getStringExtra("pass");
        domain = intent.getStringExtra("domain");
        server = intent.getStringExtra("server");
        inputport = Integer.valueOf(intent.getStringExtra("inputport"));
        outputport = Integer.valueOf(intent.getStringExtra("outputport"));
        set_global_proxy = intent.getBooleanExtra("set_global_proxy", true);
        bypass = intent.getStringExtra("bypass");

        notificationToast = intent.getBooleanExtra("notificationToast", false);
        notificationBubble = intent.getBooleanExtra("notificationBubble", false);
        notificationNotification = intent.getBooleanExtra("notificationNotification", false);
        delay = intent.getIntExtra("delay", 1);

        System.out.println("global_proxy: " + String.valueOf(set_global_proxy));
        if (set_global_proxy) {
            Utils.setWifiProxySettings(this, outputport, "");
            Toast.makeText(this, getString(R.string.OnProxy), Toast.LENGTH_LONG).show();
        }

        Log.i(getClass().getName(), "Starting for user " + user + "@" + domain + ", server " + server + ", input port " + String.valueOf(inputport) + ", output port" + String.valueOf(outputport) + " and bypass string: " + bypass);

//        s = new ServerTask(user, pass, domain, server, inputport, outputport, bypass);
//        s.execute();

        try {
            proxyThread = new HttpForwarder(server, inputport, domain, user, pass, outputport, true, bypass);
        } catch (IOException e) {
            Log.e(getClass().getName(), "The proxy thread can not be started: "  + e.getMessage());
            return START_NOT_STICKY;
        }
        executor.execute(proxyThread);

        if (notificationToast || notificationBubble || notificationNotification) {
            QuotaTask quotaTask = new QuotaTask(this, user, pass, notificationToast, notificationNotification);
            qtExecutor.scheduleAtFixedRate(quotaTask, delay, delay, TimeUnit.SECONDS);
//            qtExecutor.scheduleAtFixedRate(quotaTask, 10, 10, TimeUnit.SECONDS);
        }

        notifyit();

        //START_REDELIVER_INTENT permite que si el sistema mata el servicio entonces cuando intenta reiniciarlo envia el mismo Intent que se envio para
        //iniciarlo por primera vez
//        return START_REDELIVER_INTENT;
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void notifyit() {
        /*
         * Este método asegura que el servicio permanece en el área de notificación
		 * */

        Intent i = new Intent(this, UCIntlmDialog.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);


        Notification notification = new Notification.Builder(this)
                .setContentTitle(getApplicationContext().getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(getApplicationContext().getString(R.string.notif2) + " " + user)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();

        notification.flags |= Notification.FLAG_NO_CLEAR;
        notification.priority = Notification.PRIORITY_MAX;

        startForeground(NOTIFICATION, notification);
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e(getClass().getName(), "onTrimMemory called");
        super.onTrimMemory(level);
    }
}
