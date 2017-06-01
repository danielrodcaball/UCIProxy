package uci.uciproxy.ui.ui.tabs;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import uci.uciproxy.R;
import uci.uciproxy.ui.Security.Encripter;
import uci.uciproxy.ui.ui.fontAwesome.ButtonAwesome;

/**
 * Created by daniel on 23/02/17.
 */

public class UserInfoTab {

    public Context context;
    public View rootView;

    public CheckBox checkBoxShowPassword;
    public ImageView userLogo;
    public TextView nameTextView;
//    public ToggleButton startButton;
    public EditText username;
    public EditText pass;
    public TextView quotaStateTextView;
    public TextView assignedQuotaTextView;
    public ButtonAwesome buttonClean;

    public UserInfoTab(Context context) {
        this.context = context;
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        rootView = layoutInflater.inflate(R.layout.user_info_tab, null);
        loadUi();
    }

    private void loadUi(){
        quotaStateTextView = (TextView) rootView.findViewById(R.id.quotaStateTextView);
        checkBoxShowPassword = (CheckBox) rootView.findViewById(R.id.checkBoxPass);
        userLogo = (ImageView) rootView.findViewById(R.id.userLogo);
        nameTextView = (TextView) rootView.findViewById(R.id.nameTextView);
        username = (EditText) rootView.findViewById(R.id.euser);
        pass = (EditText) rootView.findViewById(R.id.epass);
        assignedQuotaTextView = (TextView) rootView.findViewById(R.id.assignedQuotaTextView);
        buttonClean = (ButtonAwesome) rootView.findViewById(R.id.buttonClean);
        buttonClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("user", "");
                editor.putString("password","");
                editor.putString("name","None");
                editor.apply();
                nameTextView.setText("None");
                username.setText("");
                pass.setText("");
            }
        });
    }
}
