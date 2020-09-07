package cn.demomaster.quickdatabaselibrary;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.demomaster.quickdatabaselibrary.annotation.Constraints;
import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;
import cn.demomaster.quickdatabaselibrary.model.TableColumn;
import cn.demomaster.quickdatabaselibrary.model.TableInfo;

public class TableHelper {
    static QuickDbHelper mQuickDb;
    public  TableHelper(QuickDbHelper quickDb) {
        mQuickDb = quickDb;
    }

    /**
     * 根据实体类创建表
     *
     * @param clazz
     * @return
     * @throws ClassNotFoundException
     */
    public static String generateTableSql(Class clazz) throws Exception {
        return generateTableSql(clazz.getName());
    }

    public static String generateTableSql(String className) throws ClassNotFoundException, IllegalAccessException {
        Class<?> cl = Class.forName(className);
        TableInfo tableInfo = getTableInfo(null, cl);
        List<String> columnDefs = new ArrayList<>();
        for (TableColumn tableColumn : tableInfo.getTableColumns()) {
            if (tableColumn.getField().getType() == int.class) {
                columnDefs.add(tableColumn.getColumnName() + " INTEGER " + getConstraints(tableColumn.getSqlObj().constraints()));
            } else if (tableColumn.getField().getType() == String.class) {
                columnDefs.add(tableColumn.getColumnName() + " VARCHAR(" + tableColumn.getSqlObj().value() + ")" + getConstraints(tableColumn.getSqlObj().constraints()));
            }
        }

        StringBuilder createCommand = new StringBuilder(
                "CREATE TABLE if not exists " + tableInfo.getTableName() + "(");
        boolean isFirst = true;
        for (String columnDef : columnDefs) {
            createCommand.append((isFirst ? " " : " ,") + columnDef);
            isFirst = false;
        }
        createCommand.append(");");
        String tableCreate = createCommand.toString();
        return tableCreate;
    }

    /**
     * 判断该字段是否有其他约束
     *
     * @param con
     * @return
     */
    private static String getConstraints(Constraints con) {
        String constraints = "";
        if (con.primaryKey()) {
            constraints += " PRIMARY KEY";
            if(con.autoincrement()){
                constraints += " AUTOINCREMENT";
            }
            constraints += " NOT NULL";
            //constraints += " UNIQUE";
        } else {
            if (!con.allowNull()) {
                constraints += " NOT NULL";
            }
            if (con.unique()) {
                constraints += " UNIQUE";
            }
        }
        return constraints;
    }

    public static String generateInsertSql(Object obj) {
        if (obj == null) {
            return null;
        }
        TableInfo tableInfo = getTableInfo(obj, obj.getClass());
        /*INSERT INTO TABLE_NAME (column1, column2, column3,...columnN) VALUES (value1, value2, value3,...valueN);*/
        StringBuilder insertCommand = new StringBuilder("INSERT INTO " + tableInfo.getTableName() + " ");

        StringBuilder keyStr = new StringBuilder("(");
        StringBuilder valueStr = new StringBuilder(" VALUES(");
        boolean isFirst = true;
        for (TableColumn entry : tableInfo.getTableColumns()) {
            if (entry.getValueObj() != null) {
                if(!entry.getSqlObj().constraints().autoincrement()) {
                    String value1 = entry.getValueSql();
                    if(!TextUtils.isEmpty(value1)) {
                        keyStr.append((isFirst ? "" : ",") + "\"" + entry.getColumnName() + "\"");
                        valueStr.append((isFirst ? "" : ",") + value1);
                        isFirst = false;
                    }
                }
            }
        }
        keyStr.append(")");
        valueStr.append(");");
        insertCommand.append(keyStr);
        insertCommand.append(valueStr);
        return insertCommand.toString();
    }

    /**
     * 生成条件查询语句
     * @param tableInfo
     * @param <T>
     * @return
     */
    public static <T> String generateWhereParams(TableInfo tableInfo) {
        if (tableInfo != null) {
            boolean isfirst = true;
            String whereStr = null;
            for (TableColumn tableColumn : tableInfo.getTableColumns()) {
                if (tableColumn.getValueObj() != null) {
                    if (whereStr == null) {
                        whereStr = "";
                    }
                    String valueStr = tableColumn.getValueSql();
                    if(!TextUtils.isEmpty(valueStr)) {
                        whereStr += (isfirst ? "" : " and ") + tableColumn.getColumnName() + "=" + valueStr;
                        isfirst = false;
                    }
                }
            }
            return whereStr;
        }
        return null;
    }

    public static <T> List<T> findModelList(Object obj, Class<T> clazz) throws IllegalAccessException {
        TableInfo tableInfo = getTableInfo(obj, clazz);
        return findModels(tableInfo, clazz, true);
    }

    public static <T> T findModelOne(Object obj, Class<T> clazz) throws IllegalAccessException {
        TableInfo tableInfo = getTableInfo(obj, clazz);
        List<T> list = findModels(tableInfo, clazz, false);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public static <T> List<T> findModels(TableInfo tableInfo, Class<T> clazz, boolean isArray){
        String[] params = new String[]{"*"};
        String whereParams = generateWhereParams(tableInfo);
        return generateModels(tableInfo, params, whereParams, clazz, isArray);
    }

    public static <T> TableInfo getTableInfo(Object obj) {
        return getTableInfo(obj,null);
    }

    public static <T> TableInfo getTableInfo(Class<?> clzz) {
        return getTableInfo(null,clzz);
    }
    private static <T> TableInfo getTableInfo(Object obj, Class<?> clzz) {
        Class<?> clz1 = clzz;
        if (obj != null) {
            if (!(obj instanceof String)) {
                clz1 = obj.getClass();
            }
        }
        DBTable dbTable = clz1.getAnnotation(DBTable.class);
        if (dbTable == null) {
            System.out.println("类中未找到生成数据表的相关注解:" + obj);
            return null;
        }
        String tableName = dbTable.name();
        if (TextUtils.isEmpty(tableName)) {
            tableName = clzz.getName();
        }

        TableInfo tableInfo = new TableInfo();
        tableInfo.setTableName(tableName);
        try {
            List<TableColumn> tableColumns = new ArrayList<>();
            for (Field field : clz1.getDeclaredFields()) {
                Annotation[] anns = field.getDeclaredAnnotations();
                if (anns.length <= 0) {
                    continue;
                }
                String columnName = null;
                for (Annotation annotation : anns) {
                    TableColumn tableColumn = new TableColumn();
                    if (annotation instanceof SQLObj) {
                        SQLObj sqlObj = (SQLObj) annotation;
                        tableColumn.setSqlObj(sqlObj);
                        if (sqlObj.name().length() < 1) {
                            columnName = field.getName();
                        } else {
                            columnName = sqlObj.name();
                        }
                    }
                    boolean accessFlag = field.isAccessible();
                    field.setAccessible(true);

                    tableColumn.setColumnName(columnName);
                    if (obj != null && !(obj instanceof String)) {
                        tableColumn.setValueObj(field.get(obj));
                    }
                    tableColumn.setField(field);
                    tableColumns.add(tableColumn);

                    field.setAccessible(accessFlag);
                }
            }
            tableInfo.setTableColumns(tableColumns);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tableInfo;
    }

    public static <T> List<T> generateModels(TableInfo tableInfo, String[] params, String whereParams, Class<T> clazz, boolean isArray) {
        Cursor cursor = mQuickDb.getDb().query(tableInfo.getTableName(), params, whereParams, null, null, null, null);
        return generateModelsByCursor(cursor, clazz, isArray);
    }

    public static <T> List<T> generateModels2(String sql, Class<T> clazz, boolean isArray) {
        Cursor cursor = mQuickDb.execSQL(sql);
        return generateModelsByCursor(cursor, clazz, isArray);
    }

    /**
     * 根据查询游标 生成实体类
     * @param cursor sql执行结果
     * @param clazz 目标实体类
     * @param isArray 返回值是否是集合
     * @param <T>
     * @return
     */
    public static <T> List<T> generateModelsByCursor(Cursor cursor, Class<T> clazz, boolean isArray) {
        List<T> list = new ArrayList<>();
        W:
        while (cursor.moveToNext()) {
            try {
                T model = (T) clazz.getConstructor().newInstance();
                for (Field fd : model.getClass().getDeclaredFields()) {
                    Annotation[] anns = fd.getDeclaredAnnotations();
                    if (anns.length <= 0) {
                        continue;
                    }
                    Object columnValue = null;
                    String columnName = null;
                    boolean accessFlag = fd.isAccessible();
                    fd.setAccessible(true);
                    for (Annotation annotation : anns) {
                        if (annotation instanceof SQLObj) {
                            SQLObj sqlObj = (SQLObj) annotation;
                            if (sqlObj.name().length() < 1) {
                                columnName = fd.getName();
                            } else {
                                columnName = sqlObj.name();
                            }
                            int columnCursorIndex = cursor.getColumnIndex(columnName);
                            if(columnCursorIndex!=-1) {
                                if (fd.getType() == int.class) {
                                    columnValue = cursor.getInt(columnCursorIndex);
                                } else if (fd.getType() == String.class) {
                                    columnValue = cursor.getString(columnCursorIndex);
                                }
                                if(columnValue!=null) {
                                    fd.set(model, columnValue);
                                }
                            }
                        }
                    }
                    fd.setAccessible(accessFlag);
                }

                list.add(model);
                if (!isArray) {
                    break W;
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public static void insert(Object obj){
        TableInfo tableInfo = getTableInfo(obj, obj.getClass());
        ContentValues values = new ContentValues();
        for (TableColumn tableColumn : tableInfo.getTableColumns()) {
            values.put(tableColumn.getColumnName(), tableColumn.getValueObj() + "");
        }
        mQuickDb.getDb().insert(tableInfo.getTableName(), null, values);
    }

    /**
     * 生成查询语句
     */
    public String generatQueryString(String tableName, Map<String, Object> paramsMap) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT * FROM " + tableName + " WHERE ");
        boolean b = true;
        for (Map.Entry entry : paramsMap.entrySet()) {
            Object valueObj = entry.getValue();
            String valueStr = null;
            if(valueObj instanceof Number){
                valueStr = valueObj+"";
            }else if(valueObj instanceof String){
                valueStr = "\""+valueObj+"\"";
            }
            if(!TextUtils.isEmpty(valueStr)) {
                stringBuilder.append((b ? " " : " and ") + entry.getKey() + "=" + valueStr);
                b = false;
            }
        }

        return (!b)?stringBuilder.toString():null;
    }
}