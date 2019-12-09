package com.aaron.yespdf.common.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.greenrobot.greendao.database.Database;

/**
 * @author Aaron aaronzzxup@gmail.com
 */
public class UpdateOpenHelper extends DaoMaster.OpenHelper {

    public UpdateOpenHelper(Context context, String name) {
        super(context, name);
    }

    public UpdateOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }


    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        Log.i("version", oldVersion + "---先前和更新之后的版本---" + newVersion);
        if (oldVersion < newVersion) {
            MigrationHelper.getInstance().migrate(db, PDFDao.class);
            MigrationHelper.getInstance().migrate(db, CollectionDao.class);
            //更改过的实体类(新增的不用加)   更新UserDao文件 可以添加多个  XXDao.class 文件
            //MigrationHelper.getInstance().migrate(db, UserDao.class, XXDao.class);
        }
    }
}
