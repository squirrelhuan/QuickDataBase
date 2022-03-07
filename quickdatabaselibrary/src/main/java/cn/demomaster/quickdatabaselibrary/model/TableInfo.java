package cn.demomaster.quickdatabaselibrary.model;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
    String tableName;
    List<TableColumn> tableColumns;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<TableColumn> getTableColumns() {
        return tableColumns;
    }

    public void setTableColumns(List<TableColumn> tableColumns) {
        this.tableColumns = tableColumns;
    }
    
    public void addCoumn(TableColumn tableColumn){
        if(tableColumns==null){
            tableColumns = new ArrayList<>();
        }
        tableColumns.add(tableColumn);
    }
}
