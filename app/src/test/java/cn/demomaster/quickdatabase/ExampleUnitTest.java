package cn.demomaster.quickdatabase;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.quickdatabaselibrary.annotation.DBTable;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }
    @Test
    public void setData() {
        List<String[]> stringList = new ArrayList<>();
        for(int i=0;i<500;i++){
            String[] strings = new String[4];
            for(int j=0;j<strings.length;j++) {
                strings[j]=i+"";
            }
            stringList.add(strings);
        }
        setData(stringList);
    }
    public  <T> void setData(List<T> modelList) {
        Class c = modelList.getClass();
        Class<?> clz1 = null;
        DBTable dbTable = clz1.getAnnotation(DBTable.class);
        if (dbTable == null) {
            System.out.println("类中未找到生成数据表的相关注解:");
            return;
        }
    }
}