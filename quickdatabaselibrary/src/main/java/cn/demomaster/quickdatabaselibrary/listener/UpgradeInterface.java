package cn.demomaster.quickdatabaselibrary.listener;

import android.database.sqlite.SQLiteDatabase;

public interface UpgradeInterface {
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
