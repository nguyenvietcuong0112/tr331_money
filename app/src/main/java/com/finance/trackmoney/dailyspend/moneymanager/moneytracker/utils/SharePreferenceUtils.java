package com.finance.trackmoney.dailyspend.moneymanager.moneytracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.finance.trackmoney.dailyspend.moneymanager.moneytracker.model.TransactionModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class SharePreferenceUtils {
    private static final String AI_MONEY_NAME = "AI_MONEY_NAME";
    private static final String PREFS_NAME = "CurrencyPrefs";

    private static final String TRANSACTION_LIST_KEY = "transactionList";

    private static final String KEY_SELECTED_CURRENCY = "SelectedCurrency";

    private static final String PREF_NAME = "chat_prefs";
    private static final String KEY_MESSAGES = "messages";

    private static final String COUNTER_KEY = "counter_value";

    private static final String TRANSACTION_KEY = "transaction_data";
    private SharedPreferences.Editor editor;


    private static volatile SharePreferenceUtils instance;
    private SharedPreferences sharePreference;
    private final Gson gson;

    public SharePreferenceUtils(Context context) {
        sharePreference = context.getSharedPreferences(AI_MONEY_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        editor = sharePreference.edit();

    }

    public static SharePreferenceUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (SharePreferenceUtils.class) {
                if (instance == null) {
                    instance = new SharePreferenceUtils(context);
                }
            }
        }
        return instance;
    }


    public void saveTransaction(TransactionModel transaction) {
        List<TransactionModel> transactions = getTransactionList();
        transactions.add(transaction);
        saveTransactionList(transactions);
    }

    public TransactionModel getTransaction() {
        String json = sharePreference.getString(TRANSACTION_KEY, "");
        if (json.isEmpty()) {
            return null;
        }
        return new Gson().fromJson(json, TransactionModel.class);
    }

    public void saveTransactionList(List<TransactionModel> transactions) {
        SharedPreferences.Editor editor = sharePreference.edit();
        Gson gson = new Gson();
        String json = gson.toJson(transactions);
        editor.putString(TRANSACTION_LIST_KEY, json);
        editor.apply();
    }

    public List<TransactionModel> getTransactionList() {
        String json = sharePreference.getString(TRANSACTION_LIST_KEY, "");
        if (json.isEmpty()) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<TransactionModel>>() {
        }.getType();
        return new Gson().fromJson(json, type);
    }

    public static boolean isOrganic(Context context) {
        SharedPreferences pref = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        return pref.getBoolean("organic", true);
    }

    public static void setOrganicValue(Context context, boolean value) {
        SharedPreferences pre = context.getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pre.edit();
        editor.putBoolean("organic", value);
        editor.apply();
    }

    public int getCurrentValue() {
        return sharePreference.getInt(COUNTER_KEY, 0);
    }

    public void incrementCounter() {
        sharePreference.edit()
                .putInt(COUNTER_KEY, getCurrentValue() + 1)
                .apply();
    }

    public static void saveSelectedCurrencyCode(Context context, String code) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_SELECTED_CURRENCY, code).apply();
    }

    public static String getSelectedCurrencyCode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_SELECTED_CURRENCY, "");
    }


    // Phương thức lưu object bất kỳ
    public <T> void saveObject(String key, T object) {
        String json = gson.toJson(object);
        sharePreference.edit().putString(key, json).apply();
    }

    // Phương thức đọc object bất kỳ
    public <T> T getObject(String key, Class<T> classType) {
        String json = sharePreference.getString(key, null);
        if (json == null) {
            return null;
        }
        return gson.fromJson(json, classType);
    }

    // Phương thức đọc List object bất kỳ
    public <T> List<T> getListObject(String key, Class<T> classType) {
        String json = sharePreference.getString(key, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = TypeToken.getParameterized(List.class, classType).getType();
        return gson.fromJson(json, type);
    }

    // Các phương thức tiện ích
    public void saveBoolean(String key, boolean value) {
        sharePreference.edit().putBoolean(key, value).apply();
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return sharePreference.getBoolean(key, defaultValue);
    }

    public void saveString(String key, String value) {
        sharePreference.edit().putString(key, value).apply();
    }

    public String getString(String key, String defaultValue) {
        return sharePreference.getString(key, defaultValue);
    }

    public void saveInt(String key, int value) {
        sharePreference.edit().putInt(key, value).apply();
    }

    public int getInt(String key, int defaultValue) {
        return sharePreference.getInt(key, defaultValue);
    }

    public void saveLong(String key, long value) {
        sharePreference.edit().putLong(key, value).apply();
    }

    public long getLong(String key, long defaultValue) {
        return sharePreference.getLong(key, defaultValue);
    }

    // Xóa dữ liệu
    public void remove(String key) {
        sharePreference.edit().remove(key).apply();
    }

    public void clearAll() {
        sharePreference.edit().clear().apply();
    }
}
