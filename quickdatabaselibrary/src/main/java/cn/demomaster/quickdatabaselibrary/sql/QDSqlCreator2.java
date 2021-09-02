package cn.demomaster.quickdatabaselibrary.sql;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.List;

import cn.demomaster.quickdatabaselibrary.TableHelper;
import cn.demomaster.quickdatabaselibrary.model.TableColumn;
import cn.demomaster.quickdatabaselibrary.model.TableInfo;

public class QDSqlCreator2 {

    public void insertList(List objectList) {
        SQLiteDatabase db = null;
        try {
            db.beginTransaction();
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
                db.insert(tableInfo.getTableName(), null, values);
                // System.out.println("index=" + i);
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    public void insertList3(List objectList) {
        long startTime = System.currentTimeMillis();
        SQLiteDatabase db = null;
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
