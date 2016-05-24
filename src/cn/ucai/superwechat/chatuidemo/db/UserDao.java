package cn.ucai.superwechat.chatuidemo.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import cn.ucai.superwechat.bean.User;
import cn.ucai.superwechat.chatuidemo.I;
import cn.ucai.superwechat.chatuidemo.SuperwechatApplication;
import cn.ucai.superwechat.chatuidemo.utils.MD5;

/**
 * Created by Administrator on 2016/4/8.
 */
public class UserDao extends SQLiteOpenHelper {
    public static final String Id = "_id";
    public static final String TABLENAME = "user";
    public UserDao(Context context) {
        super(context, "user.db", null,1);
    }

    /**
     * 创建表
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists "+ TABLENAME +"( " +
                I.User.USER_ID +" integer, " +
                I.User.USER_NAME +" varchar unique not null, " +
                I.User.NICK +" varchar, " +
                I.User.PASSWORD +" varchar, " +
                I.User.UN_READ_MSG_COUNT +" int default(0) " +
                ");";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 插入数据
     * @param user
     * @return
     */
    public boolean addUser(User user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID,user.getMUserId());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.USER_NAME,user.getMUserName());
        values.put(I.User.PASSWORD, MD5.getData(user.getMUserPassword()));
        values.put(I.User.UN_READ_MSG_COUNT,user.getMUserUnreadMsgCount());
        SQLiteDatabase db = getWritableDatabase();
        long insert = db.insert(TABLENAME, null, values);
        return insert>0;
    }

    /**
     * 根据用户名查找用户
     * @param userName
     * @return
     */
    public User findUserByUserName(String userName){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from "+TABLENAME+" where "+I.User.USER_NAME+"= ?";
        Cursor cursor = db.rawQuery(sql, new String[]{userName});
        while(cursor.moveToNext()){
            int uid = cursor.getInt(cursor.getColumnIndex(I.User.USER_ID));
            String nick = cursor.getString(cursor.getColumnIndex(I.User.NICK));
            String passWord = cursor.getString(cursor.getColumnIndex(I.User.PASSWORD));
            int unReadMsg = cursor.getInt(cursor.getColumnIndex(I.User.UN_READ_MSG_COUNT));
            User user = new User(uid,userName,passWord,nick,unReadMsg);
            return user;
        }
        return null;
    }

    /**
     * 更新用户信息
     * @param user
     * @return
     */
    public boolean updateUser(User user){
        ContentValues values = new ContentValues();
        values.put(I.User.USER_ID,user.getMUserId());
        values.put(I.User.NICK,user.getMUserNick());
        values.put(I.User.USER_NAME,user.getMUserName());
        values.put(I.User.PASSWORD, MD5.getData(user.getMUserPassword()));
        values.put(I.User.UN_READ_MSG_COUNT,user.getMUserUnreadMsgCount());
        SQLiteDatabase db = getWritableDatabase();
        long update = db.update(TABLENAME,values,I.User.USER_NAME+"=?",new String[]{user.getMUserName()});
        return update>0;
    }

    /**
     * 添加或更新用户
     */

    public boolean myAddDataBaseUser(User user) {
        if (findUserByUserName(user.getMUserName()) != null) {
            return updateUser(user);
        } else {
            return addUser(user);
        }
    }

    /**
     * 删除用户
     * @param user
     * @return
     */
    public boolean deleteUser(User user){
        SQLiteDatabase db = getWritableDatabase();
        int delete = db.delete(TABLENAME, I.User.USER_NAME + "=?", new String[]{user.getMUserName()});
        if(delete!=0){
            Log.e(SuperwechatApplication.TAG,"从应用数据库中删除了"+user.getMUserName()+"用户");
        }
        return delete>0;
    }
}

