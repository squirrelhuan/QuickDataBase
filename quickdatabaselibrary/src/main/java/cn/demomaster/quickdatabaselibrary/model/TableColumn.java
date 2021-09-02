package cn.demomaster.quickdatabaselibrary.model;

import java.lang.reflect.Field;

import cn.demomaster.quickdatabaselibrary.TableHelper;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;

public class TableColumn {
    private String columnName;
    private Object valueObj;
    private Field field;
    private SQLObj sqlObj;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValueObj() {
        return valueObj;
    }

    public String getValueSql() {
        if (field.getType() == boolean.class) {//true=1,false=0
            return (((boolean)valueObj)?"1":"0");
        } else if (field.getType() == int.class) {
            return valueObj + "";
        } else if (field.getType() == String.class || field.getType() == long.class) {
            return "'" + valueObj + "'";
        }
        return null;
    }
    
    public void setValueObj(Object valueObj) {
        this.valueObj = valueObj;
    }

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }

    public SQLObj getSqlObj() {
        return sqlObj;
    }

    public void setSqlObj(SQLObj sqlObj) {
        this.sqlObj = sqlObj;
    }

    public String getSqlString() {
        String sql = getColumnName() + " TEXT";
        Class clazz = getDataType();
        if (clazz == int.class || clazz == boolean.class) {
            sql = getColumnName() + " INTEGER " + TableHelper.getConstraints(getSqlObj().constraints());
        } else if (clazz == String.class || clazz == long.class) {
            sql = getColumnName() + " VARCHAR(" + getSqlObj().value() + ")" + TableHelper.getConstraints(getSqlObj().constraints());
        }
        return sql;
    }

    public Class getDataType() {
        Class clazz = getField().getType();
        if (getSqlObj().constraints() != null) {
            if (getSqlObj().constraints().autoincrement()) {
                clazz = int.class;
            }
        }
        return clazz;
    }
}
