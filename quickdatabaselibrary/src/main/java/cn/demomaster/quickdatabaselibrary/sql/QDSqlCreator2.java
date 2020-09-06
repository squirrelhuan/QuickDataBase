package cn.demomaster.quickdatabaselibrary.sql;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.util.List;

import cn.demomaster.quickdatabaselibrary.TableHelper;
import cn.demomaster.quickdatabaselibrary.model.TableColumn;
import cn.demomaster.quickdatabaselibrary.model.TableInfo;

public class QDSqlCreator2 extends SqlCreator {

    public QDSqlCreator2(SQLiteOpenHelper sqLiteHelper) {
        super(sqLiteHelper);
    }

    @Override
    public boolean insert(Object model) {
        String sql = TableHelper.generateInsertSql(model);
        if (!TextUtils.isEmpty(sql)) {
            System.out.println(sql);
            getDb().execSQL(sql);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertArray(List models) {
        long startTime = System.currentTimeMillis();
        try {
            if (models == null || models.size() < 1) {
                return false;
            }
            TableInfo tableInfo = TableHelper.getTableInfo( models.get(0).getClass());
            String value1 = "(";
            String value2 = "(";
            boolean isf1 = true;
            for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                TableColumn tableColumn = tableInfo.getTableColumns().get(i);
                value1 += (isf1 ? "" : ",") + tableColumn.getColumnName();
                value2 += (isf1 ? "?" : ",?");
                isf1 = false;
            }
            value1 += ")";
            value2 += ")";

            String sql = "insert into " + tableInfo.getTableName() + value1 + "values" + value2;
            SQLiteStatement stat = getDb().compileStatement(sql);
            getDb().beginTransaction();
            Field[] fields = models.get(0).getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
            }
            for (int j = 0; j < models.size(); j++) {
                Object obj = models.get(j);
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    stat.bindString(i + 1, field.get(obj) + "");
                }
                long result = stat.executeInsert();
            }
            getDb().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != getDb()) {
                    getDb().endTransaction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long t1 =endTime -startTime;
        System.out.println("批量写入，用时:"+t1);
        return true;
    }

    @Override
    public boolean delete(Object model) {
        return false;
    }

    @Override
    public <T> boolean deleteAll(Class<T> clazz) {
        return false;
    }

    @Override
    public <T> T modify(T model) {
        return null;
    }

    @Override
    public boolean modifyArray(List models) {
        return false;
    }

    @Override
    public <T> T findOne(T model) {
        return null;
    }

    @Override
    public <T> T findOne(String sql, Class<T> clazz) {
        return null;
    }

    @Override
    public <T> List<T> findArray(T model) {
        return null;
    }

    @Override
    public <T> List<T> findArray(String sql, Class<T> clazz) {
        return null;
    }

    public void insertList(List objectList) {
        try {
            getDb().beginTransaction();
            TableInfo tableInfo = null;
            for (int i = 0; i < objectList.size(); i++) {
                Object obj = objectList.get(i);
                if (tableInfo == null) {
                    tableInfo = TableHelper.getTableInfo(obj);
                }
                ContentValues values = new ContentValues();
                for (TableColumn tableColumn : tableInfo.getTableColumns()) {
                    if (tableColumn.getValueObj() != null) {
                        Field field = tableColumn.getField();
                        //boolean accessFlag = field.isAccessible();
                        field.setAccessible(true);
                        String valueStr = tableColumn.getField().get(obj) + "";
                        values.put(tableColumn.getColumnName(), valueStr);
                        //field.setAccessible(accessFlag);
                    }
                }
                getDb().insert(tableInfo.getTableName(), null, values);
                // System.out.println("index=" + i);
            }
            getDb().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getDb().endTransaction();
        }
    }

    public void insertList3(List objectList) throws Exception {
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = getDb();
        try {
            if (objectList == null || objectList.size() < 1) {
                return;
            }

            TableInfo tableInfo = TableHelper.getTableInfo( objectList.get(0).getClass());
            db.beginTransaction();
            ContentValues values = new ContentValues();
            for (int j = 0; j < objectList.size(); j++) {
                Object obj = objectList.get(j);
                for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                    Field field = tableInfo.getTableColumns().get(i).getField();
                    field.setAccessible(true);
                    values.put(tableInfo.getTableColumns().get(i).getColumnName(),field.get(obj)+"");
                }
                db.insert(tableInfo.getTableName(), null, values);
                values.clear();
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != db) {
                    db.endTransaction();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        long t1 =endTime -startTime;
        System.out.println("批量写入，用时:"+t1);
    }
}
