package com.yakubov.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.getcapacitor.JSObject;

import org.json.JSONObject;

public class SharedPrefManager {

  private static final String STE_KEY = "last_step_data_key";
  private static final String STE_COUNT_KEY = "last_step_count_data_key";

  private Context context;

  public SharedPrefManager(Context context) {
    this.context = context;
  }

  public void clearAll() {
    SharedPreferences settings = context.getSharedPreferences("prefs", 0);
    settings.edit().clear().apply();
  }
  public void saveSteps(int count) {
    SharedPreferences settings = context.getSharedPreferences("prefs", 0);
    SharedPreferences.Editor editore = settings.edit();
    editore.putInt(STE_COUNT_KEY, count);
    editore.apply();
    Log.e("testtest-shared", count+"");
  }

  public int getLastNumberOfSteps() {
    SharedPreferences settings = context.getSharedPreferences("prefs", 0);
    return settings.getInt(STE_COUNT_KEY, 0);
  }

  public void save(String data) {
    SharedPreferences settings = context.getSharedPreferences("prefs", 0);
    SharedPreferences.Editor editore = settings.edit();
    editore.putString(STE_KEY, data);
    editore.apply();
  }

  public String getData() {
    SharedPreferences settings = context.getSharedPreferences("prefs", 0);
    return settings.getString(STE_KEY, "");
  }


}
