package cn.demomaster.quickdatabaselibrary.sql;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public abstract class SqlCreator implements SqlCreatorInterFace{
    public SQLiteOpenHelper sqLiteHelper;

    public SqlCreator(SQLiteOpenHelper sqLiteHelper) {
        this.sqLiteHelper = sqLiteHelper;
    }

    public SQLiteDatabase getDb() {
        return sqLiteHelper.getReadableDatabase();
    }
}
