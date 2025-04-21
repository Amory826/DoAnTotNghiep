package dev.edu.doctorappointment.Model;

import android.content.Context;
import android.content.SharedPreferences;

public class UserData {
    private SharedPreferences sharedPreferences;

    public UserData(Context context) {
        sharedPreferences = context.getSharedPreferences("UserData", Context.MODE_PRIVATE);
    }

    public void saveData(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getData(String key) {
        return sharedPreferences.getString(key, "");
    }

    public void clearData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void removeData(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }
}
