package uci.uciproxy.ui.ui;

import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import uci.uciproxy.R;
import uci.uciproxy.Utils;
import uci.uciproxy.service.service.NTLMProxyService;
import uci.uciproxy.ui.Security.Encripter;
import uci.uciproxy.ui.ui.fontAwesome.DrawableAwesome;
import uci.uciproxy.ui.ui.tabs.NotificationTab;
import uci.uciproxy.ui.ui.tabs.PreferencesTab;
import uci.uciproxy.ui.ui.tabs.UserInfoTab;
import uci.uciproxy.user.User;

public class UCIntlmDialog extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String UPDATE_QUOTA_STATE = "update quota state";
    public static final int TABS_NUMBER = 3;
    public static int LIGHT_THEME = 0;
    public static int DARK_THEME = 1;
    public int themeId;

    private UserInfoTab userInfoTab;
    private PreferencesTab preferencesTab;
    private NotificationTab notificationTab;

    private User user;
    private QuotaUpdateReceiver quotaUpdateReceiver;


    private CustomPageAdapter pageAdapter;
    private ViewPager viewPager;
    private FloatingActionButton fab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chargeTheme();
        setContentView(R.layout.app_bar_main);
        initUi();
        loadConf();
        quotaUpdateReceiver = new QuotaUpdateReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilterUpdateQuotaState = new IntentFilter(UPDATE_QUOTA_STATE);
        registerReceiver(quotaUpdateReceiver, intentFilterUpdateQuotaState);

        //used to configure the form when it is restarted
        //if closed by the system
        if (isNTLMProxyServiceRunning(this)) {
            disableAll();
        } else {
            enableAll();
        }
    }


    @Override
    protected void onDestroy() {
        Log.e("state", "onDestroy");
        unregisterReceiver(quotaUpdateReceiver);
        super.onDestroy();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.e(getClass().getName(), "onTrimMemory called");
        super.onTrimMemory(level);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == notificationTab.ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                notificationTab.bubbleCheck.setChecked(false);
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //no menu needed at this time
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about_us) {
            AlertDialog alertDialog = new AlertDialog.Builder(UCIntlmDialog.this).create();
            if (themeId == this.DARK_THEME) {
                alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(UCIntlmDialog.this, R.style.AlertDialogCustom)).create();
            }
            alertDialog.setTitle(getResources().getString(R.string.createdBy));
            alertDialog.setMessage("Daniel A. Rodriguez Caballero" + "\n" +
                    "Miguel Morciego Varona\n" +
                    "Reinier Suarez Estevez");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            alertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void chargeTheme() {
        SharedPreferences settings = getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        themeId = settings.getInt("theme", LIGHT_THEME);
        if (themeId == LIGHT_THEME) {
            setTheme(R.style.AppTheme_NoActionBar);
        } else if (themeId == DARK_THEME) {
            setTheme(R.style.DarkTheme_NoActionBar);
        }
    }

    private int fetchPrimaryColor() {
        TypedValue typedValue = new TypedValue();
        TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    private void initUi() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(getApplicationContext().getResources().getDrawable(R.mipmap.ic_launcher));
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));

        //this is a very ugly solution for set the icons color
        int iconsColor;
        if (themeId == UCIntlmDialog.this.DARK_THEME) {
            iconsColor = Color.WHITE;
        } else {
            iconsColor = fetchPrimaryColor();
        }
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setIcon(new DrawableAwesome(R.string.fa_user, 35,
                iconsColor, false, false, 0, 0, 0, 0, this)));
        tabLayout.addTab(tabLayout.newTab().setIcon(new DrawableAwesome(R.string.fa_wrench, 35,
                iconsColor, false, false, 0, 0, 0, 0, this)));
        tabLayout.addTab(tabLayout.newTab().setIcon(new DrawableAwesome(R.string.fa_exclamation_triangle, 35,
                iconsColor, false, false, 0, 0, 0, 0, this)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        userInfoTab = new UserInfoTab(this);
        userInfoTab.buttonClean.setTextColor(iconsColor);
        preferencesTab = new PreferencesTab(this);
        notificationTab = new NotificationTab(this);

        pageAdapter = new CustomPageAdapter(TABS_NUMBER);
        pageAdapter.addView(userInfoTab.rootView);
        pageAdapter.addView(preferencesTab.rootView);
        pageAdapter.addView(notificationTab.rootView);
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pageAdapter);
        viewPager.setOffscreenPageLimit(TABS_NUMBER);


        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void loadConf() {
        SharedPreferences settings = getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        user = new User(settings.getString("user", ""), Encripter.decrypt(settings.getString("password", "")));
        user.name = settings.getString("name", "");
        user.usedQuota = Integer.parseInt(settings.getString("usedQuota", "0"));
        user.quota = Integer.parseInt(settings.getString("quota", "0"));
        userInfoTab.username.setText(user.username);
        userInfoTab.pass.setText(user.password);
        userInfoTab.nameTextView.setText(user.name);
        userInfoTab.quotaStateTextView.setText(String.valueOf((int) user.usedQuota));
        userInfoTab.assignedQuotaTextView.setText(String.valueOf((int) user.quota));
        String userLogo = user.username + "Logo";
        String logoPath = settings.getString(userLogo, "");
        if (!logoPath.equals("")) {
            Bitmap bm = Utils.loadPictureInFile(logoPath);
            user.logo = bm;
            userInfoTab.userLogo.setImageBitmap(user.logo);
        }

        preferencesTab.domain.setText(settings.getString("domain", "uci.cu"));
        preferencesTab.server.setText(settings.getString("server", "10.0.0.1"));
        preferencesTab.inputport.setText(settings.getString("inputport", "8080"));
        preferencesTab.outputport.setText(settings.getString("outputport", "8080"));
        preferencesTab.bypass.setText(settings.getString("bypass", "127.0.0.1,localhost,*uci.cu"));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ((CheckBox) preferencesTab.globalCheckBox).setChecked(settings.getBoolean("global_proxy", true));
        }

        preferencesTab.spinnerTheme.setSelection(themeId);
        if (userInfoTab.username.getText().toString().equals("")) {
            userInfoTab.username.requestFocus();
        } else {
            userInfoTab.pass.requestFocus();
        }

        //se cargan las configuraciones relativas a la consulta de cuota
        notificationTab.notificationsDelay.setText(String.valueOf(settings.getInt("notificationDelay", 1)));
        notificationTab.toastCheck.setChecked(settings.getBoolean("notificationToast", false));
        notificationTab.bubbleCheck.setChecked(settings.getBoolean("notificationBubble", false));
        notificationTab.notificationCheck.setChecked(settings.getBoolean("notificationNotification", true));

    }

    private void saveConf() {
        SharedPreferences settings = getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        Editor editor = settings.edit();
        editor.putString("user", user.username);
        editor.putString("password",
                Encripter.encrypt(user.password));
        editor.putString("quota", String.format("%.0f", user.quota));
        editor.putString("usedQuota", String.format("%.0f", user.usedQuota));
        editor.putString("name", user.name);

        editor.putString("domain", preferencesTab.domain.getText().toString());
        editor.putString("server", preferencesTab.server.getText().toString());
        editor.putString("inputport", preferencesTab.inputport.getText().toString());
        editor.putString("outputport", preferencesTab.outputport.getText().toString());
        editor.putString("bypass", preferencesTab.bypass.getText().toString());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            editor.putBoolean("global_proxy", ((CheckBox) preferencesTab.globalCheckBox).isChecked());
        }

        editor.putInt("notificationDelay", Integer.parseInt(notificationTab.notificationsDelay.getText().toString()));
        editor.putBoolean("notificationToast", notificationTab.toastCheck.isChecked());
        editor.putBoolean("notificationBubble", notificationTab.bubbleCheck.isChecked());
        editor.putBoolean("notificationNotification", notificationTab.notificationCheck.isChecked());
        editor.apply();
    }


    @SuppressLint("NewApi")
    private void disableAll() {
        //set the form to disable all fields and change the button to stop the service
        userInfoTab.username.setEnabled(false);
        userInfoTab.pass.setEnabled(false);
        userInfoTab.buttonClean.setEnabled(false);
        preferencesTab.domain.setEnabled(false);
        preferencesTab.server.setEnabled(false);
        preferencesTab.inputport.setEnabled(false);
        preferencesTab.outputport.setEnabled(false);
        preferencesTab.spinnerTheme.setEnabled(false);
        preferencesTab.bypass.setEnabled(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            preferencesTab.globalCheckBox.setEnabled(false);
        } else {
            preferencesTab.wiffiSettingsButton.setEnabled(false);
        }

        //TODO: falta para cuando es mayor que LOLLIPOP

        fab.setImageDrawable(new DrawableAwesome(R.string.fa_stop, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));

        //se desabilitan los elementos visuales relativos a la consulta de cuota
        notificationTab.notificationsDelay.setEnabled(false);
        notificationTab.notificationCheck.setEnabled(false);
        notificationTab.toastCheck.setEnabled(false);
        notificationTab.bubbleCheck.setEnabled(false);
    }

    @SuppressLint("NewApi")
    private void enableAll() {
        //set the form to introduce data and start the service
        userInfoTab.username.setEnabled(true);
        userInfoTab.pass.setEnabled(true);
        userInfoTab.buttonClean.setEnabled(true);
        preferencesTab.domain.setEnabled(true);
        preferencesTab.server.setEnabled(true);
        preferencesTab.inputport.setEnabled(true);
        preferencesTab.outputport.setEnabled(true);
        preferencesTab.spinnerTheme.setEnabled(true);
        preferencesTab.bypass.setEnabled(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            preferencesTab.globalCheckBox.setEnabled(true);
        } else {
            preferencesTab.wiffiSettingsButton.setEnabled(true);
        }

        fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));

        //se habilitan los elementos visuales relativos a la consulta de cuota
        notificationTab.notificationsDelay.setEnabled(true);
        notificationTab.notificationCheck.setEnabled(true);
        notificationTab.toastCheck.setEnabled(true);
        notificationTab.bubbleCheck.setEnabled(true);
    }

    public void clickRun(View arg0) {
        viewPager.setCurrentItem(0, true);

        if (preferencesTab.domain.getText().toString().equals("")
                || preferencesTab.server.getText().toString().equals("")
                || preferencesTab.inputport.getText().toString().equals("")
                || preferencesTab.outputport.getText().toString().equals("")
                || notificationTab.notificationsDelay.getText().toString().equals("")//validacion correspondiente a la consulta de cuota, hay que insertar un tiempo de delay obligatorio mientras que se seleccione la opcion de lanzar notificacions
                || userInfoTab.username.getText().toString().equals("")
                || userInfoTab.pass.getText().toString().equals("")) {

            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.nodata),
                    Toast.LENGTH_SHORT).show();
            fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));
            return;
        }

        if (!isNTLMProxyServiceRunning(this)) {
            if(notificationTab.notificationsDelay.getText().toString().equals("0")){
                notificationTab.notificationsDelay.setText("1");
            }

            user.username = userInfoTab.username.getText().toString();
            user.password = userInfoTab.pass.getText().toString();
            UserUpdater userUpdater = new UserUpdater();
            userUpdater.execute();


        } else {
            Intent proxyIntent = new Intent(this, NTLMProxyService.class);
            stopService(proxyIntent);

            if (isQuotaHeadServiceRunning(this)) {
                Intent quotaHeadServiceIntent = new Intent(this, QuotaHeadService.class);
                stopService(quotaHeadServiceIntent);
            }

            enableAll();

//            UCIntlmWidget.actualizarWidget(this.getApplicationContext(),
//                    AppWidgetManager.getInstance(this.getApplicationContext()),
//                    "off");
        }
    }

    public void taskComplete() {
        if (user.networkError) {
            Toast.makeText(getApplicationContext(), getString(R.string.networkError), Toast.LENGTH_LONG).show();
            fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));
            return;
        }

        if (!user.authenticated) {
            Toast.makeText(getApplicationContext(), getString(R.string.credentialsError), Toast.LENGTH_LONG).show();
            fab.setImageDrawable(new DrawableAwesome(R.string.fa_play, 35, Color.WHITE, false, false, 0, 0, 0, 0, this));
            return;
        }

        Intent proxyIntent = new Intent(this, NTLMProxyService.class);
        Intent quotaHeadServiceIntent = new Intent(this, QuotaHeadService.class);

        initUserView();

        saveConf();
        proxyIntent.putExtra("user", user.username);
        proxyIntent.putExtra("pass", user.password);
        proxyIntent.putExtra("domain", preferencesTab.domain.getText().toString());
        proxyIntent.putExtra("server", preferencesTab.server.getText().toString());
        proxyIntent.putExtra("inputport", preferencesTab.inputport.getText().toString());
        proxyIntent.putExtra("outputport", preferencesTab.outputport.getText().toString());
        proxyIntent.putExtra("notificationToast", notificationTab.toastCheck.isChecked());
        proxyIntent.putExtra("notificationBubble", notificationTab.bubbleCheck.isChecked());
        proxyIntent.putExtra("notificationNotification", notificationTab.notificationCheck.isChecked());
        proxyIntent.putExtra("delay", Integer.parseInt(notificationTab.notificationsDelay.getText().toString()) * 60);
        proxyIntent.putExtra("bypass", preferencesTab.bypass.getText().toString());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            proxyIntent.putExtra("set_global_proxy", preferencesTab.globalCheckBox.isChecked());
        } else {
            proxyIntent.putExtra("set_global_proxy", false);
        }

        startService(proxyIntent);
        if (notificationTab.bubbleCheck.isChecked()) {
            quotaHeadServiceIntent.putExtra("USED_QUOTA", user.usedQuota);
            quotaHeadServiceIntent.putExtra("QUOTA", user.quota);
            quotaHeadServiceIntent.putExtra("THEME", themeId);
            startService(quotaHeadServiceIntent);
        }

        disableAll();

//        UCIntlmWidget.actualizarWidget(this.getApplicationContext(),
//                AppWidgetManager.getInstance(this.getApplicationContext()),
//                "on");

    }

    private void initUserView() {
        userInfoTab.userLogo.setImageBitmap(user.logo);
        userInfoTab.nameTextView.setText(user.name);
        userInfoTab.quotaStateTextView.setText(String.format("%.0f", user.usedQuota));
        userInfoTab.assignedQuotaTextView.setText(String.format("%.0f", user.quota));
    }

    public void clickShowPassword(View arg0) {
        //show password
        if (userInfoTab.checkBoxShowPassword.isChecked()) {
            userInfoTab.pass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        } else {
            userInfoTab.pass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }
        userInfoTab.pass.setSelection(userInfoTab.pass.length());
    }


    private class QuotaUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("broad", "asd");
            if (intent.getAction().equals(UPDATE_QUOTA_STATE)) {
                double usedQuota = intent.getDoubleExtra("USED_QUOTA", 0.0);
                updateQuotaView(usedQuota);
            }
        }
    }

    private void updateQuotaView(double usedQuota) {
        userInfoTab.quotaStateTextView.setText(String.format("%.0f", usedQuota));
    }

    private class UserUpdater extends AsyncTask {

        private ProgressDialog dialog = (UCIntlmDialog.this.themeId == UCIntlmDialog.this.DARK_THEME) ?
                new ProgressDialog(new ContextThemeWrapper(UCIntlmDialog.this, R.style.ProgressBarCustom)) :
                new ProgressDialog(UCIntlmDialog.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.dialog.setMessage(getString(R.string.progresDialogMessage));
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Object o) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            taskComplete();
        }

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Utils.enableSSLSocket();
                user.update();
                if (user.authenticated) {
                    SharedPreferences settings = getSharedPreferences("UCIntlm.conf",
                            Context.MODE_PRIVATE);
                    String logoPath = settings.getString(user.username + "Logo", "");
                    if (logoPath.equals("")) {
                        user.downloadLogo(getApplicationContext());
                        if (user.logo != null) {
                            logoPath = getApplicationContext().getFilesDir() + File.separator + user.username + ".png";
                            Editor editor = settings.edit();
                            editor.putString(user.username + "Logo", logoPath);
                            editor.commit();
                            Utils.savePictureInFile(logoPath, user.logo);
                        }
                    } else {
                        user.logo = Utils.loadPictureInFile(logoPath);
                    }
                }
            } catch (IOException e1) {
                user.networkError = true;
                Log.e("IOException", e1.getMessage());
                return e1;
            } catch (NetworkErrorException e) {
                user.networkError = true;
                Log.e("NetworkErrorException", e.getMessage());
                e.printStackTrace();
                return e;
            } catch (XmlPullParserException e1) {
                e1.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private static boolean isNTLMProxyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (NTLMProxyService.class.getName().equals(
                    service.service.getClassName())) {
                Log.i(UCIntlmDialog.class.getName(), "Service running");
                return true;
            }
        }
        Log.i(UCIntlmDialog.class.getName(), "Service not running");
        return false;
    }


    private static boolean isQuotaHeadServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (QuotaHeadService.class.getName().equals(
                    service.service.getClassName())) {
                Log.i(UCIntlmDialog.class.getName(), "Service running");
                return true;
            }
        }
        Log.i(UCIntlmDialog.class.getName(), "Service not running");
        return false;
    }

}

