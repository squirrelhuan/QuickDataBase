package cn.demomaster.quickdatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cn.demomaster.quickdatabase.model.Member;
import cn.demomaster.quickdatabase.model.User;
import cn.demomaster.quickdatabaselibrary.QuickDb;
import cn.demomaster.quickdatabaselibrary.listener.UpgradeInterface;

public class MainActivity extends AppCompatActivity implements UpgradeInterface {

    TextView tv_console;

    QuickDb dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_console = findViewById(R.id.tv_console);

        dbHelper = new QuickDb(this, "quick_db.db", "quick_db.db", null, 10, this);
        //quick_db1.db 在data/data/下生成对应的db文件
        //quick_db2.db 在assets下的db文件
        //生成表
        creatTable();
    }

    public void findOne1(View view) {
        Member mm = dbHelper.findOne("select * from Member", Member.class);
        print("当前记录：" + (mm == null ? "无" : mm.toString()));
    }

    private void print(String s) {
        System.out.println(s);
        tv_console.append("\n" + s);
    }

    public void findOne2() {
        User user_t = new User();
        user_t.setId(3);
        User user2 = dbHelper.findOne(user_t);
        print("：" + (user2 == null ? "" : user2.toString()));
    }

    public void findList1(View view) {
        List<Member> members = new ArrayList<>();
        members = dbHelper.findArray("select * from member", Member.class);
        print("总记录数：" + (members == null ? "0" : members.size()));
    }

    public void findList2(View view) {
       // Member member1 = new Member();
       // member1.setDescription("描述");
        List<Member> members = dbHelper.findArray("select * from member where description=\"描述\"",Member.class);
        print("结果数量：" + (members == null ? "0" : members.size()));
    }

    private void deleteSingle() {
        User user_d = new User();
        user_d.setId(2);
        dbHelper.delete(user_d);
    }

    public void insert2(View view) {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Member member1 = new Member();
            member1.setAge(i);
            member1.setName("M_" + i);
            member1.setDescription("描述");
            members.add(member1);
        }
        dbHelper.insertArray(members);
    }

    public void insert1(View view) {
        Member member = new Member();
        member.setName("member single");
        member.setAge(new Random().nextInt());
        dbHelper.insert(member);
        print("单条数据插入:" + member.toString());

       /* User user = new User();
        user.setName("张三");
        user.setAge(18);
        dbHelper.insert(user);*/
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
        if (members == null || members.size() < 1) {
            return;
        }
        Member member = members.get(0);
        try {
            dbHelper.delete(member);
            print("删除记录" + member.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteAll(View view) {
        dbHelper.deleteAll(Member.class);
        print("删除全部记录");
    }

    public void modify01(View view) {
        List<Member> members = new ArrayList<>();
        try {
            members = dbHelper.findArray("select * from member", Member.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (members == null || members.size() < 1) {
            return;
        }
        Member member = members.get(0);
        member.setName("sb");
        member.setAge(18);
        dbHelper.modify(member);
        long id = dbHelper.getLastIndex();
        print("修改完成id=" + id);
    }
}
