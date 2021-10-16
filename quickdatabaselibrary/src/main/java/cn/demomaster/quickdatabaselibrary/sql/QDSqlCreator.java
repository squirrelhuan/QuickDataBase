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
            StringBuilder stringBuilder1 = new StringBuilder();
            stringBuilder1.append("(");
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("(");
            boolean isf1 = true;
            for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                TableColumn tableColumn = tableInfo.getTableColumns().get(i);
                if (!tableColumn.getSqlObj().constraints().autoincrement()) {
                    stringBuilder1.append ((isf1 ? "" : ","))
                    .append(tableColumn.getColumnName());
                    stringBuilder2.append(isf1 ? "?" : ",?");
                    isf1 = false;
                }
            }
            stringBuilder1.append(")");
            stringBuilder2.append(")");

            String sql = "insert into " + tableInfo.getTableName() + stringBuilder1.toString() + "values" + stringBuilder2.toString();
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
    public boolean delete(String tableName,ContentValues contentValues) {
        try {
               StringBuilder stringBuilder = new StringBuilder();
               if(contentValues!=null&&contentValues.size()>0) {
                   stringBuilder.append("delete from ")
                           .append(tableName)
               .append(" where ");
                   boolean b = true;
                   for (String key : contentValues.keySet()) {
                       stringBuilder.append((b ? " " : " and "))
                   .append(key )
                   .append("=" )
                   .append(contentValues.get(key));
                       b = false;
                   }
                   System.out.println("sql="+stringBuilder.toString());
                   getDb().execSQL(stringBuilder.toString());
               }else {
                   System.out.println("delete 条件语句限制请使用execDeleteSQL");
                   return false;
               }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean delete(Object model) {
        TableInfo tableInfo = TableHelper.getTableInfo(model);
        try {
            if (tableInfo != null && tableInfo.getTableColumns() != null && tableInfo.getTableColumns().size() > 0) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("delete from ")
            .append(tableInfo.getTableName())
            .append(" where ");
                boolean b = true;
                for (TableColumn tableColumn : tableInfo.getTableColumns()) {
                    if (tableColumn.getValueObj() != null) {
                        Field field = tableColumn.getField();
                        //boolean accessFlag = field.isAccessible();
                        field.setAccessible(true);
                        String valueStr = tableColumn.getValueSql();
                        //field.setAccessible(accessFlag);
                        stringBuilder.append ((b ? " " : " and ") )
                    .append(tableColumn.getColumnName() )
                    .append("=" )
                    .append(valueStr);
                        b = false;
                    }
                }
                System.out.println("sql="+stringBuilder.toString());
                getDb().execSQL(stringBuilder.toString());
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
            StringBuilder stringBuilder1 = new StringBuilder();
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append(" where ");
            boolean isf1 = true;
            for (int i = 0; i < tableInfo.getTableColumns().size(); i++) {
                TableColumn tableColumn = tableInfo.getTableColumns().get(i);
                if (!tableColumn.getSqlObj().constraints().autoincrement()) {
                    stringBuilder1.append ((isf1 ? "" : ",") )
                .append(tableColumn.getColumnName())
                .append("=")
                .append(tableColumn.getValueSql());
                    isf1 = false;
                }
                if(tableColumn.getSqlObj().constraints().primaryKey()){
                    stringBuilder2.append(tableColumn.getColumnName())
                .append("=")
                .append(tableColumn.getValueSql());
                }
            }

            String sql = "update " + tableInfo.getTableName() +" set "+ stringBuilder1.toString()+ stringBuilder2.toString();
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
        //TableInfo tableInfo = TableHelper.getTableInfo(clazz);
        //String[] params = new String[]{"*"};
        //String whereParams = TableHelper.generateWhereParams(tableInfo);
       // return TableHelper.generateModels(tableInfo, params, sql, clazz, true);
        return TableHelper.generateModels2(sql, clazz, true);
    }


}
