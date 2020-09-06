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

import cn.demomaster.quickdatabaselibrary.sql.QDSqlCreator;
import cn.demomaster.quickdatabaselibrary.sql.SqlCreator;
import cn.demomaster.quickdatabaselibrary.sql.SqlCreatorInterFace;

/**
 * @author squirrel桓
 * @date 2018/11/19.
 * description：
 */
public class QuickDb extends SQLiteOpenHelper implements SqlCreatorInterFace {
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
     * @param dbHelperInterface
     */
    public QuickDb(Context context, String dataBaseName, String assetsDataBasePath, SQLiteDatabase.CursorFactory factory, int version, DbHelperInterface dbHelperInterface) {
        super(context, dataBaseName, factory, version);
        this.dbHelperInterface = dbHelperInterface;
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = dataBaseName;
        this.mAssetsDataBasePath = assetsDataBasePath;
        initLocalDB();//初始化本地的db文件
    }

    //必须要有构造函数
    public QuickDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DbHelperInterface dbHelperInterface) {
        super(context, name, factory, version);
        this.dbHelperInterface = dbHelperInterface;
        this.mContext = context.getApplicationContext();
        this.DATABASE_NAME = name;
        initLocalDB();//初始化本地的db文件
    }

    public QuickDb(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    private static final String TAG = "TagSQLite";

    //当第一次创建数据库的时候，调用该方法
    public void onCreate(SQLiteDatabase db) {
        // String sql = "create table stu_table(id int,sname varchar(20),sage int,ssex varchar(10))";
        //输出创建数据库的日志信息
        // QDLogger.i(TAG, "create Database------------->");
        //execSQL函数用于执行SQL语句
        // db.execSQL(sql);
    }

    //当更新数据库的时候执行该方法
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //输出更新数据库的日志信息
        //QDLogger.println(TAG, "update Database------------->oldVersion="+oldVersion+",newVersion="+newVersion);
        if (dbHelperInterface != null) {
            dbHelperInterface.onUpgrade(db, oldVersion, newVersion);
        }
    }

    private static String DATABASE_NAME = ".db";
    private static String mAssetsDataBasePath = null;
    private static final int DATABASE_VERSION = 1;
    // private static final String SP_KEY_DB_VER = "db_ver";

    SqlCreator sqlCreator = null;
    private void initLocalDB() {
        TableHelper.db = getDb();
        sqlCreator = new QDSqlCreator(this);

        //如果数据库不存在则创建
        if (!databaseExists()) {
            createDatabaseFile();
        }

        if (databaseExists()) {
            SQLiteDatabase db = this.getReadableDatabase();
            if (db == null) {//如果数据库不存在

            } else {
                int dbVersion = db.getVersion();// prefs.getInt(SP_KEY_DB_VER, 1);
            }
            /*if (DATABASE_VERSION != dbVersion) {
                File dbFile = mContext.getDatabasePath(DATABASE_NAME);
                if (!dbFile.delete()) {
                    //QDLogger.println(TAG, "Unable to update database");
                }
            }*/
        }
    }

    private boolean databaseExists() {
        File dbFile = mContext.getDatabasePath(DATABASE_NAME);
        return dbFile.exists();
    }

    /**
     * Creates database by copying it from assets directory.
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

    /**
     * 检查某表列是否存在
     *
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
            Log.e(TAG, "checkColumnExists1..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }

    DbHelperInterface dbHelperInterface;
    public void setDbHelperInterface(DbHelperInterface dbHelperInterface) {
        this.dbHelperInterface = dbHelperInterface;
    }

    public void createTable(Class clazz) throws Exception {
        String sql = TableHelper.generateTableSql(clazz);
        System.out.println(sql);
        getDb().execSQL(sql);
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

    public static interface DbHelperInterface {
        void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
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
}
