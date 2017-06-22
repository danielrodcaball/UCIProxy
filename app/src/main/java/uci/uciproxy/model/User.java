package uci.uciproxy.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import uci.uciproxy.R;
import uci.uciproxy.Utils;
import uci.uciproxy.ui.Security.Encripter;

/**
 * Created by daniel on 18/02/17.
 */

public class User {
    public String username;
    public String password;
    public String name;
    public Bitmap logo;
    public int quota;
    public float usedQuota;
    public String navigationLevel;

    public User(String username, String password, String name, Bitmap logo,
                int quota, float usedQuota, String navigationLevel) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.logo = logo;
        this.quota = quota;
        this.usedQuota = usedQuota;
        this.navigationLevel = navigationLevel;
    }

    public static User loadUser(Context context){
        SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        String username = settings.getString("user", "");
        String password = Encripter.decrypt(settings.getString("password", ""));
        String name = settings.getString("name", "");
        int quota = settings.getInt("quota", 0);
        float usedQuota = settings.getFloat("usedQuota",0);
        String navigationLevel = settings.getString("navigationLevel", "none");
        Bitmap userLogo = findUserLogo(context, username);
        if (userLogo == null) userLogo = BitmapFactory.decodeResource(context.getResources(), R.drawable.no_photo);
        return new User(username,password,name,userLogo,quota,usedQuota,navigationLevel);
    }

    public static Bitmap findUserLogo(Context context, String username){
        SharedPreferences settings = context.getSharedPreferences("UCIntlm.conf",
                Context.MODE_PRIVATE);
        Bitmap userLogo = null;
        String userLogoString = username + "Logo";
        String logoPath = settings.getString(userLogoString, "");
        if (!logoPath.equals("")) {
            userLogo = Utils.loadPictureInFile(logoPath);
        }
        return userLogo;
    }




}
