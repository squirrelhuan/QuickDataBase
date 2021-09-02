package cn.demomaster.quickdatabaselibrary;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

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

    public TableHelper(QuickDbHelper quickDb) {
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
            if (tableColumn.getField().getType() == int.class || tableColumn.getField().getType() == long.class) {
                columnDefs.add(tableColumn.getColumnName() + " INTEGER " + getConstraints(tableColumn.getSqlObj().constraints()));
            } else if (tableColumn.getField().getType() == String.class) {
                columnDefs.add(tableColumn.getColumnName() + " VARCHAR(" + tableColumn.getSqlObj().value() + ")" + getConstraints(tableColumn.getSqlObj().constraints()));
            }
        }

        StringBuilder createCommand = new StringBuilder(
                "CREATE TABLE if not exists " + tableInfo.getTableName() + "(");
        boolean isFirst = true;
        for (String columnDef : columnDefs) {
            createCommand.append((isFirst ? " " : " ,"))
                    .append(columnDef);
            isFirst = false;
        }
        createCommand.append(");");
        String tableCreate = createCommand.toString();
        return tableCreate;
    }

    /**
     * 生成表结构更新sql
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    public static String upedateTableSql(SQLiteDatabase db, Class clazz) throws Exception {
        Class<?> cl = Class.forName(clazz.getName());
        TableInfo tableInfo = getTableInfo(null, cl);
        String updateSql = "";
        if (tableInfo != null && tableInfo.getTableColumns() != null) {
            for (TableColumn column : tableInfo.getTableColumns()) {
                upedateTableSql(db, tableInfo.getTableName(), column);
            }
        }
        return updateSql;
    }

    /**
     * @param db
     * @param tableName
     * @param fieldName
     * @param typeClass 字段類型
     * @return
     * @throws Exception
     */
    public static String upedateTableSql(SQLiteDatabase db, String tableName, String fieldName, Class typeClass) {

        return "";//generateUpedateTableSql(clazz.getName());
    }

    public static String upedateTableSql(SQLiteDatabase db, String tableName, TableColumn tableColumn) {
        //判斷字段是否存在
        String sql = null;
        if (!QuickDbHelper.isFieldExist(db, tableName, tableColumn.getColumnName())) {//不存在，添加表字段
            Log.e("TABLE", tableColumn.getColumnName() + "字段不存在");
            sql = String.format("ALTER TABLE %s ADD COLUMN %s", tableName, tableColumn.getSqlString());
        } else {
            //字段存在，判断字段类型是否一致，不一致则修改表字段。
            if (!QuickDbHelper.checkFieldType(db, tableName, tableColumn)) {
                //sqlite不支持全部的sql语句，更改表字段语句无法执行，只能先创建新表，备份数据，删除原表，
                //或者利用可视化工具直接修改，但是他执行的也是上边的过程
                //sql = String.format("alter table %s modify %s",tableName,tableColumn.getSqlString());
                String tableName2 = "_" + tableName + "_old_" + System.currentTimeMillis();

                String creatNewTableSql = getTableSql(db, tableName);
                Log.d("TABLE", "获取" + tableName + "表结构:" + creatNewTableSql);
                if (TextUtils.isEmpty(creatNewTableSql)) {
                    Log.d("TABLE", "未获取到" + tableName + "表结构");
                    return null;
                }

                //String createSql = "CREATE TABLE " + toTable + " AS SELECT * FROM " + fromTable;
                //创建表2并拷贝所有表1的内容
                String createSql1 = String.format("CREATE TABLE %s AS SELECT * FROM %s", tableName2, tableName);
                //Log.d("TABLE", "创建表2并拷贝所有表1的内容:" + createSql1);
                createSql1 = String.format("ALTER TABLE %s RENAME TO %s", tableName, tableName2);
                db.execSQL(createSql1);
                Log.d("TABLE", "对表重命名:" + createSql1);

                /*String regex = "\\((.*?)\\)";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(creatNewTableSql);
                while (matcher.find()) {
                    Log.d("TABLE", "提取表字段:" + matcher.group(1));
                }*/

                /*String deleteSql = String.format("DROP TABLE %s", tableName);
                db.execSQL(deleteSql);
                Log.d("TABLE", "删除old表:" + deleteSql);*/

                int startIndex = creatNewTableSql.indexOf("(") + 1;
                int endIndex = creatNewTableSql.lastIndexOf(")");
                if (endIndex > startIndex) {
                    String str2 = creatNewTableSql.substring(startIndex, endIndex);
                    String[] strings = str2.split(",");
                    //Log.d("TABLE", "提取表字段:" + str2+","+strings.length+ ","+Arrays.toString(strings));
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String string : strings) {
                        String s = removeFirstTrim(string);
                        if (!s.startsWith(tableColumn.getColumnName() + ' ')) {
                            stringBuilder.append(s)
                                    .append(",");
                        }
                    }
                    creatNewTableSql = "CREATE TABLE " + tableName + "(" + stringBuilder.toString() + tableColumn.getSqlString() + ")";
                    Log.d("TABLE", "增加字段:" + tableColumn.getColumnName());
                }

                db.execSQL(creatNewTableSql);
                Log.d("TABLE", "创建新表:" + creatNewTableSql);

                String insertSql = "INSERT OR IGNORE INTO " + tableName + " SELECT * FROM " + tableName2;
                db.execSQL(insertSql);
                Log.d("TABLE", "复制数据到新表:" + insertSql);

                String deleteSql1 = String.format("DROP TABLE %s", tableName2);
                db.execSQL(deleteSql1);
                Log.d("TABLE", "删除临时表:" + deleteSql1);
                //db.execSQL(String.format("ALTER TABLE %s RENAME TO %s",tableName,tableName2));
                //String table_sql2 = generateTableSql(clazz);
                //db.execSQL(table_sql2);
            }
        }
        if (TextUtils.isEmpty(sql)) {
            return null;
        }

        Log.d("TABLE", "创建字段sql:" + sql);
        db.execSQL(sql);
        return sql;
    }

    private static String removeFirstTrim(String s) {
        int len = s.length();
        int st = 0;

        if ((st < len) && (s.charAt(st) <= ' ')) {
            return removeFirstTrim(s.substring(st + 1, len));
        }
        return s;
    }

    private static String getTableSql(SQLiteDatabase db, String tableName) {
        String queryStr = String.format("select sql from sqlite_master where type = 'table' AND name='%s'", tableName);
        Cursor cursor = db.rawQuery(queryStr, null);
        String tableSql = null;
        try {
            cursor.moveToFirst();
            tableSql = cursor.getString(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return tableSql;
    }

    public String getTypeName(Class typeClass) {
        return "";
    }

    /**
     * 判断该字段是否有其他约束
     *
     * @param con
     * @return
     */
    public static String getConstraints(Constraints con) {
        String constraints = "";
        if (con.primaryKey()) {
            constraints += " PRIMARY KEY";
            if (con.autoincrement()) {
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
                if (!entry.getSqlObj().constraints().autoincrement()) {
                    String value1 = entry.getValueSql();
                    if (!TextUtils.isEmpty(value1)) {
                        keyStr.append((isFirst ? "" : ","))
                                .append("\"")
                                .append(entry.getColumnName())
                                .append("\"");
                        valueStr.append((isFirst ? "" : ","))
                                .append(value1);
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
     *
     * @param tableInfo
     * @param <T>
     * @return
     */
    public static <T> String generateWhereParams(TableInfo tableInfo) {
        if (tableInfo != null) {
            boolean isfirst = true;
            StringBuilder stringBuilder = new StringBuilder();
            for (TableColumn tableColumn : tableInfo.getTableColumns()) {
                if (tableColumn.getValueObj() != null) {
                    String valueStr = tableColumn.getValueSql();
                    if (!TextUtils.isEmpty(valueStr)) {
                        stringBuilder.append((isfirst ? "" : " and "))
                                .append(tableColumn.getColumnName())
                                .append("=")
                                .append(valueStr);
                        isfirst = false;
                    }
                }
            }
            return stringBuilder.toString();
        }
        return null;
    }

    public static <T> List<T> findModelList(Object obj, Class<T> clazz) {
        TableInfo tableInfo = getTableInfo(obj, clazz);
        return findModels(tableInfo, clazz, true);
    }

    public static <T> T findModelOne(Object obj, Class<T> clazz) {
        TableInfo tableInfo = getTableInfo(obj, clazz);
        List<T> list = findModels(tableInfo, clazz, false);
        if (list == null || list.size() == 0) {
            return null;
        }
        return list.get(0);
    }

    public static <T> List<T> findModels(TableInfo tableInfo, Class<T> clazz, boolean isArray) {
        String[] params = new String[]{"*"};
        String whereParams = generateWhereParams(tableInfo);
        return generateModels(tableInfo, params, whereParams, clazz, isArray);
    }

    public static <T> TableInfo getTableInfo(Object obj) {
        return getTableInfo(obj, null);
    }

    public static <T> TableInfo getTableInfo(Class<?> clzz) {
        return getTableInfo(null, clzz);
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
        TableInfo tableInfo = getTableInfo(null, clazz);
        if (tableInfo == null) {
            return null;
        }
        Cursor cursor = mQuickDb.execQuerySQL(tableInfo.getTableName(), sql);
        return generateModelsByCursor(cursor, clazz, isArray);
    }

    /**
     * 根据查询游标 生成实体类
     *
     * @param cursor  sql执行结果
     * @param clazz   目标实体类
     * @param isArray 返回值是否是集合
     * @param <T>
     * @return
     */
    public static <T> List<T> generateModelsByCursor(Cursor cursor, Class<T> clazz, boolean isArray) {
        List<T> list = new ArrayList<>();
        if (cursor == null) {
            return list;
        }
        while (cursor.moveToNext()) {
            try {
                T model = clazz.getConstructor().newInstance();
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
                            if (columnCursorIndex != -1) {
                                if (fd.getType() == int.class) {
                                    columnValue = cursor.getInt(columnCursorIndex);
                                } else if (fd.getType() == long.class) {
                                    columnValue = 0;
                                    String str = cursor.getString(columnCursorIndex);
                                    if (!TextUtils.isEmpty(str)) {
                                        columnValue = Long.valueOf(str);
                                    }
                                } else if (fd.getType() == String.class) {
                                    columnValue = cursor.getString(columnCursorIndex);
                                } else if (fd.getType() == boolean.class) {
                                    //0false 1 true
                                    columnValue = (cursor.getInt(columnCursorIndex) == 1);
                                }
                                if (columnValue != null) {
                                    fd.set(model, columnValue);
                                }
                            }
                        }
                    }
                    fd.setAccessible(accessFlag);
                }

                list.add(model);
                if (!isArray) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public static void insert(Object obj) {
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
        stringBuilder.append("SELECT * FROM ")
                .append(tableName)
                .append(" WHERE ");
        boolean b = true;
        for (Map.Entry entry : paramsMap.entrySet()) {
            Object valueObj = entry.getValue();
            String valueStr = null;
            if (valueObj instanceof Number) {
                valueStr = valueObj + "";
            } else if (valueObj instanceof String) {
                valueStr = "\"" + valueObj + "\"";
            }
            if (!TextUtils.isEmpty(valueStr)) {
                stringBuilder.append((b ? " " : " and "))
                        .append(entry.getKey())
                        .append("=")
                        .append(valueStr);
                b = false;
            }
        }

        return (!b) ? stringBuilder.toString() : null;
    }
}