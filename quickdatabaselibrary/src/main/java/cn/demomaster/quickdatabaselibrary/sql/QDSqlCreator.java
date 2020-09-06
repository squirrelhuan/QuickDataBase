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

public class QDSqlCreator extends SqlCreator {

    public QDSqlCreator(SQLiteOpenHelper sqLiteHelper) {
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
        SQLiteDatabase db = getDb();
        try {
            if (models == null || models.size() < 1) {
                return false;
            }
            TableInfo tableInfo = TableHelper.getTableInfo(models.get(0).getClass());
            String value1 = "(";
            String value2 = "(";
            boolean isf1 = true;
            for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                TableColumn tableColumn = tableInfo.getTableColumns().get(i);
                if(!tableColumn.getSqlObj().constraints().autoincrement()) {
                    value1 += (isf1 ? "" : ",") + tableColumn.getColumnName();
                    value2 += (isf1 ? "?" : ",?");
                    isf1 = false;
                }
            }
            value1 += ")";
            value2 += ")";

            String sql = "insert into " + tableInfo.getTableName() + value1 + "values" + value2;
            SQLiteStatement stat = db.compileStatement(sql);
            db.beginTransaction();
            for (int j = 0; j < models.size(); j++) {
                Object obj = models.get(j);
                int bindIndex =0;
                for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                    if(!tableInfo.getTableColumns().get(i).getSqlObj().constraints().autoincrement()) {
                        Field field = tableInfo.getTableColumns().get(i).getField();
                        field.setAccessible(true);
                        stat.bindString(bindIndex + 1, field.get(obj) + "");
                        bindIndex++;
                    }
                }
                long result = stat.executeInsert();
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
        long t1 = endTime - startTime;
        System.out.println("批量写入，用时:" + t1);
        return true;
    }

    @Override
    public boolean delete(Object model) {
        TableInfo tableInfo = TableHelper.getTableInfo(model);
        try {
            if (tableInfo != null && tableInfo.getTableColumns() != null && tableInfo.getTableColumns().size() > 0) {
                String sql = "delete from " + tableInfo.getTableName() + " where ";
                boolean b = true;
                for (TableColumn tableColumn : tableInfo.getTableColumns()) {
                    if (tableColumn.getValueObj() != null) {
                        Field field = tableColumn.getField();
                        //boolean accessFlag = field.isAccessible();
                        field.setAccessible(true);
                        String valueStr = "";
                        if (field.getType() == int.class) {
                            valueStr = tableColumn.getField().get(model) + "";
                        } else if (field.getType() == String.class) {
                            valueStr = "\"" + tableColumn.getField().get(model) + "\"";
                        }
                        //field.setAccessible(accessFlag);
                        sql += (b ? " " : " and ") + tableColumn.getColumnName() + "=" + valueStr;
                        b = false;
                    }
                }
                getDb().execSQL(sql);
                System.out.println("delete=>"+sql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public <T> boolean deleteAll(Class<T> clazz) {
        TableInfo tableInfo = TableHelper.getTableInfo(clazz);
        String sql = "delete from " + tableInfo.getTableName();//+" where 1=1";
        getDb().execSQL(sql);
        System.out.println("deleteAll=>"+sql);
        return true;
    }

    @Override
    public <T> T findOne(T model) {
        List<T> list = findArray(model);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> T findOne(String sql, Class<T> clazz) {
        List<T> list = findArray(sql, clazz);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> List<T> findArray(T model) {
        TableInfo tableInfo = TableHelper.getTableInfo(model);
        String[] params = new String[]{"*"};
        String whereParams = TableHelper.generateWhereParams(tableInfo);
        return (List<T>) TableHelper.generateModels(tableInfo, params, whereParams, model.getClass(), true);
    }

    @Override
    public <T> List<T> findArray(String sql, Class<T> clazz) {
        TableInfo tableInfo = TableHelper.getTableInfo(clazz);
        String[] params = new String[]{"*"};
        String whereParams = TableHelper.generateWhereParams(tableInfo);
        return TableHelper.generateModels(tableInfo, params, whereParams, clazz, true);
    }


}
