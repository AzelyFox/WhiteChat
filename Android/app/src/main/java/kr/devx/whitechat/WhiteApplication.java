package kr.devx.whitechat;

import android.app.Application;
import android.content.SharedPreferences;

import kr.devx.whitechat.Data.User;

public class WhiteApplication extends Application {

    public User User;
    private SharedPreferences appPreferences;

    public boolean IS_NOTIFICATION_FOREGROUND_ACTIVE = true;
    public boolean IS_NOTIFICATION_BACKGROUND_ACTIVE = true;

    @Override
    public void onCreate() {
        super.onCreate();
        appPreferences = getSharedPreferences("DATA", MODE_PRIVATE);
        IS_NOTIFICATION_FOREGROUND_ACTIVE = appPreferences.getBoolean("NOTIFICATION_FOREGROUND", true);
        IS_NOTIFICATION_BACKGROUND_ACTIVE = appPreferences.getBoolean("NOTIFICATION_BACKGROUND", true);
    }
}
