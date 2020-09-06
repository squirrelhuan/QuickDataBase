package cn.demomaster.quickdatabase.model;

import cn.demomaster.quickdatabaselibrary.annotation.Constraints;
import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;

@DBTable(name = "t_order")
public class Order {

    @SQLObj(name = "id",constraints = @Constraints(autoincrement = true,primaryKey = true))
    private int id;

}
