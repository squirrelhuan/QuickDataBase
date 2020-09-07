package cn.demomaster.quickdatabaselibrary;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import cn.demomaster.quickdatabaselibrary.listener.UpgradeInterface;
import cn.demomaster.quickdatabaselibrary.model.SqliteTable;
import cn.demomaster.quickdatabaselibrary.sql.QDSqlCreator;
import cn.demomaster.quickdatabaselibrary.sql.SqlCreator;
import cn.demomaster.quickdatabaselibrary.sql.SqlCreatorInterFace;

/**
 * @author squirrel桓
 * @date 2018/11/19.
 * description：
 */
public class QuickDbHelper extends SQLiteOpenHelper implements SqlCreatorInterFace {
    private Context mContext;
    private SQLiteDatabase db;

    public SQLiteDatabase getDb() {
        if (db == null) {
            getReadableDatabase();
        }
        return db;
    }

    /**
     * @param context
     * @param dataBaseName //在data/data/下生成对应的db文件
     * @param assetsDataBasePath 资源文件中的全路径名
     * @param factory
     * @param version
     * @param upgradeInterface
     */
    public QuickDbHelper(Context context, String dataBaseName, String assetsDataBasePath, SQLiteDatabase.CursorFactory factory, int version, UpgradeInterface upgradeInterface) {
        super(context, dataBaseName, factory, version);
        this.upgradeInterface = upgradeInterface;
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = assetsDataBasePath;
        initLocalDB();//初始化本地的db文件
    }

    public QuickDbHelper(Context context, String dataBaseName, String assetsDataBasePath, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dataBaseName, factory, version);
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = assetsDataBasePath;
        initLocalDB();//初始化本地的db文件
    }

    //必须要有构造函数
    public QuickDbHelper(Context context, String dataBaseName, SQLiteDatabase.CursorFactory factory, int version, UpgradeInterface upgradeInterface) {
        super(context, dataBaseName, factory, version);
        this.upgradeInterface = upgradeInterface;
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = dataBaseName;
        initLocalDB();//初始化本地的db文件
    }

    //必须要有构造函数
    public QuickDbHelper(Context context, String dataBaseName, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, dataBaseName, factory, version);
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = dataBaseName;
        initLocalDB();//初始化本地的db文件
    }

    public QuickDbHelper(Context context, String dataBaseName, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, dataBaseName, factory, version, errorHandler);
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = dataBaseName;
        initLocalDB();//初始化本地的db文件
    }

    private static final String TAG = "QuickSQLite";

    //当更新数据库的时候执行该方法
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //输出更新数据库的日志信息
        //QDLogger.println(TAG, "update Database------------->oldVersion="+oldVersion+",newVersion="+newVersion);
        if (upgradeInterface != null) {
            upgradeInterface.onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static String DATABASE_NAME = ".db";
    private static String mAssetsDataBasePath = null;

    SqlCreator sqlCreator = null;
    TableHelper tableHelper = null;
    private void initLocalDB() {
        tableHelper = new TableHelper(this);
        sqlCreator = new QDSqlCreator(this);

        //如果数据库不存在则创建
        if (!databaseExists()) {
            createDatabaseFile();
        }
    }

    private boolean databaseExists() {
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    /**
     * 生成数据库文件
     */
    private void createDatabaseFile() {
        String parentPath = mContext.getDatabasePath(DATABASE_NAME).getParent();
        String path = mContext.getDatabasePath(DATABASE_NAME).getPath();

        File parentFile = new File(parentPath);
        if (!parentFile.exists()) {
            try {
                parentFile.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        copyDataBaseFile();
    }

    /**
     * 拷贝资源文件中的数据库，到本地
     */
    private void copyDataBaseFile() {
        if (TextUtils.isEmpty(mAssetsDataBasePath)) {
            return;
        }
        String path = mContext.getDatabasePath(DATABASE_NAME).getPath();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = mContext.getAssets().open(mAssetsDataBasePath);//资源文件中的数据库路径
            os = new FileOutputStream(path);
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        db = super.getReadableDatabase();
        return db;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    /**
     * 检查某表列是否存在
     * @param db
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    public static boolean checkColumnExist(SQLiteDatabase db, String tableName
            , String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            //查询一行
            cursor = db.rawQuery("SELECT * FROM " + tableName + " LIMIT 0"
                    , null);
            result = cursor != null && cursor.getColumnIndex(columnName) != -1;
        } catch (Exception e) {
            Log.e(TAG, "checkColumnExists1：" + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    UpgradeInterface upgradeInterface;
    public void setUpgradeInterface(UpgradeInterface upgradeInterface) {
        this.upgradeInterface = upgradeInterface;
    }

    public String createTable(Class clazz) throws Exception {
        String sql = TableHelper.generateTableSql(clazz);
        getDb().execSQL(sql);
        return sql;
    }

    @Override
    public boolean insert(Object model) {
        return sqlCreator.insert(model);
    }

    @Override
    public boolean insertArray(List models) {
        return sqlCreator.insertArray(models);
    }

    @Override
    public boolean delete(Object model) {
        return sqlCreator.delete(model);
    }

    @Override
    public <T> boolean deleteAll(Class<T> clazz) {
        return sqlCreator.deleteAll(clazz);
    }

    @Override
    public <T> T modify(T model) {
        return sqlCreator.modify(model);
    }

    @Override
    public boolean modifyArray(List models) {
        return sqlCreator.modifyArray(models);
    }

    @Override
    public <T> T findOne(T model) {
        return sqlCreator.findOne(model);
    }

    @Override
    public <T> T findOne(String sql, Class<T> clazz) {
        return sqlCreator.findOne(sql,clazz);
    }

    @Override
    public <T> List<T> findArray(T model) {
        return sqlCreator.findArray(model);
    }

    @Override
    public <T> List<T> findArray(String sql, Class<T> clazz) {
        return sqlCreator.findArray(sql,clazz);
    }

    /**
     * 获取数据库中所有表
     * @return
     */
    public List<SqliteTable> getTables(){
       List<SqliteTable> tables= sqlCreator.findArray("select * from sqlite_master where type=\"table\";", SqliteTable.class);
       return tables;
    }

    /**
     * 判断某表里某字段是否存在
     *
     * @param db
     * @param tableName
     * @param fieldName
     * @return
     */
    public static boolean isFieldExist(SQLiteDatabase db, String tableName, String fieldName) {
        String queryStr = "select sql from sqlite_master where type = 'table' and name = '%s'";
        queryStr = String.format(queryStr, tableName);
        Cursor c = db.rawQuery(queryStr, null);
        String tableCreateSql = null;
        try {
            if (c != null && c.moveToFirst()) {
                tableCreateSql = c.getString(c.getColumnIndex("sql"));
            }
        } finally {
            if (c != null)
                c.close();
        }
        if (tableCreateSql != null && tableCreateSql.contains(fieldName))
            return true;
        return false;
    }

    public long getLastIndex(){
        //获取索引
        Cursor cursor = getDb().rawQuery("select LAST_INSERT_ROWID() ", null);
        cursor.moveToFirst();
        return cursor.getLong(0);
    }

    public Cursor execSQL(String sql){
        getDb().acquireReference();
        Cursor cursor =null;
        try {
            String[] selectionArgs =null;
            cursor = getDb().rawQueryWithFactory(null, sql,selectionArgs,
                    getDb().findEditTable("MEMBER"), null);
        } finally {
            getDb().releaseReference();
        }
        return cursor;
    }
}
