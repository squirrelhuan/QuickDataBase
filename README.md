# QuickDataBase
Android Sqlite

### 初始化
```
 QuickDb dbHelper;
 dbHelper = new QuickDb(this, "quick_db1.db", "quick_db2.db", null, 10, this);

 ```
 ### 生成表
```
  dbHelper.createTable(User.class);
 ```
### 实体类
```
@DBTable(name = "user")
public class User {

    @SQLObj(name = "id",constraints = @Constraints(primaryKey = true))
    private int id;

    @SQLObj(name = "name")
    private String name;

    @SQLObj(name = "header")
    private String header;

    private int age;

    private int sex;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }
}
```
### 插入数据
```
 User user = new User();
 user.setName("张三");
 user.setAge(18);
 dbHelper.insert(user);
```
### 批量插入
```
List<Member> members = new ArrayList<>();
for (int i = 1; i <= 10; i++) {
    Member member1 = new Member();
    member1.setAge(i);
    member1.setName("M_" + i);
    members.add(member1);
}
dbHelper.insertArray(members);
```

