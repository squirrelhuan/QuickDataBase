package cn.demomaster.quickdatabaselibrary.model;

import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;

@DBTable(name = "sqlite_master")
public class SqliteTable {

    @SQLObj()
    private String type;
    @SQLObj()
    private String name;
    @SQLObj(name = "tbl_name")
    private String tblname;
    @SQLObj()
    private int rootpage;
    @SQLObj()
    private String sql;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTblname() {
        return tblname;
    }

    public void setTblname(String tblname) {
        this.tblname = tblname;
    }

    public int getRootpage() {
        return rootpage;
    }

    public void setRootpage(int rootpage) {
        this.rootpage = rootpage;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}
