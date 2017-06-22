package uci.uciproxy.service.service;

import android.accounts.NetworkErrorException;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import uci.uciproxy.R;
import uci.uciproxy.Utils;
import uci.uciproxy.model.QuotaDataService;
import uci.uciproxy.ui.ui.UCIntlmDialog;
import uci.uciproxy.model.User;

/**
 * Created by daniel on 17/12/16.
 */
public class QuotaTask implements Runnable {

    private Context context;
    private String username;
    private String password;
    private int notificationNumber = 1;
    boolean notificationToast;
    boolean notificationNotification;

    public QuotaTask(Context context, String user, String pass, boolean notificationToast, boolean notificationNotification) {
        this.context = context;
        this.username = user;
        this.password = pass;
        this.notificationToast = notificationToast;
        this.notificationNotification = notificationNotification;
    }

    @Override
    public void run() {
        try {
            Log.i(getClass().getName(), "starting quota service");
            Utils.enableSSLSocket();
            QuotaDataService quotaDataService = QuotaDataService.getQuotaData(username, password);
            float usedCuota = quotaDataService.usedQuota;
            saveQuotaState(quotaDataService);
            float usedPercent = (usedCuota * 100) / quotaDataService.quota;
            showMessage(String.format("%.0f", usedPercent) + " " + context.getString(R.string.quotaSpent));
            Intent i = new Intent(UCIntlmDialog.UPDATE_QUOTA_STATE);
            i.putExtra("USED_QUOTA", usedCuota);
            i.putExtra("QUOTA", quotaDataService.quota);
            i.putExtra("NAVIGATION_LEVEL", quotaDataService.navigationLevel);
            i.putExtra("USED_PERCENT", usedPercent);
            context.sendBroadcast(i);

        } catch (IOException e) {
            showMessage(context.getString(R.string.networkError));
            Log.e(getClass().getName(), e.getMessage());
        } catch (XmlPullParserException e) {
            showMessage(context.getString(R.string.networkError));
            Log.e(getClass().getName(), e.getMessage());
        } catch (SecurityException e) {
            showMessage(context.getString(R.string.credentialsError));
            Log.e(getClass().getName(), e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            Log.e(getClass().getName(), e.getMessage());
        } catch (KeyManagementException e) {
            Log.e(getClass().getName(), e.getMessage());
        } catch (NetworkErrorException e) {
            showMessage(context.getString(R.string.networkError));
            Log.e(getClass().getName(), e.getMessage());
        }
    }

    private void saveQuotaState(QuotaDataService quotaDataService) {
        SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("quota", quotaDataService.quota);
        editor.putFloat("usedQuota", quotaDataService.usedQuota);
        editor.putString("navigationLevel", quotaDataService.navigationLevel);
        editor.apply();
    }

    private void showMessage(String message) {
        if (notificationToast) showToastInUiThread(message);
        if (notificationNotification) showNotification(message);
    }

    private void showNotification(String message) {
        NotificationCompat.Builder mBuilder =
                (NotificationCompat.Builder) new NotificationCompat.Builder(context)
                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setLargeIcon((((BitmapDrawable) getResources()
//                                .getDrawable(R.drawable.ic_launcher)).getBitmap()))
                        .setContentTitle("Consumo")
                        .setContentText(message)
                        .setNumber(notificationNumber++)
//                        .setContentInfo("consumo")
                        .setTicker(message);

//        Intent notIntent = new Intent(this, this.getClass());
//        PendingIntent contIntent = PendingIntent.getActivity(this, 0, notIntent, 0);
//        mBuilder.setContentIntent(contIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(12345);
        mNotificationManager.notify(12345, mBuilder.build());
    }

    private void showToastInUiThread(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

}



