package cn.demomaster.quickdatabaselibrary.sql;

import android.database.sqlite.SQLiteOpenHelper;

import java.util.List;

public interface SqlCreatorInterFace {
    //增加
    boolean insert(Object model);
    boolean insertArray(List models);
    //删除
    boolean delete(Object model);
    <T> boolean deleteAll(Class<T> clazz);
    //改
    //查
    <T> T findOne(T model);
    <T> T findOne(String sql,Class<T> clazz);
    <T> List<T> findArray(T model);
    <T> List<T> findArray(String sql,Class<T> clazz);
}
