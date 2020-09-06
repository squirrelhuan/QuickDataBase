package cn.demomaster.quickdatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.quickdatabase.model.Member;
import cn.demomaster.quickdatabase.model.User;
import cn.demomaster.quickdatabaselibrary.DbHelper;
import cn.demomaster.quickdatabaselibrary.TableHelper;

import static cn.demomaster.quickdatabaselibrary.TableHelper.generateTableSql;

public class MainActivity extends AppCompatActivity implements DbHelper.DbHelperInterface {

    TextView tv_console;

    DbHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_console = findViewById(R.id.tv_console);

        dbHelper = new DbHelper(this, "quick_db.db","quick_db.db", null, 10, this);
        try {
            TableHelper.db = dbHelper.getDb();

            //生成表
            creatTable();

            //单条数据插入
            //insert1();

            //批量插入数据
            //insert2();

            //删除数据
            //deleteSingle();

            //查询数据
            //findList1();

            //findOne1();

            //查询数据
            //User user_t2 = new User();
            //user_t2.setHeader("http");

            //findList2();
            //根据实体类查询

            //findList1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findOne1(View view) {
        Member mm = dbHelper.findOne("select * from Member",Member.class);
        print("当前记录："+(mm==null?"无":mm.toString()));
    }

    private void print(String s) {
        System.out.println(s);
        tv_console.append("\n"+s);
    }

    public void findOne2() {
        User user_t = new User();
        user_t.setId(3);
        User user2 = dbHelper.findOne(user_t);
        print("："+(user2==null?"":user2.toString()));
    }

    public void findList1(View view) {
        List<Member> members = new ArrayList<>();
        try {
            members = dbHelper.findArray("select * from member", Member.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("总记录数："+(members==null?"0":members.size()));
    }

    private void findList2() {
        Member member1 = new Member();
        member1.setId(1);
        List<Member> members = null;
        try {
            members = dbHelper.findArray(member1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        print("结果数量："+(members==null?"0":members.size()));
    }
    private void deleteSingle() {
        User user_d = new User();
        user_d.setId(2);
        try {
            dbHelper.delete(user_d);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert2(View view) {
        List<Member> members = new ArrayList<>();
        for(int i=1;i<=10;i++) {
            Member member1 = new Member();
            member1.setId(i);
            member1.setName("Member_"+i);
            members.add(member1);
        }
        try {
            dbHelper.insertList(members);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insert1(View view) {
        Member member = new Member();
        member.setId(12356);
        member.setName("member single");
        dbHelper.insert(member);
        print("单条数据插入:"+member.toString());
    }

    private void creatTable() {
        print("创建表");
        try {
            dbHelper.createTable(Member.class);
            dbHelper.createTable(User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void clear(View view) {
        tv_console.setText("");
    }

    public void delete01(View view) {
        List<Member> members = new ArrayList<>();
        try {
            members = dbHelper.findArray("select * from member", Member.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(members==null||members.size()<1){
            return;
        }
        Member member = members.get(0);
        try {
            dbHelper.delete(member);
            print("删除记录"+member.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAll(View view) {
        try {
            dbHelper.deleteAll(Member.class);
            print("删除全部记录");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
