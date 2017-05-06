package uci.uciproxy.ui.ui.tabs;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import uci.uciproxy.R;
import uci.uciproxy.ui.ui.UCIntlmDialog;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by daniel on 23/02/17.
 */

public class PreferencesTab {

    public Context context;
    public View rootView;

    public EditText domain;
    public EditText server;
    public EditText inputport;
    public EditText outputport;
    public EditText bypass;
    public CheckBox globalCheckBox;
    public Spinner spinnerTheme;

    //V21
    public Button wiffiSettingsButton;

    public PreferencesTab(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
            rootView = layoutInflater.inflate(R.layout.preferences_tab_v21, null);
            loadUi21();
        } else {
            rootView = layoutInflater.inflate(R.layout.preferences_tab, null);
            loadUi();
        }
    }

    private void loadUi() {
        domain = (EditText) rootView.findViewById(R.id.edomain);
        server = (EditText) rootView.findViewById(R.id.eserver);
        inputport = (EditText) rootView.findViewById(R.id.einputport);
        outputport = (EditText) rootView.findViewById(R.id.eoutputport);
        bypass = (EditText) rootView.findViewById(R.id.ebypass);
        globalCheckBox = (CheckBox) rootView.findViewById(R.id.globCheckBox);
        spinnerTheme = (Spinner) rootView.findViewById(R.id.spinnerTheme);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.themes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);
        final AppCompatActivity appCompatActivity = (AppCompatActivity) context;
        final UCIntlmDialog ucIntlmDialog = (UCIntlmDialog) context;
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != ucIntlmDialog.themeId) {
                    SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("theme", position);
                    editor.commit();
                    appCompatActivity.recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void loadUi21() {
        wiffiSettingsButton = (Button) rootView.findViewById(R.id.wifiSettingsButton);
        wiffiSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                        Context.MODE_PRIVATE);
                if (settings.getBoolean("dontShowAgain", false)){
                    Intent i = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                    i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(i);
                }
                else {
                    createWifiAlertDialog().show();
                }
            }
        });

        domain = (EditText) rootView.findViewById(R.id.edomain);
        server = (EditText) rootView.findViewById(R.id.eserver);
        inputport = (EditText) rootView.findViewById(R.id.einputport);
        outputport = (EditText) rootView.findViewById(R.id.eoutputport);
        bypass = (EditText) rootView.findViewById(R.id.ebypass);
        spinnerTheme = (Spinner) rootView.findViewById(R.id.spinnerTheme);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context, R.array.themes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTheme.setAdapter(adapter);
        final AppCompatActivity appCompatActivity = (AppCompatActivity) context;
        final UCIntlmDialog ucIntlmDialog = (UCIntlmDialog) context;
        spinnerTheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != ucIntlmDialog.themeId) {
                    SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("theme", position);
                    editor.commit();
                    appCompatActivity.recreate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void startProxySettingsActivity() {
        Intent localIntent = new Intent("android.intent.action.MAIN");
        Object[] arrayOfObject1 = new Object[1];
        arrayOfObject1[0] = ("127.0.0.1:8080");
        try {
            context.startActivity(localIntent.putExtra("title", String.format("Establecer proxy del Sistema", arrayOfObject1)).putExtra("button-label", "Aceptar").setClassName("com.android.settings", "com.android.settings.ProxySelector"));
        } catch (Exception e) {
            Toast.makeText(context, context.getResources().getString(R.string.notFunctional), Toast.LENGTH_LONG).show();
            e.fillInStackTrace();
        }
    }

    private AlertDialog createWifiAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        if (((UCIntlmDialog)context).themeId == UCIntlmDialog.DARK_THEME)
            builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        LayoutInflater inflater = ((AppCompatActivity)context).getLayoutInflater();
        View view = inflater.inflate(R.layout.wifi_alert_dialog, null);
        ImageView wifiConfigImage = (ImageView) view.findViewById(R.id.wifiConfigImageView);
        PhotoViewAttacher mAtacher = new PhotoViewAttacher(wifiConfigImage);
        CheckBox dontShowCheckBox  = (CheckBox) view.findViewById(R.id.dontShowCheckBox);
        builder.setTitle(context.getResources().getString(R.string.wifiSettings));
        builder.setView(view);
        builder.setPositiveButton(R.string.wifiSettingsPositiveButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dontShowCheckBox.isChecked()){
                    SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean("dontShowAgain", true);
                    editor.commit();
                }
                Intent i = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(i);
            }
        });
        builder.setNegativeButton(R.string.wifiSettingsNegativeButton, (dialog, which) -> dialog.cancel());

        return builder.create();
    }
}