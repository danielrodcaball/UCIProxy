package uci.uciproxy.ui.ui.tabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import uci.uciproxy.R;

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
//        startButton = (ToggleButton) rootView.findViewById(R.id.button1);
        username = (EditText) rootView.findViewById(R.id.euser);
        pass = (EditText) rootView.findViewById(R.id.epass);
        assignedQuotaTextView = (TextView) rootView.findViewById(R.id.assignedQuotaTextView);
    }
}
