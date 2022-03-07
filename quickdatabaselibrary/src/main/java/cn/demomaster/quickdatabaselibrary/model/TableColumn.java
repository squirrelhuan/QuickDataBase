package cn.demomaster.quickdatabaselibrary.model;

import android.text.TextUtils;

import java.io.Serializable;
import java.lang.reflect.Field;

import cn.demomaster.quickdatabaselibrary.TableHelper;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;

public class TableColumn implements Serializable {
    private String name;//字段名
    private String type;//字段类型 TEXT INT
    private int notNull;//是否为空 0 可为空 1不为空
    private int pk;//主键 0 否 1是
    private Object valueObj;
    private Field field;
    private SQLObj sqlObj;

    public int getPk() {
        return pk;
    }

    public void setPk(int pk) {
        this.pk = pk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNotNull() {
        return notNull;
    }

    public void setNotNull(int notNull) {
        this.notNull = notNull;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValueObj() {
        return valueObj;
    }

    public String getValueSql() {
        if(field!=null) {
            if (field.getType() == boolean.class) {//true=1,false=0
                return (((boolean) valueObj) ? "1" : "0");
            } else if (field.getType() == int.class) {
                return valueObj + "";
            } else if (field.getType() == String.class || field.getType() == long.class) {
                return "'" + valueObj + "'";
            }
        }else if(!TextUtils.isEmpty(type)){
            if (type.equalsIgnoreCase("INT")||type.equalsIgnoreCase("INTEGER")) {//true=1,false=0
                return valueObj +"";
            } else if (type.equalsIgnoreCase("TEXT")) {
                return "'" + valueObj + "'";
            }
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
        String sql = getName() + " TEXT";
        Class clazz = getDataType();
        if (clazz == int.class || clazz == boolean.class) {
            sql = getName() + " INTEGER " + TableHelper.getConstraints(getSqlObj().constraints());
        } else if (clazz == String.class || clazz == long.class) {
            sql = getName() + " VARCHAR(" + getSqlObj().value() + ")" + TableHelper.getConstraints(getSqlObj().constraints());
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

    @Override
    public String toString() {
        return "TableColumn{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", notNull=" + notNull +
                ", pk=" + pk +
                ", valueObj=" + valueObj +
                ", field=" + field +
                ", sqlObj=" + sqlObj +
                '}';
    }
}
