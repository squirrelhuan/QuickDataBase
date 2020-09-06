package cn.demomaster.quickdatabaselibrary.model;

import java.lang.reflect.Field;

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
}
