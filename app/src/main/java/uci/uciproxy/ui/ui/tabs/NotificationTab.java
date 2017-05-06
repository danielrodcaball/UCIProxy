package uci.uciproxy.ui.ui.tabs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

import uci.uciproxy.R;

/**
 * Created by daniel on 26/02/17.
 */

public class NotificationTab {

    public Context context;
    public View rootView;

    public EditText notificationsDelay;
    public CheckBox notificationCheck;
    public CheckBox toastCheck;
    public CheckBox bubbleCheck;

    public static int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 5469;

    public NotificationTab(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootView = layoutInflater.inflate(R.layout.notifications_tab, null);
        loadUi();
    }

    private void loadUi() {
        notificationsDelay = (EditText) rootView.findViewById(R.id.notificationDelay);
        notificationCheck = (CheckBox) rootView.findViewById(R.id.notificationCheck);
        toastCheck = (CheckBox) rootView.findViewById(R.id.toastCheck);
        bubbleCheck = (CheckBox) rootView.findViewById(R.id.bubbleCheck);
        bubbleCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkPermission();
                }
            }
        });
        notificationCheck.setChecked(true);
}

    public void checkPermission() {
        Activity activity = (Activity) context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }
    }

}
