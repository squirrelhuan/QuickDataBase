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
            getDb().execSQL(sql);
            return true;
        }
        return false;
    }

    @Override
    public boolean insertArray(List models) {
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
                if (!tableColumn.getSqlObj().constraints().autoincrement()) {
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
                int bindIndex = 0;
                for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                    if (!tableInfo.getTableColumns().get(i).getSqlObj().constraints().autoincrement()) {
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
                        String valueStr = tableColumn.getValueSql();
                        //field.setAccessible(accessFlag);
                        sql += (b ? " " : " and ") + tableColumn.getColumnName() + "=" + valueStr;
                        b = false;
                    }
                }
                getDb().execSQL(sql);
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
        return true;
    }

    @Override
    public <T> T modify(T model) {
        TableInfo tableInfo = TableHelper.getTableInfo(model);
        try {
            String value1 = "";
            String value2 = " where ";
            boolean isf1 = true;
            for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                TableColumn tableColumn = tableInfo.getTableColumns().get(i);
                if (!tableColumn.getSqlObj().constraints().autoincrement()) {
                    value1 += (isf1 ? "" : ",") + tableColumn.getColumnName()+"="+tableColumn.getValueSql();
                    isf1 = false;
                }
                if(tableColumn.getSqlObj().constraints().primaryKey()){
                    value2+=tableColumn.getColumnName()+"="+tableColumn.getValueSql();
                }
            }

            String sql = "update " + tableInfo.getTableName() +" set "+ value1;
            sql+= value2;
            getDb().execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean modifyArray(List models) {
        return false;
    }

    @Override
    public <T> T findOne(T model) {
        TableInfo tableInfo = TableHelper.getTableInfo(model);
        String[] params = new String[]{"*"};
        String whereParams = TableHelper.generateWhereParams(tableInfo);
        List<T> list = (List<T>) TableHelper.generateModels(tableInfo, params, whereParams, model.getClass(), false);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public <T> T findOne(String sql, Class<T> clazz) {
        List<T> list =  TableHelper.generateModels2(sql, clazz, false);
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
