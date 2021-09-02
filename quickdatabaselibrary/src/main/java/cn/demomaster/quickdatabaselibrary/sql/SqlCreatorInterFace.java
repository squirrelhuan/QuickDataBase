package cn.demomaster.quickdatabaselibrary.sql;

import android.content.ContentValues;

import java.util.List;

public interface SqlCreatorInterFace {
    //增加
    boolean insert(Object model);
    boolean insertArray(List models);
    //删除
    //boolean delete(Object model);
    boolean delete(String tableName,ContentValues contentValues);
    <T> boolean deleteAll(Class<T> clazz);
    //改
    <T> T modify(T model);
    boolean modifyArray(List models);

    //查
    <T> T findOne(T model);
    <T> T findOne(String sql,Class<T> clazz);
    <T> List<T> findArray(T model);
    <T> List<T> findArray(String sql,Class<T> clazz);
}
